package com.nitrobox.lombokbuilderhelper;

import org.jetbrains.annotations.NotNull;

public class LombokBuilderInspectionAll extends AbstractLombokBuilderInspection {

    @Override
    protected String descriptionTemplate() {
        return "Lombok builder has missing fields";
    }

    @Override
    @NotNull
    public String getFamilyName() {
        return "Add missing fields";
    }
}
