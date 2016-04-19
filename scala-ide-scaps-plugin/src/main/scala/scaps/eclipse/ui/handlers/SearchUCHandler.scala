package scaps.eclipse.ui.handlers

import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.core.IJavaProject
import scaps.eclipse.core.services.ScapsService
import org.eclipse.jdt.core.IPackageFragmentRoot

object SearchUCHandler extends AbstractUCHandler {
  def apply(): SearchUCHandler = {
    new SearchUCHandler(ScapsService(_indexDir))
  }
}

class SearchUCHandler(private val scapsService: ScapsService) {

  def apply(searchQuery: String): Unit = {
    val results = scapsService.search(searchQuery)
    println("Results:")
    results.foreach(println)
  }

}
