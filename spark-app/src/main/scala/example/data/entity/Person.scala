package example.data.entity

import java.sql.Timestamp

case class Person(id: Option[String],
                  name: Option[String],
                  age: Option[Int]=None,
                  income: Option[Long]=None,
                  married: Option[Boolean]=None,
                  timestamp: Option[Timestamp]=None
                 )

