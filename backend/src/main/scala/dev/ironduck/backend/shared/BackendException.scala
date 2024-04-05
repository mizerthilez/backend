package dev.ironduck.backend
package shared

import io.circe.derivation.*

object BackendException:
  given Configuration = Configuration.default.withDiscriminator("type")

enum BackendException extends Exception derives ConfiguredCodec:
  case BadRequestException(message: String)
  case ServerInternalErrorException(message: String)
