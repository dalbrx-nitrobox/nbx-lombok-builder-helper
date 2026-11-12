import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.20.1"
}

group = "com.nitrobox.lombokbuilderhelper"
version = "1.4.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }

}

dependencies {
    intellijPlatform {
        bundledPlugin("com.intellij.java")// Fügt Lombok-Plugin hinzu
        intellijIdea("2026.3")
        testFramework(TestFrameworkType.Platform)
        testFramework(TestFrameworkType.Plugin.Java)
        bundledPlugin("Lombook Plugin")
    }

    testImplementation("jakarta.validation:jakarta.validation-api:3.0.0")
    testImplementation("com.fasterxml.jackson.core:jackson-annotations:2.22")
    testImplementation("jakarta.annotation:jakarta.annotation-api:2.1.1")
    testImplementation("io.swagger.core.v3:swagger-annotations:2.2.28")



    // Lombok
    testCompileOnly("org.projectlombok:lombok:1.18.48")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.48")

    // Weitere benötigte Abhängigkeiten
    testImplementation("org.hibernate.validator:hibernate-validator:6.2.0.Final")

    testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")

    testImplementation("org.assertj:assertj-core:3.24.2")
}

tasks.test {
    useJUnitPlatform()

    dependsOn("buildPlugin")

    // JDK 25 starts a Wayland AWT thread when WAYLAND_DISPLAY is set, and the
    // platform test runner treats that thread as a leak.
    setEnvironment(System.getenv().filterKeys { it != "WAYLAND_DISPLAY" })
}


intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "263"
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks {

    //patchPluginXml {
    //    version.set("${project.version}")
    //    sinceBuild.set("232")
    //    untilBuild.set("")
    // }
}
