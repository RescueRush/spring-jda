package lu.rescue_rush.spring.jda.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
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

	@Autowired
	private ApplicationContext context;

	@Autowired
	private JDA jda;

	@Autowired
	private DiscordSenderService discordSenderService;

	private Map<String, SlashCommandExecutor> listeners = new HashMap<>();

	@PostConstruct
	public void init() {
		final Thread t = new Thread(() -> {
			discordSenderService.awaitJDAReady();

			final Map<String, SlashCommandExecutor> beans = context.getBeansOfType(SlashCommandExecutor.class);
			beans.entrySet().forEach(e -> registerCommand(e.getKey(), e.getValue()));
		});
		t.setName("SlashCommandListener-Init");
		t.setDaemon(true);
		t.start();
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		if (listeners.containsKey(event.getName())) {
			try {
				listeners.get(event.getName()).execute(event);
			} catch (Exception e) {
				final String msg = "A method executor (`" + event.getName() + "`) raised an exception: " + e.getMessage() + " ("
						+ e.getClass().getSimpleName() + ")";
				event.getHook().sendMessage(msg).queue(null, (f) -> event.getChannel().sendMessage(msg).queue());

				e.printStackTrace();
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
					autocompleteListener.complete(event);
				} catch (Exception e) {
					event
							.getChannel()
							.sendMessage("A method completer (`" + event.getName() + "`) raised an exception: " + e.getMessage() + " ("
									+ e.getClass().getSimpleName() + ")")
							.queue();

					e.printStackTrace();
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
		listeners.put(name, command);

		jda.upsertCommand(command.build(name)).queue((c) -> {
			LOGGER.info("Registered slash command: " + name + " (" + command.description() + ")");
		}, (e) -> {
			if (e instanceof CancellationException)
				return; // ignore

			e.printStackTrace();
		});
	}

}
