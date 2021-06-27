package com.gitlab.skibcsit
package oapicodegen

import io.swagger.parser.OpenAPIParser
import io.swagger.v3.oas.models.OpenAPI
import org.slf4j.{Logger, LoggerFactory}

package object openapi {
  private final val logger: Logger = LoggerFactory.getLogger("openapi")
  private final val Parser: OpenAPIParser = new OpenAPIParser

  def parse(location: String): OpenAPI = {
    val parseResult = Parser.readLocation(location, null, null)
    if (parseResult.getOpenAPI == null) {
      parseResult.getMessages.forEach(message => logger.debug("Received: [{}]", message))
      throw new RuntimeException(if (parseResult.getMessages.isEmpty) "Could not parse openAPI" else parseResult.getMessages.get(0))
    }
    parseResult.getOpenAPI
  }
}
