package com.nitrobox.lombokbuilderhelper;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.IdeaTestUtil;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase5;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

class OpenApiGeneratorBuilderInspectionTest extends LightJavaCodeInsightFixtureTestCase5 {

    public OpenApiGeneratorBuilderInspectionTest() {
        super(new DefaultLightProjectDescriptor() {
            @Override
            public void configureModule(@NotNull Module module, @NotNull ModifiableRootModel model, @NotNull ContentEntry contentEntry) {
                this.withRepositoryLibrary("jakarta.validation:jakarta.validation-api:3.0.0");
                this.withRepositoryLibrary("com.fasterxml.jackson.core:jackson-annotations:2.18.2");
                this.withRepositoryLibrary("jakarta.annotation:jakarta.annotation-api:2.1.1");
                this.withRepositoryLibrary("io.swagger.core.v3:swagger-annotations:2.2.28");
                super.configureModule(module, model, contentEntry);

            }

            @Override
            public Sdk getSdk() {
                    return IdeaTestUtil.getMockJdk21();
            }
        });
    }

    @Override
    protected @Nullable String getTestDataPath() {
        return "src/test/testData";
    }

    @Test
    void shouldReportErrorIfMandatoryPropertyIsMissing() {
        //given
        getFixture().enableInspections(List.of(LombokBuilderInspectionMandatory.class));
        getFixture().configureByFile("pkg/CouponRequestId.java");
        PsiFile file = getFixture().getFile();
        assertNotNull(file);

        //when
        List<HighlightInfo> highlightInfos = getFixture().doHighlighting();

        //then
        assertThat(highlightInfos).satisfiesOnlyOnce(info -> {
            assertThat(info.getInspectionToolId()) .isEqualTo("LombokBuilderInspectionMandatory");
            assertThat(info.getSeverity()) .isEqualTo(HighlightSeverity.ERROR);
            assertThat(info.getDescription()) .isEqualTo("Lombok builder is missing non nullable fields");
        });
    }
}