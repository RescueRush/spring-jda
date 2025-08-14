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
import lu.rescue_rush.spring.jda.command.message.MessageCommandExecutor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Component
public class MessageCommandListener extends ListenerAdapter {

	private static final Logger LOGGER = Logger.getLogger(MessageCommandListener.class.getName());

	@Autowired
	private ApplicationContext context;

	@Autowired
	private JDA jda;

	@Autowired
	private DiscordSenderService discordSenderService;

	private Map<String, MessageCommandExecutor> listeners = new HashMap<>();

	@PostConstruct
	public void init() {
		discordSenderService.awaitJDAReady();

		final Thread t = new Thread(() -> {
			final Map<String, MessageCommandExecutor> beans = context.getBeansOfType(MessageCommandExecutor.class);
			beans.values().stream().forEach(this::registerCommand);
		});
		t.setName("MessageCommandListener-Init");
		t.setDaemon(true);
		t.start();
	}

	@Override
	public void onMessageContextInteraction(MessageContextInteractionEvent event) {
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

			// LOGGER.info("Exception while registering command command: " + command.name() + ": " + PCUtils.getRootCauseMessage(e));
			e.printStackTrace();
		});
	}

}
