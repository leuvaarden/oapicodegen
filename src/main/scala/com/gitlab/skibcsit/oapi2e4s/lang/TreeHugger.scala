package com.gitlab.skibcsit
package oapi2e4s.lang

import treehugger.forest._
import treehuggerDSL._

// https://github.com/eed3si9n/treehugger
trait TreeHugger extends LangAlg[Tree, ValDef, Type] {
  override def langTrait(`package`: String, name: String, parents: Iterable[String], body: Iterable[Tree]): Tree =
    TRAITDEF(name)
      .withParents(parents)
      .:=(BLOCK(body))
      .inPackage(`package`)

  override def langDef(name: String, args: Iterable[ValDef], returns: Option[Type], body: Tree): Tree =
    defWithReturnType(name, returns)
      .withParams(args)
      .:=(body)

  private def defWithReturnType(name: String, returns: Option[Type]): DefTreeStart =
    returns match {
      case Some(value) => DEF(name, value)
      case None => DEF(name)
    }

  override def langVal(name: String, returns: Option[Type], body: Tree): Tree =
    valWithReturnType(name, returns)
      .:=(body)

  private def valWithReturnType(name: String, returns: Option[Type]): ValNameStart =
    returns match {
      case Some(value) => VAL(name, value)
      case None => VAL(name)
    }

  override def langType(ref: String): Type =
    TYPE_REF(ref)

  override def langRef(ref: String): Tree =
    REF(ref)

  override def langLiteral[A](literal: A): Tree =
    LIT(literal)

  override def langParam(name: String, langType: Type): ValDef =
    PARAM(name, langType)

  override def langApply(left: Tree, right: Iterable[Tree]): Tree =
    left.APPLY(right)

  override def langTypeApply(left: Type, right: Iterable[Type]): Tree =
    left.APPLYTYPE(right)

  override def langDot(left: Tree, right: String): Tree =
    left.DOT(right)

  override def langPackageObject(name: String, body: Tree): Tree =
    PACKAGEOBJECTDEF(getPackageSuffix(name))
      .:=(body)
      .inPackage(getPackagePrefix(name))

  private def getPackagePrefix(name: String): String =
    name.split('.').dropRight(1).mkString(".")

  private def getPackageSuffix(name: String): String =
    name.split('.').last

  override def langCaseClass(name: String, params: Iterable[ValDef]): Tree =
    CASECLASSDEF(name).withParams(params)

  override def langBlock(trees: Iterable[Tree]): Tree =
    BLOCK(trees)

  override def makeString(langTree: Tree): String =
    treeToString(langTree)
}
