import org.scalatest.{Matchers, WordSpec}
import com.byteslounge.slickrepo.meta.{Entity, VersionedEntity}

class SlickRepoSpec extends WordSpec with Matchers {

  trait EmptyTrait {

  }

  @SlickRepoEntity
  case class Person(override val id : Option[String]) extends EmptyTrait with Entity[Person,String]

  @SlickRepoEntity
  case class PersonVersioned(override val id : Option[String], override val version : Option[Long]) extends VersionedEntity[PersonVersioned, String, Long] with EmptyTrait

  "`withId` should be implemented by SlickRepoEntity annotation when extending Entity" in {
     Person(Some("2")).withId("3") shouldEqual Person(Some("3"))
  }

  "`withId` should be implemented by SlickRepoEntity annotation when extending VersionedEntity" in {
    PersonVersioned(Some("2"),Some(1L)).withId("3") shouldEqual PersonVersioned(Some("3"),Some(1L))
  }

  "`withVersion` should be implemented by SlickRepoEntity annotation when extending VersionedEntity" in {
    PersonVersioned(Some("2"),Some(1L)).withVersion(2L) shouldEqual PersonVersioned(Some("2"),Some(2L))
  }


  //Solving same problem without macros

  object CopyableEntity{
    type Copyable = {
      def copy[X,Z](param: Option[Z]) : X
    }
    implicit def toCopyable[T <: CopyableEntity[T, ID], ID]( base: CopyableEntity[T,ID] ): CopyableEntity[T,ID] with Copyable = base.asInstanceOf[CopyableEntity[T,ID] with Copyable]
  }

  trait CopyableEntity[T <: CopyableEntity[T, ID], ID] extends Entity[T,ID] {
    def withId(id: ID): T = (this).copy[T,ID](Some(id))
  }

  case class PersonCopy(override val id : Option[String]) extends CopyableEntity[PersonCopy,String]

  "`withId` should use case classs copy" in {
    PersonCopy(Some("2")).withId("3") shouldEqual PersonCopy(Some("3"))
  }

  object CopyableVersionEntity{
    type Copyable = {
      def copy[X,Y,Z](id: Option[Y],param: Option[Z]) : X
    }
    implicit def toCopyable[T <: CopyableVersionEntity[T, ID, V], ID, V]( base: CopyableVersionEntity[T,ID,V] ): CopyableVersionEntity[T,ID,V] with Copyable = base.asInstanceOf[CopyableVersionEntity[T,ID,V] with Copyable]
  }

  trait CopyableVersionEntity[T <: CopyableVersionEntity[T, ID, V], ID, V] extends VersionedEntity[T,ID,V] {
    def withId(id: ID): T = (this).copy[T,ID,V](Some(id),version)
    def withVersion(version: V): T = (this).copy[T,ID,V](id,Some(version))
  }


  case class PersonCopyVersion(override val id : Option[String], override val version : Option[String]) extends CopyableVersionEntity[PersonCopyVersion,String,String]

  "`withVersion` should use case classs copy" in {
    PersonCopyVersion(Some("2"),Some("2")).withId("3").withVersion("3") shouldEqual PersonCopyVersion(Some("3"),Some("3"))
  }

}