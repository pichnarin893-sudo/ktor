plugins {
    kotlin("jvm") version "1.9.22" apply false
    id("io.ktor.plugin") version "2.3.7" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22" apply false
}

group = "com.natjoub"
version = "1.0.0"

allprojects {
    repositories {
        mavenCentral()
    }
}

// subprojects {
//     apply(plugin = "org.jetbrains.kotlin.jvm")

//     tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
//         kotlinOptions.jvmTarget = "17"
//     }
// // }

// subprojects {

//     apply(plugin = "org.jetbrains.kotlin.jvm")

//     java {
//         toolchain {
//             languageVersion.set(JavaLanguageVersion.of(17))
//         }
//     }

//     kotlin {
//         jvmToolchain(17)
//     }
// }

subprojects {

    plugins.withId("org.jetbrains.kotlin.jvm") {

        java {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(17))
            }
        }

        extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
            jvmToolchain(17)
        }
    }
}

