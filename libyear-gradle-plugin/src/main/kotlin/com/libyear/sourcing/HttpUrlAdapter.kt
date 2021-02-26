package com.libyear.sourcing

import io.vavr.control.Try
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.gradle.api.artifacts.repositories.UrlArtifactRepository
import java.io.IOException
import java.time.Instant

/**
 * Retrieve artifact creation dates from HTTP repositories.
 *
 * In comparison to the adapter for repositories with Solr Search (namely Maven Central) this method
 * work with arbitrary repositories which are served via HTTP.
 * This strategy assume that the content of the Last-Modified HTTP header correlates with the creation date of the artifact.
 *
 * *Caveats*
 *
 * 1. The information of the Last-Modified HTTP header may not correlate to the artifact creation date or change over time.
 *
 * 2. Because Gradle API does not expose the qualifier of the dependency, the strategy assumes "jar" by default.
 *    To work with other qualifiers such as "pom", this strategy requires customization via [RepositoryLayout].
 *
 * 3. Because Gradle does not expose "turn this module version identifier into a URL" as part of its public API, this strategy
 *    assumes a standard one. Please see [DefaultRepositoryLayout] for details of how the artifact URLs are formed.
 */
class HttpUrlAdapter(private val repositoryLayout: RepositoryLayout = DefaultRepositoryLayout) : VersionInfoAdapter {

  private val client = OkHttpClient()

  override fun getArtifactCreated(m: ModuleVersionIdentifier, repository: ArtifactRepository) = Try.of {
    if (repository !is UrlArtifactRepository) throw IllegalArgumentException("$repository is not an URL repository")
    val url = buildUrl(repository, m)
    getLastModified(url)
  }

  private fun buildUrl(repository: UrlArtifactRepository, module: ModuleVersionIdentifier): HttpUrl {
    val repoUrl = repository.url.toHttpUrlOrNull() ?: throw IllegalArgumentException()
    return repositoryLayout.getArtifactUrl(repoUrl, module)
  }

  private fun getLastModified(url: HttpUrl): Instant {
    val request = Request.Builder().url(url).head().build()
    client.newCall(request).execute().use { response ->

      if (!response.isSuccessful) {
        throw IOException("Response for URL $url returned ${response.code}")
      }

      return response.headers.getInstant("Last-Modified") ?: throw IllegalArgumentException("Response for URL $url did not contain a Last-Modified header")
    }
  }
}

/**
 * How to find the absolute artifact URL given a base URL and module identifier?
 */
interface RepositoryLayout {
  fun getArtifactUrl(repositoryBaseUrl: HttpUrl, module: ModuleVersionIdentifier): HttpUrl
}

/**
 * Most common repository layout in which each part of the hierarchical name is a subdirectory.
 *
 * Example:
 *
 * ```
 * input = "org.apache.commons:commons-text:1.8"
 * output = baseUrl + "/org/apache/commons/commons-text/1.8/commons-text-1.8.jar"
 * ```
 */
object DefaultRepositoryLayout : RepositoryLayout {
  override fun getArtifactUrl(repositoryBaseUrl: HttpUrl, module: ModuleVersionIdentifier) =
    repositoryBaseUrl.newBuilder().apply {

      for (part in module.group.split(".")) {
        addPathSegment(part)
      }

      for (part in module.name.split(".")) {
        addPathSegment(part)
      }

      addPathSegment(module.version)

      addPathSegment("${module.name}-${module.version}.jar")
    }.build()
}
