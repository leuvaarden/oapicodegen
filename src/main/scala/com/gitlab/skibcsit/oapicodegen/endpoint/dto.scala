package com.gitlab.skibcsit
package oapicodegen.endpoint

import com.gitlab.skibcsit.oapicodegen.lang.{Expr, LangAlg}
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Schema

import scala.collection.JavaConverters._

object dto {
  /** Generates package object with case classes from "Components" section of specification */
  def generate[LangTree, LangVal, LangType](name: String, openAPI: OpenAPI): Expr[LangTree, LangVal, LangType] =
   langAlg => langAlg.langPackageObject(name, langAlg.langBlock(createCaseClasses(openAPI)(langAlg)))

  private def createCaseClasses[LangTree, LangVal, LangType](openAPI: OpenAPI): LangAlg[LangTree, LangVal, LangType] => Seq[LangTree] =
    langAlg => openAPI.getComponents.getSchemas.asScala
      .toSeq
      .sortBy(_._1)
      .map(tuple => createCaseClass(tuple._1, tuple._2)(langAlg))

  private def createCaseClass[T, LangTree, LangVal, LangType](name: String, schema: Schema[T]): Expr[LangTree, LangVal, LangType] =
    langAlg => langAlg.langCaseClass(name, getSchemaProperties(schema).map(tuple => createParam(tuple._1, tuple._2)(langAlg)))

  private def createParam[LangTree, LangVal, LangType](name: String, schema: Schema[_]): LangAlg[LangTree, LangVal, LangType] => LangVal =
    langAlg => langAlg.langParam(name, langAlg.langType(resolveType(schema).getOrElse("String")))
}
