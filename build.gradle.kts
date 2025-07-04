import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.6.0"
}

group = "com.nitrobox.lombokbuilderhelper"
version = "1.3.1"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }

}

dependencies {
    intellijPlatform {
        bundledPlugin("com.intellij.java")// Fügt Lombok-Plugin hinzu
        create(IntelliJPlatformType.IntellijIdeaUltimate, "2025.1.3")
        testFramework(TestFrameworkType.Platform)
        testFramework(TestFrameworkType.Plugin.Java)
        bundledPlugin("Lombook Plugin")
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

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    //workaround see https://youtrack.jetbrains.com/issue/IJPL-159134/JUnit5-Test-Framework-refers-to-JUnit4-java.lang.NoClassDefFoundError-junit-framework-TestCase
    testRuntimeOnly("junit:junit:4.13.2")

    testImplementation("org.assertj:assertj-core:3.24.2")
}

tasks.test {
    useJUnitPlatform()

    dependsOn("buildPlugin")
}


intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "251"
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
