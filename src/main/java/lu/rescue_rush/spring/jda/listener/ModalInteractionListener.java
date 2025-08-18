package lu.rescue_rush.spring.jda.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lu.rescue_rush.spring.jda.DiscordSenderService;
import lu.rescue_rush.spring.jda.modal.ModalInteractionExecutor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Component
public class ModalInteractionListener extends ListenerAdapter {

	private static final Logger LOGGER = Logger.getLogger(ModalInteractionListener.class.getName());

	@Autowired
	private ApplicationContext context;

	@Autowired
	private JDA jda;

	@Autowired
	private DiscordSenderService discordSenderService;

	private Map<String, ModalInteractionExecutor> listeners = new HashMap<>();

	@PostConstruct
	public void init() {
		final Thread t = new Thread(() -> {
			discordSenderService.awaitJDAReady();
			
			final Map<String, ModalInteractionExecutor> beans = context.getBeansOfType(ModalInteractionExecutor.class);
			beans.entrySet().forEach(e -> registerInteraction(e.getKey(), e.getValue()));
		});
		t.setName("ModalInteractionListener-Init");
		t.setDaemon(true);
		t.start();
	}

	@Override
	public void onModalInteraction(ModalInteractionEvent event) {
		if (hasModal(event.getModalId())) {
			// LOGGER.info("Got modal interaction '" + event.getModalId() + "' from channel: " + event.getChannelId());
			getListener(event.getModalId()).execute(event);
		} else {
			LOGGER.warning("No modal interaction registered for: " + event.getModalId());
		}
	}

	private ModalInteractionExecutor getListener(String modalId) {
		modalId = modalId.contains(":") ? modalId.split(":")[0] : modalId;
		return listeners.get(modalId);
	}

	private boolean hasModal(String modalId) {
		return listeners.containsKey(modalId)
				|| listeners.values().stream().anyMatch(list -> modalId.contains(":") ? modalId.split(":")[0].equals(list.getName()) : false);
	}

	public void registerInteraction(String name, ModalInteractionExecutor interaction) {
		listeners.put(name, interaction);
		interaction.setName(name);

		LOGGER.info("Registered modal interaction: " + name);
	}

}
