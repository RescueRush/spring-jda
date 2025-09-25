package lu.rescue_rush.spring.jda.listener;

import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lu.rescue_rush.spring.jda.DiscordSenderService;
import lu.rescue_rush.spring.jda.command.message.MessageCommandExecutor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Component
public class MessageCommandListener extends ListenerAdapter {

	private static final Logger LOGGER = Logger.getLogger(MessageCommandListener.class.getName());

	public static final String DEBUG_PROPERTY = MessageCommandListener.class.getSimpleName() + ".debug";
	public static boolean DEBUG = Boolean.getBoolean(DEBUG_PROPERTY);

	@Autowired
	private JDA jda;

	@Autowired
	private DiscordSenderService discordSenderService;

	private final Map<String, MessageCommandExecutor> listeners;

	public MessageCommandListener(Map<String, MessageCommandExecutor> listeners) {
		this.listeners = listeners;
	}

	@Async
	@EventListener(ApplicationReadyEvent.class)
	public void init() {
		discordSenderService.awaitJDAReady();

		listeners.values().stream().forEach(this::registerCommand);
	}

	@Override
	public void onMessageContextInteraction(MessageContextInteractionEvent event) {
		if (listeners.containsKey(event.getName())) {
			try {
				listeners.get(event.getName()).execute(event);
			} catch (Exception e) {
				final String msg = "A method executor (`" + event.getName() + "`) raised an exception: " + e.getMessage() + " ("
						+ e.getClass().getSimpleName() + ")";
				event
						.getHook()
						.sendMessage(msg)
						.queue(null,
								(f) -> event
										.getChannel()
										.sendMessage(msg + "\n(failed once: " + f.getMessage() + " (" + f.getClass().getSimpleName() + "))")
										.queue());

				if (isDebug()) {
					e.printStackTrace();
				}
			}
		} else {
			LOGGER.warning("No command command registered for: " + event.getName());
		}
	}

	public void registerCommand(MessageCommandExecutor command) {
		listeners.put(command.name(), command);

		jda.upsertCommand(command.build()).queue((c) -> {
			LOGGER.info("Registered command command: " + command.name());
		}, (e) -> {
			if (e instanceof CancellationException)
				return; // ignore

			// LOGGER.info("Exception while registering command command: " + command.name() + ": " +
			// PCUtils.getRootCauseMessage(e));
			e.printStackTrace();
		});
	}

	public static boolean isDebug() {
		return DEBUG || DiscordSenderService.DEBUG;
	}

}
