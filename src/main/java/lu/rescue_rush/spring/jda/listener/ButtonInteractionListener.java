package lu.rescue_rush.spring.jda.listener;

import java.util.Map;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lu.rescue_rush.spring.jda.DiscordSenderService;
import lu.rescue_rush.spring.jda.button.ButtonInteractionExecutor;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Component
public class ButtonInteractionListener extends ListenerAdapter {

	private static final Logger LOGGER = Logger.getLogger(ButtonInteractionListener.class.getName());

	public static final String DEBUG_PROPERTY = ButtonInteractionListener.class.getSimpleName() + ".debug";
	public static boolean DEBUG = Boolean.getBoolean(DEBUG_PROPERTY);

	@Autowired
	private DiscordSenderService discordSenderService;

	private final Map<String, ButtonInteractionExecutor> listeners;

	public ButtonInteractionListener(Map<String, ButtonInteractionExecutor> listeners) {
		this.listeners = listeners;
	}

	@Async
	@EventListener(ApplicationReadyEvent.class)
	public void init() {
		discordSenderService.awaitJDAReady();

		listeners.entrySet().forEach(e -> LOGGER.info("Registering button interaction: " + e.getKey()));
	}

	@Override
	public void onButtonInteraction(ButtonInteractionEvent event) {
		if (listeners.containsKey(event.getComponentId())) {
			LOGGER.info("Got button interaction '" + event.getComponentId() + "' from channel: " + event.getChannelId());
			listeners.get(event.getComponentId()).execute(event);
		} else {
			LOGGER.warning("No button interaction registered for: " + event.getComponentId());
		}
	}

	public static boolean isDebug() {
		return DEBUG || DiscordSenderService.DEBUG;
	}

}
