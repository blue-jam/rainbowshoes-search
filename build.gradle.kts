import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.5.4"
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
		exclude("org.apache.logging.log4j", "log4j-to-slf4j")
		exclude("ch.qos.logback", "logback-classic")
	}
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	implementation("org.apache.lucene:lucene-core:8.2.0")
	implementation("org.apache.lucene:lucene-queryparser:8.2.0")
	implementation("org.codelibs:lucene-analyzers-kuromoji-ipadic-neologd:8.2.0-20200120")

	implementation("com.norconex.collectors:norconex-collector-http:2.9.0") {
		exclude("edu.ucar", "jj2000")
	}
	implementation("edu.ucar:jj2000:5.2")

	// Temporary fix for log4j vulnerability https://www.lunasec.io/docs/blog/log4j-zero-day/
	implementation("org.apache.logging.log4j:log4j-api:2.15.0")
	implementation("org.apache.logging.log4j:log4j-core:2.15.0")

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
