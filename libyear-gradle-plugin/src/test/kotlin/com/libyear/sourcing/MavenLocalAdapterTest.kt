package com.libyear.sourcing

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MavenLocalAdapterTest {

  private lateinit var adapter: MavenLocalAdapter

  @BeforeEach
  fun setUp() {
    adapter = MavenLocalAdapter()
  }

  @Test
  fun testCreatedIsNull() {
    val created = adapter.getArtifactCreated(Fixtures.apacheCommonsTextArtifact, Fixtures.stubRepository)

    assertThat(created).isNull()
  }
}
