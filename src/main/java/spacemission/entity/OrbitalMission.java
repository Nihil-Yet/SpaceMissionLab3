package spacemission.entity;

import jakarta.persistence.*;
import lombok.*;
import spacemission.service.PlanetaryData;
import spacemission.service.EarthData;

@Entity
@Table(name = "orbital_missions")
@Getter @Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OrbitalMission extends Mission {
    
    private Double currHeight;
    private Double targetHeight;
    private Double inclination;

    @Enumerated(EnumType.ORDINAL)
    private EnergySource energySource;

    @Transient
    private PlanetaryData planetaryData;

    public OrbitalMission(String name, Integer budget, Integer duration,
                         Double currHeight, Double targetHeight, Double inclination,
                         EnergySource energySource, PlanetaryData planetaryData) {
        super(name, budget, duration);
        this.currHeight = currHeight != null ? currHeight : 500.0;
        this.targetHeight = targetHeight != null ? targetHeight : 500.0;
        this.inclination = inclination != null ? inclination : 0.0;
        this.energySource = energySource != null ? energySource : EnergySource.SOLAR;
        this.planetaryData = planetaryData != null ? planetaryData : new EarthData();
        setMissionType(MissionT.ORBITAL);
    }

    public OrbitalMission(String name, Integer budget) {
        this(name, budget, 180, 500.0, 500.0, 0.0, EnergySource.SOLAR, new EarthData());
    }

    public OrbitalMission() {
        super("None", 0, 0);
        this.currHeight = 500.0;
        this.targetHeight = 500.0;
        this.inclination = 0.0;
        this.energySource = EnergySource.NONE;
        this.planetaryData = new EarthData();
        setMissionType(MissionT.ORBITAL);
    }

    public Double changeOrbit(Double delta) {
        if (currHeight == null) currHeight = 500.0;
        currHeight += delta;
        return currHeight;
    }

    public void setTargetHeight(Double newTarget) {
        this.targetHeight = newTarget;
    }

    public OrbitState orbitState() {
        if (planetaryData == null) planetaryData = new EarthData();
        if (currHeight == null) currHeight = 500.0;
        
        Double rMeters = (planetaryData.getRadiusKm() + currHeight) * 1000.0;
        Double periodSeconds = 2.0 * Math.PI * Math.sqrt(
            Math.pow(rMeters, 3) / planetaryData.getGravitationalParameter());
        Double periodMinutes = periodSeconds / 60.0;

        OrbitType type = OrbitType.UNKNOWN;
        double inclinationEps = 10.0;
        double periodEps = 0.001;

        if (planetaryData.getRotationPeriodSeconds() > 0 &&
            Math.abs(periodSeconds - planetaryData.getRotationPeriodSeconds()) / 
            planetaryData.getRotationPeriodSeconds() < periodEps &&
            inclination != null && Math.abs(inclination) < inclinationEps) {
            type = OrbitType.GEOSTATIONARY;
        } else if (inclination != null && Math.abs(inclination - 90.0) < inclinationEps) {
            type = OrbitType.POLAR;
        } else if (currHeight < 2000) {
            type = OrbitType.LOW_EARTH;
        }

        return new OrbitState(currHeight, type, inclination, periodMinutes);
    }

    private Double getDebrisRiskFactor() {
        if (planetaryData == null) planetaryData = new EarthData();
        if (currHeight == null) return 1.0;
        
        for (var zone : planetaryData.getDebrisRiskZones()) {
            if (currHeight >= zone.minHeight() && currHeight < zone.maxHeight()) {
                return zone.riskFactor();
            }
        }
        return 1.0;
    }

    @Override
    public Double calcFuelConsumption() {
        if (planetaryData == null) planetaryData = new EarthData();
        if (currHeight == null) currHeight = 500.0;
        if (targetHeight == null) targetHeight = currHeight;
        
        Double deltaHeightKm = Math.abs(targetHeight - currHeight);
        if (deltaHeightKm < 1e-6) return 0.0;

        Double avgRadiusM = (planetaryData.getRadiusKm() + (currHeight + targetHeight) / 2.0) * 1000.0;
        Double g = planetaryData.getGravitationalParameter() / (avgRadiusM * avgRadiusM);
        double fuelCoeff = 1000.0;
        
        return fuelCoeff * deltaHeightKm * g;
    }

    @Override
    public Float calcRisk() {
        if (currHeight == null) currHeight = 500.0;
        
        float baseRisk;
        if (currHeight > 3000) baseRisk = 0.7f;
        else if (currHeight > 1000) baseRisk = 0.4f;
        else baseRisk = 0.2f;

        Double debrisFactor = getDebrisRiskFactor();
        float totalRisk = (float)(baseRisk * debrisFactor);
        return totalRisk > 1.0f ? 1.0f : totalRisk;
    }

    @Override
    public String getInfo() {
        OrbitState ots = orbitState();
        return super.getInfo() + "\n" +
               "Mission type: Orbital\n" +
               "Orbital type: " + ots.getType() + "\n" +
               "Period: " + String.format("%.1f", ots.getPeriod()) + " min\n" +
               "Current orbital height: " + currHeight + " km\n" +
               "Target height: " + targetHeight + " km\n" +
               "Inclination: " + inclination + "°\n" +
               "Energy Source: " + energySource;
    }
}
