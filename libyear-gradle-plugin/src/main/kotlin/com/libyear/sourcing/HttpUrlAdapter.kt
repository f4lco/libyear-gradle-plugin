package com.libyear.sourcing

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.vavr.control.Try
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.gradle.api.artifacts.repositories.UrlArtifactRepository
import org.gradle.api.internal.artifacts.DefaultModuleVersionIdentifier
import java.io.IOException
import java.time.Duration
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
  private val mapper = XmlMapper()

  override fun get(m: ModuleVersionIdentifier, repository: ArtifactRepository) = Try.of {
    if (repository !is UrlArtifactRepository) throw IllegalArgumentException("$repository is not an URL repository")
    val latestVersion = retrieveLatestVersion(repository, m)
    DependencyInfo(m, retrieveUpdate(repository, m, latestVersion))
  }

  private fun retrieveLatestVersion(repository: UrlArtifactRepository, m: ModuleVersionIdentifier): String {
    val url = buildUrlForLatestVersion(repository, m)
    val response = getResponseText(url)
    return mapper.readTree(response)?.get("versioning")?.get("release")?.asText() ?: throw IllegalArgumentException(response)
  }

  private fun buildUrlForLatestVersion(repository: UrlArtifactRepository, module: ModuleVersionIdentifier): HttpUrl {
    val repoUrl = repository.url.toHttpUrlOrNull() ?: throw IllegalArgumentException()
    return repositoryLayout.getMetadataUrl(repoUrl, module)
  }

  private fun getResponseText(url: HttpUrl): String {
    val request = Request.Builder().url(url).build()
    client.newCall(request).execute().use { response ->

      if (!response.isSuccessful) {
        throw IOException("Response for URL $url returned ${response.code}")
      }

      return response.body?.string() ?: throw IllegalStateException("HTTP response body for $url is empty")
    }
  }

  internal fun retrieveUpdate(repository: UrlArtifactRepository, m: ModuleVersionIdentifier, latestVersion: String): DependencyUpdate? {
    if (m.version == latestVersion) return null
    val currentCreated = retrieveCreated(repository, m)
    val latestCreated = retrieveCreated(repository, DefaultModuleVersionIdentifier.newId(m.module, latestVersion))

    // TODO Figure this one out.
    // Apache Tomcat is concurrently maintaining two release series: 9.x and 10.x.
    // Therefore, the latest and greatest is 10.x.
    // However, 10.x is released 1h before 9.x, that makes the latest release older than the 9.x patch release.
    // How should we handle this?
    val lag = Duration.between(currentCreated, latestCreated)
    return if (lag.isNegative) null else DependencyUpdate(latestVersion, lag)
  }

  private fun retrieveCreated(repository: UrlArtifactRepository, m: ModuleVersionIdentifier): Instant {
    val url = buildUrlForArtifact(repository, m)
    return getLastModified(url)
  }

  private fun buildUrlForArtifact(repository: UrlArtifactRepository, module: ModuleVersionIdentifier): HttpUrl {
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
  fun getMetadataUrl(repositoryBaseUrl: HttpUrl, module: ModuleVersionIdentifier): HttpUrl

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

  override fun getMetadataUrl(repositoryBaseUrl: HttpUrl, module: ModuleVersionIdentifier) =
    repositoryBaseUrl.newBuilder().apply {

      for (part in module.group.split(".")) {
        addPathSegment(part)
      }

      for (part in module.name.split(".")) {
        addPathSegment(part)
      }

      addPathSegment("maven-metadata.xml")
    }.build()

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
