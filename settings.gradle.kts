rootProject.name = "unethicalite-plugins"

include("packetutils")
include("prayerflicker")
include("automationapi")
include("autovorkath")
include("autovorkathprayer")
include("eso-pestcontrol")
include("eso-magic")
include("eso-nmz")

for (project in rootProject.children) {
    project.apply {
        projectDir = file(name)
        buildFileName = "$name.gradle.kts"

        require(projectDir.isDirectory) { "Project '${project.path} must have a $projectDir directory" }
        require(buildFile.isFile) { "Project '${project.path} must have a $buildFile build script" }
    }
}
