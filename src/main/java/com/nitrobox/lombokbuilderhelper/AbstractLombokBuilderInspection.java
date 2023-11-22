package com.nitrobox.lombokbuilderhelper;

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLocalVariable;
import com.intellij.psi.PsiMethod;
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

public abstract class AbstractLombokBuilderInspection  extends AbstractBaseJavaLocalInspectionTool {

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
                                "de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder")) {
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

    PsiClass getContainingBuilderClass(PsiMethod element) {
        PsiClass aClass = element.getContainingClass();
        while (aClass != null && !isClassBuilder(aClass)) {
            aClass = aClass.getContainingClass();
        }

        return aClass;
    }

    boolean isClassBuilder(PsiClass aClass) {
        final Set<String> builderClassQualifiedNames =
                Set.of("lombok.Builder", "lombok.experimental.SuperBuilder");
        return Arrays.stream(aClass.getAnnotations())
                .anyMatch(annotation -> builderClassQualifiedNames.contains(
                        annotation.getQualifiedName()));
    }

}
