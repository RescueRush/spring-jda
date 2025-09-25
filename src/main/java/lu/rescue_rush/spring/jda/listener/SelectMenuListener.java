package lu.rescue_rush.spring.jda.listener;

import java.util.Map;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lu.rescue_rush.spring.jda.DiscordSenderService;
import lu.rescue_rush.spring.jda.menu.DiscordEntityMenuExecutor;
import lu.rescue_rush.spring.jda.menu.DiscordMenuExecutor;
import lu.rescue_rush.spring.jda.menu.DiscordStringMenuExecutor;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Component
public class SelectMenuListener extends ListenerAdapter {

	private static final Logger LOGGER = Logger.getLogger(MessageCommandListener.class.getName());

	public static final String DEBUG_PROPERTY = SelectMenuListener.class.getSimpleName() + ".debug";
	public static boolean DEBUG = Boolean.getBoolean(DEBUG_PROPERTY);

	@Autowired
	private DiscordSenderService discordSenderService;

	private final Map<String, DiscordMenuExecutor> listeners;

	public SelectMenuListener(Map<String, DiscordMenuExecutor> listeners) {
		this.listeners = listeners;
	}

	@Async
	@EventListener(ApplicationReadyEvent.class)
	public void init() {
		discordSenderService.awaitJDAReady();

		listeners.entrySet().forEach(e -> LOGGER.info("Registering selection menu: " + e.getKey()));
	}

	@Override
	public void onStringSelectInteraction(StringSelectInteractionEvent event) {
		final String id = event.getComponentId().contains(":") ? event.getComponentId().split(":")[0] : event.getComponentId();
		if (listeners.containsKey(id)) {
			try {
				DiscordMenuExecutor listener = listeners.get(id);
				if (!(listener instanceof DiscordStringMenuExecutor)) {
					LOGGER.warning("Listener for '" + event.getComponentId() + "' isn't for String selection!");
					event.getHook().sendMessage("Listener for '" + event.getComponentId() + "' isn't for String selection!");
					return;
				}

				((DiscordStringMenuExecutor) listener).execute(event);
			} catch (Exception e) {
				final String msg = "A method executor (`" + event.getComponentId() + "`) raised an exception: " + e.getMessage() + " ("
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
			LOGGER.warning("No select menu registered for: " + event.getComponentId());
		}
	}

	@Override
	public void onEntitySelectInteraction(EntitySelectInteractionEvent event) {
		final String id = event.getComponentId().contains(":") ? event.getComponentId().split(":")[0] : event.getComponentId();
		if (listeners.containsKey(id)) {
			try {
				final DiscordMenuExecutor listener = listeners.get(id);
				if (!(listener instanceof DiscordEntityMenuExecutor)) {
					LOGGER.warning("Listener for '" + event.getComponentId() + "' isn't for Entity selection!");
					event.getHook().sendMessage("Listener for '" + event.getComponentId() + "' isn't for Entity selection!");
					return;
				}
				((DiscordEntityMenuExecutor) listener).execute(event);
			} catch (Exception e) {
				final String msg = "A method executor (`" + event.getComponentId() + "`) raised an exception: " + e.getMessage() + " ("
						+ e.getClass().getSimpleName() + ")";
				event.getHook().sendMessage(msg).queue(null, (f) -> event.getChannel().sendMessage(msg).queue());

				e.printStackTrace();
			}
		} else {
			LOGGER.warning("No select menu registered for: " + event.getComponentId());
		}
	}

	public static boolean isDebug() {
		return DEBUG || DiscordSenderService.DEBUG;
	}

}
