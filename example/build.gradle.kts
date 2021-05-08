// https://github.com/jnizet/gradle-kotlin-dsl-migration-guide
// https://docs.gradle.org/current/userguide/more_about_tasks.html#sec:task_outcomes
// https://docs.gradle.org/current/userguide/java_plugin.html
// https://docs.gradle.org/current/userguide/kotlin_dsl.html

plugins {
    java
    `maven-publish`
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    implementation(project(":asyncrmi"))
    implementation("org.slf4j:slf4j-api:1.7.25")
    implementation("org.slf4j:jul-to-slf4j:1.7.25")
    implementation("org.slf4j:slf4j-log4j12:1.7.25")
    implementation("log4j:log4j:1.2.17")
    testImplementation("junit:junit:4.11")
}

group = "com.github.barakb"
version = "1.0.3"
description = "rmi/Example"
java.sourceCompatibility = JavaVersion.VERSION_1_8

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.named<Jar>("jar"){
   dependsOn("dcl-server")
   dependsOn("dcl-client")
   dependsOn("ssl-server")
   dependsOn("ssl-client")
   dependsOn("file-client")
   dependsOn("file-server")
}

tasks {
    create<Jar>("dcl-server") {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        archiveFileName.set("dcl-server.jar")
        println("output ${sourceSets.main.get().output} ")
        from(sourceSets.main.get().output) {
            exclude("**/client/**")
            include("**/dcl/**")
        }
        manifest {
            attributes["Main-Class"] = "org.async.example.dcl.server.ServerImpl"
        }
    }
    register<Jar>("dcl-client") {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        archiveFileName.set("dcl-client.jar")
        from(sourceSets.main.get().output) {
            exclude("**/dcl/**")
            include("**/client/**")
        }
        manifest {
            attributes["Main-Class"] = "org.async.example.dcl.client.ClientImpl"
        }
    }

    register<Jar>("ssl-server") {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        archiveFileName.set("ssl-server.jar")
        from(sourceSets.main.get().output) {
            exclude("**/client/**")
            include("**/ssl/**")
        }
        manifest {
            attributes["Main-Class"] = "org.async.example.ssl.server.ServerImpl"
        }
    }
    register<Jar>("ssl-client") {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        archiveFileName.set("ssl-client.jar")
        from(sourceSets.main.get().output) {
            exclude("**/server/**")
            include("**/ssl/**")
        }
        manifest {
            attributes["Main-Class"] = "org.async.example.ssl.client.ClientImpl"
        }
    }

    register<Jar>("file-server") {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        archiveFileName.set("file-server.jar")
        from(sourceSets.main.get().output) {
            exclude("**/client/**")
            include("**/resultset/**")
        }
        manifest {
            attributes["Main-Class"] = "org.async.example.resultset.server.FileContentRetrieverServer"
        }
    }

    register<Jar>("file-client") {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        archiveFileName.set("file-client.jar")
        from(sourceSets.main.get().output) {
            exclude("**/server/**")
            include("**/resultset/**")
        }
        manifest {
            attributes["Main-Class"] = "org.async.example.resultset.client.ClientImpl"
        }
    }
}
