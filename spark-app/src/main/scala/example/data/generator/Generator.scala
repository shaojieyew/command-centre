package example.data.generator
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.Properties

import example.data.entity.Person
import org.scalacheck.Gen
import org.scalacheck.Gen.listOfN


abstract class Generator[T](writers: List[Writer]){
  protected def generateData(): T

  def write[T](): Unit ={
    val t = generateData()
    writers.foreach(_.start(t))
  }

  def writeN[T](n: Int = 1, intervalSeconds:Int = 1): Unit ={
      for(n <- 0 until n){
        write[T]
        Thread.sleep(intervalSeconds*1000)
      }
  }

  def writeForever[T](batchSize: Int = 1, intervalSeconds:Int = 1): Unit ={
    while(true){
      for(n <- 0 until batchSize){
        write[T]
      }
      Thread.sleep(intervalSeconds*1000)
    }
  }
}

class PersonGenerator(writers: List[Writer]) extends Generator[Person](writers: List[Writer]) {

  protected override def generateData(): Person = {
    def genId: Gen[String] = (for {
      idPart1 <- listOfN(1, Gen.alphaUpperChar);
      idPart2 <- listOfN(8,Gen.numChar)
      idPart3 <- listOfN(1, Gen.alphaUpperChar)
    } yield (idPart1:::idPart2:::idPart3).mkString)
    def genName: Gen[String] = (for {
      namePart1 <- listOfN(1, Gen.alphaUpperChar);
      namePart2 <- listOfN(7, Gen.alphaLowerChar)
    } yield (namePart1:::namePart2).mkString)

    val person = Person(id = genId.sample,
      name = genName.sample,
      age = Gen.choose(0,70).sample,
      income = Gen.chooseNum(1,1000).sample.map(_*100),
      married = Gen.oneOf(List(false,true)).sample,
      timestamp = Option(Timestamp.valueOf(LocalDateTime.now().minusMinutes(Gen.chooseNum(0,60).sample.get))))

    person
  }

}