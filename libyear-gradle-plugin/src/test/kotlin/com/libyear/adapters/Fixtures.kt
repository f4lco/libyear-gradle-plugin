package com.libyear.adapters

import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.internal.artifacts.DefaultModuleVersionIdentifier.newId

internal object Fixtures {

  internal val apacheCommonsTextArtifact: ModuleVersionIdentifier = newId(
    "org.apache.commons",
    "commons-text",
    "1.9",
  )

  internal val apacheCommonsCollectionsArtifact: ModuleVersionIdentifier = newId(
    "org.apache.commons",
    "commons-collections4",
    "4.4",
  )

  internal val notExistingArtifact: ModuleVersionIdentifier = newId(
    "org.apache.commons",
    "commons-text-not-existing",
    "1.9",
  )
}
