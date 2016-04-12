package scaps.eclipse

import org.junit.runner.RunWith
import org.junit.runners.Suite

import scaps.eclipse.core.adapters.ScapsAdapterIntegrationTest


@RunWith(classOf[Suite])
@Suite.SuiteClasses(Array(
  classOf[ExampleIntegrationTest],
  classOf[ScapsAdapterIntegrationTest]
))
class TestsSuite {}
