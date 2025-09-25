package lu.rescue_rush.spring.jda.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lu.rescue_rush.spring.jda.DiscordSenderService;
import lu.rescue_rush.spring.jda.command.slash.SlashCommandAutocomplete;
import lu.rescue_rush.spring.jda.command.slash.SlashCommandExecutor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Component
public class SlashCommandListener extends ListenerAdapter {

	private static final Logger LOGGER = Logger.getLogger(SlashCommandListener.class.getName());

	public static final String DEBUG_PROPERTY = SlashCommandListener.class.getSimpleName() + ".debug";
	public static boolean DEBUG = Boolean.getBoolean(DEBUG_PROPERTY);

	@Autowired
	private JDA jda;

	@Autowired
	private DiscordSenderService discordSenderService;

	private Map<String, SlashCommandExecutor> listeners = new HashMap<>();

	@Async
	@EventListener(ApplicationReadyEvent.class)
	public void init() {
		discordSenderService.awaitJDAReady();

		listeners.entrySet().forEach(e -> registerCommand(e.getKey(), e.getValue()));
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		if (listeners.containsKey(event.getName())) {
			try {
				if (event.isAcknowledged()) {
					if (DEBUG) {
						LOGGER.info("Execution interaction already acknowledged for: " + event.getName());
					}
					return;
				}

				listeners.get(event.getName()).execute(event);
			} catch (Exception e) {
				final String msg = "A method executor (`" + event.getName() + "`) raised an exception: " + e.getMessage() + " ("
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
		} else {
			LOGGER.warning("No slash command registered for: " + event.getName());
		}
	}

	@Override
	public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
		if (listeners.containsKey(event.getName())) {
			final SlashCommandExecutor listener = listeners.get(event.getName());

			if (listener instanceof SlashCommandAutocomplete autocompleteListener) {
				try {
					if (event.isAcknowledged()) {
						if (isDebug()) {
							LOGGER
									.info("Auto-complete interaction already acknowledged for: " + event.getName() + " ("
											+ event.getFocusedOption().getName() + ")");
						}
						return;
					}

					autocompleteListener.complete(event);
				} catch (Exception e) {
					final String msg = "A method completer (`" + event.getName() + "`) raised an exception: " + e.getMessage() + " ("
							+ e.getClass().getSimpleName() + ")";
					event
							.getChannel()
							.sendMessage(msg)
							.queue(null,
									(f) -> event
											.getChannel()
											.sendMessage(
													msg + "\n(failed once: " + f.getMessage() + " (" + f.getClass().getSimpleName() + "))")
											.queue());

					if (isDebug()) {
						e.printStackTrace();
					}
				}
			} else {
				LOGGER
						.warning("No slash command autocomplete registered for: " + event.getName() + " ("
								+ event.getFocusedOption().getName() + ")");
			}
		} else {
			LOGGER.warning("No slash command registered for: " + event.getName() + " (" + event.getFocusedOption().getName() + ")");
		}
	}

	public void registerCommand(String name, SlashCommandExecutor command) {
		jda
				.upsertCommand(command.build(name))
				.queue((c) -> LOGGER.info("Registered slash command: " + name + " (" + command.description() + ")"), (e) -> {
					if (e instanceof CancellationException) {
						return; // ignore
					}

					if (isDebug()) {
						e.printStackTrace();
					}
				});
	}

	public static boolean isDebug() {
		return DEBUG || DiscordSenderService.DEBUG;
	}

}
