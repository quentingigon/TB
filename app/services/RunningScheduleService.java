package services;

import models.db.Flux;
import models.db.RunningSchedule;

import java.util.List;
import java.util.Observable;

public class RunningScheduleService extends Observable implements Runnable {

	private RunningSchedule schedule;
	private List<Flux> fluxes;

	private boolean running;

	public RunningScheduleService() {
	}

	public RunningScheduleService(RunningSchedule schedule) {
		this.schedule = schedule;
		this.fluxes = schedule.getFluxes();

		running = true;
	}

	@Override
	public void run() {

		int i = 0;

		while (running) {

			if (!fluxes.isEmpty()) {

				// System.out.println("RunningScheduleService: " + i++ + " for schedule " + schedule.getName());

				// make rotation
				fluxes.add(fluxes.get(0));
				Flux flux = fluxes.remove(0);

				FluxEvent event = new FluxEvent(flux, schedule.getScreens());

				setChanged();
				notifyObservers(event);

				try {
					Thread.sleep(flux.getDuration());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public RunningSchedule getSchedule() {
		return schedule;
	}

	public void setSchedule(RunningSchedule schedule) {
		this.schedule = schedule;
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
