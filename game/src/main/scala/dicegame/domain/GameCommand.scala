package dicegame.domain

/**
 * Created by nigelnindo on 11/4/15.
 */
sealed trait GameCommand

case class StartGame(players: Seq[PlayerId]) extends GameCommand

case class RollDice(player: PlayerId) extends GameCommand
