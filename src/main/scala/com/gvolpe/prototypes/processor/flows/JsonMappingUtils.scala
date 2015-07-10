package com.gvolpe.prototypes.processor.flows

import play.api.libs.json.{Writes, Reads, Json}

import scala.util.Try

trait JsonMappingUtils[T] {

  def fromJson(value: String)(implicit reader: Reads[T]): Try[Option[T]] =
    Try(Json.fromJson[T](Json.parse(value)).asOpt)

  def toJson(obj: T)(implicit writer: Writes[T]): String = Json.toJson(obj).toString()

}
