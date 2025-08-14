package lu.rescue_rush.spring.jda.config;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

@Configuration
public class JDAProvider {

	@Autowired
	private DiscordBotConfig config;

	@Bean
	public JDA jdaConfig() throws InterruptedException {
		final Logger logger = Logger.getLogger(JDAProvider.class.getName());

		final JDA jda = JDABuilder
				.createDefault(config.token)
				.setChunkingFilter(ChunkingFilter.ALL)
				.setMemberCachePolicy(MemberCachePolicy.ALL)
				.enableIntents(GatewayIntent.GUILD_MEMBERS)
				.build();

		// jda.awaitReady();

		return jda;
	}

}
