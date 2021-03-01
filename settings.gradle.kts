rootProject.name = "libyear-gradle"
include(
  ":example",
  ":example-groovy-dsl",
  ":example-spring-boot"
)
includeBuild("libyear-gradle-plugin")
