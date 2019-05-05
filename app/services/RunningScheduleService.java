package services;

import models.db.Flux;
import models.db.RunningSchedule;
import models.repositories.ScheduleRepository;

import javax.inject.Inject;
import java.util.List;
import java.util.Observable;

public class RunningScheduleService extends Observable implements Runnable {

	@Inject
	ScheduleRepository scheduleRepository;

	private RunningSchedule runningSchedule;
	private List<Flux> fluxes;

	private boolean running;

	public RunningScheduleService() {
	}

	public RunningScheduleService(RunningSchedule runningSchedule) {
		this.runningSchedule = runningSchedule;
		this.fluxes = scheduleRepository.getById(runningSchedule.getScheduleId()).getFluxes();

		running = true;
	}

	@Override
	public void run() {

		int i = 0;

		while (running) {

			if (!fluxes.isEmpty()) {

				// System.out.println("RunningScheduleService: " + i++ + " for runningSchedule " + runningSchedule.getName());

				// make rotation
				fluxes.add(fluxes.get(0));
				Flux flux = fluxes.remove(0);

				FluxEvent event = new FluxEvent(flux, runningSchedule.getScreens());

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
