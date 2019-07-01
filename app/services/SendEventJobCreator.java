package services;

import models.db.Flux;
import models.db.FluxTrigger;
import models.db.Schedule;
import models.db.Screen;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

import java.util.List;

import static controllers.CronUtils.*;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

public class SendEventJobCreator {

	private ServicePicker servicePicker;
	private EventManager eventManager;
	private Schedule schedule;

	public SendEventJobCreator(Schedule schedule,
							   ServicePicker servicePicker,
							   EventManager eventManager) {
		this.schedule = schedule;
		this.servicePicker = servicePicker;
		this.eventManager = eventManager;
	}

	public void createJob(FluxTrigger fluxTrigger,
						  List<Screen> screens) {
		FluxService fluxService = servicePicker.getFluxService();
		Flux flux = fluxService.getFluxById(fluxTrigger.getFluxId());

		JobDetail job = newJob(SendEventJob.class)
			.withIdentity(JOB_NAME_TRIGGER + flux.getName() + "#" + fluxTrigger.getCronCmd(),
				SEND_EVENT_GROUP + "." + schedule.getName())
			.build();

		CronTrigger trigger = newTrigger()
			.withIdentity(TRIGGER_NAME + flux.getName() + "#" + fluxTrigger.getCronCmd(),
				SEND_EVENT_GROUP + "." + schedule.getName())
			.usingJobData("screenIds", getScreenIds(screens))
			.usingJobData("fluxId", flux.getId())
			.usingJobData("source", "schedule")
			.usingJobData("time", fluxTrigger.getTime())
			.usingJobData("scheduleId", schedule.getId())
			.withSchedule(cronSchedule(fluxTrigger.getCronCmd()))
			.build();

		SchedulerFactory sf = new StdSchedulerFactory();
		try {
			Scheduler scheduler = sf.getScheduler();
			scheduler.scheduleJob(job, trigger);

			if (scheduler.getListenerManager().getJobListener(SCHEDULE_JOBS_LISTENER) == null) {
				SendEventJobsListener listener = new SendEventJobsListener(
					SCHEDULE_JOBS_LISTENER,
					eventManager,
					servicePicker);

				scheduler.getListenerManager().addJobListener(listener,
					GroupMatcher.jobGroupContains(SEND_EVENT_GROUP));
			}
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
}
