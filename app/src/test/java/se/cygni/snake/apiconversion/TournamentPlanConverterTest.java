package se.cygni.snake.apiconversion;

import org.junit.Test;
import se.cygni.snake.eventapi.ApiMessageParser;
import se.cygni.snake.eventapi.model.TournamentGamePlan;
import se.cygni.snake.game.GameFeatures;
import se.cygni.snake.game.TournamentPlanTest;
import se.cygni.snake.player.IPlayer;
import se.cygni.snake.tournament.TournamentPlan;

import java.util.Set;

public class TournamentPlanConverterTest {

    @Test
    public void testGetTournamentPlan() throws Exception {
        GameFeatures gf = new GameFeatures();
        gf.setHeight(25);
        gf.setWidth(25);

        Set<IPlayer> players = TournamentPlanTest.getPlayers(20);

        TournamentPlan tp = new TournamentPlan(gf, players);


        TournamentGamePlan tgp = TournamentPlanConverter.getTournamentPlan(tp, "test-tournament", "1234");
        System.out.println(ApiMessageParser.encodeMessage(tgp));
    }
}