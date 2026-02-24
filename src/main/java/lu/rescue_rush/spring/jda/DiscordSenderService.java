package lu.rescue_rush.spring.jda;

import java.awt.Color;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import lu.rescue_rush.spring.jda.embed.DiscordButtonMessage;
import lu.rescue_rush.spring.jda.embed.DiscordEmbed;
import lu.rescue_rush.spring.jda.message.DiscordMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

@Service
@ConditionalOnBean(JDA.class)
public class DiscordSenderService {

	private static final Logger LOGGER = Logger.getLogger(DiscordSenderService.class.getName());

	public static final String DEBUG_PROPERTY = DiscordSenderService.class.getSimpleName() + ".debug";
	public static boolean DEBUG = Boolean.getBoolean(DEBUG_PROPERTY);

	private final CountDownLatch lock = new CountDownLatch(1);
	private Throwable startupError = null;

	@Autowired
	private ApplicationContext context;

	protected JDA jda;

	public DiscordSenderService(JDA jda) {
		this.jda = jda;

		final Thread t = new Thread(() -> {
			try {
				LOGGER.info("Starting JDA...");
				jda.awaitReady();
				LOGGER.info("JDA is ready");
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				startupError = e;
				LOGGER.warning("JDA startup interrupted, shutting down.");
				jda.shutdownNow();
			} catch (Throwable ts) {
				startupError = ts;
				LOGGER.severe("JDA failed to start: " + ts.getMessage());
				jda.shutdownNow();
			} finally {
				lock.countDown(); // always release waiting threads
			}
		});
		t.setDaemon(true);
		t.setName("jda-startup");
		t.start();
	}

	@Async
	@EventListener(ApplicationReadyEvent.class)
	public void init() {
		LOGGER.info("Waiting for JDA to be ready...");

		awaitJDAReady();

		LOGGER.info("Registering JDA listeners...");

		final long startTime = System.nanoTime();
		final Collection<ListenerAdapter> las = context.getBeansOfType(ListenerAdapter.class).values();
		final long startTime2 = System.nanoTime();
		las.forEach(jda::addEventListener);
		final long endTime = System.nanoTime();

		LOGGER.info("Registered " + las.size() + " JDA listeners (" + ((endTime - startTime) / 1e9) + "s / "
				+ ((endTime - startTime2) / 1e6) + "ms).");
	}

	public JDA getJda() {
		return jda;
	}

	public Message send(MessageChannel channel, String message) {
		awaitJDAReady();
		return channel.sendMessage(message).complete();
	}

	public Message send(MessageChannel channel, MessageCreateData message) {
		awaitJDAReady();
		return channel.sendMessage(message).complete();
	}

	public Message send(MessageChannel channel, MessageEmbed embed) {
		awaitJDAReady();
		return channel.sendMessageEmbeds(embed).complete();
	}

	public Message sendEmbed(MessageChannel channel, DiscordEmbed embed) {
		try {
			awaitJDAReady();
			if (embed instanceof DiscordButtonMessage buttons) {
				return channel.sendMessageEmbeds(embed.build()).addComponents(ActionRow.of(List.of(buttons.buttons())))
						.complete();
			} else {
				return channel.sendMessageEmbeds(embed.build()).complete();
			}
		} catch (Exception e) {
			throw new RuntimeException("Error when sending embed: " + embed.getClass().getName(), e);
		}
	}

	public Message sendMessage(MessageChannel channel, DiscordMessage message) {
		try {
			awaitJDAReady();
			if (message instanceof DiscordButtonMessage buttons) {
				return channel.sendMessage(message.body()).addComponents(ActionRow.of(List.of(buttons.buttons())))
						.complete();
			} else {
				return channel.sendMessage(message.body()).complete();
			}
		} catch (Exception e) {
			throw new RuntimeException("Error when sending embed: " + message.getClass().getName(), e);
		}
	}

	public Message sendEmbed(MessageChannel channel, String title, String contentTitle, String content, Color color) {
		final EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle(title);
		builder.setColor(color);
		builder.addField(new Field(contentTitle, content, true));
		return send(channel, builder.build());
	}

	public Message sendEmbed(MessageChannel channel, String title, Color color, Field... fields) {
		final EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle(title);
		builder.setColor(color);
		for (Field f : fields)
			builder.addField(f);
		return send(channel, builder.build());
	}

	@PreDestroy
	public void shutdown() throws InterruptedException {
		LOGGER.info("JDA shutdown requested.");
		if (!jda.awaitShutdown(Duration.ofSeconds(30))) {
			jda.shutdownNow();
			jda.awaitShutdown();
		}
	}

	public void shutdownNow() {
		LOGGER.info("Immediate JDA shutdown requested.");

		jda.shutdownNow();
	}

	public void awaitJDAReady() {
		if (isReady()) {
			if (isDebug()) {
				LOGGER.info("Skipping wait for " + Thread.currentThread().getName());
			}
			if (startupError != null) {
				throw new IllegalStateException("JDA not ready", startupError);
			}
			return;
		}
		try {
			if (isDebug()) {
				LOGGER.info(Thread.currentThread().getName() + " is waiting on JDA");
			}
			lock.await();
			if (isDebug()) {
				LOGGER.info(Thread.currentThread().getName() + " got released from waiting on JDA");
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while waiting for JDA to be ready.");
		}
		if (startupError != null) {
			throw new IllegalStateException("JDA not ready", startupError);
		}
	}

	public boolean isReady() {
		return lock.getCount() == 0 && startupError == null;
	}

	public static boolean isDebug() {
		return DEBUG;
	}

}
