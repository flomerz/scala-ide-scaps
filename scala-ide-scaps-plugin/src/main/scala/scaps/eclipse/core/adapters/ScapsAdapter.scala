package scaps.eclipse.core.adapters

import java.io.File

import scala.io.Source
import scala.reflect.internal.util.BatchSourceFile
import scala.util.Try

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
import scaps.searchEngine.QueryError
import scaps.searchEngine.SearchEngine
import scaps.settings.Settings
import scala.util.Failure
import scala.util.control.NonFatal

sealed class ScapsError
case class ScapsEngineError(throwable: Throwable) extends ScapsError
case class ScapsSearchError(throwable: Throwable) extends ScapsError
case class ScapsSearchQueryError(queryError: QueryError) extends ScapsError

class ScapsAdapter(indexDir: String) extends StrictLogging {

  private val workspacePath = ResourcesPlugin.getWorkspace.getRoot.getLocation

  private def compiler(classPath: Seq[String]) = CompilerUtils.createCompiler(classPath)

  private def sourceExtractor(classPath: Seq[String]) = new ScalaSourceExtractor(compiler(classPath))
  private def libraryExtractor(classPath: Seq[String]) = new JarExtractor(compiler(classPath))

  private def searchEngine: ScapsEngineError \/ SearchEngine = {
    val conf = Settings.fromApplicationConf.modIndex { index => index.copy(indexDir = indexDir) }
    try {
      \/.right(SearchEngine(conf).get)
    } catch {
      case NonFatal(t) => \/.left(ScapsEngineError(t))
    }
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
      indexDefinitions(sourceExtractor(classPath)(sourceFiles.toList))
  }

  def indexLibrary(classPath: Seq[String], librarySourceRootFile: File): Unit = {
    indexDefinitions(libraryExtractor(classPath)(librarySourceRootFile))
  }

  def indexReset = searchEngine.map(_.resetIndexes.get)

  def indexFinalize = searchEngine.map(_.finalizeIndex.get)

  def search(searchQuery: String): ScapsError \/ Seq[Result[ValueDef]] = {
    searchEngine match {
      case -\/(error) => \/.left(error)
      case \/-(engine) =>
        try {
          engine.search(searchQuery, Set.empty).get match {
            case -\/(queryError) => \/.left(ScapsSearchQueryError(queryError))
            case \/-(result)     => \/.right(result)
          }
        } catch {
          case NonFatal(t) => \/.left(ScapsSearchError(t))
        }
    }
  }

  private def indexDefinitions(extractionStream: Stream[ExtractionError \/ Definition]): Unit = {
    val definitionSteam = ExtractionError.logErrors(extractionStream, logger.info(_))
    searchEngine.map(_.index(definitionSteam).get)
  }

}
