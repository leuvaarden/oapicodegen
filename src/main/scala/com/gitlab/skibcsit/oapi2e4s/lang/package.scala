package com.gitlab.skibcsit
package oapi2e4s

package object lang {
  type Expr[LangTree, LangVal, LangType] = LangAlg[LangTree, LangVal, LangType] => LangTree
}
