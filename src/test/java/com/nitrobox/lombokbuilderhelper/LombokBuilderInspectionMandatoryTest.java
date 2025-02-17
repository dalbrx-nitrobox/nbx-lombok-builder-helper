package com.nitrobox.lombokbuilderhelper;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase4;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ListAssert;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class LombokBuilderInspectionMandatoryTest extends LightJavaCodeInsightFixtureTestCase4 {

    public LombokBuilderInspectionMandatoryTest() {
        super(new DefaultLightProjectDescriptor() {
            @Override
            public void configureModule(@NotNull Module module, @NotNull ModifiableRootModel model, @NotNull ContentEntry contentEntry) {
                this.withRepositoryLibrary("jakarta.validation:jakarta.validation-api:3.0.0");
                this.withRepositoryLibrary("org.projectlombok:lombok:1.18.24");
                super.configureModule(module, model, contentEntry);

            }
        }, "src/test/testData");
    }

    @Test
    public void test() {
        getFixture().enableInspections(List.of(LombokBuilderInspectionMandatory.class));

        getFixture().configureByFile("pkg/LombokPojo.java");

        PsiFile file = getFixture().getFile();
        assertNotNull(file);

        List<HighlightInfo> highlightInfos = getFixture().doHighlighting();

        assertThat(highlightInfos).satisfiesOnlyOnce(info -> {
            assertThat(info.getInspectionToolId()) .isEqualTo("LombokBuilderInspectionMandatory");
            assertThat(info.getSeverity()) .isEqualTo(HighlightSeverity.ERROR);
            assertThat(info.getDescription()) .isEqualTo("Lombok builder is missing non nullable fields");
        });
    }
}