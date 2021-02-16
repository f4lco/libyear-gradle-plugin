package com.libyear.sourcing

import org.gradle.api.Action
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.gradle.api.artifacts.repositories.RepositoryContentDescriptor
import org.gradle.api.internal.artifacts.DefaultModuleVersionIdentifier.newId
import java.lang.UnsupportedOperationException

internal object Fixtures {

  internal val apacheCommonsTextArtifact: ModuleVersionIdentifier = newId(
    "org.apache.commons",
    "commons-text",
    "1.9"
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
  private val name: String
) : ArtifactRepository {

  override fun getName() = name

  override fun setName(name: String) {
    throw UnsupportedOperationException()
  }

  override fun content(p0: Action<in RepositoryContentDescriptor>) {
    throw UnsupportedOperationException()
  }
}
