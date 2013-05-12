package utool.plugin.roundrobin.test;

import java.util.ArrayList;
import java.util.UUID;

import utool.plugin.Player;
import utool.plugin.roundrobin.Match;
import utool.plugin.roundrobin.Round;
import utool.plugin.roundrobin.RoundRobinPlayer;
import utool.plugin.roundrobin.mocks.MockRoundRobinTournament;
import utool.plugin.roundrobin.tieresolver.RunDifferentialTieResolver;
import android.test.AndroidTestCase;

/**
 * This class is responsible for verifying the functionality of the MatchResultTieResolver
 * @author Justin Kreier
 * @version 2/3/2013
 */
public class TestRunDifferentialTieResolver extends AndroidTestCase {
	
	/**
	 * Verifies the resolve tie functionality
	 */
	public void testResolveTieTwoPeople(){
		RoundRobinPlayer p1 = new RoundRobinPlayer(new Player("Test Player 1"));
		RoundRobinPlayer p2 = new RoundRobinPlayer(new Player("Test Player 2"));
		ArrayList<Player> players = new ArrayList<Player>();
		players.add(p1);
		players.add(p2);
		MockRoundRobinTournament t = new MockRoundRobinTournament(-1, players, "test name", UUID.randomUUID(), getContext());
		Round r = new Round(0, t);
		
		//test when neither have played one another
		Match m1 = new Match(p1,RoundRobinPlayer.BYE, r);
		Match m2 = new Match(p2, RoundRobinPlayer.BYE, r);
		
		m1.setScores(2, 0);
		m2.setScores(2, 0);
		
		RunDifferentialTieResolver resolver = new RunDifferentialTieResolver();
		assertNull(resolver.resolveTie(p1, p2));
		
		
		//test when player one won one
		r = new Round(1, t);
		m1 = new Match(p1, p2, r);
		m1.setScores(2, 0);
		
		assertEquals(p1, resolver.resolveTie(p1, p2));
		
		//test when both players have beat one another
		r = new Round(2, t);
		m1 = new Match(p1, p2, r);
		m1.setScores(0, 2);
		
		assertNull(resolver.resolveTie(p1, p2));
		
		
		//test when player two has won two
		r = new Round(3, t);
		m1 = new Match(p1, p2, r);
		m1.setScores(0, 2);
		
		assertEquals(p2, resolver.resolveTie(p1, p2));
	}
	

}
