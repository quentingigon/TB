package models;

import models.db.Flux;
import models.db.Screen;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents an Event being sent from a RunningScheduleThread to the FluxManager
 */
public class FluxEvent {

	private Flux flux;
	private List<String> macs;

	private int fluxId;
	private String screenIds;

	public FluxEvent() {
	}

	public FluxEvent(int fluxId, String screenIds) {
		this.fluxId = fluxId;
		this.screenIds = screenIds;
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

	public int getFluxId() {
		return fluxId;
	}

	public void setFluxId(int fluxId) {
		this.fluxId = fluxId;
	}

	public String getScreenIds() {
		return screenIds;
	}

	public void setScreenIds(String screenIds) {
		this.screenIds = screenIds;
	}
}
