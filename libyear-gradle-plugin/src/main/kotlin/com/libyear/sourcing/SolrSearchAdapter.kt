package com.libyear.sourcing

import com.fasterxml.jackson.databind.ObjectMapper
import io.vavr.control.Try
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.repositories.ArtifactRepository
import java.io.IOException
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
  private val repoUrl: String
) : VersionInfoAdapter {

  private val client = OkHttpClient()
  private val mapper = ObjectMapper()

  override fun getArtifactCreated(m: ModuleVersionIdentifier, repository: ArtifactRepository) = Try.of {
    val url = buildUrl(m)
    val response = getResponseText(url)
    response.let(::parse)
  }

  private fun buildUrl(m: ModuleVersionIdentifier): HttpUrl {
    return repoUrl.toHttpUrl().newBuilder()
      .setQueryParameter("q", encodeToQueryParameter(m))
      .build()
  }

  private fun encodeToQueryParameter(m: ModuleVersionIdentifier): String {
    return """g:"${m.group}" AND a:"${m.name}" AND v:"${m.version}""""
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

  private fun parse(response: String): Instant {
    val json = mapper.readTree(response) ?: throw IllegalArgumentException("Invalid JSON: $response")
    val docs = json["response"].withArray("docs")
    if (docs.size() >= 1) {
      val created = docs[0]["timestamp"].asLong()
      return Instant.ofEpochMilli(created)
    }
    throw IllegalArgumentException("List of result documents is empty in: $response")
  }

  companion object {

    fun forMavenCentral() = SolrSearchAdapter(
      repoUrl = "https://search.maven.org/solrsearch/select"
    )
  }
}
