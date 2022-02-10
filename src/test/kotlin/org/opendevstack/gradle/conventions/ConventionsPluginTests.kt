package org.opendevstack.gradle.conventions

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ConventionsPluginTests {

    lateinit var project: Project

    @BeforeEach
    internal fun setUp() {
        val project: Project = ProjectBuilder.builder()
            .build()

        // equivalent to settings in a gradle.properties file
        project.extensions.add("nexus_url", "https://nexus.org/")
        project.extensions.add("nexus_user", "nuser")
        project.extensions.add("nexus_pw", "npwd")

        project.pluginManager.apply("org.opendevstack.gradle.conventions")

        this.project = project
    }

    @Test
    fun `applying the ods plugin should apply the java and jacoco and plugin as well`() {
        val java = "java"
        val jacoco = "jacoco"
        val msg = { plugin: String -> "$plugin plugin does not exist!" }
        assertNotNull(project.plugins.findPlugin(java), msg(java))
        assertNotNull(project.plugins.findPlugin(jacoco), msg(jacoco))
    }

    @Test
    fun `jacocoTestReport will finalize the test task`() {
        val testTask = project.tasks.findByName("test")!!

        val testFinalizingTasks = testTask.finalizedBy.getDependencies(testTask)
        assertEquals(testFinalizingTasks.size, 1)

        val jacocoTestReportTask = testFinalizingTasks.first()
        assertEquals(jacocoTestReportTask.name, "jacocoTestReport")
        assertTrue((jacocoTestReportTask as JacocoReport).reports.xml.required.get())
    }
}
