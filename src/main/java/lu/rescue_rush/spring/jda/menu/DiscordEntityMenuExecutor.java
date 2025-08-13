package lu.rescue_rush.spring.jda.menu;

import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;

public non-sealed interface DiscordEntityMenuExecutor extends DiscordMenuExecutor<EntitySelectInteractionEvent> {

	void execute(EntitySelectInteractionEvent event);

}
