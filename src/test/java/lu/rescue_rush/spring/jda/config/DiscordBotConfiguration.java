package lu.rescue_rush.spring.jda.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import lu.pcy113.pclib.config.ConfigLoader;

@Configuration
public class DiscordBotConfiguration {

	public static final File CONFIG_DIR = new File("./.config/");
	
	@Bean
	public DiscordBotConfig discordBotConfig() throws FileNotFoundException, IOException {
		final Logger logger = Logger.getLogger(DiscordBotConfiguration.class.getName());

		if (extractFile("discord_bot.json", CONFIG_DIR, "discord_bot.json")) {
			logger.info("Extracted default discord_bot.json to " + CONFIG_DIR);
		} else {
			logger.info("Config file discord_bot.json already existed found.");
		}

		final DiscordBotConfig discordBotConfig = ConfigLoader
				.loadFromJSONFile(new DiscordBotConfig(), new File(CONFIG_DIR, "discord_bot.json"));

		return discordBotConfig;
	}

	
	public static boolean extractFile(String inJarPath, File configDir, String configFileName) {
		try {
			ClassPathResource resource = new ClassPathResource(inJarPath);

			Path targetPath = Path.of(new File(configDir, configFileName).getPath());

			Files.createDirectories(targetPath.getParent());

			if (Files.exists(targetPath)) {
				return false;
			}

			try (InputStream inputStream = resource.getInputStream()) {
				Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
			}

			return true;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
