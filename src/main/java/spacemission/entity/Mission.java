package spacemission.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "missions")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
public abstract class Mission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private Integer budget;
    private Integer duration;

    @Column(name = "mission_type")
    private Integer missionType;

    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MissionCrew> crewAssignments = new HashSet<>();

    public Mission() {
        this.name = "None";
        this.budget = 0;
        this.duration = 0;
        this.missionType = MissionT.UNKNOWN.ordinal();
    }

    public Mission(String name, Integer budget, Integer duration) {
        this.name = name != null ? name : "None";
        this.budget = budget != null ? budget : 0;
        this.duration = duration != null ? duration : 0;
    }

    public MissionT getMissionType() {
        if (missionType == null) return MissionT.UNKNOWN;
        return MissionT.values()[missionType];
    }
    
    public void setMissionType(MissionT type) {
        this.missionType = type != null ? type.ordinal() : MissionT.UNKNOWN.ordinal();
    }

    public Integer extendMission(Integer delta) {
        if (duration == null) duration = 0;
        duration += delta;
        return duration;
    }

    public Double budgetPerDay() {
        if (duration == null || duration == 0) return 0.0;
        return budget != null ? (double) budget / duration : 0.0;
    }

    public abstract Double calcFuelConsumption();
    public abstract Float calcRisk();

    public String getInfo() {
        return "Mission name: " + name + "\n" +
               "Budget: " + budget + " mln $\n" +
               "Duration: " + duration + " days\n" +
               "Budget per day: " + String.format("%.2f", budgetPerDay()) + " mln $";
    }
}
