package spacemission.repo;

import spacemission.entity.Mission;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MissionRepo extends CrudRepository<Mission, Long> {
    List<Mission> findByMissionType(Integer type);
    List<Mission> findByNameContainingIgnoreCase(String keyword);
    List<Mission> findByLaunchSiteId(Long launchSiteId);
}
