package com.gitlab.skibcsit
package oapi2e4s

import io.swagger.parser.OpenAPIParser
import io.swagger.v3.oas.models.OpenAPI

package object openapi {
  // TODO handle exceptions
  def parse(location: String): OpenAPI =
    new OpenAPIParser()
      .readLocation(location, null, null)
      .getOpenAPI
}
