package spacemission.service;

import java.util.List;

public interface PlanetaryData {
    Double getRadiusKm();
    Double getGravitationalParameter();
    Double getRotationPeriodSeconds();
    List<DebrisZone> getDebrisRiskZones();
    
    record DebrisZone(Double minHeight, Double maxHeight, Double riskFactor) {}
}
