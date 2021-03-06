package services;

import models.db.Flux;
import models.db.FluxLoop;
import models.db.LoopedFlux;
import models.db.Schedule;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Calendar;

import static controllers.CronUtils.*;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * This class is used to create SendLoopEventJobs, that represents a loop of flux in a Schedule.
 * It offers 2 functions: one to initate a Loop (createFromFluxLoop()) and one to continue a Loop (createFromJob()).
 */
public class LoopEventJobCreator {

	private ServicePicker servicePicker;
	private EventManager eventManager;
	private String screenIds;
	private Schedule schedule;

	public LoopEventJobCreator(Schedule schedule,
							   String screenIds,
							   ServicePicker servicePicker,
							   EventManager eventManager) {
		this.schedule = schedule;
		this.screenIds = screenIds;
		this.servicePicker = servicePicker;
		this.eventManager = eventManager;
	}

	/**
	 * This function checks if there is the time between now and the next bound (FluxTrigger).
	 * If so, it creates a SendLoopEventJob and schedules it.
	 * Some of the values used to create a new Job are recovered from the last Job of the loop
	 * @param triggerDataMap contains values of the last Job of the loop
	 */
	public void createFromJob(JobDataMap triggerDataMap) {
		// create next trigger here
		SchedulerFactory sf = new StdSchedulerFactory();
		try {
			Scheduler scheduler = sf.getScheduler();

			int currentFluxId = triggerDataMap.getInt("currentFluxId");

			String days = triggerDataMap.getString("days");
			String upperTimeBound = triggerDataMap.getString("upperBound");
			String fluxIds = triggerDataMap.getString("fluxIds");

			List<Integer> fluxIdsArray = new ArrayList<>();
			for (String fluxId: fluxIds.split(",")) {
				fluxIdsArray.add(Integer.parseInt(fluxId));
			}

			// time for next trigger
			Flux currentFlux = servicePicker.getFluxService().getFluxById(currentFluxId);
			DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm");
			LocalTime timeForNextTrigger = formatter.parseLocalTime(triggerDataMap.getString("time"));
			timeForNextTrigger = timeForNextTrigger.plusMinutes(currentFlux.getTotalDuration());

			// if there are no upper time bound or if calculated time for next flux
			// is lower than the upper bound (aka next fluxtrigger's time)
			if (upperTimeBound.equals("") ||
				formatter.print(timeForNextTrigger).compareTo(upperTimeBound) < 0) {
				// loop through flux ids
				fluxIdsArray.add(fluxIdsArray.get(0));
				currentFluxId = fluxIdsArray.remove(0);

				StringBuilder newFluxIds = new StringBuilder();
				for (Integer fluxId: fluxIdsArray) {
					newFluxIds.append(fluxId).append(",");
				}
				newFluxIds.deleteCharAt(newFluxIds.length() - 1);

				JobDetail job = newJob(SendLoopEventJob.class)
					.withIdentity(JOB_NAME_LOOP + schedule.getName(), SEND_LOOP_EVENT_GROUP)
					.build();

				CronTrigger trigger = newTrigger()
					.withIdentity(TRIGGER_NAME_LOOP + schedule.getName(), SEND_LOOP_EVENT_GROUP)
					.usingJobData("screenIds", screenIds)
					.usingJobData("fluxIds", newFluxIds.toString())
					.usingJobData("currentFluxId", currentFluxId)
					.usingJobData("entityId", schedule.getId())
					.usingJobData("source", "schedule")
					.usingJobData("days", days)
					.usingJobData("time", formatter.print(timeForNextTrigger))
					.usingJobData("upperBound", upperTimeBound)
					.withSchedule(cronSchedule(getCronCmdLoop(days, formatter.print(timeForNextTrigger))))
					.build();

				// delete previous job and trigger
				if (scheduler.checkExists(new JobKey(JOB_NAME_LOOP + schedule.getName(), SEND_LOOP_EVENT_GROUP))) {
					scheduler.deleteJob(new JobKey(JOB_NAME_LOOP + schedule.getName(), SEND_LOOP_EVENT_GROUP));
				}
				scheduler.scheduleJob(job, trigger);
			}
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This function is used to initiate a FluxLoop in a Schedule.
	 * @param loop FluxLoop from which the SendLoopEventJob is created
	 */
	public void createFromFluxLoop(FluxLoop loop) {

		JobDetail job = newJob(SendLoopEventJob.class)
			.withIdentity(JOB_NAME_LOOP + schedule.getName(), SEND_LOOP_EVENT_GROUP)
			.build();

		List<LoopedFlux> loopedFluxes = servicePicker.getFluxService().getFluxesOfFluxLoopById(loop.getId());
		loopedFluxes.sort(Comparator.comparing(LoopedFlux::getOrder));
		List<Integer> loopedFluxIds = new ArrayList<>();
		for (LoopedFlux lf: loopedFluxes) {
			loopedFluxIds.add(lf.getFluxId());
		}
		loopedFluxIds.add(loopedFluxIds.get(0));
		Integer currentFluxId = loopedFluxIds.remove(0);

		String currentTime = new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());
		DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm");
		LocalTime timeOfExecution = formatter.parseLocalTime(currentTime)
			.plusMinutes(1);
		loop.setStartTime(formatter.print(timeOfExecution));

		CronTrigger trigger = newTrigger()
			.withIdentity(TRIGGER_NAME_LOOP + schedule.getName(), SEND_LOOP_EVENT_GROUP)
			.usingJobData("screenIds", screenIds)
			.usingJobData("fluxIds", getFluxIds(loopedFluxIds))
			.usingJobData("currentFluxId", currentFluxId)
			.usingJobData("entityId", schedule.getId())
			.usingJobData("source", "schedule")
			.usingJobData("days", schedule.getDays())
			.usingJobData("time", loop.getStartTime())
			.usingJobData("upperBound", getNextFluxTriggerTimeOfSchedule(
				servicePicker.getFluxService().getFluxTriggersOfScheduleById(schedule.getId()), currentTime))
			.withSchedule(cronSchedule(getCronCmdLoop(schedule.getDays(), formatter.print(timeOfExecution))))
			.build();

		SchedulerFactory sf = new StdSchedulerFactory();
		Scheduler scheduler;
		try {
			scheduler = sf.getScheduler();
			// delete previous job and trigger
			if (scheduler.checkExists(new JobKey(JOB_NAME_LOOP + schedule.getName(), SEND_LOOP_EVENT_GROUP))) {
				scheduler.deleteJob(new JobKey(JOB_NAME_LOOP + schedule.getName(), SEND_LOOP_EVENT_GROUP));
			}
			scheduler.scheduleJob(job, trigger);

			// creates listener if it doesnt exist
			if (scheduler.getListenerManager().getJobListener(LOOP_JOBS_LISTENER) == null) {
				SendLoopEventJobsListener listener = new SendLoopEventJobsListener(
					LOOP_JOBS_LISTENER,
					eventManager,
					servicePicker);

				scheduler.getListenerManager().addJobListener(listener,
					GroupMatcher.jobGroupContains(SEND_LOOP_EVENT_GROUP));
			}
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
}
