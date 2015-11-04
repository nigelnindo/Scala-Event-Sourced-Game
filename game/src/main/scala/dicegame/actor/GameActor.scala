package dicegame.actor

import akka.actor.{Cancellable, ActorLogging, Props}
import akka.persistence.{RecoveryCompleted, PersistentActor}
import dicegame.domain._
import scala.concurrent.duration._

/**
 * Created by nigelnindo on 11/4/15.
 */

object GameActor {
  def props(id: GameId) = Props(new GameActor(id))

  sealed trait CommandResult
  case object CommandAccepted extends CommandResult
  case class CommandRejected(violation: GameRulesViolation) extends CommandResult

  private case object TickCountDown

}

class GameActor(id: GameId) extends PersistentActor with ActorLogging{

  import GameActor._

  import context.{dispatcher,system}

  override val persistenceId = id.value

  var game: Game = Game.create(id)

  var tickCancellable: Option[Cancellable] = None //find out what akka cancellable is

  override def receiveCommand = {
    case command: GameCommand => handleResult(game.handleCommand(command))
    case TickCountDown => game match {
      case rg: RunningGame => handleChanges(rg.tickCountDown())
      case _ =>
        log.warning("Game is not running, cannot update countdown")
        cancelCountDownTick()
    }
  }

  def handleResult(result: Either[GameRulesViolation,Game]) = result match {
    case Right(updatedGame) =>
      sender() ! CommandAccepted
      handleChanges(updatedGame)
    case Left(violation) =>
      sender() ! CommandRejected(violation)
  }

  def handleChanges(updatedGame: Game) = updatedGame.uncommittedEvents.foreach{
    persist(_) { ev =>
      game = game.applyEvent(ev).markCommitted
      publishEvent(ev)
      ev match {
        case _: GameStarted =>
          scheduleCountDownTick()
        case _: TurnChanged =>
          cancelCountDownTick()
          scheduleCountDownTick()
        case _: GameFinished =>
          cancelCountDownTick()
          context stop self
        case _ =>
      }
    }
  }

  def publishEvent(event: GameEvent) = {
    system.eventStream.publish(event)
  }

  def scheduleCountDownTick() = {
    val cancellable = system.scheduler.schedule(1.second,1.seconds,self,TickCountDown)
    tickCancellable = Some(cancellable)
  }

  def cancelCountDownTick() = {
    tickCancellable.foreach(_.cancel())
    tickCancellable = None
  }

  override def receiveRecover = {
    case ev: GameEvent =>
      game = game.applyEvent(ev)
    case RecoveryCompleted =>
      if (game.isRunning) scheduleCountDownTick()
  }

}
