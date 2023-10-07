plugins {
    `java-library`
    `maven-publish`
    id("com.palantir.git-version") version "0.15.0"
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }

    maven {
        url = uri("https://maven.pkg.github.com/patexoid/repo")
    }
}

val generated_dir = "build/generated/main/java"

java.sourceSets["main"].java {
    srcDir("$generated_dir")
    srcDir("src/main/java")
}

val jaxb by configurations.creating

dependencies {
    jaxb("org.glassfish.jaxb:jaxb-xjc:4.0.1")
    jaxb("org.glassfish.jaxb:jaxb-runtime:4.0.1")
    api("jakarta.xml.bind:jakarta.xml.bind-api:4.0.1")
    api("org.apache.commons:commons-lang3:3.9")
}

tasks.register<JavaExec>("generateFb2FromXsd") {
    file("$generated_dir").mkdirs()
    classpath = configurations["jaxb"]
    mainClass = "com.sun.tools.xjc.XJCFacade"
    args = listOf("src/main/resources/xsd/fb2/", "-b", "src/main/resources/xsd/bindings.xjb", "-p", "fb2", "-d", "$generated_dir", "-no-header")
}


tasks.withType<JavaCompile> {
    dependsOn("generateFb2FromXsd")
}


val versionDetails: groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails> by extra
val details = versionDetails()
group = "com.patex"
version =
        if (details.commitDistance == 0) details.lastTag else (details.lastTag + "-" + details.commitDistance + "-" + details.gitHash)
description = "fb2-java"
java.sourceCompatibility = JavaVersion.VERSION_1_8

println(version)

java {
    withSourcesJar()
    withJavadocJar()
}

if (details.commitDistance == 0) {
    publishing {
        repositories {
            maven {
                name = "github"
                url = uri("https://maven.pkg.github.com/patexoid/repo")
                credentials {
                    username = System.getenv("USERNAME")
                    password = System.getenv("TOKEN")
                }
            }
        }
        publications.create<MavenPublication>("github") {
            from(components["java"])
        }
    }
}
tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"

}


tasks.withType<Jar>() {
    duplicatesStrategy=DuplicatesStrategy.WARN

}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
    isFailOnError = false;
}
