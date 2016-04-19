package scaps.eclipse.core.adapters

import java.io.File
import scala.io.Codec
import scala.io.Source
import scala.reflect.internal.util.BatchSourceFile
import org.eclipse.core.resources.ResourcesPlugin
import com.typesafe.scalalogging.StrictLogging
import scalaz.{ \/ => \/ }
import scalaz.std.stream.streamInstance
import scaps.api.Definition
import scaps.scala.featureExtraction.CompilerUtils
import scaps.scala.featureExtraction.ExtractionError
import scaps.scala.featureExtraction.JarExtractor
import scaps.scala.featureExtraction.ScalaSourceExtractor
import scaps.searchEngine.SearchEngine
import scaps.settings.Settings
import org.eclipse.jdt.core.ICompilationUnit
import scaps.api.Result
import scaps.api.ValueDef

class ScapsAdapter(indexDir: String) extends StrictLogging {
  private val workspacePath = ResourcesPlugin.getWorkspace.getRoot.getLocation

  private def compiler(classPath: Seq[String]) = CompilerUtils.createCompiler(classPath)

  private def sourceExtractor(classPath: Seq[String]) = new ScalaSourceExtractor(compiler(classPath))
  private def libraryExtractor(classPath: Seq[String]) = new JarExtractor(compiler(classPath))

  private def searchEngine = {
    val conf = Settings.fromApplicationConf.modIndex { index => index.copy(indexDir = indexDir) }
    SearchEngine(conf).get
  }

  def indexProject(classPath: Seq[String], projectSourceUnits: Seq[ICompilationUnit]): Unit = {
    val sourceFiles = projectSourceUnits.map { projectSourceUnit =>
      val projectSourcePath = projectSourceUnit.getPath
      val projectSourceAbsolutePath = workspacePath.append(projectSourcePath).toOSString
      val codec = Codec.UTF8 // how can i get the codec from a ICompilationUnit
      val source = Source.fromFile(projectSourceAbsolutePath)(codec).toSeq
      new BatchSourceFile(projectSourceAbsolutePath, source)
    }
    indexDefinitions(sourceExtractor(classPath)(sourceFiles.toList))
  }

  def indexLibrary(classPath: Seq[String], librarySourceRootFile: File): Unit = {
    indexDefinitions(libraryExtractor(classPath)(librarySourceRootFile))
  }

  def indexFinalize = searchEngine.finalizeIndex.get

  def search(searchQuery: String): Seq[Result[ValueDef]] = {
    searchEngine.search(searchQuery, Set()).get.getOrElse(List())
  }

  private def indexDefinitions(definitionStream: Stream[ExtractionError \/ Definition]): Unit = {
    def definitions = ExtractionError.logErrors(definitionStream, logger.info(_))
    searchEngine.index(definitions).get
  }

}
