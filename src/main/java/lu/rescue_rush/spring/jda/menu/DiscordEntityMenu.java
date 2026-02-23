package lu.rescue_rush.spring.jda.menu;

import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;

non-sealed public interface DiscordEntityMenu extends DiscordMenu {

	@Override
	default EntitySelectMenu build() {
		return build(null);
	}

	EntitySelectMenu build(Object arg);

}
