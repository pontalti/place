plugins {
    java
    id("io.quarkus")
    id("idea")
    id("eclipse")
    id ("com.diffplug.eclipse.apt") version "4.4.1"
}

group = "com.demo.place"
version = "0.0.1-SNAPSHOT"
description = "Place Coding Challenge (Quarkus)"

repositories {
    mavenCentral()
    mavenLocal()
}

val quarkusPlatformGroupId = providers.gradleProperty("quarkusPlatformGroupId").get()
val quarkusPlatformArtifactId = providers.gradleProperty("quarkusPlatformArtifactId").get()
val quarkusPlatformVersion = providers.gradleProperty("quarkusPlatformVersion").get()
val mapstructVersion = providers.gradleProperty("mapstructVersion").get()
val lombokVersion = providers.gradleProperty("lombokVersion").get()

dependencies {
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation("io.quarkus:quarkus-arc")
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
	
	compileOnly("org.projectlombok:lombok:${lombokVersion}")
    annotationProcessor("org.projectlombok:lombok:${lombokVersion}")
    testCompileOnly("org.projectlombok:lombok:${lombokVersion}")
    testAnnotationProcessor("org.projectlombok:lombok:${lombokVersion}")

    implementation("org.mapstruct:mapstruct:${mapstructVersion}")
    annotationProcessor("org.mapstruct:mapstruct-processor:${mapstructVersion}")
    testAnnotationProcessor("org.mapstruct:mapstruct-processor:${mapstructVersion}")
    
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")
	testAnnotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
	testImplementation("org.assertj:assertj-core:3.27.6")
	testImplementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(27))
    }
    sourceCompatibility = JavaVersion.VERSION_27
    targetCompatibility = JavaVersion.VERSION_27
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