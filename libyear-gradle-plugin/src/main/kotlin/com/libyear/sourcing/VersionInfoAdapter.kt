package com.libyear.sourcing

import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.repositories.ArtifactRepository
import java.time.Instant

interface VersionInfoAdapter {

  fun getArtifactCreated(m: ModuleVersionIdentifier, repository: ArtifactRepository): Instant?
}
