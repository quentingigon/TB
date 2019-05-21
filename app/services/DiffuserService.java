package services;

import models.db.Diffuser;
import models.entities.DiffuserData;
import models.repositories.DiffuserRepository;
import models.repositories.RunningDiffuserRepository;

import java.util.ArrayList;
import java.util.List;

public class DiffuserService {

	public final DiffuserRepository diffuserRepository;
	public final RunningDiffuserRepository runningDiffuserRepository;

	public DiffuserService(DiffuserRepository diffuserRepository,
						   RunningDiffuserRepository runningDiffuserRepository) {
		this.diffuserRepository = diffuserRepository;
		this.runningDiffuserRepository = runningDiffuserRepository;
	}

	public Diffuser create(Diffuser diffuser) {
		return diffuserRepository.add(diffuser);
	}

	public Diffuser update(Diffuser diffuser) {
		return diffuserRepository.update(diffuser);
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
