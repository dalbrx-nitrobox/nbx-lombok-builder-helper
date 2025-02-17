package com.nitrobox.service.promotion.domain.core.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LombokPojo {

    private String property;

    public void build() {
        LombokPojo.builder().build();
    }
}
