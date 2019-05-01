package services;

import java.util.Observable;
import java.util.Observer;

public abstract class EventObserver implements Observer {

	public EventObserver() {
	}

	@Override
	@SuppressWarnings("unchecked")
	public abstract void update(Observable o, Object arg);
}
