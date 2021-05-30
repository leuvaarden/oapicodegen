package com.gitlab.skibcsit
package oapicodegen

package object lang {
  type Expr[LangTree, LangVal, LangType] = LangAlg[LangTree, LangVal, LangType] => LangTree
}
