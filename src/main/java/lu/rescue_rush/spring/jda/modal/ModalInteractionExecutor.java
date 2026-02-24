package lu.rescue_rush.spring.jda.modal;

import java.util.List;

import org.springframework.beans.factory.BeanNameAware;

import net.dv8tion.jda.api.components.ModalTopLevelComponent;
import net.dv8tion.jda.api.components.tree.ComponentTree;
import net.dv8tion.jda.api.components.tree.ModalComponentTree;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.modals.Modal;

public interface ModalInteractionExecutor extends BeanNameAware {

	default Modal build(Object obj) {
		return Modal.create(getName() + ":" + obj.toString(), title()).addComponents(component()).build();
	}

	default Modal build() {
		return Modal.create(getName(), title()).addComponents(component()).build();
	}

	Object extractData(ModalInteractionEvent event);

	default ModalComponentTree component() {
		return ComponentTree.forModal(List.of(components()));
	}

	ModalTopLevelComponent[] components();

	String title();

	void execute(ModalInteractionEvent event);

	String getName();

}
