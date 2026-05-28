package spacemission.repo;

import spacemission.entity.MissionCrew;
import spacemission.entity.MissionCrewId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MissionCrewRepo extends CrudRepository<MissionCrew, MissionCrewId> {
    List<MissionCrew> findByMissionId(Long missionId);
    List<MissionCrew> findByCrewMemberId(Long crewId);
}
