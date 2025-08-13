package lu.rescue_rush.spring.jda.embed;

import net.dv8tion.jda.api.interactions.components.buttons.Button;

public interface DiscordButtonMessage {

	Button button();

	default Button[] buttons() {
		return new Button[] { button() };
	}

}
