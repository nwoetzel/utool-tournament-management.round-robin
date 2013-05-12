package utool.plugin.roundrobin.test;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import utool.plugin.Player;
import utool.plugin.roundrobin.mocks.MockRoundRobinTournament;
import utool.plugin.roundrobin.Match;
import utool.plugin.roundrobin.Round;
import utool.plugin.roundrobin.RoundRobinPlayer;
import android.test.AndroidTestCase;

/**
 * This class is meant to verify proper functionality of the Swiss Tournament Class
 * @author Justin Kreier
 * @version 1/25/2013
 */
public class TestRoundRobinTournament extends AndroidTestCase{

	/**
	 * Verifies the constructor builds properly
	 */
	public void testConstructor(){

		//sunny day
		UUID testUUID = UUID.randomUUID();
		MockRoundRobinTournament t = new MockRoundRobinTournament(-1, new ArrayList<Player>(), "test name", testUUID, getContext());

		assertEquals(-1, t.getTournamentId());
		assertNotNull(t.getPlayers());
		assertEquals("test name", t.getTournamentName());
		assertEquals(testUUID, t.getPID());

		assertNotNull(t.getRounds());
		assertNotNull(t.getObservable());
		assertNotNull(t.getAutomaticMessageHandler());
		assertNotNull(t.getRoundRobinConfiguration());


		//null values
		try{
			t = new MockRoundRobinTournament(-1, null, "test name", testUUID, getContext());
			fail("Players cannot be null");
		} catch(NullPointerException e) {
			//expected
		}

		try{
			t = new MockRoundRobinTournament(-1, new ArrayList<Player>(), null, testUUID, getContext());
			fail("Tournament Name cannot be null");
		} catch(NullPointerException e) {
			//expected
		}

		try{
			t = new MockRoundRobinTournament(-1, new ArrayList<Player>(), "test name", null, getContext());
			fail("PID cannot be null");
		} catch(NullPointerException e) {
			//expected
		}

		try{
			t = new MockRoundRobinTournament(-1, new ArrayList<Player>(), "test name", testUUID, null);
			fail("Context cannot be null");
		} catch(NullPointerException e) {
			//expected
		}
	}

	/**
	 * Tests that the generate round function properly differentiates between the first
	 * round and other rounds
	 */
	public void testGenerateNextRound(){
		ArrayList<Player> players = new ArrayList<Player>();
		for (int i = 0; i < 16; i++){
			players.add(new Player("Player "+i));
		}
		MockRoundRobinTournament t = new MockRoundRobinTournament(-1, players, "test name", UUID.randomUUID(), getContext());

		//generate the first round (should be randomized)
		Round r = t.generateNextRound();

		assertEquals(8, r.getMatches().size());

		List<RoundRobinPlayer> swissPlayers = t.getRoundRobinPlayers();

		int errors = 0;
		for (int i = 0; i < 8; i++){
			Match m = r.getMatches().get(i);
			if (swissPlayers.get(i).equals(m.getPlayerOne())){
				errors++;
			}
		}

		for (int i = 0; i < 8; i++){
			Match m = r.getMatches().get(i);
			if (swissPlayers.get(i+1).equals(m.getPlayerTwo())){
				errors++;
			}
		}

		if (errors > 12){
			fail("There is a statistically significant chance that the list was not randomized, " +
					"verify that this failure is consistent");
		}

		//generate the second round (should not be randomized)
		r = t.generateNextRound();
		List<Match> matches = r.getMatches();
		assertEquals(8, matches.size());
	}

	/**
	 * Verifies the getSwissPlayers method returns the correct list of players
	 */
	public void testGetSwissPlayers(){
		ArrayList<Player> players = new ArrayList<Player>();
		for (int i = 0; i < 16; i++){
			players.add(new Player("Player "+i));
		}
		MockRoundRobinTournament t = new MockRoundRobinTournament(-1, players, "test name", UUID.randomUUID(), getContext());

		List<RoundRobinPlayer> swissPlayers = t.getRoundRobinPlayers();
		for (int i = 0; i < swissPlayers.size(); i++){
			assertEquals(players.get(i), swissPlayers.get(i));
		}
	}

	/**
	 * Tests that the standings array returns a list of players sorted by their score
	 */
	public void testGetStandingsArray(){
		ArrayList<Player> players = new ArrayList<Player>();
		for (int i = 0; i < 16; i++){
			players.add(new Player("Player "+i));
		}
		MockRoundRobinTournament t = new MockRoundRobinTournament(-1, players, "test name", UUID.randomUUID(), getContext());
		Round r = new Round(0, t);

		//set up the matches so the standings array should be in reverse order
		List<RoundRobinPlayer> swissPlayers = t.getRoundRobinPlayers();
		for (int i = 0; i < swissPlayers.size(); i++){
			RoundRobinPlayer p = swissPlayers.get(i);

			Match m = new Match(p, RoundRobinPlayer.BYE, r);
			m.setScores(2*i, 0);
		}

		List<RoundRobinPlayer> standingArray = t.getStandingsArray();
		assertEquals(16, standingArray.size());
		for (int i = 0; i < standingArray.size(); i++){
			RoundRobinPlayer p = standingArray.get(i);
			RoundRobinPlayer o = swissPlayers.get(15-i);

			assertEquals(o, p);
		}
	}

	/**
	 * Tests that swapping works
	 */
	public void testSwap()
	{
		ArrayList<Player> players = new ArrayList<Player>();
		for (int i = 0; i < 16; i++){
			players.add(new Player("Player "+i));
		}
		MockRoundRobinTournament t = new MockRoundRobinTournament(-1, players, "test name", UUID.randomUUID(), getContext());

		//generate the first round (should be randomized)
		Round r = t.generateNextRound();

		assertEquals(8, r.getMatches().size());

		//make sure it returns false if round<0
		assertEquals(t.switchPlayers(0, true, 0, true, -1), false);


		//swap two player ones
		Player one = r.getMatches().get(0).getPlayerOne();
		Player two = r.getMatches().get(3).getPlayerOne();

		t.switchPlayers(0, true, 3, true, 0);

		assertEquals(r.getMatches().get(0).getPlayerOne(),two);
		assertEquals(r.getMatches().get(3).getPlayerOne(),one);


		//swap player one and player two
		one = r.getMatches().get(1).getPlayerOne();
		two = r.getMatches().get(4).getPlayerTwo();

		t.switchPlayers(1, true, 4, false, 0);

		assertEquals(r.getMatches().get(1).getPlayerOne(),two);
		assertEquals(r.getMatches().get(4).getPlayerTwo(),one);

		//swap player two and player one
		one = r.getMatches().get(4).getPlayerTwo();
		two = r.getMatches().get(5).getPlayerOne();

		t.switchPlayers(4, false, 5, true, 0);

		assertEquals(r.getMatches().get(4).getPlayerTwo(),two);
		assertEquals(r.getMatches().get(5).getPlayerOne(),one);

		//swap two player twos
		one = r.getMatches().get(0).getPlayerTwo();
		two = r.getMatches().get(2).getPlayerTwo();

		t.switchPlayers(0, false, 2, false, 0);

		assertEquals(r.getMatches().get(0).getPlayerTwo(),two);
		assertEquals(r.getMatches().get(2).getPlayerTwo(),one);


	}

	/**
	 * Tests that removal works as intended
	 */
	public void testRemovePlayerFromGraph()
	{
		Player rem = null;
		ArrayList<Player> players = new ArrayList<Player>();
		for (int i = 0; i < 16; i++){
			if(i==0)
			{
				rem=new Player("Player "+i);
				players.add(rem);
			}
			else
			{
				players.add(new Player("Player "+i));
			}
		}
		MockRoundRobinTournament t = new MockRoundRobinTournament(-1, players, "test name", UUID.randomUUID(), getContext());

		//generate the first round (should be randomized)
		Round r = t.generateNextRound();

		assertEquals(8, r.getMatches().size());


		t.removePlayerFromGraph(rem.getUUID());
		ArrayList<ArrayList<UUID>> graph = t.getGraph();
		for(int i=0;i<graph.size();i++)
		{
			//pull out row
			ArrayList<UUID> row = graph.get(i);
			for(int j=0;j<row.size();j++)
			{
				if(row.get(j).equals(rem))
				{
					fail("Player still in graph at row "+i+" and col "+j);
				}
			}
		}


	}
}
