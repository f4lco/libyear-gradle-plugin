
tasks.register("check") {
    dependsOn(gradle.includedBuild("libyear-gradle-plugin").task(":functionalTest"))
}