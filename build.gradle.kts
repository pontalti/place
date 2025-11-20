plugins {
    java
    id("io.quarkus") version "3.29.3"

    // Integrações IDE
    id("idea")
    id("eclipse")
}

group = "com.demo.place"
version = "0.0.1-SNAPSHOT"
description = "Place Coding Challenge (Quarkus)"

repositories {
    mavenCentral()
    mavenLocal()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    // --- Alinha todas as versões de extensões Quarkus ---
        implementation(enforcedPlatform(
        "${property("quarkusPlatformGroupId")}:${property("quarkusPlatformArtifactId")}:${property("quarkusPlatformVersion")}"
    ))
    testImplementation(enforcedPlatform(
        "${property("quarkusPlatformGroupId")}:${property("quarkusPlatformArtifactId")}:${property("quarkusPlatformVersion")}"
    ))

    // --- Núcleo Quarkus / Web / JPA ---
    implementation("io.quarkus:quarkus-config-yaml")
    implementation("io.quarkus:quarkus-arc")               // CDI
    implementation("io.quarkus:quarkus-rest")              // Novo REST (substitui quarkus-resteasy-reactive)
    implementation("io.quarkus:quarkus-rest-jackson")      // JSON via Jackson

    implementation("io.quarkus:quarkus-hibernate-orm")
    implementation("io.quarkus:quarkus-hibernate-validator")
    implementation("io.quarkus:quarkus-hibernate-orm-panache") // opcional
    implementation("io.quarkus:quarkus-jdbc-h2")
    implementation("io.quarkus:quarkus-flyway")
   
	implementation("io.quarkus:quarkus-vertx")
	implementation("io.quarkus:quarkus-undertow")
   
    implementation("io.quarkus:quarkus-container-image-docker")

    // --- Observabilidade ---
    implementation("io.quarkus:quarkus-smallrye-health")
    implementation("io.quarkus:quarkus-micrometer-registry-prometheus")

    // --- OpenAPI / Swagger ---
    implementation("io.quarkus:quarkus-smallrye-openapi")

    // --- MapStruct ---
    implementation("org.mapstruct:mapstruct:${property("mapstructVersion")}")
    annotationProcessor("org.mapstruct:mapstruct-processor:${property("mapstructVersion")}")
    testAnnotationProcessor("org.mapstruct:mapstruct-processor:${property("mapstructVersion")}")

    // --- Lombok (opcional) ---
    compileOnly("org.projectlombok:lombok:${property("lombokVersion")}")
    annotationProcessor("org.projectlombok:lombok:${property("lombokVersion")}")
    testCompileOnly("org.projectlombok:lombok:${property("lombokVersion")}")
    testAnnotationProcessor("org.projectlombok:lombok:${property("lombokVersion")}")

    // --- Testes ---
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("org.assertj:assertj-core:3.26.0")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
    testLogging {
        events("PASSED", "FAILED", "SKIPPED")
        showStandardStreams = true
    }
}

tasks.jar {
    archiveBaseName.set("place")
    archiveVersion.set("")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Amapstruct.defaultComponentModel=cdi")
    options.compilerArgs.add("-parameters")
}

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
