package com.example

object Hello {
  def main(args: Array[String]): Unit = {
    val s = getSomeString()
    println(s)
  }

  def getRandom(): Int = {
      val r = scala.util.Random
      r.nextInt(100)
  }

  def getSomeString(): String = {
      val s = "Hello, world!"
      s
  }

  def notCovered(): String = {
      "This not tested!"
  }
}
