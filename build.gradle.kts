// Made by vProLabs [www.vprolabs.xyz]

plugins {
    java
}

group = "pl.vprolabs"
version = "1.0.0"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    javadoc {
        options.encoding = "UTF-8"
    }

    processResources {
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(
                "version" to project.version,
                "name" to "vSafemobs",
                "group" to project.group
            )
        }
    }

    jar {
        archiveFileName.set("vSafemobs-${project.version}.jar")
    }
}
