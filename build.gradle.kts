import com.novoda.gradle.release.PublishExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {

    repositories {
        mavenCentral()
        jcenter()
    }

    dependencies {
        classpath("com.novoda:bintray-release:0.9.2")
    }
}

plugins {
    kotlin("jvm") version "1.3.72"
    id("idea")
    id("java")
    id("jacoco")
}

apply(plugin = "com.novoda.bintray-release")


group = "com.duytsev"
version = "0.0.1"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.arrow-kt:arrow-core:0.10.5")
    implementation("io.arrow-kt:arrow-syntax:0.10.5")
    implementation("io.arrow-kt:arrow-fx:0.10.5")
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

configure<PublishExtension> {
    userOrg = "duytsev"
    repoName = "resilience4j-arrowkt"
    groupId = "com.duytsev"
    artifactId = "resilience4j-arrowkt"
    publishVersion = version.toString()
    desc = "Arrow data type support for Resilience4j"
    setLicences("MIT")
    website = "https://github.com/duytsev/resilience4j-arrowkt"
}

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = true
        html.isEnabled = true
    }
}
