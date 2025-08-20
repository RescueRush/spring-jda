package lu.rescue_rush.spring.jda;

import java.awt.Color;
import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lu.rescue_rush.spring.jda.embed.DiscordButtonMessage;
import lu.rescue_rush.spring.jda.embed.DiscordEmbed;
import lu.rescue_rush.spring.jda.message.DiscordMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

@Service
public class DiscordSenderService {

	private static final Logger LOGGER = Logger.getLogger(DiscordSenderService.class.getName());

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

	@PostConstruct
	private void init() {
		// init all listeners
		final Thread t = new Thread(() -> {
			LOGGER.info("Waiting for JDA to be ready...");

			awaitJDAReady();

			LOGGER.info("Registering JDA listeners...");

			final Collection<ListenerAdapter> las = context.getBeansOfType(ListenerAdapter.class).values();
			las.forEach(jda::addEventListener);

			LOGGER.info("Registered " + las.size() + " JDA listeners.");
		});
		t.setDaemon(true);
		t.setName("jda-startup-listeners");
		t.start();
	}

	public void send(MessageChannel channel, String message) {
		awaitJDAReady();
		channel.sendMessage(message).complete();
	}

	public void send(MessageChannel channel, MessageCreateData message) {
		awaitJDAReady();
		channel.sendMessage(message).complete();
	}

	public void send(MessageChannel channel, MessageEmbed embed) {
		awaitJDAReady();
		channel.sendMessageEmbeds(embed).complete();
	}

	public void sendEmbed(MessageChannel channel, DiscordEmbed embed) {
		try {
			awaitJDAReady();
			if (embed instanceof DiscordButtonMessage buttons) {
				channel.sendMessageEmbeds(embed.build()).setActionRow(buttons.buttons()).complete();
			} else {
				channel.sendMessageEmbeds(embed.build()).complete();
			}
		} catch (Exception e) {
			throw new RuntimeException("Error when sending embed: " + embed.getClass().getName(), e);
		}
	}

	public void sendMessage(MessageChannel channel, DiscordMessage message) {
		try {
			awaitJDAReady();
			if (message instanceof DiscordButtonMessage buttons) {
				channel.sendMessage(message.body()).setActionRow(buttons.buttons()).complete();
			} else {
				channel.sendMessage(message.body()).complete();
			}
		} catch (Exception e) {
			throw new RuntimeException("Error when sending embed: " + message.getClass().getName(), e);
		}
	}

	public void sendEmbed(MessageChannel channel, String title, String contentTitle, String content, Color color) {
		final EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle(title);
		builder.setColor(color);
		builder.addField(new Field(contentTitle, content, true));
		send(channel, builder.build());
	}

	public void sendEmbed(MessageChannel channel, String title, Color color, Field... fields) {
		final EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle(title);
		builder.setColor(color);
		for (Field f : fields)
			builder.addField(f);
		send(channel, builder.build());
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
		try {
			lock.await();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while waiting for JDA to be ready.");
		}
		if (startupError != null)
			throw new IllegalStateException("JDA not ready", startupError);
	}

	public boolean isReady() {
		return lock.getCount() == 0 && startupError == null;
	}

}
