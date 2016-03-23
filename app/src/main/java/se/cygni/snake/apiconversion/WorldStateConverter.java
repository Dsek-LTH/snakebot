package se.cygni.snake.apiconversion;

import se.cygni.game.Coordinate;
import se.cygni.game.Tile;
import se.cygni.game.WorldState;
import se.cygni.game.worldobject.*;
import se.cygni.snake.api.model.*;
import se.cygni.snake.player.IPlayer;

import java.util.Set;

public class WorldStateConverter {

    public static Map convertWorldState(WorldState ws, long worldTick, Set<IPlayer> players) {

        int width = ws.getWidth();
        int height = ws.getHeight();

        TileContent[][] mapTiles = getTileContents(ws);
        SnakeInfo[] snakeInfos = getSnakeInfos(ws, players);

        return new Map(
                width,
                height,
                worldTick,
                mapTiles,
                snakeInfos);
    }

    private static SnakeInfo[] getSnakeInfos(WorldState ws, Set<IPlayer> players) {

        SnakeInfo[] snakeInfos = new SnakeInfo[players.size()];

        int c = 0;
        for (IPlayer player : players) {
            snakeInfos[c++] = getSnakeInfo(ws, player);
        }

        return snakeInfos;
    }

    private static SnakeInfo getSnakeInfo(WorldState ws, IPlayer player) {
        String name = player.getName();
        String id = player.getPlayerId();
        int length = 0;
        int points = player.getTotalPoints();
        boolean alive = player.isAlive();

        try {
            SnakeHead snakeHead = ws.getSnakeHeadById(id);
            return getSnakeInfo(ws, snakeHead);
        } catch (Exception e) {}

        return new SnakeInfo(name, length, points, id, -1, -1, alive);
    }

    private static SnakeInfo getSnakeInfo(WorldState ws, SnakeHead head) {
        String name = head.getName();
        String id = head.getPlayerId();
        int length = head.getLength();
        Coordinate coord = ws.translatePosition(head.getPosition());

        return new SnakeInfo(name, length, head.getPoints(), id, coord.getX(), coord.getY(), true);
    }

    private static TileContent[][] getTileContents(WorldState ws) {
        int width = ws.getWidth();
        int height = ws.getHeight();

        TileContent[][] mapTiles = new TileContent[width][height];

        Tile[] tiles = ws.getTiles();
        int counter = 0;
        for (Tile tile : tiles) {
            Coordinate c = ws.translatePosition(counter);
            int x = c.getX();
            int y = c.getY();

            mapTiles[x][y] = convertWorldObject(tile.getContent());

            counter++;
        }

        // Add snakes
        populateSnakes(ws, mapTiles);

        return mapTiles;
    }

    private static void populateSnakes(WorldState ws, TileContent[][] mapTiles) {
        int[] headPositions = ws.listPositionsWithContentOf(SnakeHead.class);
        for (int position : headPositions) {
            SnakeHead snakeHead = (SnakeHead)ws.getTile(position).getContent();
            Coordinate headCoord = ws.translatePosition(position);
            String name = snakeHead.getName();
            String playerId = snakeHead.getPlayerId();

            MapSnakeHead mapSnakeHead = new MapSnakeHead(name, playerId);
            mapTiles[headCoord.getX()][headCoord.getY()] = mapSnakeHead;

            SnakePart snakePart = snakeHead.getNextSnakePart();
            int counter = 1;
            while (snakePart != null) {
                Coordinate partCoord = ws.translatePosition(snakePart.getPosition());
                boolean tail = snakePart.getNextSnakePart() == null;
                mapTiles[partCoord.getX()][partCoord.getY()] = new MapSnakeBody(tail, playerId, counter++);

                snakePart = snakePart.getNextSnakePart();
            }
        }
    }

    private static TileContent convertWorldObject(WorldObject wo) {

        if (wo instanceof Obstacle) {
            return new MapObstacle();
        }

        if (wo instanceof Food) {
            return new MapFood();
        }

        return new MapEmpty();
    }
}
