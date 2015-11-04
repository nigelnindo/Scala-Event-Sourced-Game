package dicegame.config

import com.typesafe.config.ConfigFactory

/**
 * Created by nigelnindo on 11/4/15.
 */
object Config {

  private val config = ConfigFactory.load()

  object  Api {
    private val apiConfig = config.getConfig("api")

    val bindHost = apiConfig.getString("bind.host")
    val bindPort = apiConfig.getInt("bind.port")

  }

  object Game {
    private val gameConfig = config.getConfig("game")

    val turnTimeOutSeconds = gameConfig.getInt("turnTimeOutSeconds")

  }

  object Events {

    private val eventsConfig = config.getConfig("events")

    val exchangeName = eventsConfig.getString("exchangeName")

  }

}
