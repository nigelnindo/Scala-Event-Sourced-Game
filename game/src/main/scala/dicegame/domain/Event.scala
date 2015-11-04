package dicegame.domain

/**
 * Created by nigelnindo on 11/4/15.
 */
sealed trait GameEvent {
  def id: GameId
}

case class GameStarted(override val id: GameId, players: Seq[PlayerId], initialTurn: Turn) extends GameEvent

case class TurnChanged(override val id: GameId, turn: Turn) extends GameEvent

case class TurnCountDownUpdated(override val id: GameId, secondsLeft: Int) extends GameEvent

case class TurnTimedOut(override val id: GameId) extends GameEvent

case class DiceRolled(override val id: GameId, rolledNumber: Int) extends GameEvent

case class GameFinished(override val id: GameId, winners: Set[PlayerId]) extends GameEvent

