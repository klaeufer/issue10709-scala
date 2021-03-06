import org.scalatest.WordSpec

import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar

class TestIssue10709 extends WordSpec with MockitoSugar {

  def scanLeftUsingIterate[T, U](it: Iterator[T])(z: U)(op: (U, T) => U): Iterator[U] =
    Iterator.iterate(Option(z)) {
      case Some(r) =>
        if (it.hasNext) Option(op(r, it.next())) else None
    } takeWhile (_.isDefined) map (_.get)

  def verifyIncremental(sut: Iterator[Int] => Iterator[Int]): Unit = {
    val it = spy(Iterator(1, 2, 3))
    val expected = Array(0, 1, 3, 6)
    val result = sut(it)
    for (i <- expected.indices) {
      assert(result.next() === expected(i))
      verify(it, times(i)).next()
    }
  }

  // TODO make more DRY

  "Iterator.scanLeft" when {
    "given an empty iterator" should {
      "produce the correct a singleton result" in {
        val result = Seq.empty[Int].iterator.scanLeft(0)(_ + _)
        assert(result.toSeq === Seq(0))
      }
    }

    "given a nonempty iterator" should {
      "produce the correct cumulative result" in {
        val result = Iterator(1, 2, 3).scanLeft(0)(_ + _)
        assert(result.toSeq === Seq(0, 1, 3, 6))
      }

      "exhibit the correct incremental element-by-element behavior" in
        verifyIncremental(_.scanLeft(0)(_ + _))
    }
  }

  "scanLeftUsingIterate" when {
    "given an empty iterator" should {
      "produce the correct a singleton result" in {
        val result = scanLeftUsingIterate(Seq.empty[Int].iterator)(0)(_ + _)
        assert(result.toSeq === Seq(0))
      }
    }

    "given a nonempty iterator" should {
      "produce the correct cumulative result" in {
        val result = scanLeftUsingIterate(Iterator(1, 2, 3))(0)(_ + _)
        assert(result.toSeq === Seq(0, 1, 3, 6))
      }

      "exhibit the correct incremental element-by-element behavior" in
        verifyIncremental(input => scanLeftUsingIterate(input)(0)(_ + _))
    }
  }
}
