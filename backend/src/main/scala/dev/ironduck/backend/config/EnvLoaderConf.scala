package dev.ironduck.backend
package config

object EnvLoaderConf:
  private val allEnvVar: Map[String, String] = sys.env

  val backendPort: String = allEnvVar.getOrElse("BACKEND_PORT", default = "8080")
