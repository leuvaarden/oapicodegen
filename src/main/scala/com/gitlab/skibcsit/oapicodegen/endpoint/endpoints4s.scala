package com.gitlab.skibcsit
package oapicodegen.endpoint

import com.gitlab.skibcsit.oapicodegen.lang.{Expr, LangAlg}
import io.swagger.v3.oas.models.PathItem.HttpMethod
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.{OpenAPI, Operation, PathItem}

import scala.jdk.CollectionConverters._

object endpoints4s {
  private final val TraitParents: Iterable[String] = List(
    "endpoints4s.algebra.Endpoints",
    "endpoints4s.algebra.circe.JsonEntitiesFromCodecs",
    "endpoints4s.algebra.StatusCodes",
  )
  private final val MethodPost: String = "Post"
  private final val MethodsMap: Map[HttpMethod, String] = Map(
    HttpMethod.GET -> "Get",
    HttpMethod.POST -> MethodPost,
    HttpMethod.PUT -> "Put",
    HttpMethod.DELETE -> "Delete",
    HttpMethod.PATCH -> "Patch",
    HttpMethod.OPTIONS -> "Options",
  )
  private final val StatusCodeOk: String = "OK"
  private final val StatusCodesMap: Map[String, String] = Map(
    "200" -> StatusCodeOk,
    "201" -> "Created",
    "202" -> "Accepted",
    "204" -> "NoContent",
    "400" -> "BadRequest",
    "401" -> "Unauthorized",
    "403" -> "Forbidden",
    "404" -> "NotFound",
    "413" -> "PayloadTooLarge",
    "429" -> "TooManyRequests",
    "500" -> "InternalServerError",
    "501" -> "NotImplemented",
  )

  /** Generates trait object with [[https://github.com/endpoints4s/endpoints4s endpoints4s]] definitions */
  def generate[LangTree, LangVal, LangType](`package`: String, openAPI: OpenAPI): Expr[LangTree, LangVal, LangType] =
    (langAlg: LangAlg[LangTree, LangVal, LangType]) => langAlg.langTrait(`package`, toTraitName(openAPI.getInfo.getTitle), TraitParents, getEndpoints(openAPI).map(tuple => endpoint(tuple._1, tuple._2, tuple._3)(langAlg)))

  // creates endpoint value definition
  private def endpoint[LangTree, LangVal, LangType](path: String, method: PathItem.HttpMethod, operation: Operation): Expr[LangTree, LangVal, LangType] =
    (langAlg: LangAlg[LangTree, LangVal, LangType]) => langAlg.langVal(operation.getOperationId, returnType(operation)(langAlg), rhs(path, method, operation)(langAlg))

  // creates return type definition for endpoints (currently not supported due to complexity)
  private def returnType[LangTree, LangVal, LangType](operation: Operation): LangAlg[LangTree, LangVal, LangType] => Option[LangType] =
    (langAlg: LangAlg[LangTree, LangVal, LangType]) => None

  // creates endpoint value
  private def rhs[LangTree, LangVal, LangType](path: String, method: HttpMethod, operation: Operation): Expr[LangTree, LangVal, LangType] =
    (langAlg: LangAlg[LangTree, LangVal, LangType]) => langAlg.langApply(langAlg.langRef("endpoint"), List(endpointRequest(path, method, operation)(langAlg), endpointResponse(operation)(langAlg)))

  // created endpoint request
  private def endpointRequest[LangTree, LangVal, LangType](path: String, method: HttpMethod, operation: Operation): Expr[LangTree, LangVal, LangType] =
    (langAlg: LangAlg[LangTree, LangVal, LangType]) => langAlg.langApply(langAlg.langRef("request"), List(resolveMethod(method)(langAlg), resolvePath(path, operation.getParameters.asScala)(langAlg), resolveContent(operation)(langAlg), resolveDescription(operation)(langAlg)))

  // creates request body if exists
  private def resolveContent[LangTree, LangVal, LangType](operation: Operation): Expr[LangTree, LangVal, LangType] =
    (langAlg: LangAlg[LangTree, LangVal, LangType]) =>
      if (operation.getRequestBody == null) langAlg.langRef("emptyRequest")
      else langAlg.langTypeApply(langAlg.langType("jsonRequest"), List(langAlg.langType(resolveType(operation.getRequestBody.getContent.entrySet().asScala.head.getValue.getSchema))))

  // creates endpoint description if exists
  private def resolveDescription[LangTree, LangVal, LangType](operation: Operation): Expr[LangTree, LangVal, LangType] =
    (langAlg: LangAlg[LangTree, LangVal, LangType]) =>
      if (operation.getDescription == null) langAlg.langRef("None")
      else langAlg.langApply(langAlg.langRef("Some"), List(langAlg.langLiteral(operation.getDescription)))

  // creates endpoint method value
  private def resolveMethod[LangTree, LangVal, LangType](httpMethod: HttpMethod): Expr[LangTree, LangVal, LangType] =
    (langAlg: LangAlg[LangTree, LangVal, LangType]) => langAlg.langRef(MethodsMap.getOrElse(httpMethod, MethodPost))

  // creates endpoint path
  private def resolvePath[LangTree, LangVal, LangType](path: String, params: Iterable[Parameter]): Expr[LangTree, LangVal, LangType] =
    (langAlg: LangAlg[LangTree, LangVal, LangType]) =>
      if (containsQueryParams(params)) langAlg.langApply(langAlg.langDot(resolveSegments(path, params)(langAlg), "/?"), List(resolveQuery(params)(langAlg)))
      else resolveSegments(path, params)(langAlg)

  // creates path params
  private def resolveSegments[LangType, LangVal, LangTree](path: String, params: Iterable[Parameter]): Expr[LangTree, LangVal, LangType] =
    (langAlg: LangAlg[LangTree, LangVal, LangType]) => parsePath(path)
      .map {
        case Left(value) => langAlg.langLiteral(value)
        case Right(value) => langAlg.langApply(langAlg.langTypeApply(langAlg.langType("segment"), List(langAlg.langType(resolveSegmentType(value, params)))), List(langAlg.langLiteral(value)))
      }
      .foldLeft(langAlg.langRef("path"))((tree1: LangTree, tree2: LangTree) => langAlg.langApply(langAlg.langDot(tree1, "/"), List(tree2)))

  // creates query params
  private def resolveQuery[LangTree, LangVal, LangType](params: Iterable[Parameter]): Expr[LangTree, LangVal, LangType] =
    (langAlg: LangAlg[LangTree, LangVal, LangType]) => params
      .filter((parameter: Parameter) => "query".equals(parameter.getIn))
      .map((parameter: Parameter) => langAlg.langApply(langAlg.langTypeApply(langAlg.langType("qs"), List(langAlg.langType(resolveType(parameter.getSchema)))), List(langAlg.langLiteral(parameter.getName))))
      .reduceLeft((tree1: LangTree, tree2: LangTree) => langAlg.langApply(langAlg.langDot(tree1, "&"), List(tree2)))

  // creates responses
  private def endpointResponse[LangTree, LangVal, LangType](operation: Operation): Expr[LangTree, LangVal, LangType] =
    (langAlg: LangAlg[LangTree, LangVal, LangType]) => getResponses(operation)
      .map(tuple => tuple._1.head match {
        case '2' => (tuple._1, Option(tuple._2.getContent).filter(content => !content.isEmpty).map(content => resolveType(content.values().asScala.head.getSchema)).map((str: String) => langAlg.langTypeApply(langAlg.langType("jsonResponse"), List(langAlg.langType(str)))), Option(tuple._2.getDescription))
        case '4' => (tuple._1, Some(langAlg.langRef("clientErrorsResponseEntity")), Option(operation.getDescription))
        case '5' => (tuple._1, Some(langAlg.langRef("serverErrorResponseEntity")), Option(operation.getDescription))
      })
      .map(tuple => langAlg.langApply(langAlg.langRef("response"), List(langAlg.langRef(StatusCodesMap.getOrElse(tuple._1, StatusCodeOk)), tuple._2.getOrElse(langAlg.langRef("emptyResponse")), tuple._3.map((str: String) => langAlg.langApply(langAlg.langRef("Some"), List(langAlg.langLiteral(str)))).getOrElse(langAlg.langRef("None")))))
      .reduceLeft((tree1: LangTree, tree2: LangTree) => langAlg.langApply(langAlg.langDot(tree1, "orElse"), List(tree2)))
}
