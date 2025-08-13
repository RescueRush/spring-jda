package lu.rescue_rush.spring.jda.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lu.rescue_rush.spring.jda.DiscordSenderService;
import lu.rescue_rush.spring.jda.menu.DiscordEntityMenuExecutor;
import lu.rescue_rush.spring.jda.menu.DiscordMenuExecutor;
import lu.rescue_rush.spring.jda.menu.DiscordStringMenuExecutor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Component
public class SelectMenuListener extends ListenerAdapter {

	private static final Logger LOGGER = Logger.getLogger(MessageCommandListener.class.getName());

	@Autowired
	private ApplicationContext context;

	@Autowired
	private JDA jda;

	@Autowired
	private DiscordSenderService discordSenderService;

	private Map<String, DiscordMenuExecutor> listeners = new HashMap<>();

	@PostConstruct
	public void init() {
		final Thread t = new Thread(() -> {
			final Map<String, DiscordMenuExecutor> beans = context.getBeansOfType(DiscordMenuExecutor.class);

			beans.values().stream().forEach(this::registerCommand);
		});
		t.setName("SelectMenuListener-Init");
		t.setDaemon(true);
		t.start();
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
				event.getHook().sendMessage(msg).queue(null, (f) -> event.getChannel().sendMessage(msg).queue());

				e.printStackTrace();
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
				DiscordMenuExecutor listener = listeners.get(id);
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

	public void registerCommand(DiscordMenuExecutor command) {
		listeners.put(command.id(), command);

		LOGGER.info("Registering selection menu: " + command.id());
	}

}
