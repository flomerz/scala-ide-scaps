/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.scalaide.scaps.actions

import java.io.File

import scala.io.Codec
import scala.io.Source
import scala.reflect.internal.util.BatchSourceFile

import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.core.IPackageFragmentRoot
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jface.action.IAction
import org.eclipse.jface.viewers.ISelection
import org.eclipse.ui.IWorkbenchWindow
import org.eclipse.ui.IWorkbenchWindowActionDelegate

import com.typesafe.scalalogging.StrictLogging

import scalaz.std.stream.streamInstance
import scaps.scala.featureExtraction.CompilerUtils
import scaps.scala.featureExtraction.ExtractionError
import scaps.scala.featureExtraction.JarExtractor
import scaps.scala.featureExtraction.ScalaSourceExtractor
import scaps.searchEngine.SearchEngine
import scaps.settings.Settings

/**
 * Our sample action implements workbench action delegate.
 * The action proxy will be created by the workbench and
 * shown in the UI. When the user tries to use the action,
 * this delegate will be created and execution will be
 * delegated to it.
 * @see IWorkbenchWindowActionDelegate
 */
class SampleAction extends IWorkbenchWindowActionDelegate with StrictLogging {
  private var window: IWorkbenchWindow = _

  /**
   * The action has been activated. The argument of the
   * method represents the 'real' action sitting
   * in the workbench UI.
   * @see IWorkbenchWindowActionDelegate#run
   */
  def run(action: IAction) {
    val workspace = ResourcesPlugin.getWorkspace.getRoot
    val workspacePath = workspace.getLocation
    val proj = workspace.getProjects.filter(_.hasNature(JavaCore.NATURE_ID)).head
    val javaProj = JavaCore.create(proj)
    val resClassPath = javaProj.getResolvedClasspath(true)
    val classPath = resClassPath.map(_.getPath.toString).toList
    val sourceClassPath = resClassPath.filter(_.getSourceAttachmentPath != null).map(_.getSourceAttachmentPath)

    val srcDirs = javaProj.getAllPackageFragmentRoots.filter(_.getKind == IPackageFragmentRoot.K_SOURCE)

    def printEach[T](array: Array[T], what: T => String): Unit = array.foreach { x => println(what(x)) }

    def printEachFile(array: Array[File]): Unit = printEach(array, { x: File => x.getAbsolutePath })

    def getFilesRecursive(file: File): Array[File] = {
      val dirFiles = file.listFiles
      printEachFile(dirFiles)
      dirFiles ++ dirFiles.filter(_.isDirectory).flatMap(getFilesRecursive)
    }

    val srcFiles = srcDirs.flatMap { x => getFilesRecursive(workspacePath.append(x.getPath).toFile()) }
    printEachFile(srcFiles)

    val scalaSrcFiles = srcFiles.filter(!_.getName.endsWith(".scala"))
    printEachFile(scalaSrcFiles)

    var conf = Settings.fromApplicationConf
    //conf.index.indexDir = workspacePath.toString
    val engine = SearchEngine(conf).get
    engine.resetIndexes()

    val compiler = CompilerUtils.createCompiler(classPath)
    val sourceExtractor = new ScalaSourceExtractor(compiler)
    val extractor = new JarExtractor(compiler)

    logger.info("classpath: [" + classPath.mkString(", ") + "]")
    logger.info("workspace location: " + workspacePath);

    val source = Source.fromFile(workspacePath + "/scala-ide-scaps-testproject/src/main/scala/edu/scaps/Hello.scala")(Codec.UTF8).toSeq

    val sourceFile = new BatchSourceFile(workspacePath + "/scala-ide-scaps-testproject/src/main/scala/edu/scaps/Hello.scala", source)

    val jars = List(workspacePath + "/scalaz-core_2.11-7.2.1-sources.jar")

    jars.foreach { jar =>
      // extrahieren aller definitionen
      val defsWithErrors = extractor(new File(jar))
      //            val defsWithErrors = sourceExtractor.apply(List(sourceFile))

      // Fehler behandeln
      def defs = ExtractionError.logErrors(defsWithErrors, logger.info(_))

      // Defs in indexieren
      engine.index(defs).get
    }

    // index schliessen
    engine.finalizeIndex().get

    val modules = engine.indexedModules().get

    val result = engine.search("max", Set()).get
    if (result.isRight) {
      println("search with no errors!")
      result.foreach(_.foreach { res =>
        println("Score: " + res.score)
        println("Explanation: " + res.explanation)
        println("Entity: " + res.entity.name)
      })
    }
    println("search done!")

  }

  /**
   * Selection in the workbench has been changed. We
   * can change the state of the 'real' action here
   * if we want, but this can only happen after
   * the delegate has been created.
   * @see IWorkbenchWindowActionDelegate#selectionChanged
   */
  def selectionChanged(action: IAction, selection: ISelection) {
  }

  /**
   * We can use this method to dispose of any system
   * resources we previously allocated.
   * @see IWorkbenchWindowActionDelegate#dispose
   */
  def dispose() {
  }

  /**
   * We will cache window object in order to
   * be able to provide parent shell for the message dialog.
   * @see IWorkbenchWindowActionDelegate#init
   */
  def init(window: IWorkbenchWindow) {
    this.window = window;
  }
}