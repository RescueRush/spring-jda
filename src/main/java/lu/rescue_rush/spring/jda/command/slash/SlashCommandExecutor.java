package lu.rescue_rush.spring.jda.command.slash;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public interface SlashCommandExecutor {

	void execute(SlashCommandInteractionEvent event);

	String id();

	String description();

	default OptionData[] options() {
		return new OptionData[0];
	}

	default SlashCommandData build() {
		return Commands.slash(id(), description()).addOptions(options());
	}

}
