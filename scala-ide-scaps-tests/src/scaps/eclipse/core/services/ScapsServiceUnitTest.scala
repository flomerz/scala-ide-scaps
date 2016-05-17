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

  val scapsService = new ScapsService()
  val pluginPreferences = mock(classOf[IEclipsePreferences])

  @Test
  def testExtractOfIJavaProject {
    scapsService.setIndexerRunning(true)
    val result = pluginPreferences.getBoolean("indexRunning", true)
    assertTrue(result)
  }

}
