package services;

import controllers.EventSourceControllerS;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FluxSender {

	private final EventSourceControllerS eventController;

	@Inject
	public FluxSender(EventSourceControllerS eventController) {
		this.eventController = eventController;
	}

	public void sendFluxEvent(String eventString) {
		eventController.send(eventString);
	}
}
