package lu.rescue_rush.spring.jda.menu;

import net.dv8tion.jda.api.components.selections.StringSelectMenu;

non-sealed public interface DiscordStringMenu extends DiscordMenu {

	@Override
	StringSelectMenu build();

}
