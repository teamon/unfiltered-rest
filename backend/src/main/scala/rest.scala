package com.monterail.rest

import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._
import unfiltered.netty.cycle.Plan
import scalaz._
import Scalaz._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.scalaz._
import net.liftweb.json.scalaz.JsonScalaz._

// Data model
case class Task(id: Int, name: String, content: String, priority: Int) extends Persistent[Task]{
    def withId(id: Int) = copy(id = id)
}

class TasksPlan extends Plan {
    val storage = new SimpleStorage[Task]

    implicit def TaskJSON = new JSON[Task] {
        def write(task: Task) = ("id" -> task.id) ~ ("name" -> task.name) ~ ("content" -> task.content) ~ ("priority" -> task.priority)
        def read(json: JValue) = Task.applyJSON(0 <~ field("id"), valid("name", notBlank), field("content"), valid("priority", min(1), max(10)))(json \ "task")
    }

    def intent = {
        case req @ Path(SegFormat("tasks" :: Nil, "json")) => req match {
            // index
            case GET(_) =>
                Json(toJSON(storage.all))

            // create
            case POST(_) & JsonParams(json) =>
                fromJSON[Task](json).map { task =>
                    Json(toJSON(storage.save(task)))
                } ||| { f => UnprocessableEntity ~> Json(toJSON(f)) }
        }

        case req @ Path(SegFormat("tasks" :: int(id) :: Nil, "json")) => req match {
            // show
            case GET(_) =>
                storage.find(id) map { task => Json(toJSON(task)) } getOrElse NotFound

            // update
            case PUT(_) & JsonParams(json) =>
                storage.find(id) map { _ =>
                    fromJSON[Task](json).map { task =>
                        Json(toJSON(storage.save(task)))
                    } ||| { f => UnprocessableEntity ~> Json(toJSON(f)) }
                } getOrElse NotFound

            // destroy
            case DELETE(_) =>
                if(storage.delete(id)) Ok else NotFound
        }
    }
}

// Only for debugging
class DebugPlan extends Plan {
    def intent = {
        case req =>
            println("""Started %s "%s" for %s""".format(req.method, req.uri, req.remoteAddr))
            Pass // Pass request to next plan
    }
}

object Main {
    def main(args: Array[String]) {
        val debug = new DebugPlan
        val tasks = new TasksPlan
        // Create http serverat port 8080
        (Http(8080) /: (debug :: tasks :: Nil))(_ handler _).run
    }
}
