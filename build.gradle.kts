import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.6.8"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.5.21"
	kotlin("plugin.spring") version "1.5.21"

	id("org.jmailen.kotlinter") version "3.2.0"
}

group = "moe.rainbowshoes"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
	maven {
		url = uri("https://maven.codelibs.org/")
	}
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web") {
		exclude("ch.qos.logback", "logback-classic")
		exclude("org.apache.logging.log4j", "log4j-to-slf4j")
	}
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	implementation("org.apache.lucene:lucene-core:8.2.0")
	implementation("org.apache.lucene:lucene-queryparser:8.2.0")
	implementation("org.codelibs:lucene-analyzers-kuromoji-ipadic-neologd:8.2.0-20200120")

	implementation("com.norconex.collectors:norconex-collector-http:3.0.0") {
		exclude("com.h2database", "h2")
		exclude("com.github.junrar", "junrar")
		exclude("com.google.guava", "guava")
		exclude("com.google.protobuf", "protobuf-java")
		exclude("com.opencsv", "opencsv")
		exclude("io.netty", "netty-all")
		exclude("org.apache.commons", "commons-compress")
		exclude("org.apache.poi", "poi")
		exclude("org.apache.poi", "poi-scratchpad")
	}
	implementation("com.h2database:h2:2.1.212")
	implementation("edu.ucar:jj2000:5.2")
	implementation("org.jsoup:jsoup:1.14.3")
	implementation("com.google.guava:guava:30.1.1-jre")
	implementation("org.apache.commons:commons-compress:1.21")

	developmentOnly("org.springframework.boot:spring-boot-devtools")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
