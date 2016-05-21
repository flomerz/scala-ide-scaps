package scaps.eclipse.core.services

import java.io.File
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Status
import org.eclipse.core.runtime.SubMonitor
import org.eclipse.core.runtime.jobs.Job
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.IPackageFragmentRoot
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot
import org.eclipse.jdt.internal.core.JavaElement
import org.eclipse.jdt.internal.core.PackageFragment
import org.eclipse.ui.IWorkingSet
import com.typesafe.scalalogging.StrictLogging
import scaps.api.Result
import scaps.api.ValueDef
import scaps.eclipse.core.adapters.ScapsAdapter
import org.eclipse.core.runtime.preferences.InstanceScope
import scaps.eclipse.ScapsPlugin
import org.eclipse.core.resources.ResourcesPlugin

object ScapsService {

  private val pluginPreferences = InstanceScope.INSTANCE.getNode(ScapsPlugin.PLUGIN_ID)
  private val indexRootDir = ResourcesPlugin.getWorkspace.getRoot.getLocation.toString + ScapsPlugin.INDEX_RELATIVE_ROOT_DIR

  val PROPERTY_INDEXER_RUNNING = "indexRunning"

  val PROPERTY_SEARCH_ON_FIRST_INDEX = "searchOnFirstIndex"
  val FIRST_INDEX_DIR = "first"
  val SECOND_INDEX_DIR = "second"

  private def isSearchOnFirstIndex = pluginPreferences.getBoolean(PROPERTY_SEARCH_ON_FIRST_INDEX, true)
  private def searchIndexDir = if (isSearchOnFirstIndex) FIRST_INDEX_DIR else SECOND_INDEX_DIR
  private def indexingIndexDir = if (isSearchOnFirstIndex) SECOND_INDEX_DIR else FIRST_INDEX_DIR

  def swapIndexDirs: Unit = {
    val searchOnFirstIndex = pluginPreferences.getBoolean(PROPERTY_SEARCH_ON_FIRST_INDEX, true)
    pluginPreferences.putBoolean(PROPERTY_SEARCH_ON_FIRST_INDEX, !searchOnFirstIndex)
    pluginPreferences.flush
  }

  def setIndexerRunning(running: Boolean): Unit = {
    pluginPreferences.putBoolean(PROPERTY_INDEXER_RUNNING, running)
    pluginPreferences.flush
  }

  def isIndexerRunning: Boolean = {
    pluginPreferences.getBoolean(PROPERTY_INDEXER_RUNNING, false)
  }

  def createSearchService = {
    val indexDir = indexRootDir + searchIndexDir
    val scapsAdapter = new ScapsAdapter(indexDir)
    new ScapsSearchService(scapsAdapter)
  }

  def createIndexService = {
    val indexDir = indexRootDir + indexingIndexDir
    val scapsAdapter = new ScapsAdapter(indexDir)
    new ScapsIndexService(scapsAdapter)
  }

}
