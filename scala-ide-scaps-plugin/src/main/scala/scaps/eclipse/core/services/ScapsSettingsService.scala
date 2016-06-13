/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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
import java.util.concurrent.Semaphore

object ScapsSettingsService {

  private[services] val indexRootDir = ResourcesPlugin.getWorkspace.getRoot.getLocation.toString + ScapsPlugin.INDEX_RELATIVE_ROOT_DIR
  private val pluginPreferences = InstanceScope.INSTANCE.getNode(ScapsPlugin.PLUGIN_ID)

  val PROPERTY_INDEXER_RUNNING = "indexRunning"

  val PROPERTY_SEARCH_ON_FIRST_INDEX = "searchOnFirstIndex"
  val FIRST_INDEX_DIR = "first"
  val SECOND_INDEX_DIR = "second"

  val PROPERTY_PREFILLED_SEARCH_QUERY = "prefilledSearchQuery"

  private def isSearchOnFirstIndex = pluginPreferences.getBoolean(PROPERTY_SEARCH_ON_FIRST_INDEX, true)
  private[services] def searchIndexDir = if (isSearchOnFirstIndex) FIRST_INDEX_DIR else SECOND_INDEX_DIR
  private[services] def indexingIndexDir = if (isSearchOnFirstIndex) SECOND_INDEX_DIR else FIRST_INDEX_DIR

  def swapIndexDirs: Unit = {
    val searchOnFirstIndex = pluginPreferences.getBoolean(PROPERTY_SEARCH_ON_FIRST_INDEX, true)
    pluginPreferences.putBoolean(PROPERTY_SEARCH_ON_FIRST_INDEX, !searchOnFirstIndex)
    pluginPreferences.flush
  }

  private val indexerRunningSemaphore = new Semaphore(1, true)
  def setIndexerRunning(running: Boolean): Unit = {
    indexerRunningSemaphore.acquire
    pluginPreferences.putBoolean(PROPERTY_INDEXER_RUNNING, running)
    pluginPreferences.flush
    indexerRunningSemaphore.release
  }

  def isIndexerRunning: Boolean = {
    indexerRunningSemaphore.acquire
    val running = pluginPreferences.getBoolean(PROPERTY_INDEXER_RUNNING, false)
    indexerRunningSemaphore.release
    running
  }

  private val prefilledSearchQuerySemaphore = new Semaphore(1, true)
  def setPrefilledSearchQuery(query: String): Unit = {
    prefilledSearchQuerySemaphore.acquire
    pluginPreferences.put(PROPERTY_PREFILLED_SEARCH_QUERY, query)
    pluginPreferences.flush
    prefilledSearchQuerySemaphore.release
  }

  def getPrefilledSearchQuery: String = {
    prefilledSearchQuerySemaphore.acquire
    val query = pluginPreferences.get(PROPERTY_PREFILLED_SEARCH_QUERY, "")
    prefilledSearchQuerySemaphore.release
    query
  }

}
