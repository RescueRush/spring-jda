package lu.rescue_rush.spring.jda;

import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import net.dv8tion.jda.api.JDA;

@SpringBootTest(classes = SpringJDAMain.class)
public class SpringJDATest {

	private static final Logger LOGGER = Logger.getLogger(SpringJDATest.class.getName());

	@Autowired
	private JDA jda;

	@Autowired
	private DiscordSenderService discordSenderService;

	@Test
	public void init() throws InterruptedException {
		if (discordSenderService.isReady()) {
			LOGGER.info("JDA is already ready.");
		} else {
			final long startTime = System.currentTimeMillis();
			LOGGER.info("JDA is not ready, waiting for it to be ready.");
			discordSenderService.awaitJDAReady();
			final long endTime = System.currentTimeMillis();
			LOGGER.info("JDA is ready after " + (endTime - startTime) + "ms.");
		}
		
		discordSenderService.shutdownNow();
	}

}
