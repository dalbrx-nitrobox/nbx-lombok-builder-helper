package com.nitrobox.lombokbuilderhelper;

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.jvm.annotation.JvmAnnotationConstantValue;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiLocalVariable;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReturnStatement;
import com.intellij.psi.impl.source.tree.java.PsiIdentifierImpl;
import com.intellij.psi.impl.source.tree.java.PsiMethodCallExpressionImpl;
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractLombokBuilderInspection extends AbstractBaseJavaLocalInspectionTool implements LocalQuickFix {

    private static final Logger LOG =
            Logger.getInstance("com.nitrobox.lombokbuilderhelper.LombokBuilderInspectionAll");

    public static final String PSI_LOMBOK_LIGHT_METHOD_BUILDER = "de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder";
    public static final Set<String> LOMBOK_BUILDER_CLASS_QUALIFIED_NAMES = Set.of("lombok.Builder", "lombok.experimental.SuperBuilder");

    protected abstract String descriptionTemplate();

    protected Set<String> skipAnnotations() {
        return Set.of(
                "lombok.Builder.Default",
                "org.hibernate.annotations.CreationTimestamp",
                "org.hibernate.annotations.UpdateTimestamp",
                "org.springframework.data.annotation.CreatedDate",
                "org.springframework.data.annotation.Id",
                "org.springframework.data.annotation.LastModifiedDate",
                "jakarta.persistence.GeneratedValue");
    }

    protected Set<String> skipNames() {
        return Set.of("id", "createdTimestamp");
    }

    protected boolean filterField(PsiField field) {
        final var annotations = Arrays.stream(field.getAnnotations()).map(PsiAnnotation::getQualifiedName).collect(Collectors.toSet());
        final PsiModifierList modifiers = field.getModifierList();
        final boolean isStaticField =
                modifiers != null && modifiers.hasModifierProperty(PsiModifier.STATIC);

        return !isStaticField && Collections.disjoint(annotations, skipAnnotations()) && !skipNames().contains(field.getName());
    }

    protected List<String> missingFields(PsiClass aClass) {
        return Arrays.stream(aClass.getAllFields()).filter(this::filterField).map(PsiField::getName).toList();
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(
            @NotNull
            final ProblemsHolder holder,
            boolean isOnTheFly) {
        var quickFix = this;
        return new JavaElementVisitor() {

            @Override
            public void visitMethodCallExpression(@NotNull PsiMethodCallExpression expression) {
                super.visitMethodCallExpression(expression);
                PsiMethod resolvedMethod = expression.resolveMethod();
                if (resolvedMethod == null) {
                    return;
                }

                if (isLombokBuilderBuildMethod(resolvedMethod) || isOpenAPIBuilderBuildMethod(resolvedMethod)) {
                    PsiClass builderClass = getContainingBuilderClass(resolvedMethod);
                    if (builderClass != null) {
                        List<String> missingMandatoryFields = processMissingFields(expression, missingFields(builderClass));
                        if (!missingMandatoryFields.isEmpty()) {
                            holder.registerProblem(
                                    expression,
                                    descriptionTemplate(),
                                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                    quickFix);
                        }
                    }
                }
            }
        };
    }

    private static PsiClass getContainingBuilderClass(PsiMethod resolvedMethod) {
        if (isLombokBuilderBuildMethod(resolvedMethod)) {
            return getContainingLombokBuilderClass(resolvedMethod);
        }

        if (isOpenAPIBuilderBuildMethod(resolvedMethod)) {
            return Optional.ofNullable(resolvedMethod.getContainingClass())
                    .flatMap(containingClass -> Optional.ofNullable(containingClass.getContainingClass()))
                    .orElse(null);
        }

        return null;
    }

    private static boolean isLombokBuilderBuildMethod(PsiMethod resolvedMethod) {
        if (isNotBuildMethod(resolvedMethod)) {
            return false;
        }

        return Objects.equals(
                resolvedMethod.getClass().getName(),
                PSI_LOMBOK_LIGHT_METHOD_BUILDER);
    }

    private static PsiClass getContainingLombokBuilderClass(PsiMethod element) {
        PsiClass aClass = element.getContainingClass();
        while (aClass != null && !isClassBuilder(aClass)) {
            aClass = aClass.getContainingClass();
        }

        return aClass;
    }

    private static boolean isClassBuilder(PsiClass aClass) {
        return Arrays.stream(aClass.getAnnotations())
                .anyMatch(annotation -> LOMBOK_BUILDER_CLASS_QUALIFIED_NAMES.contains(
                        annotation.getQualifiedName()));
    }

    private static boolean isOpenAPIBuilderBuildMethod(PsiMethod resolvedMethod) {
        if (isNotBuildMethod(resolvedMethod)) {
            return false;
        }

        return Optional.ofNullable(resolvedMethod.getContainingClass())
                .flatMap(containingClass -> Optional.ofNullable(containingClass.getContainingClass()))
                .flatMap(parentClass -> Optional.ofNullable(parentClass.getAnnotation("jakarta.annotation.Generated")))
                .map(annotation -> AbstractLombokBuilderInspection.isGeneratedByOpenApiToolsSpringCodeGen(
                        annotation,
                        "org.openapitools.codegen.languages.SpringCodegen"))
                .orElse(false);
    }

    private static boolean isNotBuildMethod(PsiMethod resolvedMethod) {
        return resolvedMethod == null || !Objects.equals(resolvedMethod.getName(), "build");
    }

    private static boolean isGeneratedByOpenApiToolsSpringCodeGen(PsiAnnotation annotation, Object constantValue) {
        return annotation.getAttributes()
                .stream()
                .map(attribute -> Optional.ofNullable(attribute.getAttributeValue())
                        .filter(JvmAnnotationConstantValue.class::isInstance)
                        .map(JvmAnnotationConstantValue.class::cast)
                        .map(JvmAnnotationConstantValue::getConstantValue)
                        .map(Object::toString)
                        .orElse(""))
                .anyMatch(constantValue::equals);
    }

    List<String> processMissingFields(PsiElement expression, List<String> originalFields) {
        List<String> fields = new ArrayList<>(originalFields);
        Queue<PsiElement> queue = new LinkedList<>();
        Set<PsiElement> seenElements = new HashSet<>();
        queue.offer(expression);

        while (!queue.isEmpty()) {
            PsiElement cur = queue.poll();
            if (cur != null) {
                seenElements.add(cur);
                if (cur instanceof PsiIdentifierImpl) {
                    fields.remove(cur.getText());
                }

                if (cur instanceof PsiMethodCallExpressionImpl psiMethod) {
                    PsiMethod resolvedMethod = psiMethod.resolveMethod();
                    if (resolvedMethod != null) {
                        // If the resolved method is not a lombok method, add the return statement to the queue
                        // to visit its nodes too
                        if (!Objects.equals(
                                resolvedMethod.getClass().getName(),
                                PSI_LOMBOK_LIGHT_METHOD_BUILDER)) {
                            for (PsiReturnStatement returnStatement : PsiUtil.findReturnStatements(
                                    resolvedMethod)) {
                                queue.offer(returnStatement.getReturnValue());
                            }
                            // If we are calling build on an element that was a result of a toBuilder call we assume
                            // that the builder already has all mandatory fields set
                        } else if (Objects.equals(resolvedMethod.getName(), "toBuilder")) {
                            fields.clear();
                            break;
                        }
                    }
                }

                if (cur instanceof PsiReferenceExpressionImpl psiReference) {
                    PsiElement resolvedElement = psiReference.resolve();
                    if (resolvedElement instanceof PsiLocalVariable psiLocalVariable) {
                        PsiElement initializer = psiLocalVariable.getInitializer();
                        if (!seenElements.contains(initializer)) {
                            queue.offer(initializer);
                        }

                        Arrays.stream(ReferencesSearch.search(
                                resolvedElement,
                                GlobalSearchScope.fileScope(resolvedElement.getContainingFile()),
                                false).toArray(PsiReference.EMPTY_ARRAY)).forEach(reference -> {
                            if (reference.getElement().getTextRange().getStartOffset()
                                    < cur.getTextRange().getStartOffset()) {
                                PsiElement referenceParent = reference.getElement().getParent();
                                if (!seenElements.contains(referenceParent)) {
                                    queue.offer(referenceParent);
                                }
                            }
                        });
                    }
                }

                for (PsiElement child : cur.getChildren()) {
                    if (!seenElements.contains(child) && (child instanceof PsiIdentifierImpl
                            || child instanceof PsiMethodCallExpressionImpl
                            || child instanceof PsiReferenceExpressionImpl)) {
                        queue.offer(child);
                    }
                }
            }
        }

        return fields;
    }

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
                    missingFields(getContainingBuilderClass(resolvedMethod)));

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
}
