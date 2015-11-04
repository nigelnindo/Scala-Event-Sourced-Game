package dicegame.domain

import java.util.UUID

/**
 * Created by nigelnindo on 11/4/15.
 */

object GameId {
  def createRandom = GameId(UUID.randomUUID().toString)
}

case class GameId(override val value: String) extends AnyVal with Id[Game]
