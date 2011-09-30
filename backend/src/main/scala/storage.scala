package com.monterail.rest

import scala.collection.mutable.Map

trait Persistent[T] {
    def id: Int

    def withId(id: Int): T
}

// Simple in-memory storage
class SimpleStorage[T <: Persistent[T]] {
    val data = Map[Int, T]()

    def find(id: Int) = data get id

    def all = data.values.toList

    def save(obj: T) = store(if(obj.id == 0) obj.withId(nextId) else obj)

    def delete(id: Int) = find(id).map(_ => data -= id).isDefined

    protected def nextId = if(data.keys.isEmpty) 1 else data.keys.max + 1

    protected def store(obj: T) = {
        data(obj.id) = obj
        obj
    }

}
