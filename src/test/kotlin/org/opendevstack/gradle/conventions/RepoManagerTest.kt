package org.opendevstack.gradle.conventions

import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class RepoManagerTest {

    @TestFactory
    fun `no_nexus is false but not all proxy settings set via envs`() = listOf(
        emptyMap(),
        mapOf(
            "NEXUS_URL" to "url"
        ),
        mapOf(
            "NEXUS_URL" to "url",
            "NEXUS_USERNAME" to "user",
        ),
    ).map { envs ->
        val project: Project = ProjectBuilder.builder().build()
        val repoManager = RepoManager(project) {
            envs[it]
        }
        DynamicTest.dynamicTest("$envs should be insufficient in") {
            assertFailsWith<InvalidUserDataException> {
                repoManager.applyRepoSettings()
            }
        }
    }

    @Test
    fun `no_nexus is false and all proxy settings are set via project properties`() {
        val project: Project = ProjectBuilder.builder().build()
        project.extensions.add("nexus_url", "url")
        project.extensions.add("nexus_user", "user")
        project.extensions.add("nexus_pw", "pwd")

        val repoManager = RepoManager(project)
        repoManager.applyRepoSettings()
    }

    @Test
    fun `no_nexus is false and all proxy settings are set via envs NEXUS_URL option`() {
        val project: Project = ProjectBuilder.builder().build()
        val envs = mapOf(
            "NEXUS_URL" to "url",
            "NEXUS_USERNAME" to "user",
            "NEXUS_PASSWORD" to "pwd",
        )
        val repoManager = RepoManager(project) {
            envs[it]
        }
        repoManager.applyRepoSettings()
    }

    @Test
    fun `no_nexus is false and all proxy settings are set via envs NEXUS_HOST option`() {
        val project: Project = ProjectBuilder.builder().build()
        val envs = mapOf(
            "NEXUS_HOST" to "url",
            "NEXUS_USERNAME" to "user",
            "NEXUS_PASSWORD" to "pwd",
        )
        val repoManager = RepoManager(project) {
            envs[it]
        }
        repoManager.applyRepoSettings()
    }

    @TestFactory
    fun `no_nexus is false but not all proxy settings set via project properties`() = listOf(
        emptyMap(),
        mapOf(
            "nexus_url" to "url"
        ),
        mapOf(
            "nexus_url" to "url",
            "nexus_user" to "user",
        ),
    ).map { props ->
        val project: Project = ProjectBuilder.builder().build()
        props.forEach { project.extensions.add(it.key, it.value) }

        val repoManager = RepoManager(project)
        DynamicTest.dynamicTest("$props should be insufficient in") {
            assertFailsWith<InvalidUserDataException> {
                repoManager.applyRepoSettings()
            }
        }
    }

    @TestFactory
    fun `if proxy manager is activated (by default) default repos should be set`() = RepoManager.defaultRepoPaths
        .map { repoPath ->
            DynamicTest.dynamicTest("a maven repository with $repoPath and credentials should be created by default") {
                val project: Project = ProjectBuilder.builder()
                    .build()
                project.extensions.add("nexus_url", "https://nexus.org/")
                project.extensions.add("nexus_user", "nuser")
                project.extensions.add("nexus_pw", "npwd")
                project.pluginManager.apply("org.opendevstack.gradle.conventions")

                val repo = project.repositories.findByName(repoPath) as MavenArtifactRepository
                assertNotNull(repo, "repo '$repoPath' does not exist!")
                assertEquals(repo.name, repoPath)
                assertEquals(repo.url.toString(), "https://nexus.org$repoPath")
                assertEquals(repo.credentials.username, "nuser")
                assertEquals(repo.credentials.password, "npwd")
            }
        }

    @Test
    fun `if proxy manager is NOT activated (no_nexus = true) only mavenCentral() should be created`() {
        val project: Project = ProjectBuilder.builder()
            .build()
        project.extensions.add("no_nexus", "true")
        project.pluginManager.apply("org.opendevstack.gradle.conventions")
        project.repositories.forEach {
            val default = it as MavenArtifactRepository
            println(default.name)
            println(default.url)
            println(default.url)
        }

        val repos = project.repositories
        assertEquals(1, repos.size)

        val mavenCentralUrl = "https://repo.maven.apache.org/maven2/"
        val mavenRepo = repos.first() as MavenArtifactRepository
        assertEquals(project.uri(mavenCentralUrl), mavenRepo.url)
    }
}
