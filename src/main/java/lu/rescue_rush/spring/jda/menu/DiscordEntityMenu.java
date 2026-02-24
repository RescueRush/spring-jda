package lu.rescue_rush.spring.jda.menu;

import net.dv8tion.jda.api.components.selections.EntitySelectMenu;

non-sealed public interface DiscordEntityMenu extends DiscordMenu {

	@Override
	EntitySelectMenu build();

}
