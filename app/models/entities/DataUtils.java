package models.entities;

import models.db.*;
import models.repositories.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static services.BlockUtils.*;
import static services.BlockUtils.blockDuration;

public class DataUtils {

	@Inject
	TeamRepository teamRepository;

	@Inject
	ScreenRepository screenRepository;

	@Inject
	FluxRepository fluxRepository;

	@Inject
	ScheduleRepository scheduleRepository;

	@Inject
	DiffuserRepository diffuserRepository;

	@Inject
	UserRepository userRepository;

	public DataUtils() {
	}

	public Integer getTeamIdOfUserByEmail(String email) {
		if (userRepository.getMemberByUserEmail(email) != null) {
			return userRepository
				.getMemberByUserEmail(email)
				.getTeamId();
		}
		else
			return -1;

	}

	public List<ScreenData> getAllScreensOfTeam(int teamId) {
		List<ScreenData> data = new ArrayList<>();
		for (Integer screenId : screenRepository.getAllScreenIdsOfTeam(teamId)) {
			if (screenRepository.getById(screenId) != null) {
				data.add(new ScreenData(screenRepository.getById(screenId)));
			}
		}
		return data;
	}

	public List<ScreenData> getAllActiveScreensOfTeam(int teamId) {
		List<ScreenData> data = new ArrayList<>();
		for (Integer screenId : screenRepository.getAllScreenIdsOfTeam(teamId)) {
			Screen screen = screenRepository.getById(screenId);
			if (screen != null && screen.isActive()) {
				data.add(new ScreenData(screenRepository.getById(screenId)));
			}
		}
		return data;
	}

	public List<FluxData> getAllFluxesOfTeam(int teamId) {
		List<FluxData> data = new ArrayList<>();
		for (Integer fluxId : fluxRepository.getAllFluxIdsOfTeam(teamId)) {
			if (fluxRepository.getById(fluxId) != null)
				data.add(new FluxData(fluxRepository.getById(fluxId)));
		}
		return data;
	}

	public List<UserData> getAllMembersOfTeam(int teamId) {
		List<UserData> data = new ArrayList<>();
		for (Integer userId : userRepository.getAllMemberIdsOfTeam(teamId)) {
			if (userRepository.getById(userId) != null)
				data.add(new UserData(userRepository.getById(userId)));
		}
		return data;
	}

	public List<ScheduleData> getAllSchedulesOfTeam(int teamId) {
		List<ScheduleData> data = new ArrayList<>();
		for (Integer scheduleId : scheduleRepository.getAllScheduleIdsOfTeam(teamId)) {
			if (scheduleRepository.getById(scheduleId) != null)
				data.add(new ScheduleData(scheduleRepository.getById(scheduleId)));
		}
		return data;
	}

	public List<DiffuserData> getAllDiffusersOfTeam(int teamId) {
		List<DiffuserData> data = new ArrayList<>();
		for (Integer diffuserId : diffuserRepository.getAllDiffuserIdsOfTeam(teamId)) {
			if (diffuserRepository.getById(diffuserId) != null)
				data.add(new DiffuserData(diffuserRepository.getById(diffuserId)));
		}
		return data;
	}

	public List<FluxData> getAllFluxes() {
		List<FluxData> data = new ArrayList<>();
		for (Flux f: fluxRepository.getAll()) {
			data.add(new FluxData(f));
		}
		return data;
	}

	public List<ScheduleData> getAllSchedules() {
		List<ScheduleData> data = new ArrayList<>();
		for (Schedule s: scheduleRepository.getAll()) {
			data.add(new ScheduleData(s));
		}
		return data;
	}

	public List<ScreenData> getAllScreens() {
		List<ScreenData> data = new ArrayList<>();
		for (Screen s: screenRepository.getAll()) {
			data.add(new ScreenData(s));
		}
		return data;
	}

	public List<UserData> getAllUsers() {
		List<UserData> data = new ArrayList<>();
		for (User u: userRepository.getAll()) {
			data.add(new UserData(u));
		}
		return data;
	}

	public List<TeamData> getAllTeams() {
		List<TeamData> data = new ArrayList<>();
		for (Team t: teamRepository.getAll()) {
			data.add(new TeamData(t));
		}
		return data;
	}

	public List<DiffuserData> getAllDiffusers() {
		List<DiffuserData> data = new ArrayList<>();
		for (Diffuser d: diffuserRepository.getAll()) {
			data.add(new DiffuserData(d));
		}
		return data;
	}

	// TODO integrate with schedule etc
	public HashMap<Integer, Integer> getTimeTable(Schedule schedule) {

		List<ScheduledFlux> scheduledFluxes = scheduleRepository.getAllScheduledFluxesByScheduleId(schedule.getId());
		Flux lastFlux = new Flux();
		long lastFluxDuration = 0;
		boolean noFluxSent;

		HashMap<Integer, Integer> timetable = new HashMap<>();
		for (int i = 0; i < blockNumber; i++) {

			noFluxSent = true;

			// if duration of last inserted ScheduledFlux is still not finished iterating over
			// we put last flux in the schedule
			if (lastFluxDuration != 0) {
				lastFluxDuration--;
				timetable.put(i, lastFlux.getId());
			}
			else {
				// check if we must insert fluxes at a certain hour
				for (ScheduledFlux sf : scheduledFluxes) {
					// a flux is set to begin at this block
					if (sf.getStartBlock().equals(i)) {
						Flux flux = fluxRepository.getById(sf.getFluxId());
						lastFlux = flux;
						lastFluxDuration = flux.getDuration() - 1;
						timetable.put(i, flux.getId());
						noFluxSent = false;
						scheduledFluxes.remove(sf);
						break;
					}
				}

				if (noFluxSent) {
					// if no flux is set at this block
					timetable.put(i, -1);
				}
			}
		}
		return timetable;
	}



}
