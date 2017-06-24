import scala.annotation.StaticAnnotation
import scala.language.experimental.macros
import scala.reflect.api.Trees
import scala.reflect.macros.whitebox

class SlickRepoEntity() extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro SlickRepoEntityImpl.impl
}

object SlickRepoEntityImpl {
  val errorMessage = "@SlickRepoEntity should be used with a case class that extends Entity or VersionedEntity"

  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    (annottees map (_.tree) toList match {
      case q"case class $className(..$fields) extends ..$parents { ..$body }" :: Nil =>
        parents
          .filter {
            case AppliedTypeTree(Ident(TypeName(entity)), _) if entity == "Entity" ||  entity == "VersionedEntity" => true
            case _ => false
          }
          .collectFirst{
            case AppliedTypeTree(_,  _  :: entityType :: versionType :: _) =>
              q"""
                override def withId(id: $entityType): $className = {
                  this.copy(id = Some(id))
                }
                override def withVersion(version: $versionType): $className = {
                  this.copy(version = Some(version))
                }
            """
            case AppliedTypeTree(_, _ :: entityType :: _)  =>
              q"""
                override def withId(id: $entityType): $className = {
                  this.copy(id = Some(id))
                }
            """
          }.map( overrideMethods =>
          c.Expr[Any](
            q"""
              case class $className ( ..$fields ) extends ..$parents {
                ..$overrideMethods
                ..$body
              }
            """
          )
        )
      case _ => None
    }).getOrElse(c.abort(c.enclosingPosition, errorMessage))
  }
}