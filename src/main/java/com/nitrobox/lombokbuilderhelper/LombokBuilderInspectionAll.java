package com.nitrobox.lombokbuilderhelper;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class LombokBuilderInspectionAll extends AbstractLombokBuilderInspection {

    @Override
    protected String descriptionTemplate() {
        return "Lombok builder has missing fields";
    }

    @Override
    protected ProblemHighlightType problemHighlightType() {
        return ProblemHighlightType.WEAK_WARNING;
    }

    @Override
    @NotNull
    public String getFamilyName() {
        return "Add missing fields";
    }
}
