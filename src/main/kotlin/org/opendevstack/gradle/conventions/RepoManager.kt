package org.opendevstack.gradle.conventions

import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import org.gradle.internal.impldep.com.google.common.annotations.VisibleForTesting

class RepoManager(
    private val project: Project,
    @VisibleForTesting
    private val envResolver: (String) -> String? = { System.getenv(it) },
) {
    companion object {
        val defaultRepoPaths = setOf(
            "/repository/jcenter/",
            "/repository/maven-public/",
            "/repository/atlassian_public/",
        )
    }

    private val urlProp = Prop("nexus_url", setOf("NEXUS_HOST", "NEXUS_URL"))
    private val userProp = Prop("nexus_user", setOf("NEXUS_USERNAME"))
    private val passwordProp = Prop("nexus_pw", setOf("NEXUS_PASSWORD"))
    private val snapshotFolderProp = Prop("nexus_folder_releases", setOf("NEXUS_FOLDER_RELEASES"))
    private val releaseFolderProp = Prop("nexus_folder_snapshots", setOf("NEXUS_FOLDER_SNAPSHOTS"))
    private val noProxyProp = Prop("no_nexus", setOf("NO_NEXUS"))

    fun applyRepoSettings() {
        resolveAndSetMavenRepoManagerSettings()
        addDefaultMavenRepos()
    }

    private fun resolveAndSetMavenRepoManagerSettings() {
        fun setExtraProperty(prop: Prop, default: Any? = null) {
            val envValue = prop.envNames.firstNotNullOfOrNull(envResolver)
            val result = property(project, prop.propName) ?: envValue

            if (result == null) {
                project.logger.info("property '$prop' was not set")
                if (default != null) {
                    project.logger.info("setting property '$prop' to default $default")
                    project.extensions.extraProperties.set(prop.propName, default)
                }
            } else {
                project.extensions.extraProperties.set(prop.propName, result)
            }
        }

        setExtraProperty(urlProp)
        setExtraProperty(userProp)
        setExtraProperty(passwordProp)
        setExtraProperty(snapshotFolderProp)
        setExtraProperty(releaseFolderProp)
        setExtraProperty(noProxyProp, "false")

        val useProxy = !property(project, noProxyProp.propName).toBoolean()
        if (useProxy) {
            fun propertyMustExist(prop: Prop) {
                if (property(project, prop.propName) == null) {
                    throw InvalidUserDataException("$noProxyProp is false but $prop is not set")
                }
            }

            propertyMustExist(urlProp)
            propertyMustExist(userProp)
            propertyMustExist(passwordProp)
        }
    }

    private fun addDefaultMavenRepos() {
        if ((property(project, noProxyProp.propName)).toBoolean()) {
            project.logger.warn("$noProxyProp is set to true, only setting mavenCentral() as repository")
            project.repositories.mavenCentral()
        } else {
            project.logger.info("repo manager is active, setting default repos ... $defaultRepoPaths")
            defaultRepoPaths.forEach {
                addMavenRepo(project = project, repoPath = it)
            }
        }
    }

    private fun addMavenRepo(project: Project, repoPath: String) {
        project.repositories.maven { mavenRepo ->
            mavenRepo.name = repoPath
            mavenRepo.credentials {
                it.username = property(project, userProp.propName)
                it.password = property(project, passwordProp.propName)
            }
            mavenRepo.url = project.uri(buildRepoUri(project, repoPath))
        }
    }

    private fun buildRepoUri(project: Project, repoPath: String): String {
        val nexusUrlProp = property(project, urlProp.propName)
        val nexusUrl = if (nexusUrlProp!!.endsWith("/")) {
            nexusUrlProp.dropLast(1)
        } else {
            nexusUrlProp
        }

        return "$nexusUrl$repoPath"
    }

    private fun property(project: Project, name: String) = project.findProperty(name) as String?
}
data class Prop(
    val propName: String,
    val envNames: Set<String>
)
