package lu.rescue_rush.spring.jda.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lu.rescue_rush.spring.jda.DiscordSenderService;
import lu.rescue_rush.spring.jda.command.slash.SlashCommandAutocomplete;
import lu.rescue_rush.spring.jda.command.slash.SlashCommandExecutor;
import lu.rescue_rush.spring.jda.command.slash.SubSlashCommandExecutor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

@Component
public class SlashCommandListener extends ListenerAdapter {

	private static final Logger LOGGER = Logger.getLogger(SlashCommandListener.class.getName());

	public static final String DEBUG_PROPERTY = SlashCommandListener.class.getSimpleName() + ".debug";
	public static boolean DEBUG = Boolean.getBoolean(DEBUG_PROPERTY);

	@Autowired
	private JDA jda;

	@Autowired
	private DiscordSenderService discordSenderService;

	private final Map<String, SlashCommandExecutor> listeners;
	private final Map<String, Map<String, SubSlashCommandExecutor>> subListeners;

	public SlashCommandListener(Map<String, SlashCommandExecutor> listeners) {
		this.listeners = new HashMap<>();
		this.subListeners = new HashMap<>();

		listeners.forEach((name, executor) -> {
			if (!(executor instanceof SubSlashCommandExecutor)) {
				this.listeners.put(name, executor);
			}
		});
		listeners.forEach((name, executor) -> {
			if (executor instanceof SubSlashCommandExecutor sub) {
				final Class<? extends SlashCommandExecutor> parentClass = sub.getCommandClass();

				final String parentName = listeners
						.entrySet()
						.stream()
						.filter(e -> e.getValue().getClass().equals(parentClass))
						.map(Map.Entry::getKey)
						.findFirst()
						.orElseThrow(() -> new IllegalStateException("Parent command not found for sub command: " + name));

				subListeners.computeIfAbsent(parentName, k -> new HashMap<>()).put(name, sub);
			}
		});
	}

	@Async
	@EventListener(ApplicationReadyEvent.class)
	public void init() {
		discordSenderService.awaitJDAReady();

		listeners.entrySet().forEach(e -> registerCommand(e.getKey(), e.getValue()));
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

		String commandName = event.getName();

		if (!listeners.containsKey(commandName)) {
			LOGGER.warning("No slash command registered for: " + commandName);
			return;
		}

		try {
			if (event.isAcknowledged()) {
				if (DEBUG) {
					LOGGER.info("Execution interaction already acknowledged for: " + commandName);
				}
				return;
			}

			final String subName = event.getSubcommandName();

			if (subName != null && subListeners.containsKey(commandName)) {
				final Map<String, SubSlashCommandExecutor> subs = subListeners.get(commandName);

				if (subs.containsKey(subName)) {
					subs.get(subName).execute(event);
					return;
				}

				LOGGER.warning("No sub command registered for: " + commandName + "/" + subName);
				return;
			}

			// normal command
			listeners.get(commandName).execute(event);

		} catch (Exception e) {
			final String msg = "A method executor (`" + commandName + "`) raised an exception: " + e.getMessage() + " ("
					+ e.getClass().getSimpleName() + ")";

			event
					.getChannel()
					.sendMessage(msg)
					.queue(null,
							(f) -> event
									.getChannel()
									.sendMessage(msg + "\n(failed once: " + f.getMessage() + " (" + f.getClass().getSimpleName() + "))")
									.queue());

			if (DEBUG) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {

		String commandName = event.getName();

		if (!listeners.containsKey(commandName)) {
			LOGGER.warning("No slash command registered for: " + commandName);
			return;
		}

		String subName = event.getSubcommandName();

		if (subName != null && subListeners.containsKey(commandName)) {
			SubSlashCommandExecutor sub = subListeners.get(commandName).get(subName);

			if (sub instanceof SlashCommandAutocomplete autocomplete) {
				autocomplete.complete(event);
				return;
			}
		}

		SlashCommandExecutor root = listeners.get(commandName);

		if (root instanceof SlashCommandAutocomplete autocomplete) {
			autocomplete.complete(event);
		}
	}

	public void registerCommand(String name, SlashCommandExecutor command) {
		final SlashCommandData data = command.build(name);

		if (subListeners.containsKey(name)) {
			subListeners.get(name).forEach((subName, subExecutor) -> {
				data.addSubcommands(new SubcommandData(subName, subExecutor.description()).addOptions(subExecutor.options()));
			});
		}

		jda
				.upsertCommand(data)
				.queue((c) -> LOGGER
						.info("Registered slash command: " + name
								+ (data.getSubcommands().size() > 0
										? " " + data.getSubcommands().stream().map(d -> d.getName()).collect(Collectors.joining(", "))
										: "")
								+ " (" + command.description() + ")"),
						(e) -> {
							if (!(e instanceof CancellationException) && isDebug()) {
								e.printStackTrace();
							}
						});
	}

	public static boolean isDebug() {
		return DEBUG || DiscordSenderService.DEBUG;
	}

}
