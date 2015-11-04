package dicegame.domain

/**
 * Created by nigelnindo on 11/4/15.
 */

package object domain {
  implicit def gameToRight(game:Game) = Right(game)

  implicit def violationToLeft(violation: GameRulesViolation) = Left(violation)
}
