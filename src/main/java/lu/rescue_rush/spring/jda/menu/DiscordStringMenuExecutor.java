package lu.rescue_rush.spring.jda.menu;

import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

non-sealed public interface DiscordStringMenuExecutor extends DiscordMenuExecutor<StringSelectInteractionEvent> {

	@Override
	void execute(StringSelectInteractionEvent event);

}
