package services;

import models.db.*;
import models.repositories.interfaces.FluxRepository;
import models.repositories.interfaces.RunningScheduleRepository;
import models.repositories.interfaces.ScheduleRepository;
import models.repositories.interfaces.ScreenRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

import static controllers.CronUtils.getScreenIds;

/**
 * This class is called when the application starts to recreate a
 * RunningScheduleThread from the RunningSchedules in the database.
 */
@Singleton
public class AutomatedScheduleStarter {


    private final EventManager eventManager;
    private final ScreenRepository screenRepository;
    private final FluxRepository fluxRepository;
    private final ScheduleRepository scheduleRepository;
    private final RunningScheduleRepository runningScheduleRepository;
    private final FluxChecker fluxChecker;
    private final ServicePicker servicePicker;

    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger("application");

    @Inject
    public AutomatedScheduleStarter(EventManager eventManager,
                                    ScreenRepository screenRepository,
                                    FluxRepository fluxRepository,
                                    RunningScheduleRepository repo,
                                    ScheduleRepository scheduleRepository,
                                    FluxChecker fluxChecker,
                                    ServicePicker servicePicker) {
        this.eventManager = eventManager;
        this.servicePicker = servicePicker;
        this.fluxChecker = fluxChecker;
        this.screenRepository = screenRepository;
        this.fluxRepository = fluxRepository;
        this.scheduleRepository = scheduleRepository;
        this.runningScheduleRepository = repo;

        FluxService fluxService = servicePicker.getFluxService();

        // This code is called when the application starts.
        // get all existing runningSchedules
        for (RunningSchedule rs: runningScheduleRepository.getAll()) {

            Schedule schedule = scheduleRepository.getById(rs.getScheduleId());

            if (schedule != null) {

                List<Screen> screens = new ArrayList<>();
                for (Integer screenId : runningScheduleRepository.getScreensIdsByRunningScheduleId(rs.getId())) {
                    Screen screen = screenRepository.getById(screenId);
                    if (screen != null)
                        screens.add(screen);
                }

                List<FluxTrigger> triggers = fluxService.getFluxTriggersOfScheduleById(schedule.getId());

                SendEventJobCreator jobCreator = new SendEventJobCreator(servicePicker, eventManager);

                // Create CRONs for all FluxTriggers of Schedule
                for (FluxTrigger ft: triggers) {
                    jobCreator.createJobForSchedule(schedule, ft, screens);
                }

                LoopJobCreator loopJobCreator = new LoopJobCreator(schedule,
                    getScreenIds(screens),
                    servicePicker,
                    eventManager);

                // if current time is for a FluxLoop, start it
                FluxLoop loop = fluxService.getFluxLoopThatMustBeStarted(triggers, schedule);
                if (loop != null) {
                    loopJobCreator.createFromFluxLoop(loop);
                }
            }
        }
    }
}
