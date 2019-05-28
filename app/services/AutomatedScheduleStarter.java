package services;

import models.db.RunningSchedule;
import models.db.Schedule;
import models.db.Screen;
import models.repositories.interfaces.FluxRepository;
import models.repositories.interfaces.RunningScheduleRepository;
import models.repositories.interfaces.ScheduleRepository;
import models.repositories.interfaces.ScreenRepository;
import play.inject.ApplicationLifecycle;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * This class demonstrates how to run code when the
 * application starts and stops. It starts a timer when the
 * application starts. When the application stops it prints out how
 * long the application was running for.
 *
 * This class is registered for Guice dependency injection in the
 * {@link Module} class. We want the class to start when the application
 * starts, so it is registered as an "eager singleton". See the code
 * in the {@link Module} class to see how this happens.
 *
 * This class needs to run code when the server stops. It uses the
 * application's {@link ApplicationLifecycle} to create a stop hook.
 */
@Singleton
public class AutomatedScheduleStarter {

    private final RunningScheduleThreadManager serviceManager;
    private final FluxManager fluxManager;
    private final TimeTableUtils timeTableUtils;

    private final ScreenRepository screenRepository;
    private final FluxRepository fluxRepository;
    private final ScheduleRepository scheduleRepository;
    private final RunningScheduleRepository runningScheduleRepository;
    private final FluxChecker fluxChecker;

    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger("application");

    @Inject
    public AutomatedScheduleStarter(FluxManager fluxManager,
                                    TimeTableUtils timeTableUtils,
                                    ScreenRepository screenRepository,
                                    FluxRepository fluxRepository,
                                    RunningScheduleRepository repo,
                                    ScheduleRepository scheduleRepository,
                                    RunningScheduleThreadManager serviceManager,
                                    FluxChecker fluxChecker) {
        this.fluxChecker = fluxChecker;
        this.screenRepository = screenRepository;
        this.fluxRepository = fluxRepository;
        this.scheduleRepository = scheduleRepository;
        this.fluxManager = fluxManager;
        this.serviceManager = serviceManager;
        this.runningScheduleRepository = repo;
        this.timeTableUtils = timeTableUtils;

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

                RunningScheduleThread service = new RunningScheduleThread(
                    rs,
                    screens,
                    new ArrayList<>(schedule.getFallbacks()),
                    timeTableUtils.getTimeTable(schedule),
                    fluxRepository,
                    fluxChecker,
                    schedule.isKeepOrder());

                service.addObserver(fluxManager);

                // the schedule is activated
                serviceManager.addRunningSchedule(schedule.getId(), service);


            }
        }
    }
}
