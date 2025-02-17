package com.nitrobox.service.promotion.domain.core.model;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LombokPojo {
    @NotNull
    private String property;

    public void build() {
        LombokPojo.builder().build();
    }
}
