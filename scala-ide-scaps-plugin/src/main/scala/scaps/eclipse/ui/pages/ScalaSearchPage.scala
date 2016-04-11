package scaps.eclipse.ui.pages

import org.eclipse.jface.dialogs.DialogPage
import org.eclipse.search.ui.ISearchPage
import org.eclipse.swt.SWT
import java.awt.Panel
import org.eclipse.search.ui.ISearchPageContainer
import org.eclipse.search2.internal.ui.InternalSearchUI
import sun.security.jca.GetInstance
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Text
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.widgets.Label
import org.eclipse.jface.dialogs.Dialog
import org.eclipse.swt.events.FocusListener
import org.eclipse.swt.events.FocusEvent

class ScalaSearchPage extends DialogPage with ISearchPage {
    
  private var inputText: Text = _
  

  def createControl(parent: Composite) {
    val result = new Composite(parent, SWT.NONE)
    result.setFont(parent.getFont)
    val layout = new GridLayout(1, false)
    result.setLayout(layout)
    
    inputText = new Text(result, SWT.BORDER)
    inputText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1))
    inputText.setText("Search for functions, methods and values...")
    inputText.addFocusListener(new FocusListener(){
      
      def focusGained(focus: FocusEvent){
        inputText.setText("")
      }
      
      def focusLost(focus: FocusEvent){
        if(inputText.equals("")){
          inputText.setText("Search for functions, methods and values...")
        }
      }
      
    })
    
    val exampleQueriesLabel = new Label(result, SWT.NONE)
    exampleQueriesLabel.setText("Example Queries")
    
    val exampleQueriesTextLabel = new Label(result, SWT.NONE)
    exampleQueriesTextLabel.setText("max: Int - An integer value with `max` in it's name or doc comment \n max: (Int, Int) => Int - A function taking two ints and returning Int. \n max: Int => Int => Int - Same query as above but in curried form. \n Ordering[String] - Implementations of the `Ordering` typeclass for strings. \n List[A] => Int => Option[A] - A generic query which uses a type parameter `A`. All type identifiers consisting of a single character are treated as type parameters. \n List => Int => Option - The identical query as above but with omitted type parameters. \n &~ - Searches for symbolic operators are also possible.")
    
    setControl(result)
    Dialog.applyDialogFont(result)
  }

  def performAction(): Boolean = {
    print(inputText.getText)
    //InternalSearchUI.getInstance.runSearchInBackground(null, null)
    true
  }

  def setContainer(container: ISearchPageContainer) {

  }
}