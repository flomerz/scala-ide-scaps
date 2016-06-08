package scaps.eclipse

import org.junit.runner.RunWith
import org.junit.runners.Suite
import scaps.eclipse.core.adapters.ScapsAdapterIntegrationTest
import scaps.eclipse.core.services.ScapsServiceUnitTest
import scaps.eclipse.core.services.ScapsIndexServiceUnitTest
import scaps.eclipse.core.services.ScapsIndexServiceUnitTest

@RunWith(classOf[Suite])
@Suite.SuiteClasses(Array(

  // Unit
  classOf[ScapsIndexServiceUnitTest],
  classOf[ScapsServiceUnitTest],

  // Integration
  classOf[ScapsAdapterIntegrationTest] //
  ))
class TestsSuite {}
