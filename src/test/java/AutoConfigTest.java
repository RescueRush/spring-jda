
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import lu.rescue_rush.spring.jda.DiscordSenderService;
import lu.rescue_rush.spring.jda.config.SpringJDAAutoConfiguration;

public class AutoConfigTest {

	@Autowired
	private DiscordSenderService discordSenderService;
	
	@Test
	void autoConfigTest() throws InterruptedException {
		new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(SpringJDAAutoConfiguration.class)).run(context -> {
			assertThat(context).hasSingleBean(DiscordSenderService.class);
		});
		
		discordSenderService.shutdownNow();
	}

}
