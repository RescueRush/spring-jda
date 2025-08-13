package lu.rescue_rush.spring.jda.command.message;

import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public interface MessageCommandExecutor {

	void execute(MessageContextInteractionEvent event);

	String name();

	default CommandData build() {
		return Commands.message(name()).setContexts(InteractionContextType.GUILD);
	}

}
