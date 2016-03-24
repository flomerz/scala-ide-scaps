package org.scalaide.scaps

import org.junit.Before
import org.junit.Test
import org.junit.Assert._

class ExampleTest {

  @Before
  def setup {
    println("iuu setup test")
  }

  @Test
  def test1 {
    println("iuu test1")
    fail("iuu fail!")
  }

}
