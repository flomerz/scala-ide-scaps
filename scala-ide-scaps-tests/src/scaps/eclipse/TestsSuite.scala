package scaps.eclipse

import org.junit.runner.RunWith
import org.junit.runners.Suite
import scaps.eclipse.core.adapters.ScapsAdapterIntegrationTest
import scaps.eclipse.core.services.ScapsServiceUnitTest

@RunWith(classOf[Suite])
@Suite.SuiteClasses(Array(
  classOf[ExampleIntegrationTest],
  classOf[ScapsAdapterIntegrationTest],
  classOf[ScapsServiceUnitTest]))
class TestsSuite {}
