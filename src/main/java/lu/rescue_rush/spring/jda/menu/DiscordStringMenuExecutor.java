package lu.rescue_rush.spring.jda.menu;

import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

public non-sealed interface DiscordStringMenuExecutor extends DiscordMenuExecutor<StringSelectInteractionEvent> {

	void execute(StringSelectInteractionEvent event);

}
