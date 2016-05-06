package scaps.eclipse.core.adapters

import java.io.File
import scala.io.Source
import scala.reflect.internal.util.BatchSourceFile
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.core.ICompilationUnit
import com.typesafe.scalalogging.StrictLogging
import scalaz._
import scalaz.std.stream.streamInstance
import scaps.api.Definition
import scaps.api.Result
import scaps.api.ValueDef
import scaps.scala.featureExtraction.CompilerUtils
import scaps.scala.featureExtraction.ExtractionError
import scaps.scala.featureExtraction.JarExtractor
import scaps.scala.featureExtraction.ScalaSourceExtractor
import scaps.searchEngine.SearchEngine
import scaps.settings.Settings
import scaps.api.Module

class ScapsAdapter(indexDir: String) extends StrictLogging {
  private val workspacePath = ResourcesPlugin.getWorkspace.getRoot.getLocation

  private def compiler(classPath: Seq[String]) = CompilerUtils.createCompiler(classPath)

  private def sourceExtractor(classPath: Seq[String]) = new ScalaSourceExtractor(compiler(classPath))
  private def libraryExtractor(classPath: Seq[String]) = new JarExtractor(compiler(classPath))

  private def searchEngine = {
    val conf = Settings.fromApplicationConf.modIndex { index => index.copy(indexDir = indexDir) }
    SearchEngine(conf).get
  }

  def indexProject(classPath: Seq[String], projectSourceUnits: Seq[ICompilationUnit]): Unit = projectSourceUnits match {
    case Seq() =>
    case _ =>
      val sourceFiles = projectSourceUnits.map { projectSourceUnit =>
        val projectSourcePath = projectSourceUnit.getPath
        val projectSourceAbsolutePath = workspacePath.append(projectSourcePath).toOSString
        val codec = projectSourceUnit.getResource.asInstanceOf[IFile].getCharset
        val source = Source.fromFile(projectSourceAbsolutePath, codec).toSeq
        new BatchSourceFile(projectSourceAbsolutePath, source)
      }
      val module = Module(projectSourceUnits.head.toString, "PROJECT SOURCE", "0.1.0-SNAPSHOT")
      indexDefinitions(sourceExtractor(classPath)(sourceFiles.toList), module)
  }

  def indexLibrary(classPath: Seq[String], librarySourceRootFile: File): Unit = {
    val module = Module(librarySourceRootFile.getAbsolutePath, librarySourceRootFile.getName, "0.1.0-SNAPSHOT")
    indexDefinitions(libraryExtractor(classPath)(librarySourceRootFile), module)
  }

  def indexReset = searchEngine.resetIndexes

  def indexFinalize = searchEngine.finalizeIndex.get

  def search(searchQuery: String): Seq[Result[ValueDef]] = {
    searchEngine.search(searchQuery, Set()).get.getOrElse(List())
  }

  private def indexDefinitions(extractionStream: Stream[ExtractionError \/ Definition], module: Module): Unit = {

    def handleExtractionErrors(extractionStream: Stream[ExtractionError \/ Definition]): Stream[Definition] =
      ExtractionError.logErrors(extractionStream, logger.info(_))

    def withModule(definitionStream: Stream[Definition], module: Module): Stream[Definition] =
      for (definition <- definitionStream) yield {
        definition.withModule(module)
      }

    val definitionSteam = withModule(handleExtractionErrors(extractionStream), module)
    searchEngine.index(definitionSteam).get
  }

}
