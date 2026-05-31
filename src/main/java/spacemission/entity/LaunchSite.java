package spacemission.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "launch_sites")
@Data
@NoArgsConstructor
public class LaunchSite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String location;
    private String country;

    @OneToMany(mappedBy = "launchSite", cascade = CascadeType.PERSIST)
    private Set<Mission> missions = new HashSet<>();

    public LaunchSite(String name, String location, String country) {
        this.name = name;
        this.location = location;
        this.country = country;
    }
}
