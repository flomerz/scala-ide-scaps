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

object ScapsService {

  def createSearchService = {
    val indexDir = ScapsSettingsService.indexRootDir + ScapsSettingsService.searchIndexDir
    val scapsAdapter = new ScapsAdapter(indexDir)
    new ScapsSearchService(scapsAdapter)
  }

  def createIndexService = {
    val indexDir = ScapsSettingsService.indexRootDir + ScapsSettingsService.indexingIndexDir
    val scapsAdapter = new ScapsAdapter(indexDir)
    new ScapsIndexService(scapsAdapter)
  }

}
