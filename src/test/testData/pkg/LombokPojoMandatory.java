package pkg;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LombokPojoMandatory {
    @NotNull
    private String property;

    public void build() {
        LombokPojoMandatory.builder().build();
    }
}
