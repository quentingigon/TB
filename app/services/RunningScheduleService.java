package services;

import models.db.Flux;
import models.db.RunningSchedule;
import models.db.Screen;

import java.util.List;
import java.util.Observable;

public class RunningScheduleService extends Observable implements Runnable {

	private RunningSchedule runningSchedule;
	private List<Flux> fluxes;
	private List<Screen> screens;

	private boolean running;
	
	public RunningScheduleService(RunningSchedule runningSchedule, List<Flux> fluxes, List<Screen> screens) {
		this.runningSchedule = runningSchedule;
		this.fluxes = fluxes;
		this.screens = screens;
		running = true;
	}

	@Override
	public void run() {

		while (running) {

			if (!fluxes.isEmpty()) {

				// make rotation
				fluxes.add(fluxes.get(0));
				Flux flux = fluxes.remove(0);

				FluxEvent event = new FluxEvent(flux, screens);

				setChanged();
				notifyObservers(event);

				try {
					if (Thread.currentThread().isInterrupted()) {
						throw new InterruptedException("Thread interrupted");
					}
					Thread.sleep(flux.getDuration());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public synchronized void removeFluxFromRunningSchedule(Flux flux) {
		fluxes.remove(flux);
	}

	public synchronized void addFluxToRunningSchedule(Flux flux) {
		fluxes.add(flux);
	}

	public RunningSchedule getRunningSchedule() {
		return runningSchedule;
	}

	public void setRunningSchedule(RunningSchedule runningSchedule) {
		this.runningSchedule = runningSchedule;
	}

	public List<Flux> getFluxes() {
		return fluxes;
	}

	public void setFluxes(List<Flux> fluxes) {
		this.fluxes = fluxes;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}
}
