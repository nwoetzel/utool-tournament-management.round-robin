package utool.plugin.roundrobin.test;

import java.util.ArrayList;
import java.util.UUID;

import utool.plugin.Player;
import utool.plugin.roundrobin.Match;
import utool.plugin.roundrobin.Round;
import utool.plugin.roundrobin.RoundRobinPlayer;
import utool.plugin.roundrobin.mocks.MockRoundRobinTournament;
import utool.plugin.roundrobin.tieresolver.HeadToHeadTieResolver;
import android.test.AndroidTestCase;

/**
 * This class is responsible for verifying the functionality of the MatchResultTieResolver
 * @author Justin Kreier
 * @version 2/3/2013
 */
public class TestHeadToHeadTieResolver extends AndroidTestCase {
	
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
		
		HeadToHeadTieResolver resolver = new HeadToHeadTieResolver();
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
	

	/**
	 * Verifies the resolve tie functionality
	 */
	public void testResolveTieThreePeople(){
		RoundRobinPlayer p1 = new RoundRobinPlayer(new Player("Test Player 1"));
		RoundRobinPlayer p2 = new RoundRobinPlayer(new Player("Test Player 2"));
		RoundRobinPlayer p3 = new RoundRobinPlayer(new Player("Test Player 3"));
		ArrayList<Player> players = new ArrayList<Player>();
		players.add(p1);
		players.add(p2);
		players.add(p3);
		MockRoundRobinTournament t = new MockRoundRobinTournament(-1, players, "test name", UUID.randomUUID(), getContext());
		Round r = new Round(0, t);
		
//		//test when none have played one another
		Match m1;
		Match m2;
//		Match m3 = new Match(p3, RoundRobinPlayer.BYE, r);
//		m1.setScores(2, 0);
//		m2.setScores(2, 0);
//		m3.setScores(2, 0);
		HeadToHeadTieResolver resolver = new HeadToHeadTieResolver();
//		//all tied
//		assertNull(resolver.resolveTie(p1, p2));
//		assertNull(resolver.resolveTie(p2, p3));
//		assertNull(resolver.resolveTie(p3, p1));
		
		//test when player one won one
		r = new Round(0, t);
		m1 = new Match(p1, p2, r);
		m2 = new Match(p3, RoundRobinPlayer.BYE, r);
		m1.setScores(2, 0);
		m2.setScores(2, 0);
		assertEquals(p1, resolver.resolveTie(p1, p2));
		assertEquals(p1, resolver.resolveTie(p2, p1));
		
		//scores diff so can't tie break
//		assertEquals(p3, resolver.resolveTie(p3, p2));
//		assertEquals(p3, resolver.resolveTie(p2, p3));
		assertEquals(null, resolver.resolveTie(p1, p3));
		assertEquals(null, resolver.resolveTie(p3, p1));
		//test when p has beat one another
		r = new Round(1, t);
		m1 = new Match(p1, p3, r);
		m2 = new Match(p2, RoundRobinPlayer.BYE, r);
		m1.setScores(0, 2);
		m2.setScores(2, 0);
		assertEquals(p1, resolver.resolveTie(p1, p2));
		assertEquals(p3, resolver.resolveTie(p3, p2));
		assertEquals(p3, resolver.resolveTie(p1, p3));
		
		//test all beat each other
		r = new Round(2, t);
		m1 = new Match(p2, p3, r);
		m2 = new Match(p1, RoundRobinPlayer.BYE, r);
		m1.setScores(2, 0);
		m2.setScores(2, 0);
		assertEquals(null, resolver.resolveTie(p1, p2));
		assertEquals(null, resolver.resolveTie(p3, p2));
		assertEquals(null, resolver.resolveTie(p1, p3));
		
		
	}

}
