package models.entities;

import models.db.*;
import models.repositories.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

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

	public List<ScreenData> getAllScreensOfTeam(int teamId) {
		List<ScreenData> data = new ArrayList<>();
		for (Integer screenId : screenRepository.getAllScreenIdsOfTeam(teamId)) {
			data.add(new ScreenData(screenRepository.getById(screenId)));
		}
		return data;
	}

	public List<FluxData> getAllFluxesOfTeam(int teamId) {
		List<FluxData> data = new ArrayList<>();
		for (Integer fluxId : fluxRepository.getAllFluxIdsOfTeam(teamId)) {
			data.add(new FluxData(fluxRepository.getById(fluxId)));
		}
		return data;
	}

	public List<UserData> getAllMembersOfTeam(int teamId) {
		List<UserData> data = new ArrayList<>();
		for (Integer userId : userRepository.getAllMemberIdsOfTeam(teamId)) {
			data.add(new UserData(userRepository.getById(userId)));
		}
		return data;
	}

	public List<ScheduleData> getAllSchedulesOfTeam(int teamId) {
		List<ScheduleData> data = new ArrayList<>();
		for (Integer scheduleId : scheduleRepository.getAllScheduleIdsOfTeam(teamId)) {
			data.add(new ScheduleData(scheduleRepository.getById(scheduleId)));
		}
		return data;
	}

	public List<DiffuserData> getAllDiffusersOfTeam(int teamId) {
		List<DiffuserData> data = new ArrayList<>();
		for (Integer diffuserId : diffuserRepository.getAllDiffuserIdsOfTeam(teamId)) {
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

}
