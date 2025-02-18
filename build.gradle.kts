import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.2.1"
}

group = "com.nitrobox.lombokbuilderhelper"
version = "1.1.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }

}

dependencies {
    intellijPlatform {
        bundledPlugin("com.intellij.java")
        create(IntelliJPlatformType.IntellijIdeaUltimate, "2024.3.3")
        testFramework(TestFrameworkType.Plugin.Java)
    }


    testImplementation("jakarta.validation:jakarta.validation-api:3.0.0")
    testImplementation("com.fasterxml.jackson.core:jackson-annotations:2.18.2")
    testImplementation("jakarta.annotation:jakarta.annotation-api:2.1.1")
    testImplementation("io.swagger.core.v3:swagger-annotations:2.2.28")



    // Lombok
    testCompileOnly("org.projectlombok:lombok:1.18.24")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.24")

    // Weitere benötigte Abhängigkeiten
    testImplementation("org.hibernate.validator:hibernate-validator:6.2.0.Final")

    testImplementation("junit:junit:4.13.2")
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "243"
        }
    }
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }

    //patchPluginXml {
    //    version.set("${project.version}")
    //    sinceBuild.set("232")
    //    untilBuild.set("")
    // }
}
