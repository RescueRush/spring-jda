package lu.rescue_rush.spring.jda.modal;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.modals.Modal;

public interface ModalInteractionExecutor {

	default Modal build(Object obj) {
		return Modal.create(name() + ":" + obj.toString(), title()).addComponents(row()).build();
	}

	default Modal build() {
		return Modal.create(name(), title()).addComponents(row()).build();
	}

	default ActionRow row() {
		return ActionRow.of(component());
	}

	ItemComponent component();

	String title();

	void execute(ModalInteractionEvent event);

	String name();

}
