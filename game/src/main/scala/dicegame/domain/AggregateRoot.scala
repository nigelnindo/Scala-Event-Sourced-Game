package dicegame.domain

/**
 * Created by nigelnindo on 11/4/15.
 */
trait AggregateRoot[T <: AggregateRoot[T,E],E] {

  self: T =>

  def id: Id[T]

  def uncommittedEvents: List[E]

  /*
    ->  E* means that as many parameters of type E can be passed to the
         applyEvents function/method.
    ->  Type E in this project will be GameEvents. You could definitely have
        more than one type though.
    ->  applyEvent function will be executed for every event passed on to
        applyEvents.
   */

  def applyEvents(events: E*): T = events.foldLeft(this)(_  applyEvent  _) //what does this refer to in this context?

  def applyEvent: PartialFunction[E,T]

  /*
    ->By definition, markCommitted returns a copy of type T.
    ->The way it will be used in this project is that:
      :: It will be overridden.
      :: It will be used in case classes.
      :: Since it used in case classes, the copy method of the case class will be called.
      :: copy will create a new instance of the case class, reflecting changes made according
         to the parameters passed to it.
   */

  def markCommitted: T

}
