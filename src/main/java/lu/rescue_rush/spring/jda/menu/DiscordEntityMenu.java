package lu.rescue_rush.spring.jda.menu;

import org.springframework.beans.factory.BeanNameAware;

import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;

public non-sealed interface DiscordEntityMenu extends DiscordMenu, BeanNameAware {

	default EntitySelectMenu build() {
		return build(null);
	}

	EntitySelectMenu build(Object arg);

}
