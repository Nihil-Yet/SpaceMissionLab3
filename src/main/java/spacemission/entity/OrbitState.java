package spacemission.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrbitState {
    private Double currHeight;
    private OrbitType type;
    private Double inclination;
    private Double period;
}
