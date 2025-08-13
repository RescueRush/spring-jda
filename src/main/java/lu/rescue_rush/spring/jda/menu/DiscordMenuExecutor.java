package lu.rescue_rush.spring.jda.menu;

import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent;

public sealed interface DiscordMenuExecutor<T extends GenericSelectMenuInteractionEvent<?, ?>>
		permits DiscordStringMenuExecutor, DiscordEntityMenuExecutor {

	void execute(T event);

	String id();

	default Object getArg(T event) {
		return event.getComponentId().contains(":") ? event.getComponentId().split(":")[1] : null;
	}

}
