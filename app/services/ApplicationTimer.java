package services;

import models.db.RunningSchedule;
import models.db.Schedule;
import models.db.Screen;
import models.entities.DataUtils;
import models.repositories.FluxRepository;
import models.repositories.RunningScheduleRepository;
import models.repositories.ScheduleRepository;
import models.repositories.ScreenRepository;
import play.inject.ApplicationLifecycle;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
public class ApplicationTimer {

    private final Clock clock;
    private final ApplicationLifecycle appLifecycle;
    private final Instant start;
    private final RunningScheduleServiceManager serviceManager;
    private final FluxManager fluxManager;
    private final DataUtils dataUtils;

    private final ScreenRepository screenRepository;
    private final FluxRepository fluxRepository;
    private final ScheduleRepository scheduleRepository;
    private final RunningScheduleRepository runningScheduleRepository;

    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger("application");

    @Inject
    public ApplicationTimer(Clock clock,
                            FluxManager fluxManager,
                            ApplicationLifecycle appLifecycle,
                            DataUtils dataUtils,
                            ScreenRepository screenRepository,
                            FluxRepository fluxRepository,
                            RunningScheduleRepository repo,
                            ScheduleRepository scheduleRepository,
                            RunningScheduleServiceManager serviceManager) {
        this.screenRepository = screenRepository;
        this.fluxRepository = fluxRepository;
        this.scheduleRepository = scheduleRepository;
        this.fluxManager = fluxManager;
        this.serviceManager = serviceManager;
        this.runningScheduleRepository = repo;
        this.dataUtils = dataUtils;
        this.clock = clock;
        this.appLifecycle = appLifecycle;
        // This code is called when the application starts.
        // TODO start active RunningSchedules here

        List<RunningSchedule> activeRunningchedules = new ArrayList<>();

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

                RunningScheduleService service = new RunningScheduleService(
                    rs,
                    screens,
                    new ArrayList<>(schedule.getFallbacks()),
                    dataUtils.getTimeTable(schedule),
                    fluxRepository);

                service.addObserver(fluxManager);

                // the schedule is activated
                serviceManager.addRunningSchedule(schedule.getId(), service);
            }


        }
        start = clock.instant();
        logger.info("ApplicationTimer demo: Starting application at " + start);

        // When the application starts, create a stop hook with the
        // ApplicationLifecycle object. The code inside the stop hook will
        // be run when the application stops.
        appLifecycle.addStopHook(() -> {
            Instant stop = clock.instant();
            Long runningTime = stop.getEpochSecond() - start.getEpochSecond();
            logger.info("ApplicationTimer demo: Stopping application at " + clock.instant() + " after " + runningTime + "s.");
            return CompletableFuture.completedFuture(null);
        });
    }

}
