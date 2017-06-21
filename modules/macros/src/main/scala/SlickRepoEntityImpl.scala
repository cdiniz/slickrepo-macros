import scala.annotation.StaticAnnotation
import scala.language.experimental.macros
import scala.reflect.api.Trees
import scala.reflect.macros.whitebox

class SlickRepoEntity() extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro SlickRepoEntityImpl.impl
}

object SlickRepoEntityImpl {

  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    def extractCaseClassesParts(classDecl: ClassDef) = classDecl match {
      case q"case class $className(..$fields) extends ..$parents { ..$body }" =>
        (className, fields, parents, body)
    }

    def modifiedDeclaration(classDecl: ClassDef) = {
      val (className, fields, parents, body) = extractCaseClassesParts(classDecl)

      def extractEntityType(parents: Seq[Trees#Tree]) = {
        parents.collectFirst {
          case AppliedTypeTree(_, _ :: b :: _) => b
        }
      }

      extractEntityType(parents) match {
        case Some(idTpe) =>
          c.Expr[Any](
            q"""
              case class $className ( ..$fields ) extends ..$parents {
                override def withId(id: $idTpe): $className = {
                  this.copy(id = Some(id))
                }
                ..$body
              }
            """
          )
        case None => c.abort(c.enclosingPosition, "Invalid  usage")
      }
    }

    annottees map (_.tree) toList match {
      case (classDecl: ClassDef) :: Nil => modifiedDeclaration(classDecl)
      case _ => c.abort(c.enclosingPosition, "Invalid usage")
    }
  }
}