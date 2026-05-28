package spacemission.service;

import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class EarthData implements PlanetaryData {
    @Override
    public Double getRadiusKm() { return 6371.0; }
    
    @Override
    public Double getGravitationalParameter() { return 3.986004418e14; }
    
    @Override
    public Double getRotationPeriodSeconds() { return 86164.0; }
    
    private static final List<PlanetaryData.DebrisZone> DEBRIS_ZONES = List.of(
        new PlanetaryData.DebrisZone(0.0, 500.0, 1.8),
        new PlanetaryData.DebrisZone(500.0, 1000.0, 1.5),
        new PlanetaryData.DebrisZone(1000.0, 2000.0, 1.2),
        new PlanetaryData.DebrisZone(2000.0, 5000.0, 1.0),
        new PlanetaryData.DebrisZone(5000.0, Double.MAX_VALUE, 0.8)
    );
    
    @Override
    public List<PlanetaryData.DebrisZone> getDebrisRiskZones() {
        return DEBRIS_ZONES;
    }
}
