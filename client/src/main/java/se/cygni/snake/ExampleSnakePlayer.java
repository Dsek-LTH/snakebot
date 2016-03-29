package se.cygni.snake;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.cygni.snake.api.event.GameEndedEvent;
import se.cygni.snake.api.event.GameStartingEvent;
import se.cygni.snake.api.event.MapUpdateEvent;
import se.cygni.snake.api.event.SnakeDeadEvent;
import se.cygni.snake.api.exception.InvalidPlayerName;
import se.cygni.snake.api.model.GameMode;
import se.cygni.snake.api.model.GameSettings;
import se.cygni.snake.api.model.SnakeDirection;
import se.cygni.snake.api.response.PlayerRegistered;
import se.cygni.snake.client.AnsiPrinter;
import se.cygni.snake.client.BaseSnakeClient;
import se.cygni.snake.client.MapUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ExampleSnakePlayer extends BaseSnakeClient {

    private static Logger log = LoggerFactory
            .getLogger(ExampleSnakePlayer.class);

    private AnsiPrinter ansiPrinter;

    public static void main(String[] args) {

        Runnable task = () -> {

            ExampleSnakePlayer sp = new ExampleSnakePlayer();
            sp.connect();

            // Keep this process alive as long as the
            // Snake is connected and playing.
            do {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (sp.isPlaying());

            log.info("Shutting down");
        };


        Thread thread = new Thread(task);
        thread.start();
    }

    public ExampleSnakePlayer() {
        ansiPrinter = new AnsiPrinter(true);
    }

    @Override
    public void onMapUpdate(MapUpdateEvent mapUpdateEvent) {
        ansiPrinter.printMap(mapUpdateEvent);

        // MapUtil contains lot's of useful methods for querying the map!
        MapUtil mapUtil = new MapUtil(mapUpdateEvent.getMap(), getPlayerId());


        List<SnakeDirection> directions = new ArrayList<>();

        // Let's see in which directions I can move
        if (mapUtil.canIMoveInDirection(SnakeDirection.LEFT))
            directions.add(SnakeDirection.LEFT);
        if (mapUtil.canIMoveInDirection(SnakeDirection.RIGHT))
            directions.add(SnakeDirection.RIGHT);
        if (mapUtil.canIMoveInDirection(SnakeDirection.UP))
            directions.add(SnakeDirection.UP);
        if (mapUtil.canIMoveInDirection(SnakeDirection.DOWN))
            directions.add(SnakeDirection.DOWN);

        Random r = new Random();
        SnakeDirection chosenDirection = SnakeDirection.DOWN;

        // Choose a random direction
        if (!directions.isEmpty())
            chosenDirection = directions.get(r.nextInt(directions.size()));

        // Register action here!
        registerMove(mapUpdateEvent.getGameTick(), chosenDirection);
    }



    @Override
    public void onInvalidPlayerName(InvalidPlayerName invalidPlayerName) {

    }

    @Override
    public void onSnakeDead(SnakeDeadEvent snakeDeadEvent) {
        log.info("A snake {} died by {}",
                snakeDeadEvent.getPlayerId(),
                snakeDeadEvent.getDeathReason() + " at tick: " + snakeDeadEvent.getGameTick());
    }

    @Override
    public void onGameEnded(GameEndedEvent gameEndedEvent) {
        log.info("GameEnded, winner: {}", gameEndedEvent.getPlayerWinnerId());
    }

    @Override
    public void onGameStarting(GameStartingEvent gameStartingEvent) {
        log.debug("GameStartingEvent: " + gameStartingEvent);
    }

    @Override
    public void onPlayerRegistered(PlayerRegistered playerRegistered) {
        log.info("PlayerRegistered: " + playerRegistered);

        // Disable this if you want to start the game manually from
        // the web GUI
//         startGame();
    }

    @Override
    public void onSessionClosed() {
        log.info("Session closed");
    }

    @Override
    public void onConnected() {
        log.info("Connected, registering for training...");
        GameSettings gameSettings = new GameSettings.GameSettingsBuilder()
                .withWidth(75)
                .withHeight(75)
                .withMaxNoofPlayers(5)
                .build();

        registerForGame(gameSettings);
    }

    @Override
    public String getName() {
        return "#emil";
    }

    /**
     * Note, color is currently not used.
     *
     * @return
     */
    @Override
    public String getColor() {
        return "black";
    }

    @Override
    public String getServerHost() {
        return "snake.cygni.se";
    }

    @Override
    public int getServerPort() {
        return 80;
    }

    @Override
    public GameMode getGameMode() {
        return GameMode.training;
    }
}
