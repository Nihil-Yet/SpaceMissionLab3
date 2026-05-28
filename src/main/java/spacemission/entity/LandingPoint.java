package spacemission.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LandingPoint {
    private String name;
    private Short x;
    private Short y;
    private Short r;
}
