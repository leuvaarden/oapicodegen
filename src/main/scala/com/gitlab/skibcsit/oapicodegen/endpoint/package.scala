package com.gitlab.skibcsit
package oapicodegen

import io.swagger.v3.oas.models._
import io.swagger.v3.oas.models.media._
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.responses.{ApiResponse, ApiResponses}

import scala.collection.JavaConverters._
import scala.util.matching.Regex

package object endpoint {
  private final val SpaceString: String = " "
  private final val NotAlphanumericPattern: String = "[^\\w]"
  private final val SpacePattern: String = "\\s+"
  private final val PathSeparatorPattern: String = "/"
  private final val QueryParamRegex: Regex = "\\{(\\w+)}".r


  /** Retrieves endpoints data from openAPI object */
  def getEndpoints(openAPI: OpenAPI): Seq[(String, PathItem.HttpMethod, Operation)] =
    openAPI.getPaths
      .entrySet().asScala
      .map(uriAndItem => (uriAndItem.getKey, uriAndItem.getValue.readOperationsMap().asScala))
      .flatMap(uriAndMap => uriAndMap._2.map(methodAndContent => (uriAndMap._1, methodAndContent._1, methodAndContent._2)))
      .toSeq
      .sortBy(tuple => if (tuple._3.getOperationId == null) tuple._1 else tuple._3.getOperationId)

  /** Removes spaces, converts to upper camel case, concatenates */
  def toTraitName(s: String): String = s.replaceAll(NotAlphanumericPattern, SpaceString)
    .split(SpacePattern)
    .filter((str: String) => str.nonEmpty)
    .map((str: String) => str.head.toUpper + str.tail.toLowerCase)
    .mkString

  /** Converts path to iterable of either Left(path segment) Right(path param) */
  def parsePath(path: String): Seq[Either[String, String]] =
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
      .map((schema: Schema[_]) => resolveType(schema).getOrElse("String"))
      .head

  /** True if there are query params */
  def containsQueryParams(params: Iterable[Parameter]): Boolean =
    params != null && params.nonEmpty && params.count((parameter: Parameter) => "query".equals(parameter.getIn)) > 0

  /** Extracts responses from endpoint */
  def getResponses(operation: Operation): Seq[(String, ApiResponse)] =
    if (operation.getResponses == null)
      List(("200", new ApiResponse()))
    else if (!operation.getResponses.containsKey("200") && !operation.getResponses.containsKey("default"))
      ("200", new ApiResponse()) +: getResponsesSequence(operation.getResponses)
    else
      getResponsesSequence(operation.getResponses)

  def getResponsesSequence(apiResponses: ApiResponses): Seq[(String, ApiResponse)] =
    apiResponses.keySet().asScala
      .map(key => if (key.equals("default")) "200" else key)
      .filter(key => key.forall((c: Char) => c.isDigit))
      .toSeq
      .distinct
      .sorted
      .map(key => (key, apiResponses.getOrDefault(key, apiResponses.get("default"))))

  /** Converts openAPI schema to Scala type */
  def resolveType[T](schema: Schema[T]): Option[String] =
    schema match {

      // TODO support more schemes (e.g. ComposedSchema)
      // TODO support non-jvm platform
      // TODO support required flag

      // primitive types
      case _: BooleanSchema => Some("Boolean")
      case schema: IntegerSchema => schema.getFormat match {
        case "int32" => Some("Int")
        case "int64" => Some("Long")
      }
      case schema: NumberSchema => schema.getFormat match {
        case "float" => Some("Float")
        case "double" => Some("Double")
      }
      case _: StringSchema => Some("String")

      // array types
      case _: ByteArraySchema => Some("Iterable[Byte]")
      case _: BinarySchema => Some("Iterable[Byte]")
      case _: FileSchema => Some("Iterable[Byte]")
      case schema: ArraySchema => Some("Iterable[" + resolveType(schema.getItems).getOrElse("String") + "]")

      // platform specific types
      case schema: DateTimeSchema => schema.getType match {
        case "date" => Some("java.time.LocalDate")
        case "time" => Some("java.time.OffsetTime")
        case _ => Some("java.time.OffsetDateTime")
      }
      case _: UUIDSchema => Some("java.util.UUID")

      // class
      case schema: ObjectSchema => Some(schema.getType)

      case schema =>
        // reference to another schema => ref suffix
        if (schema.get$ref() != null) {
          return Some(schema.get$ref().split('/').last)
        }

        // inline schema definition => tuple
        if (schema.getProperties != null) {
          return Some("(" + schema.getProperties.values().asScala.map(schema => resolveType(schema).getOrElse("String")).mkString(", ") + ")")
        }

        None
    }

  def getSchemaProperties[T](schema: Schema[T]): Seq[(String, Schema[_])] =
    schema.getProperties.entrySet().asScala
      .map(entry => (entry.getKey, entry.getValue))
      .toSeq
      .sortBy(_._1)

  def getFirstContentSchema(content: Content): Option[Schema[_]] =
    if (content.isEmpty)
      None
    else
      Some(content.entrySet()
        .asScala
        .head
        .getValue
        .getSchema)
}