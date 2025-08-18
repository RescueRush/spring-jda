package lu.rescue_rush.spring.jda.modal;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

public abstract class DefaultModalInteractionExecutor implements ModalInteractionExecutor {

	private String name;

	@Override
	public Object extractData(ModalInteractionEvent event) {
		return event.getModalId().contains(":") ? event.getModalId().split(":")[1] : event.getModalId();
	}

	@Override
	public void setBeanName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

}
