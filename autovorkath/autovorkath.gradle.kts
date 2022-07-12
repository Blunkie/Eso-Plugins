import ProjectVersions.unethicaliteVersion

version = "0.0.1"

project.extra["PluginName"] = "Auto Vorkath"
project.extra["PluginDescription"] = "Auto vorkath"

dependencies {
    compileOnly("net.unethicalite:runelite-api:$unethicaliteVersion+")
    compileOnly("net.unethicalite:runelite-client:$unethicaliteVersion+")
    compileOnly(project(":automationapi"))
}

tasks {
    jar {
        manifest {
            attributes(mapOf(
                    "Plugin-Version" to project.version,
                    "Plugin-Id" to nameToId(project.extra["PluginName"] as String),
                    "Plugin-Provider" to project.extra["PluginProvider"],
                    "Plugin-Dependencies" to arrayOf(nameToId("automationapi")).joinToString(),
                    "Plugin-Description" to project.extra["PluginDescription"],
                    "Plugin-License" to project.extra["PluginLicense"]
            ))
        }
    }
}
