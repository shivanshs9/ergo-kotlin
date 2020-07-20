package publish

import org.gradle.api.Project
import org.gradle.api.component.Component
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.support.delegates.ProjectDelegate
import java.net.URI

/**
 * Created by shivanshs9 on 21/07/20.
 */
const val defaultOrg = "headout"
const val defaultRepo = "ergo-kotlin"

fun PublishingExtension.GithubPackage(project: Project, org: String = defaultOrg, repo: String = defaultRepo) {
    repositories {
        maven {
            name = "GitHubPackages"
            url = URI("https://maven.pkg.github.com/$org/$repo")
            credentials {
                username = (project.findProperty("gpr.user") as? String) ?: System.getenv("GH_USERNAME")
                password = (project.findProperty("gpr.key") as? String) ?: System.getenv("GH_TOKEN")
            }
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            from(project.components["java"])
        }
    }
}
