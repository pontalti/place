plugins {
    java
    id("io.quarkus")
    id("idea")
    id("eclipse")
    id ("com.diffplug.eclipse.apt") version "4.3.0"
}

group = "com.demo.place"
version = "0.0.1-SNAPSHOT"
description = "Place Coding Challenge (Quarkus)"

repositories {
    mavenCentral()
    mavenLocal()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project
val mapstructVersion: String by project
val lombokVersion: String by project

dependencies {
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-rest")
	implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-rest-jackson")
    implementation("io.quarkus:quarkus-config-yaml")

    implementation("io.quarkus:quarkus-hibernate-orm")
    implementation("io.quarkus:quarkus-hibernate-validator")
    implementation("io.quarkus:quarkus-hibernate-orm-panache")
    implementation("io.quarkus:quarkus-jdbc-h2")
    implementation("io.quarkus:quarkus-flyway")
   
	implementation("io.quarkus:quarkus-vertx")
	implementation("io.quarkus:quarkus-undertow")
   
    implementation("io.quarkus:quarkus-container-image-docker")

    implementation("io.quarkus:quarkus-smallrye-health")
    implementation("io.quarkus:quarkus-micrometer-registry-prometheus")

    implementation("io.quarkus:quarkus-smallrye-openapi")
	
	compileOnly("org.projectlombok:lombok:${property("lombokVersion")}")
    annotationProcessor("org.projectlombok:lombok:${property("lombokVersion")}")
    testCompileOnly("org.projectlombok:lombok:${property("lombokVersion")}")
    testAnnotationProcessor("org.projectlombok:lombok:${property("lombokVersion")}")

    implementation("org.mapstruct:mapstruct:${property("mapstructVersion")}")
    annotationProcessor("org.mapstruct:mapstruct-processor:${property("mapstructVersion")}")
    testAnnotationProcessor("org.mapstruct:mapstruct-processor:${property("mapstructVersion")}")

    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
	testImplementation("org.assertj:assertj-core:3.27.6")
	testImplementation(enforcedPlatform("${property("quarkusPlatformGroupId")}:${property("quarkusPlatformArtifactId")}:${property("quarkusPlatformVersion")}"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks.jar {
    archiveBaseName.set("place")
    archiveVersion.set("")
}

tasks.withType<Test>().configureEach {
	useJUnitPlatform()
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
    jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED")
	testLogging {
        events("PASSED", "FAILED", "SKIPPED")
        showStandardStreams = true
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
	options.compilerArgs.add("-Amapstruct.defaultComponentModel=jakarta-cdi")
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