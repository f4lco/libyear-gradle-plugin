package com.libyear.adapters

import org.gradle.api.artifacts.ModuleVersionIdentifier
import java.time.Instant

interface VersionInfoAdapter {

  fun getArtifactCreated(m: ModuleVersionIdentifier): Instant?
}
