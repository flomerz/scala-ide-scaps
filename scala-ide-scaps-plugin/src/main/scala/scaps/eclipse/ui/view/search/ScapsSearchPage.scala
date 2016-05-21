package scaps.eclipse.ui.view.search

import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.IStatus
import org.eclipse.jface.dialogs.Dialog
import org.eclipse.jface.dialogs.DialogPage
import org.eclipse.search.ui.IQueryListener
import org.eclipse.search.ui.ISearchPage
import org.eclipse.search.ui.ISearchPageContainer
import org.eclipse.search.ui.ISearchQuery
import org.eclipse.search.ui.ISearchResult
import org.eclipse.search.ui.NewSearchUI
import org.eclipse.swt.SWT
import org.eclipse.swt.events.FocusEvent
import org.eclipse.swt.events.FocusListener
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Label
import org.eclipse.swt.widgets.Text
import scaps.eclipse.ui.handlers.SearchUCHandler
import org.eclipse.core.runtime.Status
import org.eclipse.search.ui.ISearchResultListener
import org.eclipse.jface.resource.ImageDescriptor
import com.typesafe.scalalogging.StrictLogging
import org.eclipse.swt.widgets.Link
import org.eclipse.swt.events.SelectionAdapter
import org.eclipse.swt.events.SelectionEvent

class ScapsSearchPage extends DialogPage with ISearchPage with StrictLogging {

  private var inputText: Text = _

  private val exampleQueriesText = """<a>max: Int</a> - An integer value with `max` in it's name or doc comment
                                     |<a>max: (Int, Int) => Int</a> - A function taking two ints and returning Int.
                                     |<a>max: Int => Int => Int</a> - Same query as above but in curried form.
                                     |<a>Ordering[String]</a> - Implementations of the `Ordering` typeclass for strings.
                                     |<a>List[A] => Int => Option[A]</a> - A generic query which uses a type parameter `A`.
                                     |All type identifiers consisting of a single character are treated as type parameters.
                                     |<a>List => Int => Option</a> - The identical query as above but with omitted type parameters.
                                     |<a>+</a> - Searches for symbolic operators are also possible.""".stripMargin

  def createControl(parent: Composite): Unit = {
    val result = new Composite(parent, SWT.NONE)
    result.setFont(parent.getFont)
    val layout = new GridLayout(1, false)
    result.setLayout(layout)

    val searchTitleLabel = new Label(result, SWT.NONE)
    searchTitleLabel.setText("Search string:")

    inputText = new Text(result, SWT.BORDER)
    inputText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1))

    val exampleQueriesLabel = new Label(result, SWT.NONE)
    exampleQueriesLabel.setText("Example Queries:")

    val exampleQueriesTextLink = new Link(result, SWT.NONE)
    exampleQueriesTextLink.setText(exampleQueriesText)
    exampleQueriesTextLink.addSelectionListener(new SelectionAdapter {
      override def widgetSelected(event: SelectionEvent): Unit = {
        inputText.setText(event.text)
      }
    })

    setControl(result)
    Dialog.applyDialogFont(result)
  }

  def performAction: Boolean = {
    search(inputText.getText)
    true
  }

  private def search(text: String): Unit = {
    NewSearchUI.runQueryInBackground(new ScapsSearchQuery(text))
    NewSearchUI.activateSearchResultView()
  }

  def setContainer(container: ISearchPageContainer): Unit = {}
}
