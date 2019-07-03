package services;

import models.db.Flux;
import models.db.FluxLoop;
import models.db.Schedule;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static controllers.CronUtils.*;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

public class LoopJobCreator {

	private ServicePicker servicePicker;
	private EventManager eventManager;
	private String screenIds;
	private Schedule schedule;

	public LoopJobCreator(Schedule schedule,
						  String screenIds,
						  ServicePicker servicePicker,
						  EventManager eventManager) {
		this.schedule = schedule;
		this.screenIds = screenIds;
		this.servicePicker = servicePicker;
		this.eventManager = eventManager;
	}

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
					.usingJobData("scheduleId", schedule.getId())
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

	public void createFromFluxLoop(FluxLoop loop) {
		String timeStamp = new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());

		JobDetail job = newJob(SendLoopEventJob.class)
			.withIdentity(JOB_NAME_LOOP + schedule.getName(), SEND_LOOP_EVENT_GROUP)
			.build();

		// TODO implement a way to have the loopedflux in correct order
		List<Integer> loopedFluxIds = new ArrayList<>(loop.getFluxes());
		loopedFluxIds.add(loopedFluxIds.get(0));
		Integer currentFluxId = loopedFluxIds.remove(0);

		CronTrigger trigger = newTrigger()
			.withIdentity(TRIGGER_NAME_LOOP + schedule.getName(), SEND_LOOP_EVENT_GROUP)
			.usingJobData("screenIds", screenIds)
			.usingJobData("fluxIds", getFluxIds(loopedFluxIds))
			.usingJobData("currentFluxId", currentFluxId)
			.usingJobData("scheduleId", schedule.getId())
			.usingJobData("source", "schedule")
			.usingJobData("days", schedule.getDays())
			.usingJobData("time", loop.getStartTime())
			.usingJobData("upperBound", getNextFluxTriggerTimeOfSchedule(
				servicePicker.getFluxService().getFluxTriggersOfScheduleById(schedule.getId()), timeStamp))
			.withSchedule(cronSchedule(getCronCmdLoop(schedule.getDays(), loop.getStartTime())))
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

			if (scheduler.getListenerManager().getJobListener(SCHEDULE_LOOP_JOBS_LISTENER) == null) {
				SendLoopEventJobsListener listener = new SendLoopEventJobsListener(
					SCHEDULE_LOOP_JOBS_LISTENER,
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
