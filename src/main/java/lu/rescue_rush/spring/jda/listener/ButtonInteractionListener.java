package lu.rescue_rush.spring.jda.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lu.rescue_rush.spring.jda.DiscordSenderService;
import lu.rescue_rush.spring.jda.button.ButtonInteractionExecutor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Component
public class ButtonInteractionListener extends ListenerAdapter {

	private static final Logger LOGGER = Logger.getLogger(ButtonInteractionListener.class.getName());

	@Autowired
	private ApplicationContext context;
	
	@Autowired
	private JDA jda;

	@Autowired
	private DiscordSenderService discordSenderService;

	private Map<String, ButtonInteractionExecutor> listeners = new HashMap<>();

	@PostConstruct
	public void init() {
		discordSenderService.awaitJDAReady();

		final Thread t = new Thread(() -> {
			final Map<String, ButtonInteractionExecutor> beans = context.getBeansOfType(ButtonInteractionExecutor.class);
			beans.entrySet().forEach(e -> registerInteraction(e.getKey(), e.getValue()));
		});
		t.setName("ButtonInteractionListener-Init");
		t.setDaemon(true);
		t.start();
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

	public void registerInteraction(String name, ButtonInteractionExecutor interaction) {
		listeners.put(name, interaction);

		LOGGER.info("Registering button interaction: " + name);
	}

}
