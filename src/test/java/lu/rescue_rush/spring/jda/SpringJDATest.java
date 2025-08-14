package lu.rescue_rush.spring.jda;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.JDA;

@SpringBootTest
public class SpringJDATest {

	private static final Logger LOGGER = Logger.getLogger(SpringJDATest.class.getName());
	
	@Autowired
	private JDA jda;
	
	@Autowired
	private DiscordSenderService discordSenderService;
	
	@PostConstruct
	private void init() {
		if(discordSenderService.isReady()) {
			LOGGER.info("JDA is already ready.");
		}else {
			final long startTime = System.currentTimeMillis();
			LOGGER.info("JDA is not ready, waiting for it to be ready.");
			discordSenderService.awaitJDAReady();
			final long endTime = System.currentTimeMillis();
			LOGGER.info("JDA is ready after " + (endTime - startTime) + "ms.");
		}
	}
	
}
