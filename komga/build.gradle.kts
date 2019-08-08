import com.palantir.gradle.docker.DockerExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  run {
    val kotlinVersion = "1.3.41"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
    kotlin("kapt") version kotlinVersion
  }
  id("org.springframework.boot") version "2.1.6.RELEASE"
  id("io.spring.dependency-management") version "1.0.8.RELEASE"
  id("com.github.ben-manes.versions") version "0.22.0"
  id("com.palantir.docker") version "0.22.1"
  jacoco
}

group = "org.gotson"
version = "0.1.0"

repositories {
  jcenter()
  mavenCentral()
  maven("https://jitpack.io")
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  implementation(kotlin("reflect"))
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.0-M2")

  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.springframework.boot:spring-boot-starter-security")

  kapt("org.springframework.boot:spring-boot-configuration-processor")

//  implementation("com.github.ben-manes.caffeine:caffeine:2.7.0")

  implementation("io.github.microutils:kotlin-logging:1.6.26")

  run {
    val springfoxVersion = "2.9.2"
    implementation("io.springfox:springfox-swagger2:$springfoxVersion")
    implementation("io.springfox:springfox-swagger-ui:$springfoxVersion")
  }

  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.9")

  implementation("com.github.klinq:klinq-jpaspec:0.8")

  runtimeOnly("com.h2database:h2:1.4.199")

  testImplementation("org.springframework.boot:spring-boot-starter-test") {
    exclude(module = "junit")
    exclude(module = "mockito-core")
  }
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("org.junit.jupiter:junit-jupiter:5.5.0")
  testImplementation("com.ninja-squad:springmockk:1.1.2")
  testImplementation("io.mockk:mockk:1.9.3")
}

tasks {
  withType<KotlinCompile> {
    kotlinOptions {
      jvmTarget = "1.8"
      freeCompilerArgs = listOf("-Xjsr305=strict")
    }
  }

  withType<Test> {
    useJUnitPlatform()
    systemProperty("spring.profiles.active", "test")
  }

  withType<ProcessResources> {
    filesMatching("application*.yml") {
      expand(project.properties)
    }
  }

  register<Copy>("unpack") {
    dependsOn(bootJar)
    from(zipTree(getByName("bootJar").outputs.files.singleFile))
    into("$buildDir/dependency")
  }
}

configure<DockerExtension> {
  name = "gotson/komga:latest"
  copySpec.from(tasks.getByName("unpack").outputs).into("dependency")
  buildArgs(mapOf("DEPENDENCY" to "dependency"))
  dependsOn(tasks.getByName("clean"), tasks.getByName("test"))
}
