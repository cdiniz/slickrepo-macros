import org.scalatest.{Matchers, WordSpec}
import com.byteslounge.slickrepo.meta.Entity

class SlickRepoSpec extends WordSpec with Matchers {
  @SlickRepoEntity
  case class Person(override val id : Option[String]) extends Entity[Person,String]

  "`withId` should be implemented by SlickRepoEntity annotation" in {
     Person(Some("2")).withId("3") shouldEqual Person(Some("3"))
  }

}
