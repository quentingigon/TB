package services;

import models.db.Flux;
import models.db.Screen;

import java.util.ArrayList;
import java.util.List;

public class FluxEvent {

	private Flux flux;
	private List<String> macs;

	public FluxEvent() {
	}

	public FluxEvent(Flux flux, List<Screen> screens) {
		this.flux = flux;
		getMacAddresses(screens);
	}

	public FluxEvent(Flux flux, Screen screen) {
		this.flux = flux;
		List<Screen> screens = new ArrayList<>();
		screens.add(screen);
		getMacAddresses(screens);
	}

	private void getMacAddresses(List<Screen> screens) {
		macs = new ArrayList<>();

		for(Screen s: screens) {
			macs.add(s.getMacAddress());
		}
	}

	public synchronized Flux getFlux() {
		return flux;
	}

	public void setFlux(Flux flux) {
		this.flux = flux;
	}

	public synchronized List<String> getMacs() {
		return macs;
	}

	public void setMacs(List<String> macs) {
		this.macs = macs;
	}
}
