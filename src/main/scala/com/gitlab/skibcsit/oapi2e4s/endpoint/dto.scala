package com.gitlab.skibcsit
package oapi2e4s.endpoint

import oapi2e4s.lang.{Expr, LangAlg}

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Schema

import scala.jdk.CollectionConverters._

object dto {
  /** Generates package object with case classes from "Components" section of specification */
  def generate[LangTree, LangVal, LangType](name: String, openAPI: OpenAPI): Expr[LangTree, LangVal, LangType] =
    (langAlg: LangAlg[LangTree, LangVal, LangType]) => langAlg.langPackageObject(name, langAlg.langBlock(createCaseClasses(openAPI).map((value: Expr[LangTree, LangVal, LangType]) => value(langAlg))))

  // creates collection of case classes
  private def createCaseClasses[LangTree, LangVal, LangType](openAPI: OpenAPI): Iterable[Expr[LangTree, LangVal, LangType]] =
    openAPI.getComponents.getSchemas.asScala.map(tuple => (langAlg: LangAlg[LangTree, LangVal, LangType]) => createCaseClass(tuple._1, tuple._2)(langAlg))

  // creates case class
  private def createCaseClass[T, LangTree, LangVal, LangType](name: String, schema: Schema[T]): Expr[LangTree, LangVal, LangType] =
    (langAlg: LangAlg[LangTree, LangVal, LangType]) => langAlg.langCaseClass(name, schema.getProperties.entrySet().asScala.map(entry => createParam(entry.getKey, entry.getValue)(langAlg)))

  // creates param definition for case class
  private def createParam[T, LangTree, LangVal, LangType](name: String, schema: Schema[T]): LangAlg[LangTree, LangVal, LangType] => LangVal =
    (langAlg: LangAlg[LangTree, LangVal, LangType]) => langAlg.langParam(name, langAlg.langType(resolveType(schema)))
}
