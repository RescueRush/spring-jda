package lu.rescue_rush.spring.jda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackageClasses = { DiscordSenderService.class })
public class SpringJDAMain {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(SpringJDAMain.class);

		app.run(args);
	}
	
}
