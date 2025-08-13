package lu.rescue_rush.spring.jda.menu;

import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

public non-sealed interface DiscordStringMenu extends DiscordMenu {

	StringSelectMenu build();

}
