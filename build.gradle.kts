plugins {
    kotlin("jvm") version "2.4.0"
    kotlin("plugin.serialization") version "2.4.0"
    `java-library`
    `maven-publish`
    signing
}

kotlin {
    explicitApi()
}

group = "io.github.jeff-gillot"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    // Standard kotlinx serialization JSON library
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")

    // Kotlin test utilities
    testImplementation(kotlin("test"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
    withJavadocJar()
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    testLogging {
        events("failed")
        showExceptions = true
        showStackTraces = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = group.toString()
            artifactId = "kojsonpath"
            version = project.version.toString()

            from(components["java"])

            pom {
                name = "KoJsonPath"
                description = "Lightweight extension functions to navigate complex JsonObject structures from kotlinx.serialization using JSON Path shortcuts."
                url = "https://github.com/Jeff-Gillot/kojsonpath"

                licenses {
                    license {
                        name = "MIT License"
                        url = "https://opensource.org/licenses/MIT"
                    }
                }

                developers {
                    developer {
                        id = "Jeff-Gillot"
                        name = "Jean-Francois Gillot"
                    }
                }

                scm {
                    url = "https://github.com/Jeff-Gillot/kojsonpath"
                    connection = "scm:git:git://github.com/Jeff-Gillot/kojsonpath.git"
                    developerConnection = "scm:git:ssh://github.com/Jeff-Gillot/kojsonpath.git"
                }
            }
        }
    }

    repositories {
        maven {
            name = "Local"
            url = uri(layout.buildDirectory.dir("staging-deploy"))
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["maven"])
}
