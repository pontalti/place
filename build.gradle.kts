plugins {
    java
    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.4"

    // IDE integrations
    id("idea")
    id("eclipse")
}

group = "com.demo.place"
version = "0.0.1-SNAPSHOT"
description = "Place Coding Challenge"

repositories {
    mavenCentral()
}

extra["springdocOpenapiVersion"] = "2.8.8"
extra["mapstructVersion"] = "1.6.3"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-aop")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${property("springdocOpenapiVersion")}")
    implementation("org.mapstruct:mapstruct:${property("mapstructVersion")}")

    runtimeOnly("com.h2database:h2")
    
    implementation("org.flywaydb:flyway-core")

    // Lombok (optional)
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    // MapStruct processors
    annotationProcessor("org.mapstruct:mapstruct-processor:${property("mapstructVersion")}")
    testAnnotationProcessor("org.mapstruct:mapstruct-processor:${property("mapstructVersion")}")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Produce plain artifact names
tasks.jar {
    archiveBaseName.set("place")
    archiveVersion.set("")
}
tasks.bootJar {
    archiveBaseName.set("place")
    archiveVersion.set("")
}

// Force MapStruct to generate Spring components (@Mapper(componentModel = "spring"))
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Amapstruct.defaultComponentModel=spring")
}

/**
 * Make generated annotation-processor sources visible to IDEs.
 * Gradle puts them under:
 *   build/generated/sources/annotationProcessor/java/{main|test}
 * We mark them in IDEA and add them to Eclipse classpath.
 */
idea {
    module {
        generatedSourceDirs.add(file("build/generated/sources/annotationProcessor/java/main"))
        generatedSourceDirs.add(file("build/generated/sources/annotationProcessor/java/test"))
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

eclipse {
    classpath {
        // Add generated folders to Eclipse classpath (no private flags used)
        file.whenMerged {
            val cp = this as org.gradle.plugins.ide.eclipse.model.Classpath
            cp.entries.add(
                org.gradle.plugins.ide.eclipse.model.SourceFolder(
                    "build/generated/sources/annotationProcessor/java/main",
                    null
                )
            )
            cp.entries.add(
                org.gradle.plugins.ide.eclipse.model.SourceFolder(
                    "build/generated/sources/annotationProcessor/java/test",
                    null
                )
            )
        }
    }
}
