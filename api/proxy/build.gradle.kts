val mavenGroup: String by rootProject
group = "$mavenGroup.api"

val javadocProjects = listOf(
    project(":api:proxy"),
    project(":api:common"),
    project(":api:server-common"),
    project(":protocol")
)

dependencies {
    api(project(":api:common"))
    api(project(":api:server-common"))

    api("su.plo.slib:api-proxy:${rootProject.libs.versions.crosslib.get()}")
}

tasks {
//    javadoc {
//        source(javadocProjects.map {
//            it.sourceSets.main.get().allJava
//        })
//
//        classpath = files(javadocProjects.map { it.sourceSets.main.get().compileClasspath })
//        setDestinationDir(file("${buildDir}/docs/javadoc"))
//    }
//
    sourcesJar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(javadocProjects.map {
            it.sourceSets.main.get().allSource
        })
    }
}
