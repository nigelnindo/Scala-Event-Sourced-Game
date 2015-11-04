package dicegame.domain

/**
 * Created by nigelnindo on 11/4/15.
 */

/*
  ->This trait is what makes the override in the GameId case class work
 */

trait Id[T] extends Any{
  def value: String
}
