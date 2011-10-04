package com.monterail

import unfiltered.request._
import unfiltered.response._
import scalaz._
import Scalaz._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.scalaz._
import net.liftweb.json.scalaz.JsonScalaz._

package object rest {
    implicit def defaultFieldValue[A](default: A) = new {
        def <~(f: JValue => Result[A]): JValue => Result[A] = json => (f(json) | default).success
    }

    implicit object NonEmptyListErrorJSON extends JSONW[NonEmptyList[JsonScalaz.Error]] {
        def errorMsg(error: JsonScalaz.Error) = error match {
            case UnexpectedJSONError(was, expected) => "Unexpected JSON error. Was " + was + ", expected " + expected
            case NoSuchFieldError(name, json) => name.capitalize + " can't be blank"
            case UncategorizedError(key, desc, args) => "%s %s".format(key.capitalize, desc)
        }
        def write(nel: NonEmptyList[JsonScalaz.Error]) = ("errors" -> nel.list.map(errorMsg))
    }

    // Match request with format
    object SegFormat {
        def unapply(path: String): Option[(List[String], String)] = Seg unapply path map extractFormat

        val Rx = """(.+)\.([^.]+)$""".r

        def extractFormat(parts: List[String]): (List[String], String) = parts.reverse match {
            case Rx(part, format) :: xs => ((part :: xs).reverse, format)
            case _ => (parts, "")
        }
    }

    // Extract body as json object
    object JsonParams {
        def unapply[T](req: HttpRequest[T]) = JsonBody(req)
    }

    object int {
        def unapply(s: String) = catchAll(s.toInt)
    }

    def catchAll[T](f: => T) = try { Some(f) } catch { case _ => None }
}