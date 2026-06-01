package spacemission.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "planetary_missions")
@Getter @Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PlanetaryMission extends Mission {
    
    private String planet;
    private Integer atmoDensity;
    private String landingPointName;
    private Short landingPointX;
    private Short landingPointY;
    private Short landingPointR;

    @Transient
    private LandingPoint landingPoint;

    public PlanetaryMission(String name, Integer budget, Integer duration,
                           String planet, Integer atmoDensity, LandingPoint landingPoint) {
        super(name, budget, duration);
        this.planet = planet != null ? planet : "Unknown";
        this.atmoDensity = atmoDensity != null ? atmoDensity : 0;
        this.landingPoint = landingPoint != null ? landingPoint : new LandingPoint("Default", (short)0, (short)0, (short)0);
        syncLandingPointFields();
        setMissionType(MissionT.PLANETARY);
    }

    public PlanetaryMission(String name, Integer budget, String planet) {
        this(name, budget, 365, planet, 100, new LandingPoint("Default", (short)0, (short)0, (short)0));
    }

    public PlanetaryMission() {
        super("None", 0, 0);
        this.planet = "Unknown";
        this.atmoDensity = 0;
        this.landingPoint = new LandingPoint("Default", (short)0, (short)0, (short)0);
        syncLandingPointFields();
        setMissionType(MissionT.PLANETARY);
    }

    private void syncLandingPointFields() {
        if (landingPoint != null) {
            this.landingPointName = landingPoint.getName();
            this.landingPointX = landingPoint.getX();
            this.landingPointY = landingPoint.getY();
            this.landingPointR = landingPoint.getR();
        }
    }

    private void syncLandingPointObject() {
        this.landingPoint = new LandingPoint(
            landingPointName != null ? landingPointName : "",
            landingPointX != null ? landingPointX : 0,
            landingPointY != null ? landingPointY : 0,
            landingPointR != null ? landingPointR : 0
        );
    }

    public void setLandingPoint(LandingPoint p) {
        this.landingPoint = p;
        syncLandingPointFields();
    }

    public Boolean hasAtmosphere() {
        return atmoDensity != null && atmoDensity > 0;
    }

    public String scientificGoal() {
        if ("Mars".equalsIgnoreCase(planet)) {
            return "Поиск следов жизни и анализ геологии";
        }
        if ("Venus".equalsIgnoreCase(planet)) {
            return "Анализ климата и атмосферы";
        }
        return "Планетарное исследование поверхности";
    }

    @Override
    public Double calcFuelConsumption() {
        double baseFuel = 1000.0;
        if (hasAtmosphere() && atmoDensity != null) {
            double reduction = 0.4 * (atmoDensity / 255.0);
            baseFuel *= (1.0 - reduction);
        }
        return baseFuel;
    }

    @Override
    public Float calcRisk() {
        float risk = 0.3f;
        if (!hasAtmosphere()) {
            risk += 0.3f;
        } else if (atmoDensity != null) {
            risk -= 0.1f * (atmoDensity / 255.0f);
        }

        if ("Venus".equalsIgnoreCase(planet)) {
            risk += 0.3f;
        } else if ("Mars".equalsIgnoreCase(planet)) {
            risk += 0.1f;
        }

        return Math.max(0.0f, Math.min(1.0f, risk));
    }

    @Override
    public String getInfo() {
        syncLandingPointObject();
        return super.getInfo() + "\n" +
               "Mission type: Planetary\n" +
               "Planet: " + planet + "\n" +
               "Atmosphere: " + (hasAtmosphere() ? "Yes" : "No") + "\n" +
               "Landing point: " + (landingPoint.getName() == null || landingPoint.getName().isEmpty() ? "Unnamed" : landingPoint.getName()) + 
               " (" + landingPoint.getX() + ", " + landingPoint.getY() + "), R=" + landingPoint.getR();
    }
}
