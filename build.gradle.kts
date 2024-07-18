plugins {
    id("fabric-loom") version "1.7.2"
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
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings("net.fabricmc:yarn:$yarnMappings:v2")
    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.100.7+1.21")

    modInclude("me.shedaniel.cloth:cloth-config-fabric:15.0.127")
    modImplementation("com.terraformersmc:modmenu:11.0.0")
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
