package services;

import akka.stream.javadsl.Source;

import javax.inject.Singleton;
import java.util.Observable;
import java.util.Observer;

@Singleton
public class EventObserver implements Observer {

	private Source<String, ?> source;
	private boolean updated = false;

	private static final EventObserver instance = new EventObserver();

	@Override
	@SuppressWarnings("unchecked")
	public synchronized void update(Observable o, Object arg) {

		if (source == null) {
			source = Source.single((String) arg);
		}
		else {
			source.concat(Source.single((String) arg));
		}
		//source = Source.single((String) arg);
		// updated = true;
	}

	public static final EventObserver getInstance()
	{
		return instance;
	}

	// TODO make this method return error flux if source is null or something else
	public synchronized Source<String, ?> getSource() {
		return source;
	}

	public boolean isUpdated() {
		return updated;
	}

	public void setUpdated(boolean updated) {
		this.updated = updated;
	}
}
