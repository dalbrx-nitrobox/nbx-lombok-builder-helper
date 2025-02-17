package com.nitrobox.lombokbuilderhelper;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiPrimitiveType;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class LombokBuilderInspectionMandatory extends AbstractLombokBuilderInspection {

    @Override
    protected String descriptionTemplate() {
        return "Lombok builder is missing non nullable fields";
    }

    @Override
    @NotNull
    public String getFamilyName() {
        return "Add all mandatory fields";
    }

    @Override
    protected boolean filterField(PsiField field) {
        final var isPrimitiveType = field.getType() instanceof PsiPrimitiveType;

        final var nonNullAnnotations =
                Set.of("lombok.NonNull", "org.jetbrains.annotations.NotNull",
                        "javax.validation.constraints.NotNull",
                        "jakarta.validation.constraints.NotNull");
        final var annotations = Arrays.stream(field.getAnnotations()).map(PsiAnnotation::getQualifiedName).collect(Collectors.toSet());
        final var isNonNull = !Collections.disjoint(nonNullAnnotations, annotations);

        return super.filterField(field) && (isPrimitiveType || isNonNull);
    }
}
