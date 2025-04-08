package com.libyear.sourcing

import com.fasterxml.jackson.databind.ObjectMapper
import io.vavr.control.Try
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.gradle.api.artifacts.ModuleIdentifier
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.gradle.api.internal.artifacts.DefaultModuleVersionIdentifier
import java.time.Duration
import java.time.Instant

/**
 * Retrieve artifact creation dates from repositories supporting search via Solr queries.
 *
 * This strategy assumes that sets of artifacts located by group:artifact:version coordinates posses
 * the same creation date. For example, this strategy assumes that POM, classes, Javadoc, sources
 * artifacts have the same logical creation date.
 *
 * Using this assumption of equal creation dates regardless of the artifact date, this strategy
 * forms a Solr query and picks the creation date of the first artifact as final result.
 *
 * For reference, below is an alternate implementation using the command line tools HTTPie and jq:
 *
 * ```
 * http "https://search.maven.org/solrsearch/select" q=='g:"org.apache.commons" AND a:"commons-text" AND v:"1.9"' | jq ".response.docs[].timestamp"
 * ```
 *
 * The original idea / existence of this API is credited to the
 * [Liferay dependency checker plugin](https://github.com/liferay/liferay-portal/tree/master/modules/sdk/gradle-plugins-dependency-checker/src/main/java/com/liferay/gradle/plugins/dependency/checker).
 * Despite the fact that no other repository software offers this type of API, this strategy is very reliable
 * for Maven Central, one of the most popular repositories.
 */
class SolrSearchAdapter(
  private val repoUrl: String,
  private val maxRetries: Int = 3,
  private val initialRetryDelayMillis: Long = 2000,
  private val retryBackoffMultiplier: Int = 2
) : VersionInfoAdapter {

  private val client = OkHttpClient()
  private val retryableClient = RetryableHttpClient(
    client = client,
    maxRetries = maxRetries,
    initialRetryDelayMillis = initialRetryDelayMillis,
    retryBackoffMultiplier = retryBackoffMultiplier
  )
  private val mapper = ObjectMapper()

  override fun get(m: ModuleVersionIdentifier, repository: ArtifactRepository) = Try.of {
    val latestVersion = retrieveLatestVersion(m.module)
    DependencyInfo(m, retrieveUpdate(m, latestVersion))
  }

  private fun retrieveLatestVersion(m: ModuleIdentifier): String {
    val url = buildUrlForLatestVersion(m)
    val response = getResponseText(url)
    return parseLatestVersion(response)
  }

  private fun buildUrlForLatestVersion(m: ModuleIdentifier): HttpUrl {
    val query =
      """g:"${m.group}" AND a:"${m.name}""""
    return repoUrl.toHttpUrl().newBuilder()
      .setQueryParameter("q", query)
      .build()
  }

  private fun parseLatestVersion(response: String): String {
    val json = mapper.readTree(response) ?: throw IllegalArgumentException("Invalid JSON: $response")
    val doc = json["response"].withArray("docs").first()
    return doc["latestVersion"].asText()
  }

  private fun retrieveCreated(m: ModuleVersionIdentifier): Instant {
    val url = buildUrlForArtifact(m)
    val response = getResponseText(url)
    return parseCreated(response)
  }

  private fun buildUrlForArtifact(m: ModuleVersionIdentifier): HttpUrl {
    val query =
      """g:"${m.group}" AND a:"${m.name}" AND v:"${m.version}""""
    return repoUrl.toHttpUrl().newBuilder()
      .setQueryParameter("q", query)
      .build()
  }

  private fun getResponseText(url: HttpUrl): String {
    val request = Request.Builder().url(url).build()
    return retryableClient.executeWithRetry(request).use { response ->
      response.body?.string() ?: throw IllegalStateException("HTTP response body for $url is empty")
    }
  }

  private fun parseCreated(response: String): Instant {
    val json = mapper.readTree(response) ?: throw IllegalArgumentException("Invalid JSON: $response")
    val doc = json["response"].withArray("docs").first()
    return doc["timestamp"].asLong().let(Instant::ofEpochMilli)
  }

  internal fun retrieveUpdate(m: ModuleVersionIdentifier, latestVersion: String): DependencyUpdate? {
    if (m.version == latestVersion) return null
    val currentCreated = retrieveCreated(m)
    val latestCreated = retrieveCreated(DefaultModuleVersionIdentifier.newId(m.module, latestVersion))

    // TODO Figure this one out.
    // Apache Tomcat is concurrently maintaining two release series: 9.x and 10.x.
    // Therefore, the latest and greatest is 10.x.
    // However, 10.x is released 1h before 9.x, that makes the latest release older than the 9.x patch release.
    // How should we handle this?
    val lag = Duration.between(currentCreated, latestCreated)
    return if (lag.isNegative) null else DependencyUpdate(latestVersion, lag)
  }

  companion object {

    fun forMavenCentral(
      maxRetries: Int = 3,
      initialRetryDelayMillis: Long = 2000,
      retryBackoffMultiplier: Int = 2
    ) = SolrSearchAdapter(
      repoUrl = "https://search.maven.org/solrsearch/select",
      maxRetries = maxRetries,
      initialRetryDelayMillis = initialRetryDelayMillis,
      retryBackoffMultiplier = retryBackoffMultiplier
    )
  }
}
