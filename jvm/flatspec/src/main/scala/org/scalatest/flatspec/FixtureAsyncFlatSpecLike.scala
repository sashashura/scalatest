/*
 * Copyright 2001-2013 Artima, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.scalatest.flatspec

import org.scalatest._
import org.scalatest.fixture.{NoArgTestWrapper, AsyncPendingTransformer}
import org.scalactic.source
import scala.concurrent.Future
import org.scalatest.Suite.autoTagClassAnnotations
import verbs.{ResultOfTaggedAsInvocation, ResultOfStringPassedToVerb, BehaveWord, ShouldVerb, MustVerb, CanVerb, StringVerbStringInvocation, StringVerbBehaveLikeInvocation}
import scala.util.Try

/**
 * Implementation trait for class <code>FixtureAsyncFlatSpec</code>, which is
 * a sister class to <code>org.scalatest.flatspec.FixtureAsyncFlatSpec</code> that can pass a
 * fixture object into its tests.
 *
 * <p>
 * <a href="FixtureAsyncFlatSpec.html"><code>FixtureAsyncFlatSpec</code></a> is a class,
 * not a trait, to minimize compile time given there is a slight compiler
 * overhead to mixing in traits compared to extending classes. If you need
 * to mix the behavior of <code>FixtureAsyncFlatSpec</code> into some other
 * class, you can use this trait instead, because class
 * <code>FixtureAsyncFlatSpec</code> does nothing more than extend this trait and add a nice <code>toString</code> implementation.
 * </p>
 *
 * <p>
 * See the documentation of the class for a <a href="FixtureAsyncFlatSpec.html">detailed
 * overview of <code>FixtureAsyncFlatSpec</code></a>.
 * </p>
 *
 * @author Bill Venners
 */
//SCALATESTJS-ONLY @scala.scalajs.reflect.annotation.EnableReflectiveInstantiation
//SCALATESTNATIVE-ONLY @scala.scalanative.reflect.annotation.EnableReflectiveInstantiation
@Finders(Array("org.scalatest.finders.FlatSpecFinder"))
trait FixtureAsyncFlatSpecLike extends org.scalatest.FixtureAsyncTestSuite with ShouldVerb with MustVerb with CanVerb with Informing with Notifying with Alerting with Documenting { thisSuite =>

  private final val engine = new AsyncFixtureEngine[FixtureParam](Resources.concurrentFixtureFlatSpecMod, "FixtureFlatSpec")

  import engine._

  /**
   * Returns an <code>Informer</code> that during test execution will forward strings passed to its
   * <code>apply</code> method to the current reporter. If invoked in a constructor, it
   * will register the passed string for forwarding later during test execution. If invoked from inside a scope,
   * it will forward the information to the current reporter immediately.  If invoked from inside a test function,
   * it will record the information and forward it to the current reporter only after the test completed, as <code>recordedEvents</code>
   * of the test completed event, such as <code>TestSucceeded</code>. If invoked at any other time, it will print to the standard output.
   * This method can be called safely by any thread.
   */
  protected def info: Informer = atomicInformer.get

  /**
   * Returns a <code>Notifier</code> that during test execution will forward strings (and other objects) passed to its
   * <code>apply</code> method to the current reporter. If invoked in a constructor, it
   * will register the passed string for forwarding later during test execution. If invoked while this
   * <code>FixtureAsyncFlatSpec</code> is being executed, such as from inside a test function, it will forward the information to
   * the current reporter immediately. If invoked at any other time, it will
   * print to the standard output. This method can be called safely by any thread.
   */
  protected def note: Notifier = atomicNotifier.get

  /**
   * Returns an <code>Alerter</code> that during test execution will forward strings (and other objects) passed to its
   * <code>apply</code> method to the current reporter. If invoked in a constructor, it
   * will register the passed string for forwarding later during test execution. If invoked while this
   * <code>FixtureAsyncFlatSpec</code> is being executed, such as from inside a test function, it will forward the information to
   * the current reporter immediately. If invoked at any other time, it will
   * print to the standard output. This method can be called safely by any thread.
   */
  protected def alert: Alerter = atomicAlerter.get

  /**
   * Returns a <code>Documenter</code> that during test execution will forward strings passed to its
   * <code>apply</code> method to the current reporter. If invoked in a constructor, it
   * will register the passed string for forwarding later during test execution. If invoked from inside a scope,
   * it will forward the information to the current reporter immediately.  If invoked from inside a test function,
   * it will record the information and forward it to the current reporter only after the test completed, as <code>recordedEvents</code>
   * of the test completed event, such as <code>TestSucceeded</code>. If invoked at any other time, it will print to the standard output.
   * This method can be called safely by any thread.
   */
  protected def markup: Documenter = atomicDocumenter.get

  private final def registerAsyncTestImpl(testText: String, testTags: Tag*)(testFun: FixtureParam => Future[compatible.Assertion], pos: source.Position): Unit = {
    engine.registerAsyncTest(testText, transformToOutcome(testFun), Resources.testCannotBeNestedInsideAnotherTest, None, None, pos, testTags: _*)
  }

  // SKIP-DOTTY-START
  final def registerAsyncTest(testText: String, testTags: Tag*)(testFun: FixtureParam => Future[compatible.Assertion])(implicit pos: source.Position): Unit = {
    registerAsyncTestImpl(testText, testTags: _*)(testFun, pos)
  }
  // SKIP-DOTTY-END
  //DOTTY-ONLY inline def registerAsyncTest(testText: String, testTags: Tag*)(testFun: FixtureParam => Future[compatible.Assertion])(implicit pos: source.Position): Unit = {
  //DOTTY-ONLY   ${ source.Position.withPosition[Unit]('{(pos: source.Position) => registerAsyncTestImpl(testText, testTags: _*)(testFun, pos) }) } 
  //DOTTY-ONLY }

  private final def registerIgnoredAsyncTestImpl(testText: String, testTags: Tag*)(testFun: FixtureParam => Future[compatible.Assertion], pos: source.Position): Unit = {
    engine.registerIgnoredAsyncTest(testText, transformToOutcome(testFun), Resources.testCannotBeNestedInsideAnotherTest, None, pos, testTags: _*)
  }

  // SKIP-DOTTY-START
  final def registerIgnoredAsyncTest(testText: String, testTags: Tag*)(testFun: FixtureParam => Future[compatible.Assertion])(implicit pos: source.Position): Unit = {
    registerIgnoredAsyncTestImpl(testText, testTags: _*)(testFun, pos)
  }
  // SKIP-DOTTY-END
  //DOTTY-ONLY inline def registerIgnoredAsyncTest(testText: String, testTags: Tag*)(testFun: FixtureParam => Future[compatible.Assertion])(implicit pos: source.Position): Unit = {
  //DOTTY-ONLY   ${ source.Position.withPosition[Unit]('{(pos: source.Position) => registerAsyncTestImpl(testText, testTags: _*)(testFun, pos) }) } 
  //DOTTY-ONLY }

  /**
   * Register a test with the given spec text, optional tags, and test function value that takes no arguments.
   * An invocation of this method is called an &ldquo;example.&rdquo;
   *
   * This method will register the test for later execution via an invocation of one of the <code>execute</code>
   * methods. The name of the test will be a concatenation of the text of all surrounding describers,
   * from outside in, and the passed spec text, with one space placed between each item. (See the documenation
   * for <code>testNames</code> for an example.) The resulting test name must not have been registered previously on
   * this <code>FixtureAsyncFlatSpec</code> instance.
   *
   * @param specText the specification text, which will be combined with the descText of any surrounding describers
   * to form the test name
   * @param testTags the optional list of tags for this test
   * @param methodName caller's method name
   * @param testFun the test function
   * @throws DuplicateTestNameException if a test with the same name has been registered previously
   * @throws TestRegistrationClosedException if invoked after <code>run</code> has been invoked on this suite
   * @throws NullArgumentException if <code>specText</code> or any passed test tag is <code>null</code>
   */

  private def registerAsyncTestToRun(specText: String, testTags: List[Tag], methodName: String, testFun: FixtureParam => Future[compatible.Assertion], pos: source.Position): Unit = {
    // TODO: This is what was being used before but it is wrong
    def testRegistrationClosedMessageFun: String =
      methodName match {
        case "in" => Resources.inCannotAppearInsideAnotherInOrIs
        case "is" => Resources.isCannotAppearInsideAnotherInOrIs
      }
    engine.registerAsyncTest(specText, transformToOutcome(testFun), testRegistrationClosedMessageFun, None, None, pos, testTags: _*)
  }

  private def registerPendingTestToRun(specText: String, testTags: List[Tag], methodName: String, testFun: FixtureParam => PendingStatement, pos: source.Position): Unit = {
    // TODO: This is what was being used before but it is wrong
    def testRegistrationClosedMessageFun: String =
      methodName match {
        case "in" => Resources.inCannotAppearInsideAnotherInOrIs
        case "is" => Resources.isCannotAppearInsideAnotherInOrIs
      }
    engine.registerAsyncTest(specText, AsyncPendingTransformer(testFun), testRegistrationClosedMessageFun, None, None, pos, testTags: _*)
  }

  /**
   * Class that supports the registration of a &ldquo;subject&rdquo; being specified and tested via the
   * instance referenced from <code>FixtureAsyncFlatSpec</code>'s <code>behavior</code> field.
   *
   * <p>
   * This field enables syntax such as the following subject registration:
   * </p>
   *
   * <pre class="stHighlight">
   * behavior of "A Stack"
   * ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of the <code>behavior</code> field, see the <a href="AnyFlatSpec.html">main documentation</a>
   * for trait <code>AnyFlatSpec</code>.
   * </p>
   */
  protected final class BehaviorWord {

    private final def ofImpl(description: String, pos: source.Position): Unit = {
      registerFlatBranch(description, Resources.behaviorOfCannotAppearInsideAnIn, pos)
    }

    /**
     * Supports the registration of a &ldquo;subject&rdquo; being specified and tested via the
     * instance referenced from <code>FixtureAsyncFlatSpec</code>'s <code>behavior</code> field.
     *
     * <p>
     * This method enables syntax such as the following subject registration:
     * </p>
     *
     * <pre class="stHighlight">
     * behavior of "A Stack"
     *          ^
     * </pre>
     *
     * <p>
     * For more information and examples of the use of this method, see the <a href="AnyFlatSpec.html">main documentation</a>
     * for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param description the description text
     */
    // SKIP-DOTTY-START
    def of(description: String)(implicit pos: source.Position): Unit = {
      ofImpl(description, pos)
    }
    // SKIP-DOTTY-END
    //DOTTY-ONLY inline def of(description: String): Unit = {
    //DOTTY-ONLY   ${ source.Position.withPosition[Unit]('{(pos: source.Position) => ofImpl(description, pos) }) } 
    //DOTTY-ONLY }
  }

  /**
   * Supports the registration of a &ldquo;subject&rdquo; being specified and tested.
   *
   * <p>
   * This field enables syntax such as the following subject registration:
   * </p>
   *
   * <pre class="stHighlight">
   * behavior of "A Stack"
   * ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of the <code>behavior</code> field, see the <a href="AnyFlatSpec.html">main documentation</a>
   * for trait <code>AnyFlatSpec</code>.
   * </p>
   */
  protected val behavior = new BehaviorWord

  // TODO: Do a walk through. Are all these being used. I guess I'll find out when
  // I document them.
  /**
   * Class that supports the registration of tagged tests via the <code>ItWord</code> instance
   * referenced from <code>FixtureAsyncFlatSpec</code>'s <code>it</code> field.
   *
   * <p>
   * This class enables syntax such as the following tagged test registration:
   * </p>
   *
   * <pre class="stHighlight">
   * it should "pop values in last-in-first-out order" taggedAs(SlowTest) in { ... }
   *                                                                      ^
   * </pre>
   *
   * <p>
   * It also enables syntax such as the following registration of an ignored, tagged test:
   * </p>
   *
   * <pre class="stHighlight">
   * it should "pop values in last-in-first-out order" taggedAs(SlowTest) ignore { ... }
   *                                                                      ^
   * </pre>
   *
   * <p>
   * In addition, it enables syntax such as the following registration of a pending, tagged test:
   * </p>
   *
   * <pre class="stHighlight">
   * it should "pop values in last-in-first-out order" taggedAs(SlowTest) is (pending)
   *                                                                      ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of the <code>it</code> field to register tagged tests, see
   * the <a href="AnyFlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation for trait <code>AnyFlatSpec</code>.
   * </p>
   *
   * @param verb the verb
   * @param name the name
   * @param tags the list of tags
   */
  protected final class ItVerbStringTaggedAs(verb: String, name: String, tags: List[Tag]) {

    private final def inImpl(testFun: () => Future[compatible.Assertion], pos: source.Position): Unit = {
      registerAsyncTestToRun(verb.trim + " " + name.trim, tags, "in", new NoArgTestWrapper[FixtureParam, Future[compatible.Assertion]](testFun), pos)
    }

    /**
     * Supports the registration of tagged, no-arg tests in a <code>FixtureAnyFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * it must "pop values in last-in-first-out order" taggedAs(SlowTest) in { () => ... }
     *                                                                    ^
     * </pre>
     *
     * <p>
     * For examples of tagged test registration, see
     * the <a href="AnyFlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param testFun the test function
     */
    // SKIP-DOTTY-START
    def in(testFun: () => Future[compatible.Assertion])(implicit pos: source.Position): Unit = {
      inImpl(testFun, pos)
    }
    // SKIP-DOTTY-END
    //DOTTY-ONLY inline def in(testFun: () => Future[compatible.Assertion]): Unit = {
    //DOTTY-ONLY   ${ source.Position.withPosition[Unit]('{(pos: source.Position) => inImpl(testFun, pos) }) } 
    //DOTTY-ONLY }

    /**
     * Supports the registration of tagged, one-arg tests (tests that take a <code>FixtureParam</code> object as a parameter) in a <code>FixtureAnyFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * it must "pop values in last-in-first-out order" taggedAs(SlowTest) in { fixture => ... }
     *                                                                    ^
     * </pre>
     *
     * <p>
     * For examples of tagged test registration, see
     * the <a href="AnyFlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param testFun the test function
     */
    // SKIP-DOTTY-START
    def in(testFun: FixtureParam => Future[compatible.Assertion])(implicit pos: source.Position): Unit = {
      registerAsyncTestToRun(verb.trim + " " + name.trim, tags, "in", testFun, pos)
    }
    // SKIP-DOTTY-END
    //DOTTY-ONLY inline def in(testFun: FixtureParam => Future[compatible.Assertion]): Unit = {
    //DOTTY-ONLY   ${ source.Position.withPosition[Unit]('{(pos: source.Position) => registerAsyncTestToRun(verb.trim + " " + name.trim, tags, "in", testFun, pos) }) } 
    //DOTTY-ONLY }

    /**
     * Supports the registration of pending, tagged tests in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * it must "pop values in last-in-first-out order" taggedAs(SlowTest) is (pending)
     *                                                                    ^
     * </pre>
     *
     * <p>
     * For examples of pending test registration, see the <a href="AnyFlatSpec.html#PendingTests">Pending tests section</a> in the main documentation
     * for trait <code>AnyFlatSpec</code>.  And for examples of tagged test registration, see
     * the <a href="AnyFlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param testFun the test function
     */
    // SKIP-DOTTY-START
    def is(testFun: => PendingStatement)(implicit pos: source.Position): Unit = {
      registerPendingTestToRun(verb.trim + " " + name.trim, tags, "is", unusedFixtureParam => testFun, pos)
    }
    // SKIP-DOTTY-END
    //DOTTY-ONLY inline def is(testFun: => PendingStatement): Unit = {
    //DOTTY-ONLY   ${ source.Position.withPosition[Unit]('{(pos: source.Position) => registerPendingTestToRun(verb.trim + " " + name.trim, tags, "is", unusedFixtureParam => testFun, pos) }) } 
    //DOTTY-ONLY }

    private final def ignoreImpl(testFun: () => Future[compatible.Assertion], pos: source.Position): Unit = {
      registerAsyncTestToIgnore(verb.trim + " " + name.trim, tags, new NoArgTestWrapper(testFun), pos)
    }

    /**
     * Supports the registration of ignored, tagged, no-arg tests in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * it must "pop values in last-in-first-out order" taggedAs(SlowTest) ignore { () => ... }
     *                                                                    ^
     * </pre>
     *
     * <p>
     * For examples of ignored test registration, see the <a href="AnyFlatSpec.html#IgnoredTests">Ignored tests section</a> in the main documentation
     * for trait <code>AnyFlatSpec</code>.  And for examples of tagged test registration, see
     * the <a href="AnyFlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param testFun the test function
     */
    // SKIP-DOTTY-START
    def ignore(testFun: () => Future[compatible.Assertion])(implicit pos: source.Position): Unit = {
      ignoreImpl(testFun, pos)
    }
    // SKIP-DOTTY-END
    //DOTTY-ONLY inline def ignore(testFun: () => Future[compatible.Assertion]): Unit = {
    //DOTTY-ONLY   ${ source.Position.withPosition[Unit]('{(pos: source.Position) => ignoreImpl(testFun, pos) }) } 
    //DOTTY-ONLY }

    /**
     * Supports the registration of ignored, tagged, one-arg tests (tests that take a <code>FixtureParam</code> object
     * as a parameter) in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * it must "pop values in last-in-first-out order" taggedAs(SlowTest) ignore { fixture => ... }
     *                                                                    ^
     * </pre>
     *
     * <p>
     * For examples of ignored test registration, see the <a href="AnyFlatSpec.html#IgnoredTests">Ignored tests section</a> in the main documentation
     * for trait <code>AnyFlatSpec</code>.  And for examples of tagged test registration, see
     * the <a href="AnyFlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param testFun the test function
     */
    // SKIP-DOTTY-START
    def ignore(testFun: FixtureParam => Future[compatible.Assertion])(implicit pos: source.Position): Unit = {
      registerAsyncTestToIgnore(verb.trim + " " + name.trim, tags, testFun, pos)
    }
    // SKIP-DOTTY-END
    //DOTTY-ONLY inline def ignore(testFun: FixtureParam => Future[compatible.Assertion]): Unit = {
    //DOTTY-ONLY   ${ source.Position.withPosition[Unit]('{(pos: source.Position) => registerAsyncTestToIgnore(verb.trim + " " + name.trim, tags, testFun, pos) }) } 
    //DOTTY-ONLY }
  }

  /**
   * Class that supports test registration via the instance referenced from <code>FixtureAnyFlatSpec</code>'s <code>it</code> field.
   *
   * <p>
   * This class enables syntax such as the following test registration:
   * </p>
   *
   * <pre class="stHighlight">
   * it should "pop values in last-in-first-out order" in { ... }
   *                                                   ^
   * </pre>
   *
   * <p>
   * It also enables syntax such as the following registration of an ignored test:
   * </p>
   *
   * <pre class="stHighlight">
   * it should "pop values in last-in-first-out order" ignore { ... }
   *                                                   ^
   * </pre>
   *
   * <p>
   * In addition, it enables syntax such as the following registration of a pending test:
   * </p>
   *
   * <pre class="stHighlight">
   * it should "pop values in last-in-first-out order" is (pending)
   *                                                   ^
   * </pre>
   *
   * <p>
   * And finally, it also enables syntax such as the following tagged test registration:
   * </p>
   *
   * <pre class="stHighlight">
   * it should "pop values in last-in-first-out order" taggedAs(SlowTest) in { ... }
   *                                                   ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of the <code>it</code> field, see the <a href="AnyFlatSpec.html">main documentation</a>
   * for trait <code>AnyFlatSpec</code>.
   * </p>
   *
   * @param verb the verb
   * @param name the name
   */
  protected final class ItVerbString(verb: String, name: String) {

    private final def inImpl(testFun: () => Future[compatible.Assertion], pos: source.Position): Unit = {
      registerAsyncTestToRun(verb.trim + " " + name.trim, List(), "in", new NoArgTestWrapper(testFun), pos)
    }

    /**
     * Supports the registration of no-arg tests in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * it must "pop values in last-in-first-out order" in { () => ... }
     *                                                 ^
     * </pre>
     *
     * <p>
     * For examples of no-arg test registration, see the <a href="AnyFlatSpec.html">main documentation</a>
     * for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param testFun the test function
     */
    // SKIP-DOTTY-START
    def in(testFun: () => Future[compatible.Assertion])(implicit pos: source.Position): Unit = {
      inImpl(testFun, pos)
    }
    // SKIP-DOTTY-END
    //DOTTY-ONLY inline def in(testFun: () => Future[compatible.Assertion]): Unit = {
    //DOTTY-ONLY   ${ source.Position.withPosition[Unit]('{(pos: source.Position) => inImpl(testFun, pos) }) } 
    //DOTTY-ONLY }

    /**
     * Supports the registration of one-arg tests (tests that take a <code>FixtureParam</code> object as a parameter) in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * it must "pop values in last-in-first-out order" in { fixture => ... }
     *                                                 ^
     * </pre>
     *
     * <p>
     * For examples of one-arg test registration, see the <a href="AnyFlatSpec.html">main documentation</a>
     * for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param testFun the test function
     */
    // SKIP-DOTTY-START
    def in(testFun: FixtureParam => Future[compatible.Assertion])(implicit pos: source.Position): Unit = {
      registerAsyncTestToRun(verb.trim + " " + name.trim, List(), "in", testFun, pos)
    }
    // SKIP-DOTTY-END
    //DOTTY-ONLY inline def in(testFun: FixtureParam => Future[compatible.Assertion]): Unit = {
    //DOTTY-ONLY   ${ source.Position.withPosition[Unit]('{(pos: source.Position) => registerAsyncTestToRun(verb.trim + " " + name.trim, List(), "in", testFun, pos) }) } 
    //DOTTY-ONLY }

    /**
     * Supports the registration of pending tests in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * it must "pop values in last-in-first-out order" is (pending)
     *                                                 ^
     * </pre>
     *
     * <p>
     * For examples of pending test registration, see the <a href="AnyFlatSpec.html#PendingTests">Pending tests section</a> in the main documentation
     * for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param testFun the test function
     */
    // SKIP-DOTTY-START
    def is(testFun: => PendingStatement)(implicit pos: source.Position): Unit = {
      registerPendingTestToRun(verb.trim + " " + name.trim, List(), "is", unusedFixtureParam => testFun, pos)
    }
    // SKIP-DOTTY-END
    //DOTTY-ONLY inline def is(testFun: => PendingStatement): Unit = {
    //DOTTY-ONLY   ${ source.Position.withPosition[Unit]('{(pos: source.Position) => registerPendingTestToRun(verb.trim + " " + name.trim, List(), "is", unusedFixtureParam => testFun, pos) }) } 
    //DOTTY-ONLY }

    private final def ignoreImpl(testFun: () => Future[compatible.Assertion], pos: source.Position): Unit = {
      registerAsyncTestToIgnore(verb.trim + " " + name.trim, List(), new NoArgTestWrapper(testFun), pos)
    }

    /**
     * Supports the registration of ignored no-arg tests in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * it must "pop values in last-in-first-out order" ignore { () => ... }
     *                                                 ^
     * </pre>
     *
     * <p>
     * For examples of ignored test registration, see the <a href="AnyFlatSpec.html#IgnoredTests">Ignored tests section</a> in the main documentation
     * for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param testFun the test function
     */
    // SKIP-DOTTY-START
    def ignore(testFun: () => Future[compatible.Assertion])(implicit pos: source.Position): Unit = {
      ignoreImpl(testFun, pos)
    }
    // SKIP-DOTTY-END
    //DOTTY-ONLY inline def ignore(testFun: () => Future[compatible.Assertion]): Unit = {
    //DOTTY-ONLY   ${ source.Position.withPosition[Unit]('{(pos: source.Position) => ignoreImpl(testFun, pos) }) } 
    //DOTTY-ONLY }

    /**
     * Supports the registration of ignored one-arg tests (tests that take a <code>FixtureParam</code> object as a parameter) in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * it must "pop values in last-in-first-out order" ignore { fixture => ... }
     *                                                 ^
     * </pre>
     *
     * <p>
     * For examples of ignored test registration, see the <a href="AnyFlatSpec.html#IgnoredTests">Ignored tests section</a> in the main documentation
     * for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param testFun the test function
     */
    // SKIP-DOTTY-START
    def ignore(testFun: FixtureParam => Future[compatible.Assertion])(implicit pos: source.Position): Unit = {
      registerAsyncTestToIgnore(verb.trim + " " + name.trim, List(), testFun, pos)
    }
    // SKIP-DOTTY-END
    //DOTTY-ONLY inline def ignore(testFun: FixtureParam => Future[compatible.Assertion]): Unit = {
    //DOTTY-ONLY   ${ source.Position.withPosition[Unit]('{(pos: source.Position) => registerAsyncTestToIgnore(verb.trim + " " + name.trim, List(), testFun, pos) }) } 
    //DOTTY-ONLY }

    /**
     * Supports the registration of tagged tests in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * it must "pop values in last-in-first-out order" taggedAs(SlowTest) in { ... }
     *                                                 ^
     * </pre>
     *
     * <p>
     * For examples of tagged test registration, see the <a href="AnyFlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation
     * for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param firstTestTag the first mandatory test tag
     * @param otherTestTags the others additional test tags
     */
    def taggedAs(firstTestTag: Tag, otherTestTags: Tag*) = {
      val tagList = firstTestTag :: otherTestTags.toList
      new ItVerbStringTaggedAs(verb, name, tagList)
    }
  }

  /**
   * Class that supports test (and shared test) registration via the instance referenced from <code>FixtureAsyncFlatSpec</code>'s <code>it</code> field.
   *
   * <p>
   * This class enables syntax such as the following test registration:
   * </p>
   *
   * <pre class="stHighlight">
   * it should "pop values in last-in-first-out order" in { ... }
   * ^
   * </pre>
   *
   * <p>
   * It also enables syntax such as the following shared test registration:
   * </p>
   *
   * <pre class="stHighlight">
   * it should behave like nonEmptyStack(lastItemPushed)
   * ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of the <code>it</code> field, see the main documentation
   * for trait <a href="AnyFlatSpec.html"><code>AnyFlatSpec</code></a>.
   * </p>
   */
  protected final class ItWord {

    /**
     * Supports the registration of tests with <code>should</code> in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * it should "pop values in last-in-first-out order" in { ... }
     *    ^
     * </pre>
     *
     * <p>
     * For examples of test registration, see the <a href="AnyFlatSpec.html">main documentation</a>
     * for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param string the string description
     */
    def should(string: String) = new ItVerbString("should", string)

    /**
     * Supports the registration of tests with <code>must</code> in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * it must "pop values in last-in-first-out order" in { ... }
     *    ^
     * </pre>
     *
     * <p>
     * For examples of test registration, see the <a href="AnyFlatSpec.html">main documentation</a>
     * for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param string the string description
     */
    def must(string: String) = new ItVerbString("must", string)

    /**
     * Supports the registration of tests with <code>can</code> in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * it can "pop values in last-in-first-out order" in { ... }
     *    ^
     * </pre>
     *
     * <p>
     * For examples of test registration, see the <a href="AnyFlatSpec.html">main documentation</a>
     * for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param string the string description
     */
    def can(string: String) = new ItVerbString("can", string)

    /**
     * Supports the registration of shared tests with <code>should</code> in a <code>FixtureAnyFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * it should behave like nonFullStack(stackWithOneItem)
     *    ^
     * </pre>
     *
     * <p>
     * For examples of shared tests, see the <a href="AnyFlatSpec.html#SharedTests">Shared tests section</a>
     * in the main documentation for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param behaveWord the <code>BehaveWord</code>
     */
    def should(behaveWord: BehaveWord) = behaveWord

    /**
     * Supports the registration of shared tests with <code>must</code> in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * it must behave like nonFullStack(stackWithOneItem)
     *    ^
     * </pre>
     *
     * <p>
     * For examples of shared tests, see the <a href="AnyFlatSpec.html#SharedTests">Shared tests section</a>
     * in the main documentation for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param behaveWord the <code>BehaveWord</code>
     */
    def must(behaveWord: BehaveWord) = behaveWord

    /**
     * Supports the registration of shared tests with <code>can</code> in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * it can behave like nonFullStack(stackWithOneItem)
     *    ^
     * </pre>
     *
     * <p>
     * For examples of shared tests, see the <a href="AnyFlatSpec.html#SharedTests">Shared tests section</a>
     * in the main documentation for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param behaveWord the <code>BehaveWord</code>
     */
    def can(behaveWord: BehaveWord) = behaveWord
  }

  /**
   * Supports test (and shared test) registration in <code>FixtureAsyncFlatSpec</code>s.
   *
   * <p>
   * This field enables syntax such as the following test registration:
   * </p>
   *
   * <pre class="stHighlight">
   * it should "pop values in last-in-first-out order" in { ... }
   * ^
   * </pre>
   *
   * <p>
   * It also enables syntax such as the following shared test registration:
   * </p>
   *
   * <pre class="stHighlight">
   * it should behave like nonEmptyStack(lastItemPushed)
   * ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of the <code>it</code> field, see the main documentation
   * for trait <a href="AnyFlatSpec.html"><code>AnyFlatSpec</code></a>.
   * </p>
   */
  protected val it = new ItWord

  /**
   * Class that supports the registration of tagged tests via the <code>TheyWord</code> instance
   * referenced from <code>FixtureAsyncFlatSpec</code>'s <code>they</code> field.
   *
   * <p>
   * This class enables syntax such as the following tagged test registration:
   * </p>
   *
   * <pre class="stHighlight">
   * they should "pop values in last-in-first-out order" taggedAs(SlowTest) in { ... }
   *                                                                        ^
   * </pre>
   *
   * <p>
   * It also enables syntax such as the following registration of an ignored, tagged test:
   * </p>
   *
   * <pre class="stHighlight">
   * they should "pop values in last-in-first-out order" taggedAs(SlowTest) ignore { ... }
   *                                                                        ^
   * </pre>
   *
   * <p>
   * In addition, it enables syntax such as the following registration of a pending, tagged test:
   * </p>
   *
   * <pre class="stHighlight">
   * they should "pop values in last-in-first-out order" taggedAs(SlowTest) is (pending)
   *                                                                        ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of the <code>it</code> field to register tagged tests, see
   * the <a href="AnyFlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation for trait <code>AnyFlatSpec</code>.
   * </p>
   *
   * @param verb the verb
   * @param name the name
   * @param tags the list of tags
   */
  protected final class TheyVerbStringTaggedAs(verb: String, name: String, tags: List[Tag]) {

    private final def inImpl(testFun: () => Future[compatible.Assertion], pos: source.Position): Unit = {
      registerAsyncTestToRun(verb.trim + " " + name.trim, tags, "in", new NoArgTestWrapper(testFun), pos)
    }

    /**
     * Supports the registration of tagged, no-arg tests in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * they must "pop values in last-in-first-out order" taggedAs(SlowTest) in { () => ... }
     *                                                                      ^
     * </pre>
     *
     * <p>
     * For examples of tagged test registration, see
     * the <a href="AnyFlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param testFun the test function
     */
    // SKIP-DOTTY-START
    def in(testFun: () => Future[compatible.Assertion])(implicit pos: source.Position): Unit = {
      inImpl(testFun, pos)
    }
    // SKIP-DOTTY-END
    //DOTTY-ONLY inline def in(testFun: () => Future[compatible.Assertion]): Unit = {
    //DOTTY-ONLY   ${ source.Position.withPosition[Unit]('{(pos: source.Position) => inImpl(testFun, pos) }) } 
    //DOTTY-ONLY }

    /**
     * Supports the registration of tagged, one-arg tests (tests that take a <code>FixtureParam</code> object as a parameter) in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * they must "pop values in last-in-first-out order" taggedAs(SlowTest) in { fixture => ... }
     *                                                                      ^
     * </pre>
     *
     * <p>
     * For examples of tagged test registration, see
     * the <a href="AnyFlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param testFun the test function
     */
    // SKIP-DOTTY-START
    def in(testFun: FixtureParam => Future[compatible.Assertion])(implicit pos: source.Position): Unit = {
      registerAsyncTestToRun(verb.trim + " " + name.trim, tags, "in", testFun, pos)
    }
    // SKIP-DOTTY-END
    //DOTTY-ONLY inline def in(testFun: FixtureParam => Future[compatible.Assertion]): Unit = {
    //DOTTY-ONLY   ${ source.Position.withPosition[Unit]('{(pos: source.Position) => registerAsyncTestToRun(verb.trim + " " + name.trim, tags, "in", testFun, pos) }) } 
    //DOTTY-ONLY }

    /**
     * Supports the registration of pending, tagged tests in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * they must "pop values in last-in-first-out order" taggedAs(SlowTest) is (pending)
     *                                                                      ^
     * </pre>
     *
     * <p>
     * For examples of pending test registration, see the <a href="AnyFlatSpec.html#PendingTests">Pending tests section</a> in the main documentation
     * for trait <code>AnyFlatSpec</code>.  And for examples of tagged test registration, see
     * the <a href="AnyFlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param testFun the test function
     */
    // SKIP-DOTTY-START
    def is(testFun: => PendingStatement)(implicit pos: source.Position): Unit = {
      registerPendingTestToRun(verb.trim + " " + name.trim, tags, "is", unusedFixtureParam => testFun, pos)
    }
    // SKIP-DOTTY-END
    //DOTTY-ONLY inline def is(testFun: => PendingStatement): Unit = {
    //DOTTY-ONLY   ${ source.Position.withPosition[Unit]('{(pos: source.Position) => registerPendingTestToRun(verb.trim + " " + name.trim, tags, "is", unusedFixtureParam => testFun, pos) }) } 
    //DOTTY-ONLY }

    private final def ignoreImpl(testFun: () => Future[compatible.Assertion], pos: source.Position): Unit = {
      registerAsyncTestToIgnore(verb.trim + " " + name.trim, tags, new NoArgTestWrapper(testFun), pos)
    }

    /**
     * Supports the registration of ignored, tagged, no-arg tests in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * they must "pop values in last-in-first-out order" taggedAs(SlowTest) ignore { () => ... }
     *                                                                      ^
     * </pre>
     *
     * <p>
     * For examples of ignored test registration, see the <a href="AnyFlatSpec.html#IgnoredTests">Ignored tests section</a> in the main documentation
     * for trait <code>AnyFlatSpec</code>.  And for examples of tagged test registration, see
     * the <a href="AnyFlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param testFun the test function
     */
    // SKIP-DOTTY-START
    def ignore(testFun: () => Future[compatible.Assertion])(implicit pos: source.Position): Unit = {
      ignoreImpl(testFun, pos)
    }
    // SKIP-DOTTY-END
    //DOTTY-ONLY inline def ignore(testFun: () => Future[compatible.Assertion]): Unit = {
    //DOTTY-ONLY   ${ source.Position.withPosition[Unit]('{(pos: source.Position) => ignoreImpl(testFun, pos) }) } 
    //DOTTY-ONLY }

    /**
     * Supports the registration of ignored, tagged, one-arg tests (tests that take a <code>FixtureParam</code> object
     * as a parameter) in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * they must "pop values in last-in-first-out order" taggedAs(SlowTest) ignore { fixture => ... }
     *                                                                      ^
     * </pre>
     *
     * <p>
     * For examples of ignored test registration, see the <a href="AnyFlatSpec.html#IgnoredTests">Ignored tests section</a> in the main documentation
     * for trait <code>AnyFlatSpec</code>.  And for examples of tagged test registration, see
     * the <a href="AnyFlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param testFun the test function
     */
    // SKIP-DOTTY-START
    def ignore(testFun: FixtureParam => Future[compatible.Assertion])(implicit pos: source.Position): Unit = {
      registerAsyncTestToIgnore(verb.trim + " " + name.trim, tags, testFun, pos)
    }
    // SKIP-DOTTY-END
    //DOTTY-ONLY inline def ignore(testFun: FixtureParam => Future[compatible.Assertion]): Unit = {
    //DOTTY-ONLY   ${ source.Position.withPosition[Unit]('{(pos: source.Position) => registerAsyncTestToIgnore(verb.trim + " " + name.trim, tags, testFun, pos) }) } 
    //DOTTY-ONLY }
  }

  /**
   * Class that supports test registration via the instance referenced from <code>FixtureAsyncFlatSpec</code>'s <code>it</code> field.
   *
   * <p>
   * This class enables syntax such as the following test registration:
   * </p>
   *
   * <pre class="stHighlight">
   * they should "pop values in last-in-first-out order" in { ... }
   *                                                     ^
   * </pre>
   *
   * <p>
   * It also enables syntax such as the following registration of an ignored test:
   * </p>
   *
   * <pre class="stHighlight">
   * they should "pop values in last-in-first-out order" ignore { ... }
   *                                                     ^
   * </pre>
   *
   * <p>
   * In addition, it enables syntax such as the following registration of a pending test:
   * </p>
   *
   * <pre class="stHighlight">
   * they should "pop values in last-in-first-out order" is (pending)
   *                                                     ^
   * </pre>
   *
   * <p>
   * And finally, it also enables syntax such as the following tagged test registration:
   * </p>
   *
   * <pre class="stHighlight">
   * they should "pop values in last-in-first-out order" taggedAs(SlowTest) in { ... }
   *                                                     ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of the <code>it</code> field, see the <a href="AnyFlatSpec.html">main documentation</a>
   * for trait <code>AnyFlatSpec</code>.
   * </p>
   *
   * @param verb the verb
   * @param name the name
   */
  protected final class TheyVerbString(verb: String, name: String) {

    private final def inImpl(testFun: () => Future[compatible.Assertion], pos: source.Position): Unit = {
      registerAsyncTestToRun(verb.trim + " " + name.trim, List(), "in", new NoArgTestWrapper(testFun), pos)
    }

    /**
     * Supports the registration of no-arg tests in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * they must "pop values in last-in-first-out order" in { () => ... }
     *                                                   ^
     * </pre>
     *
     * <p>
     * For examples of no-arg test registration, see the <a href="AnyFlatSpec.html">main documentation</a>
     * for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param testFun the test function
     */
    // SKIP-DOTTY-START
    def in(testFun: () => Future[compatible.Assertion])(implicit pos: source.Position): Unit = {
      inImpl(testFun, pos)
    }
    // SKIP-DOTTY-END
    //DOTTY-ONLY inline def in(testFun: () => Future[compatible.Assertion]): Unit = {
    //DOTTY-ONLY   ${ source.Position.withPosition[Unit]('{(pos: source.Position) => inImpl(testFun, pos) }) } 
    //DOTTY-ONLY }

    /**
     * Supports the registration of one-arg tests (tests that take a <code>FixtureParam</code> object as a parameter) in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * they must "pop values in last-in-first-out order" in { fixture => ... }
     *                                                   ^
     * </pre>
     *
     * <p>
     * For examples of one-arg test registration, see the <a href="AnyFlatSpec.html">main documentation</a>
     * for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param testFun the test function
     */
    // SKIP-DOTTY-START
    def in(testFun: FixtureParam => Future[compatible.Assertion])(implicit pos: source.Position): Unit = {
      registerAsyncTestToRun(verb.trim + " " + name.trim, List(), "in", testFun, pos)
    }
    // SKIP-DOTTY-END
    //DOTTY-ONLY inline def in(testFun: FixtureParam => Future[compatible.Assertion]): Unit = {
    //DOTTY-ONLY   ${ source.Position.withPosition[Unit]('{(pos: source.Position) => registerAsyncTestToRun(verb.trim + " " + name.trim, List(), "in", testFun, pos) }) } 
    //DOTTY-ONLY }

    /**
     * Supports the registration of pending tests in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * they must "pop values in last-in-first-out order" is (pending)
     *                                                   ^
     * </pre>
     *
     * <p>
     * For examples of pending test registration, see the <a href="AnyFlatSpec.html#PendingTests">Pending tests section</a> in the main documentation
     * for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param testFun the test function
     */
    // SKIP-DOTTY-START
    def is(testFun: => PendingStatement)(implicit pos: source.Position): Unit = {
      registerPendingTestToRun(verb.trim + " " + name.trim, List(), "is", unusedFixtureParam => testFun, pos)
    }
    // SKIP-DOTTY-END
    //DOTTY-ONLY inline def is(testFun: => PendingStatement): Unit = {
    //DOTTY-ONLY   ${ source.Position.withPosition[Unit]('{(pos: source.Position) => registerPendingTestToRun(verb.trim + " " + name.trim, List(), "is", unusedFixtureParam => testFun, pos) }) } 
    //DOTTY-ONLY }

    private final def ignoreImpl(testFun: () => Future[compatible.Assertion], pos: source.Position): Unit = {
      registerAsyncTestToIgnore(verb.trim + " " + name.trim, List(), new NoArgTestWrapper(testFun), pos)
    }

    /**
     * Supports the registration of ignored no-arg tests in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * they must "pop values in last-in-first-out order" ignore { () => ... }
     *                                                   ^
     * </pre>
     *
     * <p>
     * For examples of ignored test registration, see the <a href="AnyFlatSpec.html#IgnoredTests">Ignored tests section</a> in the main documentation
     * for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param testFun the test function
     */
    // SKIP-DOTTY-START
    def ignore(testFun: () => Future[compatible.Assertion])(implicit pos: source.Position): Unit = {
      ignoreImpl(testFun, pos)
    }
    // SKIP-DOTTY-END
    //DOTTY-ONLY inline def ignore(testFun: () => Future[compatible.Assertion]): Unit = {
    //DOTTY-ONLY   ${ source.Position.withPosition[Unit]('{(pos: source.Position) => ignoreImpl(testFun, pos) }) } 
    //DOTTY-ONLY }

    /**
     * Supports the registration of ignored one-arg tests (tests that take a <code>FixtureParam</code> object as a parameter) in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * they must "pop values in last-in-first-out order" ignore { fixture => ... }
     *                                                   ^
     * </pre>
     *
     * <p>
     * For examples of ignored test registration, see the <a href="AnyFlatSpec.html#IgnoredTests">Ignored tests section</a> in the main documentation
     * for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param testFun the test function
     (implicit pos: source.Position)*/
    // SKIP-DOTTY-START
    def ignore(testFun: FixtureParam => Future[compatible.Assertion])(implicit pos: source.Position): Unit = {
      registerAsyncTestToIgnore(verb.trim + " " + name.trim, List(), testFun, pos)
    }
    // SKIP-DOTTY-END
    //DOTTY-ONLY inline def ignore(testFun: FixtureParam => Future[compatible.Assertion]): Unit = {
    //DOTTY-ONLY   ${ source.Position.withPosition[Unit]('{(pos: source.Position) => registerAsyncTestToIgnore(verb.trim + " " + name.trim, List(), testFun, pos) }) } 
    //DOTTY-ONLY }

    /**
     * Supports the registration of tagged tests in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * they must "pop values in last-in-first-out order" taggedAs(SlowTest) in { ... }
     *                                                   ^
     * </pre>
     *
     * <p>
     * For examples of tagged test registration, see the <a href="AnyFlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation
     * for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param firstTestTag the first mandatory test tag
     * @param otherTestTags the others additional test tags
     */
    def taggedAs(firstTestTag: Tag, otherTestTags: Tag*) = {
      val tagList = firstTestTag :: otherTestTags.toList
      new ItVerbStringTaggedAs(verb, name, tagList)
    }
  }

  /**
   * Class that supports test (and shared test) registration via the instance referenced from <code>FixtureAsyncFlatSpec</code>'s <code>they</code> field.
   *
   * <p>
   * This class enables syntax such as the following test registration:
   * </p>
   *
   * <pre class="stHighlight">
   * they should "pop values in last-in-first-out order" in { ... }
   * ^
   * </pre>
   *
   * <p>
   * It also enables syntax such as the following shared test registration:
   * </p>
   *
   * <pre class="stHighlight">
   * they should behave like nonEmptyStack(lastItemPushed)
   * ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of the <code>it</code> field, see the main documentation
   * for trait <a href="AnyFlatSpec.html"><code>AnyFlatSpec</code></a>.
   * </p>
   */
  protected final class TheyWord {

    /**
     * Supports the registration of tests with <code>should</code> in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * they should "pop values in last-in-first-out order" in { ... }
     *      ^
     * </pre>
     *
     * <p>
     * For examples of test registration, see the <a href="AnyFlatSpec.html">main documentation</a>
     * for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param string the string description
     */
    def should(string: String) = new ItVerbString("should", string)

    /**
     * Supports the registration of tests with <code>must</code> in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * they must "pop values in last-in-first-out order" in { ... }
     *      ^
     * </pre>
     *
     * <p>
     * For examples of test registration, see the <a href="AnyFlatSpec.html">main documentation</a>
     * for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param string the string description
     */
    def must(string: String) = new ItVerbString("must", string)

    /**
     * Supports the registration of tests with <code>can</code> in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * they can "pop values in last-in-first-out order" in { ... }
     *      ^
     * </pre>
     *
     * <p>
     * For examples of test registration, see the <a href="AnyFlatSpec.html">main documentation</a>
     * for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param string the string description
     */
    def can(string: String) = new ItVerbString("can", string)

    /**
     * Supports the registration of shared tests with <code>should</code> in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * they should behave like nonFullStack(stackWithOneItem)
     *      ^
     * </pre>
     *
     * <p>
     * For examples of shared tests, see the <a href="AnyFlatSpec.html#SharedTests">Shared tests section</a>
     * in the main documentation for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param behaveWord the <code>BehaveWord</code>
     */
    def should(behaveWord: BehaveWord) = behaveWord

    /**
     * Supports the registration of shared tests with <code>must</code> in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * they must behave like nonFullStack(stackWithOneItem)
     *      ^
     * </pre>
     *
     * <p>
     * For examples of shared tests, see the <a href="AnyFlatSpec.html#SharedTests">Shared tests section</a>
     * in the main documentation for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param behaveWord the <code>BehaveWord</code>
     */
    def must(behaveWord: BehaveWord) = behaveWord

    /**
     * Supports the registration of shared tests with <code>can</code> in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * they can behave like nonFullStack(stackWithOneItem)
     *      ^
     * </pre>
     *
     * <p>
     * For examples of shared tests, see the <a href="AnyFlatSpec.html#SharedTests">Shared tests section</a>
     * in the main documentation for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param behaveWord the <code>BehaveWord</code>
     */
    def can(behaveWord: BehaveWord) = behaveWord
  }

  /**
   * Supports test (and shared test) registration in <code>FixtureAsyncFlatSpec</code>s.
   *
   * <p>
   * This field enables syntax such as the following test registration:
   * </p>
   *
   * <pre class="stHighlight">
   * they should "pop values in last-in-first-out order" in { ... }
   * ^
   * </pre>
   *
   * <p>
   * It also enables syntax such as the following shared test registration:
   * </p>
   *
   * <pre class="stHighlight">
   * they should behave like nonEmptyStack(lastItemPushed)
   * ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of the <code>it</code> field, see the main documentation
   * for trait <a href="AnyFlatSpec.html"><code>AnyFlatSpec</code></a>.
   * </p>
   */
  protected val they = new TheyWord

  /**
   * Class that supports registration of ignored, tagged tests via the <code>IgnoreWord</code> instance referenced
   * from <code>FixtureAsyncFlatSpec</code>'s <code>ignore</code> field.
   *
   * <p>
   * This class enables syntax such as the following registration of an ignored, tagged test:
   * </p>
   *
   * <pre class="stHighlight">
   * ignore should "pop values in last-in-first-out order" taggedAs(SlowTest) in { ... }
   *                                                                          ^
   * </pre>
   *
   * <p>
   * In addition, it enables syntax such as the following registration of an ignored, tagged, pending test:
   * </p>
   *
   * <pre class="stHighlight">
   * ignore should "pop values in last-in-first-out order" taggedAs(SlowTest) is (pending)
   *                                                                          ^
   * </pre>
   *
   * <p>
   * Note: the <code>is</code> method is provided for completeness and design symmetry, given there's no way
   * to prevent changing <code>is</code> to <code>ignore</code> and marking a pending test as ignored that way.
   * Although it isn't clear why someone would want to mark a pending test as ignored, it can be done.
   * </p>
   *
   * <p>
   * For more information and examples of the use of the <code>ignore</code> field, see
   * the <a href="AnyFlatSpec.html#IgnoredTests">Ignored tests section</a>
   * in the main documentation for trait <code>AnyFlatSpec</code>. For examples of tagged test registration, see
   * the <a href="AnyFlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation for trait <code>AnyFlatSpec</code>.
   * </p>
   *
   * @param verb the verb
   * @param name the name
   * @param tags the list of tags
   */
  protected final class IgnoreVerbStringTaggedAs(verb: String, name: String, tags: List[Tag]) {

    private final def inImpl(testFun: () => Future[compatible.Assertion], pos: source.Position): Unit = {
      registerAsyncTestToIgnore(verb.trim + " " + name.trim, tags, new NoArgTestWrapper(testFun), pos)
    }

    /**
     * Supports the registration of ignored, tagged, no-arg tests in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * ignore must "pop values in last-in-first-out order" taggedAs(SlowTest) in { () => ... }
     *                                                                        ^
     * </pre>
     *
     * <p>
     * For examples of the registration of ignored tests, see the <a href="AnyFlatSpec.html#IgnoredTests">Ignored tests section</a>
     * in the main documentation for trait <code>AnyFlatSpec</code>. For examples of tagged test registration, see
     * the <a href="AnyFlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param testFun the test function
     */
    // SKIP-DOTTY-START
    def in(testFun: () => Future[compatible.Assertion])(implicit pos: source.Position): Unit = {
      inImpl(testFun, pos)
    }
    // SKIP-DOTTY-END
    //DOTTY-ONLY inline def in(testFun: () => Future[compatible.Assertion]): Unit = {
    //DOTTY-ONLY   ${ source.Position.withPosition[Unit]('{(pos: source.Position) => inImpl(testFun, pos) }) } 
    //DOTTY-ONLY }

    /**
     * Supports the registration of ignored, tagged, one-arg tests (tests that take a <code>FixtureParam</code> object as a parameter)
     * in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * ignore must "pop values in last-in-first-out order" taggedAs(SlowTest) in { fixture => ... }
     *                                                                        ^
     * </pre>
     *
     * <p>
     * For examples of the registration of ignored tests, see the <a href="AnyFlatSpec.html#IgnoredTests">Ignored tests section</a>
     * in the main documentation for trait <code>AnyFlatSpec</code>. For examples of tagged test registration, see
     * the <a href="AnyFlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param testFun the test function
     */
    // SKIP-DOTTY-START
    def in(testFun: FixtureParam => Future[compatible.Assertion])(implicit pos: source.Position): Unit = {
      registerAsyncTestToIgnore(verb.trim + " " + name.trim, tags, testFun, pos)
    }
    // SKIP-DOTTY-END
    //DOTTY-ONLY inline def in(testFun: FixtureParam => Future[compatible.Assertion]): Unit = {
    //DOTTY-ONLY   ${ source.Position.withPosition[Unit]('{(pos: source.Position) => registerAsyncTestToIgnore(verb.trim + " " + name.trim, tags, testFun, pos) }) } 
    //DOTTY-ONLY }

    /**
     * Supports the registration of ignored, tagged, pending tests in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * ignore must "pop values in last-in-first-out order" taggedAs(SlowTest) is (pending)
     *                                                                        ^
     * </pre>
     *
     * <p>
     * Note: this <code>is</code> method is provided for completeness and design symmetry, given there's no way
     * to prevent changing <code>is</code> to <code>ignore</code> and marking a pending test as ignored that way.
     * Although it isn't clear why someone would want to mark a pending test as ignored, it can be done.
     * </p>
     *
     * <p>
     * For examples of pending test registration, see the <a href="AnyFlatSpec.html#PendingTests">Pending tests section</a> in the main documentation
     * for trait <code>AnyFlatSpec</code>.  For examples of the registration of ignored tests,
     * see the <a href="AnyFlatSpec.html#IgnoredTests">Ignored tests section</a>
     * in the main documentation for trait <code>AnyFlatSpec</code>. For examples of tagged test registration, see
     * the <a href="AnyFlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param testFun the test function
     */
    // SKIP-DOTTY-START
    def is(testFun: => PendingStatement)(implicit pos: source.Position): Unit = {
      registerPendingTestToIgnore(verb.trim + " " + name.trim, tags, unusedFixtureParam => testFun, pos)
    }
    // SKIP-DOTTY-END
    //DOTTY-ONLY inline def is(testFun: => PendingStatement): Unit = {
    //DOTTY-ONLY   ${ source.Position.withPosition[Unit]('{(pos: source.Position) => registerPendingTestToIgnore(verb.trim + " " + name.trim, tags, unusedFixtureParam => testFun, pos) }) } 
    //DOTTY-ONLY }
  }

  /**
   * Class that supports registration of ignored tests via the <code>IgnoreWord</code> instance referenced
   * from <code>FixtureAsyncFlatSpec</code>'s <code>ignore</code> field.
   *
   * <p>
   * This class enables syntax such as the following registration of an ignored test:
   * </p>
   *
   * <pre class="stHighlight">
   * ignore should "pop values in last-in-first-out order" in { ... }
   *                                                       ^
   * </pre>
   *
   * <p>
   * In addition, it enables syntax such as the following registration of an ignored, pending test:
   * </p>
   *
   * <pre class="stHighlight">
   * ignore should "pop values in last-in-first-out order" is (pending)
   *                                                       ^
   * </pre>
   *
   * Note: the <code>is</code> method is provided for completeness and design symmetry, given there's no way
   * to prevent changing <code>is</code> to <code>ignore</code> and marking a pending test as ignored that way.
   * Although it isn't clear why someone would want to mark a pending test as ignored, it can be done.
   * </p>
   *
   * <p>
   * And finally, it also enables syntax such as the following ignored, tagged test registration:
   * </p>
   *
   * <pre class="stHighlight">
   * ignore should "pop values in last-in-first-out order" taggedAs(SlowTest) in { ... }
   *                                                       ^
   * </pre>
   *
   * <p>
   * <p>
   * For more information and examples of the use of the <code>ignore</code> field, see the <a href="AnyFlatSpec.html#IgnoredTests">Ignored tests section</a>
   * in the main documentation for trait <code>AnyFlatSpec</code>.
   * </p>
   *
   * @param verb the verb
   * @param name the name
   */
  protected final class IgnoreVerbString(verb: String, name: String) {

    private final def inImpl(testFun: () => Future[compatible.Assertion], pos: source.Position): Unit = {
      registerAsyncTestToIgnore(verb.trim + " " + name.trim, List(), new NoArgTestWrapper(testFun), pos)
    }

    /**
     * Supports the registration of ignored, no-arg tests in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * ignore must "pop values in last-in-first-out order" in { () => ... }
     *                                                     ^
     * </pre>
     *
     * <p>
     * For examples of the registration of ignored tests, see the <a href="AnyFlatSpec.html#IgnoredTests">Ignored tests section</a>
     * in the main documentation for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param testFun the test function
     */
    // SKIP-DOTTY-START
    def in(testFun: () => Future[compatible.Assertion])(implicit pos: source.Position): Unit = {
      inImpl(testFun, pos)
    }
    // SKIP-DOTTY-END
    //DOTTY-ONLY inline def in(testFun: () => Future[compatible.Assertion]): Unit = {
    //DOTTY-ONLY   ${ source.Position.withPosition[Unit]('{(pos: source.Position) => inImpl(testFun, pos) }) } 
    //DOTTY-ONLY }

    /**
     * Supports the registration of ignored, one-arg tests (tests that take a <code>FixtureParam</code> object
     * as a parameter) in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * ignore must "pop values in last-in-first-out order" in { fixture => ... }
     *                                                     ^
     * </pre>
     *
     * <p>
     * For examples of the registration of ignored tests, see the <a href="AnyFlatSpec.html#IgnoredTests">Ignored tests section</a>
     * in the main documentation for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param testFun the test function
     */
    // SKIP-DOTTY-START
    def in(testFun: FixtureParam => Future[compatible.Assertion])(implicit pos: source.Position): Unit = {
      registerAsyncTestToIgnore(verb.trim + " " + name.trim, List(), testFun, pos)
    }
    // SKIP-DOTTY-END
    //DOTTY-ONLY inline def in(testFun: FixtureParam => Future[compatible.Assertion]): Unit = {
    //DOTTY-ONLY   ${ source.Position.withPosition[Unit]('{(pos: source.Position) => registerAsyncTestToIgnore(verb.trim + " " + name.trim, List(), testFun, pos) }) } 
    //DOTTY-ONLY }

    /**
     * Supports the registration of ignored, pending tests in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * ignore must "pop values in last-in-first-out order" is (pending)
     *                                                     ^
     * </pre>
     *
     * <p>
     * Note: this <code>is</code> method is provided for completeness and design symmetry, given there's no way
     * to prevent changing <code>is</code> to <code>ignore</code> and marking a pending test as ignored that way.
     * Although it isn't clear why someone would want to mark a pending test as ignored, it can be done.
     * </p>
     *
     * <p>
     * For examples of pending test registration, see the <a href="AnyFlatSpec.html#PendingTests">Pending tests section</a> in the main documentation
     * for trait <code>AnyFlatSpec</code>.  For examples of the registration of ignored tests,
     * see the <a href="AnyFlatSpec.html#IgnoredTests">Ignored tests section</a>
     * in the main documentation for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param testFun the test function
     */
    // SKIP-DOTTY-START
    def is(testFun: => PendingStatement)(implicit pos: source.Position): Unit = {
      registerPendingTestToIgnore(verb.trim + " " + name.trim, List(), unusedFixtureParam => testFun, pos)
    }
    // SKIP-DOTTY-END
    //DOTTY-ONLY inline def is(testFun: => PendingStatement): Unit = {
    //DOTTY-ONLY   ${ source.Position.withPosition[Unit]('{(pos: source.Position) => registerPendingTestToIgnore(verb.trim + " " + name.trim, List(), unusedFixtureParam => testFun, pos) }) } 
    //DOTTY-ONLY }

    /**
     * Supports the registration of ignored, tagged tests in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * ignore must "pop values in last-in-first-out order" taggedAs(SlowTest) in { ... }
     *                                                     ^
     * </pre>
     *
     * <p>
     * For examples of tagged test registration, see the <a href="AnyFlatSpec.html#TaggingTests">Tagging tests section</a> in the main documentation
     * for trait <code>AnyFlatSpec</code>.  For examples of the registration of ignored tests,
     * see the <a href="AnyFlatSpec.html#IgnoredTests">Ignored tests section</a>
     * in the main documentation for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param firstTestTag the first mandatory test tag
     * @param otherTestTags the others additional test tags
     */
    def taggedAs(firstTestTag: Tag, otherTestTags: Tag*) = {
      val tagList = firstTestTag :: otherTestTags.toList
      new IgnoreVerbStringTaggedAs(verb, name, tagList)
    }
  }

  /**
   * Class that supports registration of ignored tests via the instance referenced from <code>FixtureAsyncFlatSpec</code>'s <code>ignore</code> field.
   *
   * <p>
   * This class enables syntax such as the following registration of an ignored test:
   * </p>
   *
   * <pre class="stHighlight">
   * ignore should "pop values in last-in-first-out order" in { ... }
   * ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of the <code>ignore</code> field, see <a href="AnyFlatSpec.html#IgnoredTests">Ignored tests section</a>
   * in the main documentation for this trait.
   * </p>
   */
  protected final class IgnoreWord {

    /**
     * Supports the registration of ignored tests with <code>should</code> in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * ignore should "pop values in last-in-first-out order" in { ... }
     *        ^
     * </pre>
     *
     * <p>
     * For more information and examples of the use of the <code>ignore</code> field, see <a href="AnyFlatSpec.html#IgnoredTests">Ignored tests section</a>
     * in the main documentation for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param string the string description
     */
    def should(string: String) = new IgnoreVerbString("should", string)

    /**
     * Supports the registration of ignored tests with <code>must</code> in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * ignore must "pop values in last-in-first-out order" in { ... }
     *        ^
     * </pre>
     *
     * <p>
     * For more information and examples of the use of the <code>ignore</code> field, see <a href="AnyFlatSpec.html#IgnoredTests">Ignored tests section</a>
     * in the main documentation for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param string the string description
     */
    def must(string: String) = new IgnoreVerbString("must", string)

    /**
     * Supports the registration of ignored tests with <code>can</code> in a <code>FixtureAsyncFlatSpec</code>.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * ignore can "pop values in last-in-first-out order" in { ... }
     *        ^
     * </pre>
     *
     * <p>
     * For more information and examples of the use of the <code>ignore</code> field, see <a href="AnyFlatSpec.html#IgnoredTests">Ignored tests section</a>
     * in the main documentation for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param string the string description
     */
    def can(string: String) = new IgnoreVerbString("can", string)
  }

  /**
   * Supports registration of ignored tests in <code>FixtureAsyncFlatSpec</code>s.
   *
   * <p>
   * This field enables syntax such as the following registration of an ignored test:
   * </p>
   *
   * <pre class="stHighlight">
   * ignore should "pop values in last-in-first-out order" in { ... }
   * ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of the <code>ignore</code> field, see the
   * <a href="../AnyFlatSpec.html#IgnoredTests">Ignored tests section</a> in the main documentation for trait <code>AnyFlatSpec</code>.
   * </p>
   */
  protected val ignore = new IgnoreWord

  /**
   * Class that supports test registration in shorthand form.
   *
   * <p>
   * For example, this class enables syntax such as the following test registration
   * in shorthand form:
   * </p>
   *
   * <pre class="stHighlight">
   * "A Stack (when empty)" should "be empty" in { ... }
   *                                          ^
   * </pre>
   *
   * <p>
   * This class also enables syntax such as the following ignored test registration
   * in shorthand form:
   * </p>
   *
   * <pre class="stHighlight">
   * "A Stack (when empty)" should "be empty" ignore { ... }
   *                                          ^
   * </pre>
   *
   * <p>
   * This class is used via an implicit conversion (named <code>convertToInAndIgnoreMethods</code>)
   * from <code>ResultOfStringPassedToVerb</code>. The <code>ResultOfStringPassedToVerb</code> class
   * does not declare any methods named <code>in</code>, because the
   * type passed to <code>in</code> differs in a <code>AsyncFlatSpec</code> and a <code>org.scalatest.flatspec.FixtureAsyncFlatSpec</code>.
   * A <code>org.scalatest.flatspec.FixtureAsyncFlatSpec</code> needs two <code>in</code> methods, one that takes a no-arg
   * test function and another that takes a one-arg test function (a test that takes a
   * <code>FixtureParam</code> as its parameter). By constrast, a <code>AsyncFlatSpec</code> needs
   * only one <code>in</code> method that takes a by-name parameter. As a result,
   * <code>AsyncFlatSpec</code> and <code>org.scalatest.flatspec.FixtureAsyncFlatSpec</code> each provide an implicit conversion
   * from <code>ResultOfStringPassedToVerb</code> to a type that provides the appropriate
   * <code>in</code> methods.
   * </p>
   *
   * @author Bill Venners
   */
  protected final class InAndIgnoreMethods(resultOfStringPassedToVerb: ResultOfStringPassedToVerb) {

    import resultOfStringPassedToVerb.rest
import resultOfStringPassedToVerb.verb

    private final def inImpl(testFun: () => Future[compatible.Assertion], pos: source.Position): Unit = {
      registerAsyncTestToRun(verb.trim + " " + rest.trim, List(), "in", new NoArgTestWrapper(testFun), pos)
    }

    /**
     * Supports the registration of no-arg tests in shorthand form.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * "A Stack" must "pop values in last-in-first-out order" in { () => ... }
     *                                                        ^
     * </pre>
     *
     * <p>
     * For examples of test registration, see the <a href="AnyFlatSpec.html">main documentation</a>
     * for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param testFun the test function
     */
    // SKIP-DOTTY-START
    def in(testFun: () => Future[compatible.Assertion])(implicit pos: source.Position): Unit = {
      inImpl(testFun, pos)
    }
    // SKIP-DOTTY-END
    //DOTTY-ONLY inline def in(testFun: () => Future[compatible.Assertion]): Unit = {
    //DOTTY-ONLY   ${ source.Position.withPosition[Unit]('{(pos: source.Position) => inImpl(testFun, pos) }) } 
    //DOTTY-ONLY }

    private final def ignoreImpl(testFun: () => Future[compatible.Assertion], pos: source.Position): Unit = {
      registerAsyncTestToIgnore(verb.trim + " " + rest.trim, List(), new NoArgTestWrapper(testFun), pos)
    }

    /**
     * Supports the registration of ignored, no-arg tests in shorthand form.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * "A Stack" must "pop values in last-in-first-out order" ignore { () => ... }
     *                                                        ^
     * </pre>
     *
     * <p>
     * For examples of ignored test registration, see the <a href="AnyFlatSpec.html#IgnoredTests">Ignored tests section</a>
     * in the main documentation for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param testFun the test function
     */
    // SKIP-DOTTY-START
    def ignore(testFun: () => Future[compatible.Assertion])(implicit pos: source.Position): Unit = {
      ignoreImpl(testFun, pos)
    }
    // SKIP-DOTTY-END
    //DOTTY-ONLY inline def ignore(testFun: () => Future[compatible.Assertion]): Unit = {
    //DOTTY-ONLY   ${ source.Position.withPosition[Unit]('{(pos: source.Position) => ignoreImpl(testFun, pos) }) } 
    //DOTTY-ONLY }

    /**
     * Supports the registration of one-arg tests (tests that take a <code>FixtureParam</code> parameter) in shorthand form.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * "A Stack" must "pop values in last-in-first-out order" in { fixture => ... }
     *                                                        ^
     * </pre>
     *
     * <p>
     * For examples of test registration, see the <a href="AnyFlatSpec.html">main documentation</a>
     * for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param testFun the test function
     */
    // SKIP-DOTTY-START
    def in(testFun: FixtureParam => Future[compatible.Assertion])(implicit pos: source.Position): Unit = {
      registerAsyncTestToRun(verb.trim + " " + rest.trim, List(), "in", testFun, pos)
    }
    // SKIP-DOTTY-END
    //DOTTY-ONLY inline def in(testFun: FixtureParam => Future[compatible.Assertion]): Unit = {
    //DOTTY-ONLY   ${ source.Position.withPosition[Unit]('{(pos: source.Position) => registerAsyncTestToRun(verb.trim + " " + rest.trim, List(), "in", testFun, pos) }) } 
    //DOTTY-ONLY }

    /**
     * Supports the registration of ignored, one-arg tests (tests that take a <code>FixtureParam</code> parameter) in shorthand form.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * "A Stack" must "pop values in last-in-first-out order" ignore { fixture => ... }
     *                                                        ^
     * </pre>
     *
     * <p>
     * For examples of ignored test registration, see the <a href="AnyFlatSpec.html#IgnoredTests">Ignored tests section</a>
     * in the main documentation for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param testFun the test function
     */
    // SKIP-DOTTY-START
    def ignore(testFun: FixtureParam => Future[compatible.Assertion])(implicit pos: source.Position): Unit = {
      registerAsyncTestToIgnore(verb.trim + " " + rest.trim, List(), testFun, pos)
    }
    // SKIP-DOTTY-END
    //DOTTY-ONLY inline def ignore(testFun: FixtureParam => Future[compatible.Assertion]): Unit = {
    //DOTTY-ONLY   ${ source.Position.withPosition[Unit]('{(pos: source.Position) => registerAsyncTestToIgnore(verb.trim + " " + rest.trim, List(), testFun, pos) }) } 
    //DOTTY-ONLY }
  }

  import scala.language.implicitConversions

  /**
   * Implicitly converts an object of type <code>ResultOfStringPassedToVerb</code> to an
   * <code>InAndIgnoreMethods</code>, to enable <code>in</code> and <code>ignore</code>
   * methods to be invokable on that object.
   *
   * @param resultOfStringPassedToVerb an <code>ResultOfStringPassedToVerb</code> instance
   */
  protected implicit def convertToInAndIgnoreMethods(resultOfStringPassedToVerb: ResultOfStringPassedToVerb): InAndIgnoreMethods =
    new InAndIgnoreMethods(resultOfStringPassedToVerb)

  /**
   * Class that supports tagged test registration in shorthand form.
   *
   * <p>
   * For example, this class enables syntax such as the following tagged test registration
   * in shorthand form:
   * </p>
   *
   * <pre class="stHighlight">
   * "A Stack (when empty)" should "be empty" taggedAs() in { ... }
   *                                                     ^
   * </pre>
   *
   * <p>
   * This class also enables syntax such as the following tagged, ignored test registration
   * in shorthand form:
   * </p>
   *
   * <pre class="stHighlight">
   * "A Stack (when empty)" should "be empty" taggedAs(SlowTest) ignore { ... }
   *                                                             ^
   * </pre>
   *
   * <p>
   * This class is used via an implicit conversion (named <code>convertToInAndIgnoreMethodsAfterTaggedAs</code>)
   * from <code>ResultOfTaggedAsInvocation</code>. The <code>ResultOfTaggedAsInvocation</code> class
   * does not declare any methods named <code>in</code>, because the
   * type passed to <code>in</code> differs in a <code>AsyncFlatSpec</code> and a <code>FixtureAsyncFlatSpec</code>.
   * A <code>FixtureAsyncFlatSpec</code> needs two <code>in</code> methods, one that takes a no-arg
   * test function and another that takes a one-arg test function (a test that takes a
   * <code>FixtureParam</code> as its parameter). By constrast, a <code>AsyncFlatSpec</code> needs
   * only one <code>in</code> method that takes a by-name parameter. As a result,
   * <code>AsyncFlatSpec</code> and <code>FixtureAsyncFlatSpec</code> each provide an implicit conversion
   * from <code>ResultOfTaggedAsInvocation</code> to a type that provides the appropriate
   * <code>in</code> methods.
   * </p>
   *
   * @param resultOfTaggedAsInvocation an <code>ResultOfTaggedAsInvocation</code> instance
   *
   * @author Bill Venners
   * @author Chee Seng
   */
  protected final class InAndIgnoreMethodsAfterTaggedAs(resultOfTaggedAsInvocation: ResultOfTaggedAsInvocation) {

    import resultOfTaggedAsInvocation.verb
    import resultOfTaggedAsInvocation.rest
    import resultOfTaggedAsInvocation.{tags => tagsList}

    private final def inImpl(testFun: () => Future[compatible.Assertion], pos: source.Position): Unit = {
      registerAsyncTestToRun(verb.trim + " " + rest.trim, tagsList, "in", new NoArgTestWrapper(testFun), pos)
    }

    /**
     * Supports the registration of tagged, no-arg tests in shorthand form.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * "A Stack" must "pop values in last-in-first-out order" taggedAs(SlowTest) in { () => ... }
     *                                                                           ^
     * </pre>
     *
     * <p>
     * For examples of tagged test registration, see the <a href="AnyFlatSpec.html#TaggingTests">Tagging tests section</a>
     * in the main documentation for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param testFun the test function
     */
    // SKIP-DOTTY-START
    def in(testFun: () => Future[compatible.Assertion])(implicit pos: source.Position): Unit = {
      inImpl(testFun, pos)
    }
    // SKIP-DOTTY-END
    //DOTTY-ONLY inline def in(testFun: () => Future[compatible.Assertion]): Unit = {
    //DOTTY-ONLY   ${ source.Position.withPosition[Unit]('{(pos: source.Position) => inImpl(testFun, pos) }) } 
    //DOTTY-ONLY }

    private final def ignoreImpl(testFun: () => Future[compatible.Assertion], pos: source.Position): Unit = {
      registerAsyncTestToIgnore(verb.trim + " " + rest.trim, tagsList, new NoArgTestWrapper(testFun), pos)
    }

    /**
     * Supports the registration of tagged, ignored, no-arg tests in shorthand form.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * "A Stack" must "pop values in last-in-first-out order" taggedAs(SlowTest) ignore { () => ... }
     *                                                                           ^
     * </pre>
     *
     * <p>
     * For examples of ignored test registration, see the <a href="AnyFlatSpec.html#IgnoredTests">Ignored tests section</a>
     * in the main documentation for trait <code>AnyFlatSpec</code>.
     * For examples of tagged test registration, see the <a href="AnyFlatSpec.html#TaggingTests">Tagging tests section</a>
     * in the main documentation for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param testFun the test function
     */
    // SKIP-DOTTY-START
    def ignore(testFun: () => Future[compatible.Assertion])(implicit pos: source.Position): Unit = {
      ignoreImpl(testFun, pos)
    }
    // SKIP-DOTTY-END
    //DOTTY-ONLY inline def ignore(testFun: () => Future[compatible.Assertion]): Unit = {
    //DOTTY-ONLY   ${ source.Position.withPosition[Unit]('{(pos: source.Position) => ignoreImpl(testFun, pos) }) } 
    //DOTTY-ONLY }

    /**
     * Supports the registration of tagged, one-arg tests (tests that take a <code>FixtureParam</code> parameter) in shorthand form.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * "A Stack" must "pop values in last-in-first-out order" taggedAs(SlowTest) in { fixture => ... }
     *                                                                           ^
     * </pre>
     *
     * <p>
     * For examples of tagged test registration, see the <a href="AnyFlatSpec.html#TaggingTests">Tagging tests section</a>
     * in the main documentation for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param testFun the test function
     */
    // SKIP-DOTTY-START
    def in(testFun: FixtureParam => Future[compatible.Assertion])(implicit pos: source.Position): Unit = {
      registerAsyncTestToRun(verb.trim + " " + rest.trim, tagsList, "in", testFun, pos)
    }
    // SKIP-DOTTY-END
    //DOTTY-ONLY inline def in(testFun: FixtureParam => Future[compatible.Assertion]): Unit = {
    //DOTTY-ONLY   ${ source.Position.withPosition[Unit]('{(pos: source.Position) => registerAsyncTestToRun(verb.trim + " " + rest.trim, tagsList, "in", testFun, pos) }) } 
    //DOTTY-ONLY }

    /**
     * Supports the registration of tagged, ignored, one-arg tests (tests that take a <code>FixtureParam</code> parameter) in shorthand form.
     *
     * <p>
     * This method supports syntax such as the following:
     * </p>
     *
     * <pre class="stHighlight">
     * "A Stack" must "pop values in last-in-first-out order" taggedAs(SlowTest) ignore { fixture => ... }
     *                                                                           ^
     * </pre>
     *
     * <p>
     * For examples of ignored test registration, see the <a href="AnyFlatSpec.html#IgnoredTests">Ignored tests section</a>
     * in the main documentation for trait <code>AnyFlatSpec</code>.
     * For examples of tagged test registration, see the <a href="AnyFlatSpec.html#TaggingTests">Tagging tests section</a>
     * in the main documentation for trait <code>AnyFlatSpec</code>.
     * </p>
     *
     * @param testFun the test function
     */
    // SKIP-DOTTY-START
    def ignore(testFun: FixtureParam => Future[compatible.Assertion])(implicit pos: source.Position): Unit = {
      registerAsyncTestToIgnore(verb.trim + " " + rest.trim, tagsList, testFun, pos)
    }
    // SKIP-DOTTY-END
    //DOTTY-ONLY inline def ignore(testFun: FixtureParam => Future[compatible.Assertion]): Unit = {
    //DOTTY-ONLY   ${ source.Position.withPosition[Unit]('{(pos: source.Position) => registerAsyncTestToIgnore(verb.trim + " " + rest.trim, tagsList, testFun, pos) }) } 
    //DOTTY-ONLY }
  }

  /**
   * Implicitly converts an object of type <code>ResultOfTaggedAsInvocation</code> to an
   * <code>InAndIgnoreMethodsAfterTaggedAs</code>, to enable <code>in</code> and <code>ignore</code>
   * methods to be invokable on that object.
   *
   * @param resultOfTaggedAsInvocation an <code>ResultOfTaggedAsInvocation</code> instance
   */
  protected implicit def convertToInAndIgnoreMethodsAfterTaggedAs(resultOfTaggedAsInvocation: ResultOfTaggedAsInvocation): InAndIgnoreMethodsAfterTaggedAs =
    new InAndIgnoreMethodsAfterTaggedAs(resultOfTaggedAsInvocation)

  /**
   * Supports the shorthand form of test registration.
   *
   * <p>
   * For example, this method enables syntax such as the following:
   * </p>
   *
   * <pre class="stHighlight">
   * "A Stack (when empty)" should "be empty" in { ... }
   *                        ^
   * </pre>
   *
   * <p>
   * This function is passed as an implicit parameter to a <code>should</code> method
   * provided in <code>ShouldVerb</code>, a <code>must</code> method
   * provided in <code>MustVerb</code>, and a <code>can</code> method
   * provided in <code>CanVerb</code>. When invoked, this function registers the
   * subject description (the first parameter to the function) and returns a <code>ResultOfStringPassedToVerb</code>
   * initialized with the verb and rest parameters (the second and third parameters to
   * the function, respectively).
   * </p>
   */
  protected implicit val shorthandTestRegistrationFunction: StringVerbStringInvocation =
    new StringVerbStringInvocation {
      def apply(subject: String, verb: String, rest: String, pos: source.Position): ResultOfStringPassedToVerb = {
        registerFlatBranch(subject, Resources.shouldCannotAppearInsideAnIn, pos)
        new ResultOfStringPassedToVerb(verb, rest) {
          def is(testFun: => PendingStatement): Unit = {
            registerPendingTestToRun(this.verb.trim + " " + this.rest.trim, List(), "is", unusedFixtureParam => testFun, pos)
          }
          def taggedAs(firstTestTag: Tag, otherTestTags: Tag*) = {
            val tagList = firstTestTag :: otherTestTags.toList
            new ResultOfTaggedAsInvocation(this.verb, this.rest, tagList) {
              // "A Stack" must "test this" taggedAs(mytags.SlowAsMolasses) is (pending)
              //                                                            ^
              def is(testFun: => PendingStatement): Unit = {
                registerPendingTestToRun(this.verb.trim + " " + this.rest.trim, this.tags, "is", new NoArgTestWrapper(() => testFun), pos)
              }
            }
          }
        }
      }
    }

  // TODO: Get rid of unusedfixture, and use NoArgTestFunction instead

  /**
   * Supports the shorthand form of shared test registration.
   *
   * <p>
   * For example, this method enables syntax such as the following:
   * </p>
   *
   * <pre class="stHighlight">
   * "A Stack (with one item)" should behave like nonEmptyStack(stackWithOneItem, lastValuePushed)
   *                           ^
   * </pre>
   *
   * <p>
   * This function is passed as an implicit parameter to a <code>should</code> method
   * provided in <code>ShouldVerb</code>, a <code>must</code> method
   * provided in <code>MustVerb</code>, and a <code>can</code> method
   * provided in <code>CanVerb</code>. When invoked, this function registers the
   * subject description (the  parameter to the function) and returns a <code>BehaveWord</code>.
   * </p>
   */
  protected implicit val shorthandSharedTestRegistrationFunction: StringVerbBehaveLikeInvocation =
    new StringVerbBehaveLikeInvocation {
      def apply(subject: String, pos: source.Position): BehaveWord = {
        registerFlatBranch(subject, Resources.shouldCannotAppearInsideAnIn, pos)
        new BehaveWord
      }
    }

  /**
   * Register a test to ignore, which has the given spec text, optional tags, and test function value that takes no arguments.
   * This method will register the test for later ignoring via an invocation of one of the <code>execute</code>
   * methods. This method exists to make it easy to ignore an existing test by changing the call to <code>it</code>
   * to <code>ignore</code> without deleting or commenting out the actual test code. The test will not be executed, but a
   * report will be sent that indicates the test was ignored. The name of the test will be a concatenation of the text of all surrounding describers,
   * from outside in, and the passed spec text, with one space placed between each item. (See the documenation
   * for <code>testNames</code> for an example.) The resulting test name must not have been registered previously on
   * this <code>FixtureAsyncFlatSpec</code> instance.
   *
   * @param specText the specification text, which will be combined with the descText of any surrounding describers
   * to form the test name
   * @param testTags the optional list of tags for this test
   * @param methodName caller's method name
   * @param testFun the test function
   * @throws DuplicateTestNameException if a test with the same name has been registered previously
   * @throws TestRegistrationClosedException if invoked after <code>run</code> has been invoked on this suite
   * @throws NullArgumentException if <code>specText</code> or any passed test tag is <code>null</code>
   */
  private def registerAsyncTestToIgnore(specText: String, testTags: List[Tag], testFun: FixtureParam => Future[compatible.Assertion], pos: source.Position): Unit = {
    engine.registerIgnoredAsyncTest(specText, transformToOutcome(testFun), Resources.ignoreCannotAppearInsideAnInOrAnIs, None, pos, testTags: _*)
  }

  private def registerPendingTestToIgnore(specText: String, testTags: List[Tag], testFun: FixtureParam => PendingStatement, pos: source.Position): Unit = {
    engine.registerIgnoredAsyncTest(specText, AsyncPendingTransformer(testFun), Resources.ignoreCannotAppearInsideAnInOrAnIs, None, pos, testTags: _*)
  }

  /**
   * A <code>Map</code> whose keys are <code>String</code> tag names to which tests in this <code>FixtureAsyncFlatSpec</code> belong, and values
   * the <code>Set</code> of test names that belong to each tag. If this <code>FixtureAsyncFlatSpec</code> contains no tags, this method returns an empty <code>Map</code>.
   *
   * <p>
   * This trait's implementation returns tags that were passed as strings contained in <code>Tag</code> objects passed to
   * methods <code>test</code> and <code>ignore</code>.
   * </p>
   *
   * <p>
   * In addition, this trait's implementation will also auto-tag tests with class level annotations.
   * For example, if you annotate @Ignore at the class level, all test methods in the class will be auto-annotated with @Ignore.
   * </p>
   */
  override def tags: Map[String, Set[String]] = autoTagClassAnnotations(atomic.get.tagsMap, this)

  /**
   * Run a test. This trait's implementation runs the test registered with the name specified by
   * <code>testName</code>. Each test's name is a concatenation of the text of all describers surrounding a test,
   * from outside in, and the test's  spec text, with one space placed between each item. (See the documenation
   * for <code>testNames</code> for an example.)
   *
   * @param testName the name of one test to execute.
   * @param args the <code>Args</code> for this test
   * @return a <code>Status</code> object that indicates when the test started by this method has completed, and whether or not it failed .
   * @throws NullArgumentException if <code>testName</code> or <code>args</code> is <code>null</code>.
   */
  protected override def runTest(testName: String, args: Args): Status = {
    def invokeWithAsyncFixture(theTest: TestLeaf, onCompleteFun: Try[Outcome] => Unit): AsyncOutcome = {
      val theConfigMap = args.configMap
      val testData = testDataFor(testName, theConfigMap)
      FutureAsyncOutcome(
        withFixture(
          new OneArgAsyncTest {
            val name = testData.name

            def apply(fixture: FixtureParam): FutureOutcome =
              theTest.testFun(fixture).toFutureOutcome

            val configMap = testData.configMap
            val scopes = testData.scopes
            val text = testData.text
            val tags = testData.tags
            val pos = testData.pos
          }
        ).underlying,
        onCompleteFun
      )
    }

    runTestImpl(thisSuite, testName, args, true, parallelAsyncTestExecution, invokeWithAsyncFixture)
  }

  /**
   * <p>
   * Run zero to many of this <code>FixtureAsyncFlatSpec</code>'s tests.
   * </p>
   *
   * <p>
   * This method takes a <code>testName</code> parameter that optionally specifies a test to invoke.
   * If <code>testName</code> is <code>Some</code>, this trait's implementation of this method
   * invokes <code>runTest</code> on this object with passed <code>args</code>.
   * </p>
   *
   * <p>
   * This method takes an <code>args</code> that contains a <code>Set</code> of tag names that should be included (<code>tagsToInclude</code>), and a <code>Set</code>
   * that should be excluded (<code>tagsToExclude</code>), when deciding which of this <code>Suite</code>'s tests to execute.
   * If <code>tagsToInclude</code> is empty, all tests will be executed
   * except those those belonging to tags listed in the <code>tagsToExclude</code> <code>Set</code>. If <code>tagsToInclude</code> is non-empty, only tests
   * belonging to tags mentioned in <code>tagsToInclude</code>, and not mentioned in <code>tagsToExclude</code>
   * will be executed. However, if <code>testName</code> is <code>Some</code>, <code>tagsToInclude</code> and <code>tagsToExclude</code> are essentially ignored.
   * Only if <code>testName</code> is <code>None</code> will <code>tagsToInclude</code> and <code>tagsToExclude</code> be consulted to
   * determine which of the tests named in the <code>testNames</code> <code>Set</code> should be run. For more information on trait tags, see the main documentation for this trait.
   * </p>
   *
   * <p>
   * If <code>testName</code> is <code>None</code>, this trait's implementation of this method
   * invokes <code>testNames</code> on this <code>Suite</code> to get a <code>Set</code> of names of tests to potentially execute.
   * (A <code>testNames</code> value of <code>None</code> essentially acts as a wildcard that means all tests in
   * this <code>Suite</code> that are selected by <code>tagsToInclude</code> and <code>tagsToExclude</code> should be executed.)
   * For each test in the <code>testName</code> <code>Set</code>, in the order
   * they appear in the iterator obtained by invoking the <code>elements</code> method on the <code>Set</code>, this trait's implementation
   * of this method checks whether the test should be run based on the <code>tagsToInclude</code> and <code>tagsToExclude</code> <code>Set</code>s.
   * If so, this implementation invokes <code>runTest</code> with the passed <code>args</code>.
   * </p>
   *
   * @param testName an optional name of one test to execute. If <code>None</code>, all relevant tests should be executed.
   *                 I.e., <code>None</code> acts like a wildcard that means execute all relevant tests in this <code>FixtureAsyncFlatSpecLike</code>.
   * @param args the <code>Args</code> for this run
   * @return a <code>Status</code> object that indicates when all tests started by this method have completed, and whether or not a failure occurred.
   * @throws NullArgumentException if any of <code>testName</code> or <code>args</code> is <code>null</code>.
   */
  protected override def runTests(testName: Option[String], args: Args): Status = {
    runTestsImpl(thisSuite, testName, args, true, parallelAsyncTestExecution, runTest)
  }

  /**
   * An immutable <code>Set</code> of test names. If this <code>FixtureAsyncFlatSpec</code> contains no tests, this method returns an
   * empty <code>Set</code>.
   *
   * <p>
   * This trait's implementation of this method will return a set that contains the names of all registered tests. The set's
   * iterator will return those names in the order in which the tests were registered. Each test's name is composed
   * of the concatenation of the text of each surrounding describer, in order from outside in, and the text of the
   * example itself, with all components separated by a space.
   * </p>
   *
   * @return the <code>Set</code> of test names
   */
  override def testNames: Set[String] = {
    InsertionOrderSet(atomic.get.testNamesList)
  }

  override def run(testName: Option[String], args: Args): Status = {
    runImpl(thisSuite, testName, args, parallelAsyncTestExecution, super.run)
  }

  /**
   * Supports shared test registration in <code>FixtureAsyncFlatSpec</code>s.
   *
   * <p>
   * This field supports syntax such as the following:
   * </p>
   *
   * <pre class="stHighlight">
   * it should behave like nonFullStack(stackWithOneItem)
   *           ^
   * </pre>
   *
   * <p>
   * For more information and examples of the use of <code>behave</code>, see the <a href="AnyFlatSpec.html#SharedTests">Shared tests section</a>
   * in the main documentation for trait <code>AnyFlatSpec</code>.
   * </p>
   */
  protected val behave = new BehaveWord

  /**
   * <strong>The <code>styleName</code> lifecycle method has been deprecated and will be removed in a future version of ScalaTest.</strong>
   *
   * <p>This method was used to support the chosen styles feature, which was deactivated in 3.1.0. The internal modularization of ScalaTest in 3.2.0
   * will replace chosen styles as the tool to encourage consistency across a project. We do not plan a replacement for <code>styleName</code>.</p>
   */
  @deprecated("The styleName lifecycle method has been deprecated and will be removed in a future version of ScalaTest with no replacement.", "3.1.0")
  final override val styleName: String = "org.scalatest.fixture.FlatSpec"

  override def testDataFor(testName: String, theConfigMap: ConfigMap = ConfigMap.empty): TestData = createTestDataFor(testName, theConfigMap, this)
}
