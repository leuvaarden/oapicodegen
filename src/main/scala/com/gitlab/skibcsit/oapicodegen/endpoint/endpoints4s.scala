package com.gitlab.skibcsit
package oapicodegen.endpoint

import com.gitlab.skibcsit.oapicodegen.lang.{Expr, LangAlg}
import io.swagger.v3.oas.models.PathItem.HttpMethod
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.{OpenAPI, Operation, PathItem}

import scala.collection.JavaConverters._

object endpoints4s {
  private final val CirceAutoPackage = "io.circe.generic.auto._"
  private final val MethodPost: String = "Post"
  private final val MethodsMap: Map[HttpMethod, String] = Map(
    HttpMethod.GET -> "Get",
    HttpMethod.POST -> MethodPost,
    HttpMethod.PUT -> "Put",
    HttpMethod.DELETE -> "Delete",
    HttpMethod.PATCH -> "Patch",
    HttpMethod.OPTIONS -> "Options",
  )
  private final val StatusCode2xx: String = "OK"
  private final val StatusCode3xx: String = "NotModified"
  private final val StatusCode4xx: String = "BadRequest"
  private final val StatusCode5xx: String = "InternalServerError"
  private final val StatusCodesMap: Map[String, String] = Map(
    "200" -> StatusCode2xx,
    "201" -> "Created",
    "202" -> "Accepted",
    "203" -> "NonAuthoritativeInformation",
    "204" -> "NoContent",
    "205" -> "ResetContent",
    "206" -> "PartialContent",
    "207" -> "MultiStatus",
    "208" -> "AlreadyReported",
    "226" -> "IMUsed",

    "304" -> StatusCode3xx,

    "400" -> StatusCode4xx,
    "401" -> "Unauthorized",
    "402" -> "PaymentRequired",
    "403" -> "Forbidden",
    "404" -> "NotFound",
    "405" -> "MethodNotAllowed",
    "406" -> "NotAcceptable",
    "407" -> "ProxyAuthenticationRequired",
    "408" -> "RequestTimeout",
    "409" -> "Conflict",
    "410" -> "Gone",
    "411" -> "LengthRequired",
    "412" -> "PreconditionFailed",
    "413" -> "PayloadTooLarge",
    "414" -> "UriTooLong",
    "415" -> "UnsupportedMediaType",
    "416" -> "RangeNotSatisfiable",
    "417" -> "ExpectationFailed",
    "421" -> "MisdirectedRequest",
    "422" -> "UnprocessableEntity",
    "423" -> "Locked",
    "424" -> "FailedDependency",
    "425" -> "TooEarly",
    "426" -> "UpgradeRequired",
    "428" -> "PreconditionRequired",
    "429" -> "TooManyRequests",
    "431" -> "RequestHeaderFieldsTooLarge",
    "451" -> "UnavailableForLegalReasons",

    "500" -> StatusCode5xx,
    "501" -> "NotImplemented"
  )
  private final val TraitParents: Seq[String] = List(
    "endpoints4s.algebra.Endpoints",
    "endpoints4s.algebra.circe.JsonEntitiesFromCodecs",
    "endpoints4s.algebra.StatusCodes",
  )

  /** Generates trait object with [[https://github.com/endpoints4s/endpoints4s endpoints4s]] definitions */
  def generate[LangTree, LangVal, LangType](packageName: String, openAPI: OpenAPI): Expr[LangTree, LangVal, LangType] =
    langAlg => langAlg.langTrait(packageName, toTraitName(openAPI.getInfo.getTitle), TraitParents, langAlg.langImport(CirceAutoPackage) +: getEndpoints(openAPI).map(tuple => endpointDefinition(tuple._1, tuple._2, tuple._3)(langAlg)))

  private def endpointDefinition[LangTree, LangVal, LangType](path: String, method: PathItem.HttpMethod, operation: Operation): Expr[LangTree, LangVal, LangType] =
    langAlg => langAlg.langVal(operation.getOperationId, returnType(operation)(langAlg), rhs(path, method, operation)(langAlg))

  // currently does not generate return type
  private def returnType[LangTree, LangVal, LangType](operation: Operation): LangAlg[LangTree, LangVal, LangType] => Option[LangType] =
    _ => None

  private def rhs[LangTree, LangVal, LangType](path: String, method: HttpMethod, operation: Operation): Expr[LangTree, LangVal, LangType] =
    langAlg => langAlg.langApply(langAlg.langRef("endpoint"), List(endpointRequest(path, method, operation)(langAlg), endpointResponse(operation)(langAlg)))

  private def endpointRequest[LangTree, LangVal, LangType](path: String, method: HttpMethod, operation: Operation): Expr[LangTree, LangVal, LangType] =
    langAlg => langAlg.langApply(langAlg.langRef("request"), List(endpointRequestMethod(method)(langAlg), endpointRequestUrl(path, operation.getParameters.asScala)(langAlg), endpointRequestBody(operation)(langAlg), endpointDescription(operation)(langAlg)))

  private def endpointRequestMethod[LangTree, LangVal, LangType](httpMethod: HttpMethod): Expr[LangTree, LangVal, LangType] =
    langAlg => langAlg.langRef(MethodsMap.getOrElse(httpMethod, MethodPost))

  private def endpointRequestUrl[LangTree, LangVal, LangType](path: String, params: Iterable[Parameter]): Expr[LangTree, LangVal, LangType] =
    langAlg =>
      if (containsQueryParams(params))
        langAlg.langApply(langAlg.langDot(endpointUrlSegments(path, params)(langAlg), "/?"), List(endpointQueryParams(params)(langAlg)))
      else
        endpointUrlSegments(path, params)(langAlg)

  private def endpointUrlSegments[LangType, LangVal, LangTree](path: String, params: Iterable[Parameter]): Expr[LangTree, LangVal, LangType] =
    langAlg => parsePath(path)
      .map {
        case Left(value) => langAlg.langLiteral(value)
        case Right(value) => langAlg.langApply(langAlg.langTypeApply(langAlg.langType("segment"), List(langAlg.langType(resolveSegmentType(value, params)))), List(langAlg.langLiteral(value)))
      }
      .foldLeft(langAlg.langRef("path"))((tree1, tree2) => langAlg.langApply(langAlg.langDot(tree1, "/"), List(tree2)))

  private def endpointQueryParams[LangTree, LangVal, LangType](params: Iterable[Parameter]): Expr[LangTree, LangVal, LangType] =
    langAlg => params
      .filter((parameter: Parameter) => "query".equals(parameter.getIn))
      .map((parameter: Parameter) => langAlg.langApply(langAlg.langTypeApply(langAlg.langType("qs"), List(langAlg.langType(resolveType(parameter.getSchema).getOrElse("String")))), List(langAlg.langLiteral(parameter.getName))))
      .reduceLeft((tree1: LangTree, tree2: LangTree) => langAlg.langApply(langAlg.langDot(tree1, "&"), List(tree2)))

  private def endpointRequestBody[LangTree, LangVal, LangType](operation: Operation): Expr[LangTree, LangVal, LangType] =
    langAlg =>
      if (operation.getRequestBody == null)
        langAlg.langRef("emptyRequest")
      else
        langAlg.langTypeApply(langAlg.langType("jsonRequest"), List(langAlg.langType(resolveType(getFirstContentSchema(operation.getRequestBody.getContent).getOrElse(new StringSchema)).getOrElse("String"))))

  private def endpointDescription[LangTree, LangVal, LangType](operation: Operation): Expr[LangTree, LangVal, LangType] =
    langAlg =>
      if (operation.getDescription == null)
        langAlg.langRef("None")
      else
        langAlg.langApply(langAlg.langRef("Some"), List(langAlg.langLiteral(operation.getDescription)))

  private def endpointResponse[LangTree, LangVal, LangType](operation: Operation): Expr[LangTree, LangVal, LangType] =
    langAlg => getResponses(operation)
      .map(tuple => (langAlg.langRef(endpointResponseCode(tuple._1)), endpointResponseDescription(tuple._2.getDescription)(langAlg), endpointResponseBody(tuple._2)(langAlg)))
      .map(tuple => langAlg.langApply(langAlg.langRef("response"), List(tuple._1, tuple._3, tuple._2)))
      .reduceLeft((tree1, tree2) => langAlg.langApply(langAlg.langDot(tree1, "orElse"), List(tree2)))

  private def endpointResponseCode[LangType, LangVal, LangTree](code: String): String =
    StatusCodesMap.getOrElse(code, code.head match {
      case '2' => StatusCode2xx
      case '3' => StatusCode3xx
      case '4' => StatusCode4xx
      case _ => StatusCode5xx
    })

  private def endpointResponseDescription[LangTree, LangVal, LangType](literal: Any): Expr[LangTree, LangVal, LangType] =
    langAlg =>
      if (literal == null)
        langAlg.langRef("None")
      else
        langAlg.langApply(langAlg.langRef("Some"), List(langAlg.langLiteral(literal)))

  private def endpointResponseBody[LangTree, LangVal, LangType](response: ApiResponse): Expr[LangTree, LangVal, LangType] =
    langAlg => Option(response.getContent)
      .filter(content => !content.isEmpty)
      .flatMap(content => getFirstContentSchema(content))
      .flatMap(schema => resolveType(schema))
      .flatMap(resolved => if ("String".equalsIgnoreCase(resolved)) None else Some(langAlg.langTypeApply(langAlg.langType("jsonResponse"), List(langAlg.langType(resolved)))))
      .getOrElse(langAlg.langRef("textResponse"))
}
