/*
* Copyright 2001-2013 Artima, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.scalatest

import org.scalactic.Equality
import org.scalactic.Uniformity
import org.scalactic.StringNormalizations._
import SharedHelpers._
import FailureMessages.decorateToStringValue
import Matchers._
import exceptions.TestFailedException

class ListShouldContainAtMostOneElementOfLogicalAndSpec extends FunSpec {

  val upperCaseStringEquality =
    new Equality[String] {
      def areEqual(a: String, b: Any): Boolean = upperCase(a) == upperCase(b)
    }

  val invertedListOfStringEquality =
    new Equality[List[String]] {
      def areEqual(a: List[String], b: Any): Boolean = a != b
    }

  private def upperCase(value: Any): Any =
    value match {
      case l: List[_] => l.map(upperCase(_))
      case s: String => s.toUpperCase
      case c: Char => c.toString.toUpperCase.charAt(0)
      case (s1: String, s2: String) => (s1.toUpperCase, s2.toUpperCase)
      case e: java.util.Map.Entry[_, _] =>
        (e.getKey, e.getValue) match {
          case (k: String, v: String) => Entry(k.toUpperCase, v.toUpperCase)
          case _ => value
        }
      case _ => value
    }

  //ADDITIONAL//

  val fileName: String = "ListShouldContainAtMostOneElementOfLogicalAndSpec.scala"

  describe("a List") {

    val fumList: List[String] = List("fum", "foe")
    val toList: List[String] = List("to", "you")

    describe("when used with (contain atMostOneElementOf Seq(..) and contain atMostOneElementOf Seq(..))") {

      it("should do nothing if valid, else throw a TFE with an appropriate error message") {
        fumList should (contain atMostOneElementOf Seq("fee", "fie", "foe", "fam") and contain atMostOneElementOf Seq("fie", "fee", "fam", "foe"))
        val e1 = intercept[TestFailedException] {
          fumList should (contain atMostOneElementOf Seq("fee", "fie", "foe", "fum") and contain atMostOneElementOf Seq("fie", "fee", "fam", "foe"))
        }
        checkMessageStackDepth(e1, FailureMessages.didNotContainAtMostOneElementOf(fumList, Seq("fee", "fie", "foe", "fum")), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          fumList should (contain atMostOneElementOf Seq("fee", "fie", "foe", "fam") and contain atMostOneElementOf Seq("fie", "fee", "fum", "foe"))
        }
        checkMessageStackDepth(e2, FailureMessages.containedAtMostOneElementOf(fumList, Seq("fee", "fie", "foe", "fam")) + ", but " + FailureMessages.didNotContainAtMostOneElementOf(fumList, Seq("fie", "fee", "fum", "foe")), fileName, thisLineNumber - 2)
      }

      it("should use the implicit Equality in scope") {
        implicit val ise = upperCaseStringEquality
        fumList should (contain atMostOneElementOf Seq("FEE", "FIE", "FOE", "FAM") and contain atMostOneElementOf Seq("FIE", "FEE", "FAM", "FOE"))
        val e1 = intercept[TestFailedException] {
          fumList should (contain atMostOneElementOf Seq("FEE", "FIE", "FOE", "FUM") and contain atMostOneElementOf Seq("FIE", "FEE", "FAM", "FOE"))
        }
        checkMessageStackDepth(e1, FailureMessages.didNotContainAtMostOneElementOf(fumList, Seq("FEE", "FIE", "FOE", "FUM")), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          fumList should (contain atMostOneElementOf Seq("FEE", "FIE", "FOE", "FAM") and (contain atMostOneElementOf Seq("FIE", "FEE", "FUM", "FOE")))
        }
        checkMessageStackDepth(e2, FailureMessages.containedAtMostOneElementOf(fumList, Seq("FEE", "FIE", "FOE", "FAM")) + ", but " + FailureMessages.didNotContainAtMostOneElementOf(fumList, Seq("FIE", "FEE", "FUM", "FOE")), fileName, thisLineNumber - 2)
      }

      it("should use an explicitly provided Equality") {
        (fumList should (contain atMostOneElementOf Seq("FEE", "FIE", "FOE", "FAM") and contain atMostOneElementOf Seq("FIE", "FEE", "FAM", "FOE"))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        val e1 = intercept[TestFailedException] {
          (fumList should (contain atMostOneElementOf Seq("FEE", "FIE", "FOE", "FAM") and contain atMostOneElementOf Seq("FIE", "FEE", "FUM", "FOE"))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, FailureMessages.containedAtMostOneElementOf(fumList, Seq("FEE", "FIE", "FOE", "FAM")) + ", but " + FailureMessages.didNotContainAtMostOneElementOf(fumList, Seq("FIE", "FEE", "FUM", "FOE")), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          (fumList should (contain atMostOneElementOf Seq("FEE", "FIE", "FOE", "FUM") and contain atMostOneElementOf Seq("FIE", "FEE", "FAM", "FOE"))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e2, FailureMessages.didNotContainAtMostOneElementOf(fumList, Seq("FEE", "FIE", "FOE", "FUM")), fileName, thisLineNumber - 2)
        (fumList should (contain atMostOneElementOf Seq(" FEE ", " FIE ", " FOE ", " FAM ") and contain atMostOneElementOf Seq(" FEE ", " FIE ", " FOE ", " FAM "))) (after being lowerCased and trimmed, after being lowerCased and trimmed)
      }

      it("should do nothing when RHS contain duplicated value") {
        fumList should (contain atMostOneElementOf Seq("fee", "fie", "foe", "fie", "fam") and contain atMostOneElementOf Seq("fie", "fee", "fam", "foe"))
        fumList should (contain atMostOneElementOf Seq("fie", "fee", "fam", "foe") and contain atMostOneElementOf Seq("fee", "fie", "foe", "fie", "fam"))
      }
    }

    describe("when used with (equal (..) and contain atMostOneElementOf Seq(..))") {

      it("should do nothing if valid, else throw a TFE with an appropriate error message") {
        fumList should (equal (fumList) and contain atMostOneElementOf Seq("fie", "fee", "fam", "foe"))
        val e1 = intercept[TestFailedException] {
          fumList should (equal (toList) and contain atMostOneElementOf Seq("fee", "fie", "foe", "fam"))
        }
        checkMessageStackDepth(e1, FailureMessages.didNotEqual(fumList, toList), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          fumList should (equal (fumList) and contain atMostOneElementOf Seq("fie", "fee", "fum", "foe"))
        }
        checkMessageStackDepth(e2, FailureMessages.equaled(fumList, fumList) + ", but " + FailureMessages.didNotContainAtMostOneElementOf(fumList, Seq("fie", "fee", "fum", "foe")), fileName, thisLineNumber - 2)
      }

      it("should use the implicit Equality in scope") {
        implicit val ise = upperCaseStringEquality
        fumList should (equal (fumList) and contain atMostOneElementOf Seq("FIE", "FEE", "FAM", "FOE"))
        val e1 = intercept[TestFailedException] {
          fumList should (equal (toList) and contain atMostOneElementOf Seq("FIE", "FEE", "FAM", "FOE"))
        }
        checkMessageStackDepth(e1, FailureMessages.didNotEqual(fumList, toList), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          fumList should (equal (fumList) and (contain atMostOneElementOf Seq("FIE", "FEE", "FUM", "FOE")))
        }
        checkMessageStackDepth(e2, FailureMessages.equaled(fumList, fumList) + ", but " + FailureMessages.didNotContainAtMostOneElementOf(fumList, Seq("FIE", "FEE", "FUM", "FOE")), fileName, thisLineNumber - 2)
      }

      it("should use an explicitly provided Equality") {
        (fumList should (equal (toList) and contain atMostOneElementOf Seq("FIE", "FEE", "FAM", "FOE"))) (decided by invertedListOfStringEquality, decided by upperCaseStringEquality)
        val e1 = intercept[TestFailedException] {
          (fumList should (equal (toList) and contain atMostOneElementOf Seq("FIE", "FEE", "FUM", "FOE"))) (decided by invertedListOfStringEquality, decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, FailureMessages.equaled(fumList, toList) + ", but " + FailureMessages.didNotContainAtMostOneElementOf(fumList, Seq("FIE", "FEE", "FUM", "FOE")), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          (fumList should (equal (fumList) and contain atMostOneElementOf Seq("FIE", "FEE", "FAM", "FOE"))) (decided by invertedListOfStringEquality, decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e2, FailureMessages.didNotEqual(fumList, fumList), fileName, thisLineNumber - 2)
        (fumList should (equal (toList) and contain atMostOneElementOf Seq(" FEE ", " FIE ", " FOE ", " FAM "))) (decided by invertedListOfStringEquality, after being lowerCased and trimmed)
      }

      it("should do nothing when RHS contain duplicated value") {
        fumList should (equal (fumList) and contain atMostOneElementOf Seq("fee", "fie", "foe", "fie", "fam"))
      }
    }

    describe("when used with (be (..) and contain atMostOneElementOf Seq(..))") {

      it("should do nothing if valid, else throw a TFE with an appropriate error message") {
        fumList should (be (fumList) and contain atMostOneElementOf Seq("fie", "fee", "fam", "foe"))
        val e1 = intercept[TestFailedException] {
          fumList should (be (toList) and contain atMostOneElementOf Seq("fee", "fie", "foe", "fam"))
        }
        checkMessageStackDepth(e1, FailureMessages.wasNotEqualTo(fumList, toList), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          fumList should (be (fumList) and contain atMostOneElementOf Seq("fee", "fie", "foe", "fum"))
        }
        checkMessageStackDepth(e2, FailureMessages.wasEqualTo(fumList, fumList) + ", but " + FailureMessages.didNotContainAtMostOneElementOf(fumList, Seq("fee", "fie", "foe", "fum")), fileName, thisLineNumber - 2)
      }

      it("should use the implicit Equality in scope") {
        implicit val ise = upperCaseStringEquality
        fumList should (be (fumList) and contain atMostOneElementOf Seq("FEE", "FIE", "FOE", "FAM"))
        val e1 = intercept[TestFailedException] {
          fumList should (be (toList) and contain atMostOneElementOf Seq("FEE", "FIE", "FOE", "FAM"))
        }
        checkMessageStackDepth(e1, FailureMessages.wasNotEqualTo(fumList, toList), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          fumList should (be (fumList) and (contain atMostOneElementOf Seq("FEE", "FIE", "FOE", "FUM")))
        }
        checkMessageStackDepth(e2, FailureMessages.wasEqualTo(fumList, fumList) + ", but " + FailureMessages.didNotContainAtMostOneElementOf(fumList, Seq("FEE", "FIE", "FOE", "FUM")), fileName, thisLineNumber - 2)
      }

      it("should use an explicitly provided Equality") {
        (fumList should (be (fumList) and contain atMostOneElementOf Seq("FEE", "FIE", "FOE", "FAM"))) (decided by upperCaseStringEquality)
        val e1 = intercept[TestFailedException] {
          (fumList should (be (fumList) and contain atMostOneElementOf Seq("FEE", "FIE", "FOE", "FUM"))) (decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, FailureMessages.wasEqualTo(fumList, fumList) + ", but " + FailureMessages.didNotContainAtMostOneElementOf(fumList, Seq("FEE", "FIE", "FOE", "FUM")), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          (fumList should (be (toList) and contain atMostOneElementOf Seq("FEE", "FIE", "FOE", "FAM"))) (decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e2, FailureMessages.wasNotEqualTo(fumList, toList), fileName, thisLineNumber - 2)
        (fumList should (be (fumList) and contain atMostOneElementOf Seq(" FEE ", " FIE ", " FOE ", " FAM "))) (after being lowerCased and trimmed)
      }

      it("should do nothing when RHS contain duplicated value") {
        fumList should (be (fumList) and contain atMostOneElementOf Seq("fee", "fie", "foe", "fie", "fam"))
      }
    }

    describe("when used with (contain atMostOneElementOf Seq(..) and be (..))") {

      it("should do nothing if valid, else throw a TFE with an appropriate error message") {
        fumList should (contain atMostOneElementOf Seq("fie", "fee", "fam", "foe") and be (fumList))
        val e1 = intercept[TestFailedException] {
          fumList should (contain atMostOneElementOf Seq("fee", "fie", "foe", "fam") and be (toList))
        }
        checkMessageStackDepth(e1, FailureMessages.containedAtMostOneElementOf(fumList, Seq("fee", "fie", "foe", "fam")) + ", but " + FailureMessages.wasNotEqualTo(fumList, toList), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          fumList should (contain atMostOneElementOf Seq("fee", "fie", "foe", "fum") and be (fumList))
        }
        checkMessageStackDepth(e2, FailureMessages.didNotContainAtMostOneElementOf(fumList, Seq("fee", "fie", "foe", "fum")), fileName, thisLineNumber - 2)
      }

      it("should use the implicit Equality in scope") {
        implicit val ise = upperCaseStringEquality
        fumList should (contain atMostOneElementOf Seq("FEE", "FIE", "FOE", "FAM") and be (fumList))
        val e1 = intercept[TestFailedException] {
          fumList should (contain atMostOneElementOf Seq("FEE", "FIE", "FOE", "FUM") and be (fumList))
        }
        checkMessageStackDepth(e1, FailureMessages.didNotContainAtMostOneElementOf(fumList, Seq("FEE", "FIE", "FOE", "FUM")), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          fumList should (contain atMostOneElementOf Seq("FEE", "FIE", "FOE", "FAM") and (be (toList)))
        }
        checkMessageStackDepth(e2, FailureMessages.containedAtMostOneElementOf(fumList, Seq("FEE", "FIE", "FOE", "FAM")) + ", but " + FailureMessages.wasNotEqualTo(fumList, toList), fileName, thisLineNumber - 2)
      }

      it("should use an explicitly provided Equality") {
        (fumList should (contain atMostOneElementOf Seq("FEE", "FIE", "FOE", "FAM") and be (fumList))) (decided by upperCaseStringEquality)
        val e1 = intercept[TestFailedException] {
          (fumList should (contain atMostOneElementOf Seq("FEE", "FIE", "FOE", "FUM") and be (fumList))) (decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, FailureMessages.didNotContainAtMostOneElementOf(fumList, Seq("FEE", "FIE", "FOE", "FUM")), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          (fumList should (contain atMostOneElementOf Seq("FEE", "FIE", "FOE", "FAM") and be (toList))) (decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e2, FailureMessages.containedAtMostOneElementOf(fumList, Seq("FEE", "FIE", "FOE", "FAM")) + ", but " + FailureMessages.wasNotEqualTo(fumList, toList), fileName, thisLineNumber - 2)
        (fumList should (contain atMostOneElementOf Seq(" FEE ", " FIE ", " FOE ", " FAM ") and be (fumList))) (after being lowerCased and trimmed)
      }

      it("should do nothing when RHS contain duplicated value") {
        fumList should (contain atMostOneElementOf Seq("fee", "fie", "foe", "fie", "fam") and be (fumList))
      }
    }

    describe("when used with (not contain atMostOneElementOf Seq(..) and not contain atMostOneElementOf Seq(..))") {

      it("should do nothing if valid, else throw a TFE with an appropriate error message") {
        fumList should (not contain atMostOneElementOf (Seq("fee", "fie", "foe", "fum")) and not contain atMostOneElementOf (Seq("fie", "fee", "fum", "foe")))
        val e1 = intercept[TestFailedException] {
          fumList should (not contain atMostOneElementOf (Seq("fee", "fie", "foe", "fuu")) and not contain atMostOneElementOf (Seq("fie", "fee", "fum", "foe")))
        }
        checkMessageStackDepth(e1, FailureMessages.containedAtMostOneElementOf(fumList, Seq("fee", "fie", "foe", "fuu")), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          fumList should (not contain atMostOneElementOf (Seq("fee", "fie", "foe", "fum")) and not contain atMostOneElementOf (Seq("fie", "fee", "fam", "foe")))
        }
        checkMessageStackDepth(e2, FailureMessages.didNotContainAtMostOneElementOf(fumList, Seq("fee", "fie", "foe", "fum")) + ", but " + FailureMessages.containedAtMostOneElementOf(fumList, Seq("fie", "fee", "fam", "foe")), fileName, thisLineNumber - 2)
      }

      it("should use the implicit Equality in scope") {
        implicit val ise = upperCaseStringEquality
        fumList should (not contain atMostOneElementOf (Seq("FEE", "FIE", "FOE", "FUM")) and not contain atMostOneElementOf (Seq("FIE", "FEE", "FUM", "FOE")))
        val e1 = intercept[TestFailedException] {
          fumList should (not contain atMostOneElementOf (Seq("FEE", "FIE", "FOE", "FUU")) and not contain atMostOneElementOf (Seq("FIE", "FEE", "FUM", "FOE")))
        }
        checkMessageStackDepth(e1, FailureMessages.containedAtMostOneElementOf(fumList, Seq("FEE", "FIE", "FOE", "FUU")), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          fumList should (not contain atMostOneElementOf (Seq("FEE", "FIE", "FOE", "FUM")) and (not contain atMostOneElementOf (Seq("FIE", "FEE", "FAM", "FOE"))))
        }
        checkMessageStackDepth(e2, FailureMessages.didNotContainAtMostOneElementOf(fumList, Seq("FEE", "FIE", "FOE", "FUM")) + ", but " + FailureMessages.containedAtMostOneElementOf(fumList, Seq("FIE", "FEE", "FAM", "FOE")), fileName, thisLineNumber - 2)
      }

      it("should use an explicitly provided Equality") {
        (fumList should (not contain atMostOneElementOf (Seq("FEE", "FIE", "FOE", "FUM")) and not contain atMostOneElementOf (Seq("FIE", "FEE", "FUM", "FOE")))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        val e1 = intercept[TestFailedException] {
          (fumList should (not contain atMostOneElementOf (Seq("FEE", "FIE", "FOE", "FUM")) and not contain atMostOneElementOf (Seq("FIE", "FEE", "FAM", "FOE")))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, FailureMessages.didNotContainAtMostOneElementOf(fumList, Seq("FEE", "FIE", "FOE", "FUM")) + ", but " + FailureMessages.containedAtMostOneElementOf(fumList, Seq("FIE", "FEE", "FAM", "FOE")), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          (fumList should (not contain atMostOneElementOf (Seq("FEE", "FIE", "FOE", "FAM")) and not contain atMostOneElementOf (Seq("FIE", "FEE", "FUM", "FOE")))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e2, FailureMessages.containedAtMostOneElementOf(fumList, Seq("FEE", "FIE", "FOE", "FAM")), fileName, thisLineNumber - 2)
        (fumList should (contain atMostOneElementOf Seq(" FEE ", " FIE ", " FOE ", " FAM ") and contain atMostOneElementOf Seq(" FEE ", " FIE ", " FOE ", " FAM "))) (after being lowerCased and trimmed, after being lowerCased and trimmed)
      }

      it("should do nothing when RHS contain duplicated value") {
        fumList should (not contain atMostOneElementOf (Seq("fee", "fie", "foe", "fie", "fum")) and not contain atMostOneElementOf (Seq("fie", "fee", "fum", "foe")))
        fumList should (not contain atMostOneElementOf (Seq("fie", "fee", "fum", "foe")) and not contain atMostOneElementOf (Seq("fee", "fie", "foe", "fie", "fum")))
      }
    }

    describe("when used with (not equal (..) and not contain atMostOneElementOf Seq(..))") {

      it("should do nothing if valid, else throw a TFE with an appropriate error message") {
        fumList should (not equal (toList) and not contain atMostOneElementOf (Seq("fie", "fee", "fum", "foe")))
        val e1 = intercept[TestFailedException] {
          fumList should (not equal (fumList) and not contain atMostOneElementOf (Seq("fie", "fee", "fum", "foe")))
        }
        checkMessageStackDepth(e1, FailureMessages.equaled(fumList, fumList), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          fumList should (not equal (toList) and not contain atMostOneElementOf (Seq("fie", "fee", "fam", "foe")))
        }
        checkMessageStackDepth(e2, FailureMessages.didNotEqual(fumList, toList) + ", but " + FailureMessages.containedAtMostOneElementOf(fumList, Seq("fie", "fee", "fam", "foe")), fileName, thisLineNumber - 2)
      }

      it("should use the implicit Equality in scope") {
        implicit val ise = upperCaseStringEquality
        fumList should (not equal (toList) and not contain atMostOneElementOf (Seq("FIE", "FEE", "FUM", "FOE")))
        val e1 = intercept[TestFailedException] {
          fumList should (not equal (fumList) and not contain atMostOneElementOf (Seq("FIE", "FEE", "FUM", "FOE")))
        }
        checkMessageStackDepth(e1, FailureMessages.equaled(fumList, fumList), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          fumList should (not equal (toList) and (not contain atMostOneElementOf (Seq("FIE", "FEE", "FAM", "FOE"))))
        }
        checkMessageStackDepth(e2, FailureMessages.didNotEqual(fumList, toList) + ", but " + FailureMessages.containedAtMostOneElementOf(fumList, Seq("FIE", "FEE", "FAM", "FOE")), fileName, thisLineNumber - 2)
      }

      it("should use an explicitly provided Equality") {
        (fumList should (not equal (fumList) and not contain atMostOneElementOf (Seq("FIE", "FEE", "FUM", "FOE")))) (decided by invertedListOfStringEquality, decided by upperCaseStringEquality)
        val e1 = intercept[TestFailedException] {
          (fumList should (not equal (fumList) and not contain atMostOneElementOf (Seq("FIE", "FEE", "FAM", "FOE")))) (decided by invertedListOfStringEquality, decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, FailureMessages.didNotEqual(fumList, fumList) + ", but " + FailureMessages.containedAtMostOneElementOf(fumList, Seq("FIE", "FEE", "FAM", "FOE")), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          (fumList should (not equal (toList) and not contain atMostOneElementOf (Seq("FIE", "FEE", "FUM", "FOE")))) (decided by invertedListOfStringEquality, decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e2, FailureMessages.equaled(fumList, toList), fileName, thisLineNumber - 2)
        (fumList should (not contain atMostOneElementOf (Seq(" FEE ", " FIE ", " FOE ", " FUM ")) and not contain atMostOneElementOf (Seq(" FEE ", " FIE ", " FOE ", " FUM ")))) (after being lowerCased and trimmed, after being lowerCased and trimmed)
      }

      it("should do nothing when RHS contain duplicated value") {
        fumList should (not equal (toList) and not contain atMostOneElementOf (Seq("fee", "fie", "foe", "fie", "fum")))
      }
    }

    describe("when used with (not be (..) and not contain atMostOneElementOf Seq(..))") {

      it("should do nothing if valid, else throw a TFE with an appropriate error message") {
        fumList should (not be (toList) and not contain atMostOneElementOf (Seq("fie", "fee", "fum", "foe")))
        val e1 = intercept[TestFailedException] {
          fumList should (not be (fumList) and not contain atMostOneElementOf (Seq("fie", "fee", "fum", "foe")))
        }
        checkMessageStackDepth(e1, FailureMessages.wasEqualTo(fumList, fumList), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          fumList should (not be (toList) and not contain atMostOneElementOf (Seq("fie", "fee", "fam", "foe")))
        }
        checkMessageStackDepth(e2, FailureMessages.wasNotEqualTo(fumList, toList) + ", but " + FailureMessages.containedAtMostOneElementOf(fumList, Seq("fie", "fee", "fam", "foe")), fileName, thisLineNumber - 2)
      }

      it("should use the implicit Equality in scope") {
        implicit val ise = upperCaseStringEquality
        fumList should (not be (toList) and not contain atMostOneElementOf (Seq("FIE", "FEE", "FUM", "FOE")))
        val e1 = intercept[TestFailedException] {
          fumList should (not be (fumList) and not contain atMostOneElementOf (Seq("FIE", "FEE", "FUM", "FOE")))
        }
        checkMessageStackDepth(e1, FailureMessages.wasEqualTo(fumList, fumList), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          fumList should (not be (toList) and (not contain atMostOneElementOf (Seq("FIE", "FEE", "FAM", "FOE"))))
        }
        checkMessageStackDepth(e2, FailureMessages.wasNotEqualTo(fumList, toList) + ", but " + FailureMessages.containedAtMostOneElementOf(fumList, Seq("FIE", "FEE", "FAM", "FOE")), fileName, thisLineNumber - 2)
      }

      it("should use an explicitly provided Equality") {
        (fumList should (not be (toList) and not contain atMostOneElementOf (Seq("FIE", "FEE", "FUM", "FOE")))) (decided by upperCaseStringEquality)
        val e1 = intercept[TestFailedException] {
          (fumList should (not be (toList) and not contain atMostOneElementOf (Seq("FIE", "FEE", "FAM", "FOE")))) (decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, FailureMessages.wasNotEqualTo(fumList, toList) + ", but " + FailureMessages.containedAtMostOneElementOf(fumList, Seq("FIE", "FEE", "FAM", "FOE")), fileName, thisLineNumber - 2)
        val e2 = intercept[TestFailedException] {
          (fumList should (not be (fumList) and not contain atMostOneElementOf (Seq("FIE", "FEE", "FUM", "FOE")))) (decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e2, FailureMessages.wasEqualTo(fumList, fumList), fileName, thisLineNumber - 2)
        (fumList should (not contain atMostOneElementOf (Seq(" FEE ", " FIE ", " FOE ", " FUM ")) and not contain atMostOneElementOf (Seq(" FEE ", " FIE ", " FOE ", " FUM ")))) (after being lowerCased and trimmed, after being lowerCased and trimmed)
      }

      it("should do nothing when RHS contain duplicated value") {
        fumList should (not be (toList) and not contain atMostOneElementOf (Seq("fee", "fie", "foe", "fie", "fum")))
      }
    }

  }

  describe("collection of Lists") {

    val list1s: Vector[List[Int]] = Vector(List(1, 2), List(1, 2), List(1, 2))
    val lists: Vector[List[Int]] = Vector(List(1, 2), List(1, 2), List(2, 3))
    val nils: Vector[List[Int]] = Vector(Nil, Nil, Nil)
    val listsNil: Vector[List[Int]] = Vector(List(1), List(1), Nil)
    val hiLists: Vector[List[String]] = Vector(List("hi", "he"), List("hi", "he"), List("hi", "he"))
    val toLists: Vector[List[String]] = Vector(List("to", "you"), List("to", "you"), List("to", "you"))

    def allErrMsg(index: Int, message: String, lineNumber: Int, left: Any): String =
      "'all' inspection failed, because: \n" +
        "  at index " + index + ", " + message + " (" + fileName + ":" + (lineNumber) + ") \n" +
        "in " + decorateToStringValue(left)

    describe("when used with (contain atMostOneElementOf Seq(..) and contain atMostOneElementOf Seq(..))") {

      it("should do nothing if valid, else throw a TFE with an appropriate error message") {
        all (list1s) should (contain atMostOneElementOf Seq(3, 4, 1) and contain atMostOneElementOf Seq(1, 3, 4))
        atLeast (2, lists) should (contain atMostOneElementOf Seq(3, 2, 5) and contain atMostOneElementOf Seq(2, 3, 4))
        atMost (2, lists) should (contain atMostOneElementOf Seq(3, 2, 8) and contain atMostOneElementOf Seq(2, 3, 4))
        no (lists) should (contain atMostOneElementOf Seq(1, 2, 3) and contain atMostOneElementOf Seq(3, 2, 1))

        val e1 = intercept[TestFailedException] {
          all (lists) should (contain atMostOneElementOf Seq(2, 3, 4) and contain atMostOneElementOf Seq(1, 3, 4))
        }
        checkMessageStackDepth(e1, allErrMsg(2, decorateToStringValue(lists(2)) + " did not contain at most one element of " + decorateToStringValue(Seq(2, 3, 4)), thisLineNumber - 2, lists), fileName, thisLineNumber - 2)

        val e2 = intercept[TestFailedException] {
          all (lists) should (contain atMostOneElementOf Seq(1, 3, 4) and contain atMostOneElementOf Seq(2, 3, 4))
        }
        checkMessageStackDepth(e2, allErrMsg(2, decorateToStringValue(lists(2)) + " contained at most one element of " + decorateToStringValue(Seq(1, 3, 4)) + ", but " + decorateToStringValue(lists(2)) + " did not contain at most one element of " + decorateToStringValue(Seq(2, 3, 4)), thisLineNumber - 2, lists), fileName, thisLineNumber - 2)

        val e4 = intercept[TestFailedException] {
          all (hiLists) should (contain atMostOneElementOf Seq("hi", "hello") and contain atMostOneElementOf Seq("hi", "he"))
        }
        checkMessageStackDepth(e4, allErrMsg(0, decorateToStringValue(hiLists(0)) + " contained at most one element of " + decorateToStringValue(Seq("hi", "hello")) + ", but " + decorateToStringValue(hiLists(0)) + " did not contain at most one element of " + decorateToStringValue(Seq("hi", "he")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)

        val e6 = intercept[TestFailedException] {
          all (lists) should (contain atMostOneElementOf Seq(1, 6, 8) and contain atMostOneElementOf Seq(2, 3, 4))
        }
        checkMessageStackDepth(e6, allErrMsg(2, decorateToStringValue(lists(2)) + " contained at most one element of " + decorateToStringValue(Seq(1, 6, 8)) + ", but " + decorateToStringValue(lists(2)) + " did not contain at most one element of " + decorateToStringValue(Seq(2, 3, 4)), thisLineNumber - 2, lists), fileName, thisLineNumber - 2)
      }

      it("should use the implicit Equality in scope") {
        implicit val ise = upperCaseStringEquality

        all (hiLists) should (contain atMostOneElementOf Seq("HI", "HO") and contain atMostOneElementOf Seq("HE", "HO"))

        val e1 = intercept[TestFailedException] {
          all (hiLists) should (contain atMostOneElementOf Seq("HI", "HE") and contain atMostOneElementOf Seq("HE", "HO"))
        }
        checkMessageStackDepth(e1, allErrMsg(0, decorateToStringValue(hiLists(0)) + " did not contain at most one element of " + decorateToStringValue(Seq("HI", "HE")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)

        val e2 = intercept[TestFailedException] {
          all (hiLists) should (contain atMostOneElementOf Seq("HI", "HO") and contain atMostOneElementOf Seq("HI", "HE"))
        }
        checkMessageStackDepth(e2, allErrMsg(0, decorateToStringValue(hiLists(0)) + " contained at most one element of " + decorateToStringValue(Seq("HI", "HO")) + ", but " + decorateToStringValue(hiLists(0)) + " did not contain at most one element of " + decorateToStringValue(Seq("HI", "HE")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)
      }

      it("should use an explicitly provided Equality") {
        (all (hiLists) should (contain atMostOneElementOf Seq("HI", "HO") and contain atMostOneElementOf Seq("HE", "HO"))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        val e1 = intercept[TestFailedException] {
          (all (hiLists) should (contain atMostOneElementOf Seq("HI", "HE") and contain atMostOneElementOf Seq("HE", "HO"))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, allErrMsg(0, decorateToStringValue(hiLists(0)) + " did not contain at most one element of " + decorateToStringValue(Seq("HI", "HE")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)

        val e2 = intercept[TestFailedException] {
          (all (hiLists) should (contain atMostOneElementOf Seq("HI", "HO") and contain atMostOneElementOf Seq("HI", "HE"))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e2, allErrMsg(0, decorateToStringValue(hiLists(0)) + " contained at most one element of " + decorateToStringValue(Seq("HI", "HO")) + ", but " + decorateToStringValue(hiLists(0)) + " did not contain at most one element of " + decorateToStringValue(Seq("HI", "HE")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)
      }

      it("should do nothing when RHS contain duplicated value") {
        all (list1s) should (contain atMostOneElementOf Seq(1, 3, 3, 8) and contain atMostOneElementOf Seq(1, 3, 4))
        all (list1s) should (contain atMostOneElementOf Seq(1, 3, 4) and contain atMostOneElementOf Seq(1, 3, 3, 8))
      }
    }

    describe("when used with (be (..) and contain atMostOneElementOf Seq(..))") {

      it("should do nothing if valid, else throw a TFE with an appropriate error message") {
        all (list1s) should (be (List(1, 2)) and contain atMostOneElementOf Seq(1, 3, 4))
        atLeast (2, lists) should (be (List(1, 2)) and contain atMostOneElementOf Seq(2, 3, 4))
        atMost (2, lists) should (be (List(1, 2)) and contain atMostOneElementOf Seq(2, 3, 4))
        no (lists) should (be (List(8)) and contain atMostOneElementOf Seq(3, 2, 1))

        val e1 = intercept[TestFailedException] {
          all (lists) should (be (List(1, 2)) and contain atMostOneElementOf Seq(1, 3, 4))
        }
        checkMessageStackDepth(e1, allErrMsg(2, decorateToStringValue(lists(2)) + " was not equal to " + decorateToStringValue(List(1, 2)), thisLineNumber - 2, lists), fileName, thisLineNumber - 2)

        val e2 = intercept[TestFailedException] {
          all (list1s) should (be (List(1, 2)) and contain atMostOneElementOf Seq(1, 2, 3))
        }
        checkMessageStackDepth(e2, allErrMsg(0, decorateToStringValue(list1s(0)) + " was equal to " + decorateToStringValue(List(1, 2)) + ", but " + decorateToStringValue(list1s(0)) + " did not contain at most one element of " + decorateToStringValue(Seq(1, 2, 3)), thisLineNumber - 2, list1s), fileName, thisLineNumber - 2)

        val e4 = intercept[TestFailedException] {
          all (hiLists) should (be (List("hi", "he")) and contain atMostOneElementOf Seq("hi", "he"))
        }
        checkMessageStackDepth(e4, allErrMsg(0, decorateToStringValue(hiLists(0)) + " was equal to " + decorateToStringValue(List("hi", "he")) + ", but " + decorateToStringValue(hiLists(0)) + " did not contain at most one element of " + decorateToStringValue(Seq("hi", "he")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)

        val e6 = intercept[TestFailedException] {
          all (list1s) should (be (List(1, 2)) and contain atMostOneElementOf Seq(1, 2, 3))
        }
        checkMessageStackDepth(e6, allErrMsg(0, decorateToStringValue(list1s(0)) + " was equal to " + decorateToStringValue(List(1, 2)) + ", but " + decorateToStringValue(list1s(0)) + " did not contain at most one element of " + decorateToStringValue(Seq(1, 2, 3)), thisLineNumber - 2, list1s), fileName, thisLineNumber - 2)
      }

      it("should use the implicit Equality in scope") {
        implicit val ise = upperCaseStringEquality

        all (hiLists) should (be (List("hi", "he")) and contain atMostOneElementOf Seq("HO", "HE"))

        val e1 = intercept[TestFailedException] {
          all (hiLists) should (be (List("ho")) and contain atMostOneElementOf Seq("HO", "HE"))
        }
        checkMessageStackDepth(e1, allErrMsg(0, decorateToStringValue(hiLists(0)) + " was not equal to " + decorateToStringValue(List("ho")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)

        val e2 = intercept[TestFailedException] {
          all (hiLists) should (be (List("hi", "he")) and contain atMostOneElementOf Seq("HI", "HE"))
        }
        checkMessageStackDepth(e2, allErrMsg(0, decorateToStringValue(hiLists(0)) + " was equal to " + decorateToStringValue(List("hi", "he")) + ", but " + decorateToStringValue(hiLists(0)) + " did not contain at most one element of " + decorateToStringValue(Seq("HI", "HE")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)
      }

      it("should use an explicitly provided Equality") {
        (all (hiLists) should (be (List("hi", "he")) and contain atMostOneElementOf Seq("HO", "HE"))) (decided by upperCaseStringEquality)
        val e1 = intercept[TestFailedException] {
          (all (hiLists) should (be (List("ho")) and contain atMostOneElementOf Seq("HO", "HE"))) (decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, allErrMsg(0, decorateToStringValue(hiLists(0)) + " was not equal to " + decorateToStringValue(List("ho")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)

        val e2 = intercept[TestFailedException] {
          (all (hiLists) should (be (List("hi", "he")) and contain atMostOneElementOf Seq("HI", "HE"))) (decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e2, allErrMsg(0, decorateToStringValue(hiLists(0)) + " was equal to " + decorateToStringValue(List("hi", "he")) + ", but " + decorateToStringValue(hiLists(0)) + " did not contain at most one element of " + decorateToStringValue(Seq("HI", "HE")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)
      }

      it("should do nothing when RHS contain duplicated value") {
        all (list1s) should (be (List(1, 2)) and contain atMostOneElementOf Seq(1, 3, 3, 8))
      }
    }

    describe("when used with (not contain atMostOneElementOf Seq(..) and not contain atMostOneElementOf Seq(..))") {

      it("should do nothing if valid, else throw a TFE with an appropriate error message") {
        all (list1s) should (not contain atMostOneElementOf (Seq(3, 2, 1)) and not contain atMostOneElementOf (Seq(1, 2, 3)))
        atLeast (2, lists) should (not contain atMostOneElementOf (Seq(1, 2, 8)) and not contain atMostOneElementOf (Seq(1, 2, 3)))
        atMost (2, lists) should (not contain atMostOneElementOf (Seq(1, 2, 8)) and contain atMostOneElementOf (Seq(1, 2, 3)))
        no (lists) should (not contain atMostOneElementOf (Seq(2, 6, 8)) and not contain atMostOneElementOf (Seq(3, 6, 8)))

        val e1 = intercept[TestFailedException] {
          all (lists) should (not contain atMostOneElementOf (Seq(1, 2, 8)) and not contain atMostOneElementOf (Seq(1, 2, 3)))
        }
        checkMessageStackDepth(e1, allErrMsg(2, decorateToStringValue(lists(2)) + " contained at most one element of " + decorateToStringValue(Seq(1, 2, 8)), thisLineNumber - 2, lists), fileName, thisLineNumber - 2)

        val e2 = intercept[TestFailedException] {
          all (lists) should (not contain atMostOneElementOf (Seq(1, 2, 3)) and not contain atMostOneElementOf (Seq(1, 2, 8)))
        }
        checkMessageStackDepth(e2, allErrMsg(2, decorateToStringValue(lists(2)) + " did not contain at most one element of " + decorateToStringValue(Seq(1, 2, 3)) + ", but " + decorateToStringValue(lists(2)) + " contained at most one element of " + decorateToStringValue(Seq(1, 2, 8)), thisLineNumber - 2, lists), fileName, thisLineNumber - 2)

        val e3 = intercept[TestFailedException] {
          all (hiLists) should (not contain atMostOneElementOf (Seq("hi", "hello")) and not contain atMostOneElementOf (Seq("ho", "hey", "howdy")))
        }
        checkMessageStackDepth(e3, allErrMsg(0, decorateToStringValue(hiLists(0)) + " contained at most one element of " + decorateToStringValue(Seq("hi", "hello")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)

        val e4 = intercept[TestFailedException] {
          all (hiLists) should (not contain atMostOneElementOf (Seq("hi", "he")) and not contain atMostOneElementOf (Seq("hi", "hello")))
        }
        checkMessageStackDepth(e4, allErrMsg(0, decorateToStringValue(hiLists(0)) + " did not contain at most one element of " + decorateToStringValue(Seq("hi", "he")) + ", but " + decorateToStringValue(hiLists(0)) + " contained at most one element of " + decorateToStringValue(Seq("hi", "hello")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)
      }

      it("should use the implicit Equality in scope") {
        implicit val ise = upperCaseStringEquality

        all (hiLists) should (not contain atMostOneElementOf (Seq("HI", "HE")) and not contain atMostOneElementOf (Seq("HE", "HI")))

        val e1 = intercept[TestFailedException] {
          all (hiLists) should (not contain atMostOneElementOf (Seq("HI", "HO")) and not contain atMostOneElementOf (Seq("HE", "HI")))
        }
        checkMessageStackDepth(e1, allErrMsg(0, decorateToStringValue(hiLists(0)) + " contained at most one element of " + decorateToStringValue(Seq("HI", "HO")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)

        val e2 = intercept[TestFailedException] {
          all (hiLists) should (not contain atMostOneElementOf (Seq("HI", "HE")) and not contain atMostOneElementOf (Seq("HI", "HO")))
        }
        checkMessageStackDepth(e2, allErrMsg(0, decorateToStringValue(hiLists(0)) + " did not contain at most one element of " + decorateToStringValue(Seq("HI", "HE")) + ", but " + decorateToStringValue(hiLists(0)) + " contained at most one element of " + decorateToStringValue(Seq("HI", "HO")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)
      }

      it("should use an explicitly provided Equality") {
        (all (hiLists) should (not contain atMostOneElementOf (Seq("HI", "HE")) and not contain atMostOneElementOf (Seq("HE", "HI")))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        val e1 = intercept[TestFailedException] {
          (all (hiLists) should (not contain atMostOneElementOf (Seq("HI", "HO")) and not contain atMostOneElementOf (Seq("HE", "HI")))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, allErrMsg(0, decorateToStringValue(hiLists(0)) + " contained at most one element of " + decorateToStringValue(Seq("HI", "HO")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)

        val e2 = intercept[TestFailedException] {
          (all (hiLists) should (not contain atMostOneElementOf (Seq("HI", "HE")) and not contain atMostOneElementOf (Seq("HI", "HO")))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e2, allErrMsg(0, decorateToStringValue(hiLists(0)) + " did not contain at most one element of " + decorateToStringValue(Seq("HI", "HE")) + ", but " + decorateToStringValue(hiLists(0)) + " contained at most one element of " + decorateToStringValue(Seq("HI", "HO")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)
      }

      it("should do nothing when RHS contain duplicated value") {
        all (list1s) should (not contain atMostOneElementOf (Seq(1, 2, 2, 3)) and not contain atMostOneElementOf (Seq(1, 2, 3)))
        all (list1s) should (not contain atMostOneElementOf (Seq(1, 2, 3)) and not contain atMostOneElementOf (Seq(1, 2, 2, 3)))
      }
    }

    describe("when used with (not be (..) and not contain atMostOneElementOf Seq(..))") {

      it("should do nothing if valid, else throw a TFE with an appropriate error message") {
        all (list1s) should (not be (List(2, 3)) and not contain atMostOneElementOf (Seq(1, 2, 3)))
        atLeast (2, lists) should (not be (List(2, 3)) and not contain atMostOneElementOf (Seq(1, 2, 3)))
        atMost (2, lists) should (not be (List(2, 3)) and contain atMostOneElementOf (Seq(1, 2, 3)))
        no (list1s) should (not be (List(1, 2)) and not contain atMostOneElementOf (Seq(3, 6, 8)))

        val e1 = intercept[TestFailedException] {
          all (lists) should (not be (List(2, 3)) and not contain atMostOneElementOf (Seq(1, 2, 3)))
        }
        checkMessageStackDepth(e1, allErrMsg(2, decorateToStringValue(lists(2)) + " was equal to " + decorateToStringValue(List(2, 3)), thisLineNumber - 2, lists), fileName, thisLineNumber - 2)

        val e2 = intercept[TestFailedException] {
          all (lists) should (not be (List(2, 3, 4)) and not contain atMostOneElementOf (Seq(1, 2, 8)))
        }
        checkMessageStackDepth(e2, allErrMsg(2, decorateToStringValue(lists(2)) + " was not equal to " + decorateToStringValue(List(2, 3, 4)) + ", but " + decorateToStringValue(lists(2)) + " contained at most one element of " + decorateToStringValue(Seq(1, 2, 8)), thisLineNumber - 2, lists), fileName, thisLineNumber - 2)

        val e3 = intercept[TestFailedException] {
          all (hiLists) should (not be (List("hi", "he")) and not contain atMostOneElementOf (Seq("ho", "hey", "howdy")))
        }
        checkMessageStackDepth(e3, allErrMsg(0, decorateToStringValue(hiLists(0)) + " was equal to " + decorateToStringValue(List("hi", "he")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)

        val e4 = intercept[TestFailedException] {
          all (hiLists) should (not be (List("hi", "ho")) and not contain atMostOneElementOf (Seq("hi", "hello")))
        }
        checkMessageStackDepth(e4, allErrMsg(0, decorateToStringValue(hiLists(0)) + " was not equal to " + decorateToStringValue(List("hi", "ho")) + ", but " + decorateToStringValue(hiLists(0)) + " contained at most one element of " + decorateToStringValue(Seq("hi", "hello")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)
      }

      it("should use the implicit Equality in scope") {
        implicit val ise = upperCaseStringEquality

        all (hiLists) should (not be (List("HI", "HO")) and not contain atMostOneElementOf (Seq("HI", "HE")))

        val e1 = intercept[TestFailedException] {
          all (hiLists) should (not be (List("hi", "he")) and not contain atMostOneElementOf (Seq("hi", "he")))
        }
        checkMessageStackDepth(e1, allErrMsg(0, decorateToStringValue(hiLists(0)) + " was equal to " + decorateToStringValue(List("hi", "he")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)

        val e2 = intercept[TestFailedException] {
          all (hiLists) should (not be (List("hi", "ho")) and not contain atMostOneElementOf (Seq("HI", "HO")))
        }
        checkMessageStackDepth(e2, allErrMsg(0, decorateToStringValue(hiLists(0)) + " was not equal to " + decorateToStringValue(List("hi", "ho")) + ", but " + decorateToStringValue(hiLists(0)) + " contained at most one element of " + decorateToStringValue(Seq("HI", "HO")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)
      }

      it("should use an explicitly provided Equality") {
        (all (hiLists) should (not be (List("HI", "HO")) and not contain atMostOneElementOf (Seq("HI", "HE")))) (decided by upperCaseStringEquality)
        val e1 = intercept[TestFailedException] {
          (all (hiLists) should (not be (List("hi", "he")) and not contain atMostOneElementOf (Seq("HI", "HE")))) (decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, allErrMsg(0, decorateToStringValue(hiLists(0)) + " was equal to " + decorateToStringValue(List("hi", "he")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)

        val e2 = intercept[TestFailedException] {
          (all (hiLists) should (not be (List("hi", "ho")) and not contain atMostOneElementOf (Seq("HI", "HO")))) (decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e2, allErrMsg(0, decorateToStringValue(hiLists(0)) + " was not equal to " + decorateToStringValue(List("hi", "ho")) + ", but " + decorateToStringValue(hiLists(0)) + " contained at most one element of " + decorateToStringValue(Seq("HI", "HO")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)
      }

      it("should do nothing when RHS contain duplicated value") {
        all (list1s) should (not be (List(2, 3)) and not contain atMostOneElementOf (Seq(1, 2, 2, 3)))
      }
    }
  }
}
