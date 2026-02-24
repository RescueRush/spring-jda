package lu.rescue_rush.spring.jda;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.convert.ConversionService;

import lu.rescue_rush.spring.jda.config.SpringJDAAutoConfig;

public class ConfigTest {

	@Test
	void autoConfigLoads() {
		new ApplicationContextRunner()
				.withInitializer(context -> AutoConfigurationPackages.register((BeanDefinitionRegistry) context,
						"lu.kbra.pclib"))
				.withConfiguration(AutoConfigurations.of(SpringJDAAutoConfig.class))
				.withBean(ConversionService.class, ApplicationConversionService::new).run(context -> {
					assertThat(context).hasSingleBean(DiscordSenderService.class);
				});
	}

}
