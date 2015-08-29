package classifier

trait Distance[A,D] {
  def distance(x: A, y: A): D
}
