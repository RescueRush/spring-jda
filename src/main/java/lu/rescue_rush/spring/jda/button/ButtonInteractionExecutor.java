package lu.rescue_rush.spring.jda.button;

import org.springframework.beans.factory.BeanNameAware;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public interface ButtonInteractionExecutor extends BeanNameAware {

	void execute(ButtonInteractionEvent event);

}
