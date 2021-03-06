package services

import java.sql.Date

import spray.json.{DeserializationException, JsString, JsValue, RootJsonFormat}

/**
  *
  */
object FormatService {
  implicit object DateJsonFormat extends RootJsonFormat[Date] {
    def write(date:Date) = JsString(date.toString)
    
    def read(value: JsValue): Date = value match {
      case JsString(dateStr) ⇒ Date.valueOf(dateStr)
      case _ ⇒ throw DeserializationException("Date expected")
    }
  }
}
