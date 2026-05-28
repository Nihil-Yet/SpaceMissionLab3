package spacemission.repo;

import spacemission.entity.CrewMember;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CrewMemberRepo extends CrudRepository<CrewMember, Long> {
    List<CrewMember> findByRoleContainingIgnoreCase(String role);
}
