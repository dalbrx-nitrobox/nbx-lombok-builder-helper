plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.16.0"
}

group = "com.nitrobox.lombokbuilderhelper"
version = "1.0.4"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}

intellij {
    version.set("2023.2")
    plugins.set(listOf("com.intellij.java"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    patchPluginXml {
        version.set("${project.version}")
        sinceBuild.set("232")
        untilBuild.set("")
    }
}
