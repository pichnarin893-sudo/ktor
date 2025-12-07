plugins {
    kotlin("jvm") version "1.9.22" apply false
    id("io.ktor.plugin") version "3.3.3" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22" apply false
}

group = "com.natjoub"
version = "1.0.0"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
}
