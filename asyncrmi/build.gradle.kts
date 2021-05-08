import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://maven.java.net/content/repositories/snapshots/")
    }

    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    implementation("io.netty:netty-all:4.1.63.Final")
    implementation("org.javassist:javassist:3.18.0-GA")
    implementation("org.slf4j:slf4j-api:1.7.25")
    implementation("org.slf4j:slf4j-site:1.7.25")
    implementation("org.yaml:snakeyaml:1.15")
    testImplementation("org.slf4j:jul-to-slf4j:1.7.25")
    testImplementation("org.apache.logging.log4j:log4j-slf4j-impl:2.14.1")
    testImplementation("org.apache.logging.log4j:log4j-api:2.14.1")
    testImplementation("org.apache.logging.log4j:log4j-core:2.14.1")
    testImplementation("junit:junit:4.13.2")
}

group = "com.github.barakb"
version = "1.0.4"
description = "rmi"
java.sourceCompatibility = JavaVersion.VERSION_1_8

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.withType<Test>()  {
//    maxParallelForks = 1
    setForkEvery(1L)
}

tasks {
    named<ShadowJar>("shadowJar") {
        minimize()
    }
    build {
        dependsOn("shadowJar")
    }
}
