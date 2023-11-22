package com.nitrobox.lombokbuilderhelper;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;

public class LombokBuilderInspectionAll extends AbstractLombokBuilderInspection {

    public static final String QUICK_FIX_NAME = "Add missing fields";

    private static final Logger LOG =
            Logger.getInstance("#com.nitrobox.lombokbuilderhelper.LombokBuilderInspectionAll");
    private final LbiAllQuickFix allQuickFix = new LbiAllQuickFix();


    private List<String> getAllFields(PsiClass aClass) {
        final String defaultBuilderValueAnnotation = "lombok.Builder.Default";
        return Arrays.stream(aClass.getAllFields()).filter(field -> {
            final PsiAnnotation[] annotations = field.getAnnotations();
            final PsiModifierList modifiers = field.getModifierList();
            final boolean isStaticField =
                    modifiers != null && modifiers.hasModifierProperty(PsiModifier.STATIC);
            return !isStaticField && Arrays.stream(annotations)
                    .noneMatch(annotation -> Objects.equals(
                            annotation.getQualifiedName(),
                            defaultBuilderValueAnnotation));
        }).map(PsiField::getName).filter(equalsString("id").negate()).filter(equalsString("createdTimestamp").negate()).toList();
    }

    private Predicate<String> equalsString(String string) {
        return string::equals;
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(
            @NotNull
            final ProblemsHolder holder,
            boolean isOnTheFly) {
        return new JavaElementVisitor() {
            private static final String DESCRIPTION_TEMPLATE =
                    "Lombok builder has missing fields";

            @Override
            public void visitMethodCallExpression(@NotNull PsiMethodCallExpression expression) {
                super.visitMethodCallExpression(expression);
                PsiMethod resolvedMethod = expression.resolveMethod();

                if (resolvedMethod != null && Objects.equals(
                        resolvedMethod.getClass().getName(),
                        "de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder")
                        && Objects.equals(resolvedMethod.getName(), "build")) {
                    PsiClass builderClass = getContainingBuilderClass(resolvedMethod);
                    if (builderClass != null) {
                        List<String> missingMandatoryFields = processMissingFields(expression, getAllFields(builderClass));
                        if (!missingMandatoryFields.isEmpty()) {
                            holder.registerProblem(expression, DESCRIPTION_TEMPLATE,
                                    ProblemHighlightType.WEAK_WARNING, allQuickFix);
                        }
                    }
                }
            }
        };
    }

    private class LbiAllQuickFix implements LocalQuickFix {

        @Override
        public void applyFix(
                @NotNull Project project,
                @NotNull ProblemDescriptor descriptor) {
            PsiMethodCallExpression expression =
                    (PsiMethodCallExpression) descriptor.getPsiElement();
            PsiMethod resolvedMethod = expression.resolveMethod();

            if (resolvedMethod != null) {
                List<String> missingFields = processMissingFields(
                        expression,
                        getAllFields(getContainingBuilderClass(resolvedMethod)));

                if (!missingFields.isEmpty()) {
                    String errorText = expression.getText();

                    PsiElementFactory factory =
                            JavaPsiFacade.getInstance(project).getElementFactory();
                    PsiMethodCallExpression fixedMethodExpression =
                            (PsiMethodCallExpression) factory.createExpressionFromText(
                                    replaceLast(errorText, ".build()", "." + String.join("().", missingFields) + "().build()"),
                                    null);

                    expression.replace(fixedMethodExpression);
                }
            } else {
                LOG.error("Resolved method null when applying fix");
            }
        }

        public static String replaceLast(String string, String toReplace, String replacement) {
            int pos = string.lastIndexOf(toReplace);
            if (pos > -1) {
                return string.substring(0, pos)
                        + replacement
                        + string.substring(pos + toReplace.length());
            } else {
                return string;
            }
        }

        @NotNull
        public String getFamilyName() {
            return QUICK_FIX_NAME;
        }
    }

}
