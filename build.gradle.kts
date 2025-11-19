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
    mavenLocal() // opcional, mas recomendado pelo Quarkus
}

extra["mapstructVersion"] = "1.6.3"
extra["lombokVersion"] = "1.18.34"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    // --- Alinha todas as versões de extensões Quarkus ---
    implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:3.29.3"))

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

tasks.withType<Test> {
    useJUnitPlatform()
    // recomendado pelo Quarkus para logging correto nos testes
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}

// Nome do jar (não é obrigatório, mas se quiser manter)
tasks.jar {
    archiveBaseName.set("place2")
    archiveVersion.set("")
}

// MapStruct usando CDI (equivalente ao "spring" em Spring Boot)
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Amapstruct.defaultComponentModel=cdi")
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
