package org.opendevstack.gradle.conventions

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.tasks.JacocoReport

class ConventionsPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        project.apply {
            it.plugin("java")
            it.plugin("jacoco")
        }

        val repoManager = RepoManager(project)
        repoManager.applyRepoSettings()

        setupCodeCoverage(project)
    }

    private fun setupCodeCoverage(project: Project) {
        val tasks = project.tasks

        tasks.named("test", Test::class.java) {
            it.finalizedBy("jacocoTestReport")
        }

        tasks.named("jacocoTestReport", JacocoReport::class.java) {
            it.description = "tests are required to run before generating the report"
            it.dependsOn("test")
            it.reports { rc ->
                rc.xml.required.set(true)
            }
        }
    }
}
