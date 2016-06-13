/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package scaps.eclipse.core.services

import org.eclipse.core.runtime.IAdaptable
import org.eclipse.core.runtime.IPath
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.IPackageFragmentRoot
import org.eclipse.ui.IWorkingSet
import org.junit.Assert._
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.runners.MockitoJUnitRunner
import org.junit.Before
import java.io.File
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot
import org.eclipse.core.runtime.preferences.IEclipsePreferences

@RunWith(classOf[MockitoJUnitRunner])
class ScapsServiceUnitTest {

  val scapsService = ScapsSettingsService
  val pluginPreferences = mock(classOf[IEclipsePreferences])

  @Test
  def testExtractOfIJavaProject {
    when(pluginPreferences.getBoolean(anyString(), anyBoolean())).thenReturn(true)
    scapsService.setIndexerRunning(true)
    val result = pluginPreferences.getBoolean("indexRunning", true)
    assertTrue(result)
  }

}
