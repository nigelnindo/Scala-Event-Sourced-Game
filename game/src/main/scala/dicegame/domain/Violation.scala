package dicegame.domain

/**
 * Created by nigelnindo on 11/4/15.
 */
sealed trait GameRulesViolation

case object NotEnoughPlayersViolation extends GameRulesViolation
case object NotCurrentPlayerViolation extends GameRulesViolation
case object GameAlreadyStartedViolation extends GameRulesViolation
case object GameNotRunningViolation extends GameRulesViolation

