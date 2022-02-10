plugins {
    idea
    id("org.jetbrains.kotlin.jvm") version "1.5.31"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "0.21.0"
    `maven-publish`
}

group = "org.opendevstack.gradle"
version = "0.0.1"

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

java {
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(gradleTestKit())
}

tasks.withType<Test> {
    useJUnitPlatform()
}

gradlePlugin {
    plugins {
        create("conventionsPlugin") {
            id = "org.opendevstack.gradle.conventions"
            displayName = "Opendevstack conventions plugin"
            description = "This Gradle plugin sets basic OpenDevStack conventions when applied to a project, thus making the gradle project compatible to the build requirements."
            implementationClass = "org.opendevstack.gradle.conventions.ConventionsPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/opendevstack/ods-gradle-conventions"
    vcsUrl = "https://github.com/opendevstack/ods-gradle-conventions"
    description = "This Gradle plugin sets basic OpenDevStack conventions when applied to a project, thus making the gradle project compatible to the build requirements."
    tags = listOf("opendevstack")
}

/*
 You can use local publishing to build and test the plugin locally
 in a custom plugin repository https://docs.gradle.org/current/userguide/publishing_gradle_plugins.html#custom-plugin-repositories
 */
publishing {
    repositories {
        maven {
            name = "localPluginRepository"
            url = uri("${System.getenv("HOME")}/.gradle/local-plugin-repository")
        }
    }
}
