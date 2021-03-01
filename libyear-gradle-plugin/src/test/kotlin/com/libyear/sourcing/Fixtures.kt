package com.libyear.sourcing

import okhttp3.HttpUrl.Companion.toHttpUrl
import org.gradle.api.Action
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.gradle.api.artifacts.repositories.RepositoryContentDescriptor
import org.gradle.api.artifacts.repositories.UrlArtifactRepository
import org.gradle.api.internal.artifacts.DefaultModuleVersionIdentifier.newId
import java.net.URI

internal object Fixtures {

  internal val apacheCommonsTextArtifact: ModuleVersionIdentifier = newId(
    "org.apache.commons",
    "commons-text",
    "1.8"
  )

  internal val apacheCommonsCollectionsArtifact: ModuleVersionIdentifier = newId(
    "org.apache.commons",
    "commons-collections4",
    "4.4"
  )

  internal val notExistingArtifact: ModuleVersionIdentifier = newId(
    "org.apache.commons",
    "commons-text-not-existing",
    "1.9"
  )

  internal val stubRepository = StubRepository("stubRepo1")
}

internal class StubRepository(
  private val name: String,
  private val url: String = ""
) : ArtifactRepository, UrlArtifactRepository {

  override fun getName() = name

  override fun setName(name: String) {
    throw UnsupportedOperationException()
  }

  override fun content(p0: Action<in RepositoryContentDescriptor>) {
    throw UnsupportedOperationException()
  }

  override fun getUrl() = url.toHttpUrl().toUri()

  override fun setUrl(url: URI) {
    throw UnsupportedOperationException()
  }

  override fun setUrl(url: Any) {
    throw UnsupportedOperationException()
  }

  override fun isAllowInsecureProtocol() = false

  override fun setAllowInsecureProtocol(allowInsecureProtocol: Boolean) {
    throw UnsupportedOperationException()
  }
}
