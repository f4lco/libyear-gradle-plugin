package com.libyear.sourcing

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.gradle.api.artifacts.repositories.UrlArtifactRepository
import org.slf4j.LoggerFactory
import java.io.IOException
import java.lang.IllegalArgumentException
import java.time.Instant

class HttpUrlAdapter(private val repositoryLayout: RepositoryLayout = DefaultRepositoryLayout) : VersionInfoAdapter {

  private val client = OkHttpClient()

  override fun getArtifactCreated(m: ModuleVersionIdentifier, repository: ArtifactRepository): Instant? {
    if (repository !is UrlArtifactRepository) throw IllegalArgumentException()
    val url = buildUrl(repository, m)
    return getLastModified(url)
  }

  private fun buildUrl(repository: UrlArtifactRepository, module: ModuleVersionIdentifier): HttpUrl {
    val repoUrl = repository.url.toHttpUrlOrNull() ?: throw IllegalArgumentException()
    return repositoryLayout.getArtifactUrl(repoUrl, module)
  }

  private fun getLastModified(url: HttpUrl): Instant? {
    val request = Request.Builder().url(url).head().build()
    try {
      client.newCall(request).execute().use { response ->
        if (response.isSuccessful) return response.headers.getInstant("Last-Modified")
      }
    } catch (e: IOException) {
      LOG.debug("HEAD request to {} failed", url, e)
    }

    return null
  }

  companion object {
    private val LOG = LoggerFactory.getLogger(HttpUrlAdapter::class.java)
  }
}

interface RepositoryLayout {
  fun getArtifactUrl(repositoryBaseUrl: HttpUrl, module: ModuleVersionIdentifier): HttpUrl
}

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
