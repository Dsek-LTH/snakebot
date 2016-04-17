package se.cygni.snake.player.bot;

import com.google.common.eventbus.EventBus;
import se.cygni.snake.api.event.MapUpdateEvent;
import se.cygni.snake.api.model.Map;
import se.cygni.snake.api.model.SnakeDirection;
import se.cygni.snake.api.request.RegisterMove;
import se.cygni.snake.client.MapUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class RandomBot extends BotPlayer {

    private SnakeDirection myLastDirection;

    public RandomBot(String playerId, EventBus incomingEventbus) {
        super(playerId, incomingEventbus);
    }

    @Override
    public void onWorldUpdate(MapUpdateEvent mapUpdateEvent) {
        CompletableFuture cf = CompletableFuture.runAsync(() -> {
            postNextMove(mapUpdateEvent.getGameId(), mapUpdateEvent.getMap(), mapUpdateEvent.getGameTick());
        });
    }

    private void postNextMove(String gameId, Map map, long gameTick) {

        MapUtil mapUtil = new MapUtil(map, playerId);

        SnakeDirection rndDirection = getRandomDirection();
        List<SnakeDirection> validDirections = getValidDirections(mapUtil);
        if (validDirections.size() > 0) {
            rndDirection = getRandomDirection(validDirections);
        }

        RegisterMove registerMove = new RegisterMove(gameId, gameTick, rndDirection);
        registerMove.setReceivingPlayerId(playerId);
        incomingEventbus.post(registerMove);
    }

    private List<SnakeDirection> getValidDirections(MapUtil mapUtil) {

        List<SnakeDirection> validDirections = new ArrayList<>();

        for (SnakeDirection direction : SnakeDirection.values()) {
            if (mapUtil.canIMoveInDirection(direction))
                validDirections.add(direction);
        }

        return validDirections;
    }

    private SnakeDirection getRandomDirection(List<SnakeDirection> directions) {

        Random r = new Random();

        // Let's prefer the last direction if it is available
        if (directions.contains(myLastDirection)) {
            if (r.nextDouble() < 0.5) {
                return myLastDirection;
            }
        }

        int max = directions.size()-1;
        if (max == 0)
            return directions.get(0);

        return directions.get(r.nextInt(max));
    }

    private SnakeDirection getRandomDirection() {
        int max = SnakeDirection.values().length-1;

        Random r = new Random();
        return SnakeDirection.values()[r.nextInt(max)];
    }
}
