package com.gitlab.skibcsit
package oapicodegen.lang

/** Algebra that defines rules of code generation */
trait LangAlg[LangTree, LangVal, LangType] {
  def langApply(left: LangTree, right: Iterable[LangTree]): LangTree

  def langBlock(trees: Iterable[LangTree]): LangTree

  def langCaseClass(name: String, params: Iterable[LangVal]): LangTree

  def langDef(name: String, args: Iterable[LangVal], returns: Option[LangType], body: LangTree): LangTree

  def langDot(left: LangTree, right: String): LangTree

  def langImport(statement: String): LangTree

  def langLiteral[A](literal: A): LangTree

  def langPackageObject(name: String, body: LangTree): LangTree

  def langParam(name: String, langType: LangType): LangVal

  def langRef(ref: String): LangTree

  def langTrait(`package`: String, name: String, parents: Iterable[String], body: Iterable[LangTree]): LangTree

  def langType(ref: String): LangType

  def langTypeApply(left: LangType, right: Iterable[LangType]): LangTree

  def langVal(name: String, returns: Option[LangType], body: LangTree): LangTree

  def makeString(langTree: LangTree): String
}
