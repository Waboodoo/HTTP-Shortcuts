package buildSrc

import org.gradle.api.Project
import java.util.Properties

object LocalProperties {
    private lateinit var properties: Properties

    fun init(project: Project) {
        properties = Properties()
        val propertiesFile = project.file("local.properties")
        if (propertiesFile.exists()) {
            properties.load(propertiesFile.inputStream())
        }
    }

    fun getString(key: String): String? = properties.getProperty(key)

    fun getBoolean(key: String): Boolean? = getString(key)?.toBoolean()
}
