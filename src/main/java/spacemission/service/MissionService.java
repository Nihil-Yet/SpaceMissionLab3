package spacemission.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import spacemission.entity.Mission;
import spacemission.entity.OrbitalMission;
import spacemission.repo.MissionRepo;
import java.util.List;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class MissionService {
    private final MissionRepo missionRepo;
    private final PlanetaryData planetaryData;

    public List<Mission> findAll() {
        return StreamSupport.stream(missionRepo.findAll().spliterator(), false).toList();
    }

    public Mission findById(Long id) {
        return missionRepo.findById(id).orElse(null);
    }

    public Mission save(Mission mission) {
        if (mission instanceof OrbitalMission om) {
            om.setPlanetaryData(planetaryData);
        }
        return missionRepo.save(mission);
    }

    public void deleteById(Long id) {
        missionRepo.deleteById(id);
    }

    public List<Mission> findByType(Integer type) {
        return missionRepo.findByMissionType(type);
    }
}
