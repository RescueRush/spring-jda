package lu.rescue_rush.spring.jda.button;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public interface ButtonInteractionExecutor {

	void execute(ButtonInteractionEvent event);

	String id();

}
