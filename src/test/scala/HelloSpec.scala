import org.scalatest._
import com.example._

class HelloSpec extends FlatSpec with Matchers {
  "Hello" should "always pass" in {
    true should === (true)
  }

  "Hello" should "sometimes pass" in {
      val n = Hello.getRandom()
      n should be > 50
  }

  "Hello" should "greet me" in {
      val s = Hello.getSomeString()
      s should include ("Hello")
  }
}
