package lu.rescue_rush.spring.jda.command.slash;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;

public interface SlashCommandAutocomplete {

	void complete(CommandAutoCompleteInteractionEvent event);

}
