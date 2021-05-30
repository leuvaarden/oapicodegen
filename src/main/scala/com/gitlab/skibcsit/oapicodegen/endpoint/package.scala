package com.gitlab.skibcsit
package oapicodegen

import io.swagger.v3.oas.models._
import io.swagger.v3.oas.models.media._
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.responses.ApiResponse

import scala.jdk.CollectionConverters._
import scala.util.matching.Regex

package object endpoint {
  private final val SpacePattern: String = "\\s+"
  private final val PathSeparatorPattern: String = "/"
  private final val QueryParamRegex: Regex = "\\{(\\w+)}".r

  /** Retrieves endpoints data from openAPI object */
  def getEndpoints(openAPI: OpenAPI): Iterable[(String, PathItem.HttpMethod, Operation)] = openAPI.getPaths
    .entrySet().asScala
    .map(uriAndItem => (uriAndItem.getKey, uriAndItem.getValue.readOperationsMap().asScala))
    .flatMap(uriAndMap => uriAndMap._2.map(methodAndContent => (uriAndMap._1, methodAndContent._1, methodAndContent._2)))

  /** Removes spaces, converts to upper camel case, concatenates */
  def toTraitName(s: String): String = s.split(SpacePattern)
    .filter((str: String) => str.nonEmpty)
    .map((str: String) => str.head.toUpper + str.tail.toLowerCase)
    .mkString

  /** Convertes path to iterable of either Left(path segment) Right(path param) */
  def parsePath(path: String): Iterable[Either[String, String]] =
    path.split(PathSeparatorPattern)
      .filter((str: String) => str.nonEmpty)
      .map((str: String) => QueryParamRegex.findFirstMatchIn(str) match {
        case Some(value) => Right(value.group(1))
        case None => Left(str)
      })

  /** Finds and resolves type of param */
  def resolveSegmentType(name: String, params: Iterable[Parameter]): String =
    params.filter((parameter: Parameter) => parameter.getName.equals(name))
      .map((parameter: Parameter) => parameter.getSchema)
      .map((schema: Schema[_]) => resolveType(schema))
      .head

  /** True if there are query params */
  def containsQueryParams(params: Iterable[Parameter]): Boolean =
    params != null && params.nonEmpty && params.count((parameter: Parameter) => "query".equals(parameter.getIn)) > 0

  /** Extracts responses from endpoint */
  def getResponses(operation: Operation): Iterable[(String, ApiResponse)] =
    if (operation.getResponses == null) List(("200", new ApiResponse()))
    else operation.getResponses
      .keySet().asScala
      .map(key => if (key.equals("default")) "200" else key)
      .filter(key => key.forall((c: Char) => c.isDigit))
      .toList
      .distinct
      .sorted
      .map(key => (key, operation.getResponses.getOrDefault(key, operation.getResponses.get("default"))))

  // TODO support more schemes (e.g. ComposedSchema)
  // TODO support non-jvm platform
  // TODO support required flag

  /** Converts openAPI schema to Scala type */
  def resolveType[T](schema: Schema[T]): String = schema match {
    // primitive types
    case _: BooleanSchema => "Boolean"
    case schema: IntegerSchema => schema.getFormat match {
      case "int32" => "Int"
      case "int64" => "Long"
    }
    case schema: NumberSchema => schema.getFormat match {
      case "float" => "Float"
      case "double" => "Double"
    }
    case _: StringSchema => "String"

    // array types
    case _: ByteArraySchema => "Iterable[Byte]"
    case _: FileSchema => "Iterable[Byte]"
    case schema: ArraySchema => "Iterable[" + resolveType(schema.getItems) + "]"

    // platform specific types
    case schema: DateTimeSchema => schema.getType match {
      case "date" => "java.time.LocalDate"
      case "time" => "java.time.OffsetTime"
      case _ => "java.time.OffsetDateTime"
    }
    case _: UUIDSchema => "java.util.UUID"

    // class
    case schema: ObjectSchema => schema.getType

    case schema =>
      // reference to another schema => ref suffix
      if (schema.get$ref() != null) {
        return schema.get$ref().split('/').last
      }

      // inline schema definition => tuple
      if (schema.getProperties != null) {
        return "(" + schema.getProperties.values().asScala.map((schema: Schema[_]) => resolveType(schema)).mkString(", ") + ")"
      }

      // defaults to String
      "String"
  }
}