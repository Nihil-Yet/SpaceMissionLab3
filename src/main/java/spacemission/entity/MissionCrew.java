package spacemission.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "mission_crew")
@Data
@NoArgsConstructor
@IdClass(MissionCrewId.class)
public class MissionCrew {
    @Id
    @ManyToOne
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    @Id
    @ManyToOne
    @JoinColumn(name = "crew_id", nullable = false)
    private CrewMember crewMember;

    private LocalDate assignedDate;

    @Column(name = "role_in_mission")
    private String missionRole;
}
