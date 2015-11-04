package dicegame.domain

/**
 * Created by nigelnindo on 11/4/15.
 */
trait AggregateRoot[T <: AggregateRoot[T,E],E] {

  self: T =>

  def id: Id[T]
gi
  def uncommittedEvents: List[E]

  def applyEvents(events: E*): T = events.foldLeft(this)(_  applyEvent  _)

  def applyEvent: PartialFunction[E,T]

  def markCommitted: T

}
