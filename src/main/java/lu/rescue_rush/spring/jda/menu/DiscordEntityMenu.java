package lu.rescue_rush.spring.jda.menu;

import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;

public non-sealed interface DiscordEntityMenu extends DiscordMenu {

	default EntitySelectMenu build() {
		return build(null);
	}

	EntitySelectMenu build(Object arg);

}
