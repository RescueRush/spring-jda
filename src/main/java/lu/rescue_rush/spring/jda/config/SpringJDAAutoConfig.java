package lu.rescue_rush.spring.jda.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

import lu.rescue_rush.spring.jda.DiscordSenderService;

@AutoConfiguration
@ComponentScan(basePackageClasses = DiscordSenderService.class)
public class SpringJDAAutoConfig {

}
