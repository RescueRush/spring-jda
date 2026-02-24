package lu.rescue_rush.spring.jda.menu;

import org.springframework.beans.factory.BeanNameAware;

import net.dv8tion.jda.api.components.selections.SelectMenu;

sealed public interface DiscordMenu extends BeanNameAware
		permits DiscordEntityMenu, DiscordStringMenu {

	SelectMenu build();

}
