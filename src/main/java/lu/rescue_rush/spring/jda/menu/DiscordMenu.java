package lu.rescue_rush.spring.jda.menu;

import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;

public sealed interface DiscordMenu permits DiscordEntityMenu, DiscordStringMenu {

	SelectMenu build();

}
