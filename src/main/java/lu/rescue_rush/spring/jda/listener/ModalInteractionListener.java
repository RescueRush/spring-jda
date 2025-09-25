package lu.rescue_rush.spring.jda.listener;

import java.util.Map;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lu.rescue_rush.spring.jda.DiscordSenderService;
import lu.rescue_rush.spring.jda.modal.ModalInteractionExecutor;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Component
public class ModalInteractionListener extends ListenerAdapter {

	private static final Logger LOGGER = Logger.getLogger(ModalInteractionListener.class.getName());

	public static final String DEBUG_PROPERTY = ModalInteractionListener.class.getSimpleName() + ".debug";
	public static boolean DEBUG = Boolean.getBoolean(DEBUG_PROPERTY);

	@Autowired
	private DiscordSenderService discordSenderService;

	@Autowired
	private Map<String, ModalInteractionExecutor> listeners;

	@PostConstruct
	public void init() {
		discordSenderService.awaitJDAReady();

		listeners.entrySet().forEach(e -> LOGGER.info("Registered modal interaction: " + e.getKey()));
	}

	@Override
	public void onModalInteraction(ModalInteractionEvent event) {
		if (hasModal(event.getModalId())) {
			try {
				getListener(event.getModalId()).execute(event);
			} catch (Exception e) {
				final String msg = "A method executor (`" + event.getModalId() + "`) raised an exception: " + e.getMessage() + " ("
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
			LOGGER.warning("No modal interaction registered for: " + event.getModalId());
		}
	}

	private ModalInteractionExecutor getListener(String modalId) {
		modalId = modalId.contains(":") ? modalId.split(":")[0] : modalId;
		return listeners.get(modalId);
	}

	private boolean hasModal(String modalId) {
		return listeners.containsKey(modalId) || listeners
				.values()
				.stream()
				.anyMatch(list -> modalId.contains(":") ? modalId.split(":")[0].equals(list.getName()) : false);
	}

	public static boolean isDebug() {
		return DEBUG || DiscordSenderService.DEBUG;
	}

}
