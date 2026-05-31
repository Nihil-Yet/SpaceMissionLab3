package spacemission.repo;

import spacemission.entity.LaunchSite;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LaunchSiteRepo extends CrudRepository<LaunchSite, Long> {
    Optional<LaunchSite> findByName(String name);
    List<LaunchSite> findByCountry(String country);
}
