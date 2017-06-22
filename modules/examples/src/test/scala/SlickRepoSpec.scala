import org.scalatest.{Matchers, WordSpec}
import com.byteslounge.slickrepo.meta.{Entity, VersionedEntity}

class SlickRepoSpec extends WordSpec with Matchers {
  @SlickRepoEntity
  case class Person(override val id : Option[String]) extends Entity[Person,String]

  @SlickRepoEntity
  case class PersonVersioned(override val id : Option[String], override val version : Option[Long]) extends VersionedEntity[PersonVersioned, String, Long]

  "`withId` should be implemented by SlickRepoEntity annotation when extending Entity" in {
     Person(Some("2")).withId("3") shouldEqual Person(Some("3"))
  }

  "`withId` should be implemented by SlickRepoEntity annotation when extending VersionedEntity" in {
    PersonVersioned(Some("2"),Some(1L)).withId("3") shouldEqual PersonVersioned(Some("3"),Some(1L))
  }

  "`withVersion` should be implemented by SlickRepoEntity annotation when extending VersionedEntity" in {
    PersonVersioned(Some("2"),Some(1L)).withVersion(2L) shouldEqual PersonVersioned(Some("2"),Some(2L))
  }

}
