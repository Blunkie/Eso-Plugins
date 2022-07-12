import ProjectVersions.unethicaliteVersion

version = "0.0.1"

project.extra["PluginName"] = "Automation API"
project.extra["PluginDescription"] = "API for creating automated plugins"

dependencies {
    compileOnly("net.unethicalite:runelite-api:$unethicaliteVersion+")
    compileOnly("net.unethicalite:runelite-client:$unethicaliteVersion+")
    compileOnly(group = "net.unethicalite.rs", name = "runescape-api", version = unethicaliteVersion)
    compileOnly(group = "net.unethicalite", name = "injection-annotations", version = "1.0.2-SNAPSHOT")
    compileOnly(group = "org.apache.commons", name = "commons-lang3", version = "3.11")
}

tasks {
    jar {
        manifest {
            attributes(mapOf(
                "Plugin-Version" to project.version,
                "Plugin-Id" to nameToId(project.extra["PluginName"] as String),
                "Plugin-Provider" to project.extra["PluginProvider"],
                "Plugin-Description" to project.extra["PluginDescription"],
                "Plugin-License" to project.extra["PluginLicense"]
            ))
        }
    }
}
