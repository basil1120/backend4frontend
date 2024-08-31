import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.1.0"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.7.22"
    kotlin("plugin.spring") version "1.7.22"
    kotlin("plugin.jpa") version "1.7.22"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.1.0")
    implementation("org.springframework.boot:spring-boot-starter-web:3.1.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.1")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.22")
    implementation("org.springframework.boot:spring-boot-starter-validation:3.1.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.22")
    runtimeOnly("org.postgresql:postgresql:42.7.1")
    runtimeOnly("com.h2database:h2:2.2.224")

    // Swagger / Open API
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0")

    // Unit Testing stuff
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.1.0")
    testImplementation("org.assertj:assertj-core:3.25.1")
    testImplementation("io.mockk:mockk:1.13.9")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
