package services;

import models.db.Diffuser;
import models.db.RunningDiffuser;
import models.entities.DiffuserData;
import models.repositories.interfaces.DiffuserRepository;
import models.repositories.interfaces.RunningDiffuserRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the service used to make operations on the database for Diffusers.
 */
public class DiffuserService {

	public final DiffuserRepository diffuserRepository;
	public final RunningDiffuserRepository runningDiffuserRepository;

	public DiffuserService(DiffuserRepository diffuserRepository,
						   RunningDiffuserRepository runningDiffuserRepository) {
		this.diffuserRepository = diffuserRepository;
		this.runningDiffuserRepository = runningDiffuserRepository;
	}

	public Diffuser getDiffuserByName(String name) {
		return diffuserRepository.getByName(name);
	}

	public Diffuser create(Diffuser diffuser) {
		return diffuserRepository.add(diffuser);
	}

	public Diffuser update(Diffuser diffuser) {
		return diffuserRepository.update(diffuser);
	}

	public void delete(Diffuser diffuser) {
		diffuserRepository.delete(diffuser);
	}

	public RunningDiffuser getRunningDiffuserByDiffuserId(Integer id) {
		return runningDiffuserRepository.getByDiffuserId(id);
	}

	public RunningDiffuser getRunningDiffuserById(Integer id) {
		return runningDiffuserRepository.getById(id);
	}

	public Integer getRunningDiffuserIdByScreenId(Integer screenId) {
		return runningDiffuserRepository.getRunningDiffuserIdOfScreenById(screenId);
	}

	public RunningDiffuser create(RunningDiffuser diffuser) {
		return runningDiffuserRepository.add(diffuser);
	}

	public RunningDiffuser update(RunningDiffuser diffuser) {
		return runningDiffuserRepository.update(diffuser);
	}

	public void delete(RunningDiffuser diffuser) {
		runningDiffuserRepository.delete(diffuser);
	}

	public List<Integer> getScreenIdsOfRunningDiffuserById(Integer id) {
		return runningDiffuserRepository.getScreenIdsOfRunningDiffuser(id);
	}

	public List<DiffuserData> getAllDiffusersOfTeam(int teamId) {
		List<DiffuserData> data = new ArrayList<>();
		for (Integer diffuserId : diffuserRepository.getAllDiffuserIdsOfTeam(teamId)) {
			if (diffuserRepository.getById(diffuserId) != null) {
				DiffuserData dd = new DiffuserData(diffuserRepository.getById(diffuserId));

				// if diffuser is activated
				if (runningDiffuserRepository.getByDiffuserId(diffuserId) != null) {
					dd.setActivated(true);
				}
				data.add(dd);
			}
		}
		return data;
	}

	public List<DiffuserData> getAllActiveDiffusersOfTeam(int teamId) {
		List<DiffuserData> diffusers = new ArrayList<>();
		for (DiffuserData data: getAllDiffusersOfTeam(teamId)) {
			Diffuser diffuser = diffuserRepository.getByName(data.getName());

			if (runningDiffuserRepository.getByDiffuserId(diffuser.getId()) != null) {
				data.setActivated(true);
				diffusers.add(data);
			}

		}
		return diffusers;
	}

	public List<DiffuserData> getAllDiffusers() {
		List<DiffuserData> data = new ArrayList<>();
		for (Diffuser d: diffuserRepository.getAll()) {
			data.add(new DiffuserData(d));
		}
		return data;
	}
}
