import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.71"
    id("idea")
    id("java")
}

group = "com.duytsev"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.arrow-kt:arrow-core:0.10.4")
    implementation("io.arrow-kt:arrow-syntax:0.10.4")
    implementation("io.arrow-kt:arrow-fx:0.10.4")
    implementation("io.github.resilience4j:resilience4j-all:1.3.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit") {
        exclude(group = "junit", module = "junit")
    }
    testImplementation("io.mockk:mockk:1.9.3")
    testImplementation("io.strikt:strikt-core:0.24.0")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.2")
}

tasks {
    test {
        useJUnitPlatform()
    }

    withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "11"
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }
}
