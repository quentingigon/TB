package services;

import models.db.*;
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

	public SendEventJobCreator(ServicePicker servicePicker,
							   EventManager eventManager) {
		this.servicePicker = servicePicker;
		this.eventManager = eventManager;
	}

	public void createJobForDiffuser(Diffuser diffuser,
									 Flux diffusedFlux,
									 List<Screen> screens) {
		createJob("diffuser",
			diffuser.getName(),
			diffuser.getId(),
			getCronCmdDiffuser(diffuser, diffuser.getTime()),
			diffuser.getTime(),
			diffusedFlux.getId(),
			screens,
			JOBS_LISTENER);
	}

	public void createJobForSchedule(Schedule schedule,
									 FluxTrigger fluxTrigger,
									 List<Screen> screens) {
		createJob("schedule",
			schedule.getName(),
			schedule.getId(),
			getCronCmdSchedule(schedule, fluxTrigger.getTime()),
			fluxTrigger.getTime(),
			fluxTrigger.getFluxId(),
			screens,
			JOBS_LISTENER);
	}

	private void createJob(String source, String name, Integer entityId, String cronCmd,
						   String time, Integer fluxId, List<Screen> screens, String jobListenerName) {
		FluxService fluxService = servicePicker.getFluxService();
		Flux flux = fluxService.getFluxById(fluxId);

		JobDetail job = newJob(SendEventJob.class)
			.withIdentity(JOB_NAME_TRIGGER + flux.getName() + "#" + cronCmd,
				SEND_EVENT_GROUP + "." + source + "." + name)
			.build();

		CronTrigger trigger = newTrigger()
			.withIdentity(TRIGGER_NAME + flux.getName() + "#" + cronCmd,
				SEND_EVENT_GROUP + "." + source + "." + name)
			.usingJobData("screenIds", getScreenIds(screens))
			.usingJobData("fluxId", flux.getId())
			.usingJobData("source", source)
			.usingJobData("time", time)
			.usingJobData("entityId", entityId)
			.withSchedule(cronSchedule(cronCmd))
			.build();

		SchedulerFactory sf = new StdSchedulerFactory();
		try {
			Scheduler scheduler = sf.getScheduler();

			// delete previous job and trigger
			if (scheduler.checkExists(new JobKey(JOB_NAME_TRIGGER + name, SEND_EVENT_GROUP))) {
				scheduler.deleteJob(new JobKey(JOB_NAME_TRIGGER + name, SEND_EVENT_GROUP));
			}

			scheduler.scheduleJob(job, trigger);

			if (scheduler.getListenerManager().getJobListener(jobListenerName) == null) {
				SendEventJobsListener listener = new SendEventJobsListener(
					jobListenerName,
					eventManager,
					servicePicker);

				scheduler.getListenerManager().addJobListener(listener,
					GroupMatcher.jobGroupContains(SEND_EVENT_GROUP + "." + source));
			}
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
}
