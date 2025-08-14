package lu.rescue_rush.spring.jda.modal;

import java.util.Arrays;
import java.util.stream.Collectors;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.modals.Modal;

public interface ModalInteractionExecutor {

	default Modal build(Object obj) {
		return Modal.create(getName() + ":" + obj.toString(), title()).addComponents(rows()).build();
	}

	default Modal build() {
		return Modal.create(getName(), title()).addComponents(rows()).build();
	}

	default ActionRow[] rows() {
		return components() == null ? new ActionRow[] { ActionRow.of(component()) }
				: Arrays.stream(components()).map(ActionRow::of).collect(Collectors.toList()).toArray(new ActionRow[0]);
	}

	Object extractData(ModalInteractionEvent event);

	ItemComponent component();

	default ItemComponent[] components() {
		return null;
	}

	String title();

	void execute(ModalInteractionEvent event);

	String getName();

	void setName(String name);

}
