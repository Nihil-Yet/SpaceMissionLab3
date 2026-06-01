package spacemission.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "launch_sites")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "missions")
@EqualsAndHashCode(exclude = "missions")
public class LaunchSite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String location;
    private String country;

    @OneToMany(mappedBy = "launchSite", fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    private Set<Mission> missions = new HashSet<>();

    public LaunchSite(String name, String location, String country) {
        this.name = name;
        this.location = location;
        this.country = country;
    }
    
    public void addMission(Mission mission) {
        missions.add(mission);
        mission.setLaunchSite(this);
    }
    
    public void removeMission(Mission mission) {
        missions.remove(mission);
        mission.setLaunchSite(null);
    }
}
