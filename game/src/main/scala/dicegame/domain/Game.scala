package dicegame.domain

import dicegame.config.Config.Game._

import scala.util.Random

/**
 * Created by nigelnindo on 11/4/15.
 */

/*
  ->The object Game makes sure that a Game is created in an uninitialized state.
  ->
 */

object Game {
  /*
    ->create method could be return an instance of Game or UninitializedGame. Find
      out which on is actually returned.
   */
  def create(id:GameId) = UninitializedGame(id)
}

sealed trait Game extends AggregateRoot[Game,GameEvent]{

  def id: GameId

  implicit def gameToRight(game:Game) = Right(game)

  implicit def violationToLeft(violation: GameRulesViolation) = Left(violation)

  /*
    ->handleCommand checks the current state of Game (using the this reference)
      and responds accordingly as shown below.
   */

  def handleCommand(command:GameCommand): Either[GameRulesViolation,Game] = command match {
    case StartGame(players) => this match {
      case ug: UninitializedGame => ug.start(players)
      case _ => GameAlreadyStartedViolation
    }
    case RollDice(player) => this match {
      case rg: RunningGame => rg.roll(player)
      case _ => GameNotRunningViolation
    }
  }

  /*
    ->Methods isFinished and isRunning also check the current state of the Game sealed trait
      and return a boolean value.
   */

  def isFinished = this match {
    case fg: FinishedGame => true
    case _ => false
  }

  def isRunning = this match {
    case rg: RunningGame => true
    case _ => false
  }

}

/*
  ->case class UninitializedGame overrides 'id' and 'uncommittedEvents'
    from the AggregateRoot trait
 */

case class UninitializedGame(override val id: GameId,
                               override val uncommittedEvents: List[GameEvent] = Nil) extends Game{

  /*
    ->To start a game, pass in a sequence of PlayerId's
    ->You need at least two players to start the Game
    ->On successful start of a game, send out the GameStarted event
   */

  def start(players: Seq[PlayerId]): Either[GameRulesViolation,Game] = {
    if (players.size < 2) {
      NotEnoughPlayersViolation
    }
    else {
      val firstPlayer = players.head
      applyEvents(GameStarted(id,players,Turn(firstPlayer,turnTimeOutSeconds)))
    }
  }

  /*
    ->The applyEvent PartialFunction will respond to a GameStarted event
    ->Remember that applyEvent has to return an instance of type T. type T
      in this example is an instance of Game
    ->Returning RunningGame still works because RunningGame extends the InitializedGame
      trait which extends the Game class.
    ->Any subclass (or if you like, children) of class Game can be returned without
      the compiler complaining.
   */

  override def applyEvent = {
    case ev @ GameStarted(_, players, initialTurn) =>
      RunningGame(id,players,initialTurn, uncommittedEvents = uncommittedEvents :+ ev)
  }

  override def markCommitted = copy(uncommittedEvents = Nil)

}

sealed trait InitializedGame extends Game {
  def players: Seq[PlayerId]
}

case class Turn(currentPlayer: PlayerId, secondsLeft: Int)

/*
  ->case class RunningGame overrides 'players' from the Initialized
   trait it extends from.

 */

case class RunningGame(override val id: GameId, override val players: Seq[PlayerId],
                       turn: Turn, rolledNumbers: Map[PlayerId,Int] = Map.empty,
                        override val uncommittedEvents: List[GameEvent] = Nil) extends InitializedGame {

  def roll(player: PlayerId): Either[GameRulesViolation,Game] = {

    if (turn.currentPlayer == player){

      val rolledNumber = randomBetween(1,6)
      val diceRolled = DiceRolled(id,rolledNumber)

      nextPlayerOpt match {
        case Some(nextPlayer) =>
          applyEvents(diceRolled, TurnChanged(id,Turn(nextPlayer,turnTimeOutSeconds)))
        case None =>
          applyEvent(diceRolled) match {
            case rg: RunningGame => rg.applyEvent(GameFinished(id,rg.bestPlayers))
            case other => other
          }
      }
    }
    else{
      NotCurrentPlayerViolation
    }
  }

  def randomBetween(min:Int, max:Int) =  Random.nextInt(max - min + 1) + min

  def bestPlayers: Set[PlayerId] = {
    val highest = highestRolledNumber
    /*
      ->Collect runs a PartialFunction on the rolledNumbers Map.
      ->Only items which meet the predicate function are collected.
      ->In this case the predicate matches the highest number in the Map.
      ->At the end the collected items are converted into a set.
      ->The transformation '=>' shows that only the players who match
        the predicate are added to the set
     */
    rolledNumbers.collect {
      case (player, `highest`) => player
    }.toSet
  }

  def highestRolledNumber: Int = {
    if (rolledNumbers.isEmpty){
      0
    }
    else{
      //rolledNumbers.map(_._2).max
      rolledNumbers.values.max //same as rolledNumbers.map(_._2).max
    }
  }

  def tickCountDown(): Game = {
    val countDownUpdated = TurnCountDownUpdated(id, turn.secondsLeft - 1 )
    if (turn.secondsLeft <= 1){
      val timedOut = TurnTimedOut(id)
      nextPlayerOpt match{
        case Some(nextPlayer) =>
          applyEvents(countDownUpdated,timedOut, TurnChanged(id,Turn(nextPlayer,turnTimeOutSeconds)))
        case None =>
          applyEvents(countDownUpdated,timedOut,GameFinished(id,bestPlayers))
      }
    }else applyEvent(countDownUpdated)
  }

  private def nextPlayerOpt: Option[PlayerId] = {
    val currentPlayerIndex = players.indexOf(turn.currentPlayer)
    val nextPlayerIndex = currentPlayerIndex + 1

    if (players.isDefinedAt(nextPlayerIndex)){
      Some(players(nextPlayerIndex))
    }else{
      None
    }

  }

  override def applyEvent = {
    case ev @ TurnChanged(_, newTurn) =>
      copy(turn = newTurn, uncommittedEvents = uncommittedEvents :+ ev)
    case ev @ DiceRolled(_, rolledNumber) =>
      copy(rolledNumbers = rolledNumbers + (turn.currentPlayer -> rolledNumber)) //review this line
    case ev @ TurnCountDownUpdated(_, secondsLeft) =>
      val updatedTurn = turn.copy(secondsLeft = secondsLeft )
      copy(turn = updatedTurn, uncommittedEvents = uncommittedEvents :+ ev )
    case ev @ GameFinished(_, winners) =>
      FinishedGame(id, players, winners, uncommittedEvents = uncommittedEvents :+ ev)
    case ev: TurnTimedOut =>
      copy(uncommittedEvents = uncommittedEvents :+ ev)
  }

  override def markCommitted = copy(uncommittedEvents = Nil)

}

case class FinishedGame(override val id: GameId, override val players: Seq[PlayerId], winners: Set[PlayerId],
                         override val uncommittedEvents: List[GameEvent] = Nil) extends InitializedGame {

  override def applyEvent = PartialFunction.empty

  override def markCommitted = copy(uncommittedEvents = Nil)

}

