plugins {
    id("fabric-loom") version "1.13.6"
    id("org.cadixdev.licenser") version "0.6.1"
}

val modVersion: String by project
val minecraftVersion: String by project
val yarnMappings: String by project
val loaderVersion: String by project

group = "dev.denwav"
version = modVersion

val modInclude: Configuration by configurations.creating {
    exclude(group = "net.fabricmc.fabric-api")
}
configurations.modApi {
    extendsFrom(modInclude)
}
configurations.include {
    extendsFrom(modInclude)
}

repositories {
    mavenCentral()
    maven("https://maven.terraformersmc.com/releases/")
    maven("https://maven.shedaniel.me/")
    maven("https://maven.nucleoid.xyz/") { name = "Nucleoid" }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings("net.fabricmc:yarn:$yarnMappings:v2")
    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.141.3+1.21.11")

    modInclude("me.shedaniel.cloth:cloth-config-fabric:17.0.144")
    modImplementation("com.terraformersmc:modmenu:17.0.0")
    modImplementation("eu.pb4:placeholder-api:2.8.2+1.21.10")
}

java {
    withSourcesJar()

    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

license {
    header(file("header.txt"))
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = 21
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.jar {
    from("license.txt")
}
