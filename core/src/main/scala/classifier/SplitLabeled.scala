package classifier

/**
 * Created by arya on 5/23/15.
 */
trait SplitLabeled[LI,L,I] {
  def label(li: LI): L
  def unlabeled(li: LI): I
  def split(li: LI): (I,L)
}
