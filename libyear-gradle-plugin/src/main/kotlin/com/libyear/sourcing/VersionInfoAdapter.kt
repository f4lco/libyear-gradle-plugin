package com.libyear.sourcing

import io.vavr.control.Try
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.repositories.ArtifactRepository
import java.time.Instant

interface VersionInfoAdapter {

  fun getArtifactCreated(m: ModuleVersionIdentifier, repository: ArtifactRepository): Try<Instant>
}
