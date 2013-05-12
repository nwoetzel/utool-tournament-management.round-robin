package utool.plugin.roundrobin.test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import utool.plugin.Player;
import utool.plugin.roundrobin.mocks.MockRoundRobinTournament;
import utool.plugin.roundrobin.roundgenerator.BergerRoundGenerator;
import utool.plugin.roundrobin.Match;
import utool.plugin.roundrobin.Round;
import utool.plugin.roundrobin.RoundRobinPlayer;
import android.test.AndroidTestCase;

/**
 * Test for the berger round generator
 * @author Maria Waltz
 * @version 2/2/2013
 */
public class TestBergerRoundGenerator extends AndroidTestCase {

	/**
	 * Tests the round generation for the standard round generator at round one
	 */
	public void testGenerateRoundOne(){	

		ArrayList<Player> players = new ArrayList<Player>();
		for (int i = 0; i < 16; i++){
			players.add(new Player("Player "+i));
		}
		MockRoundRobinTournament t = new MockRoundRobinTournament(-1, players, "test name", UUID.randomUUID(), getContext());

		BergerRoundGenerator g = new BergerRoundGenerator();
		Round r = g.generateRound(t.getRoundRobinPlayers(), t);

		List<Match> matches = r.getMatches();

		//check that it orders as expected with no scores set
		for (int i = 0; i < matches.size(); i++){
			Match m = matches.get(i);

			assertEquals(players.get(i), m.getPlayerOne());
			assertEquals(players.get(players.size()-1-i), m.getPlayerTwo());
		}
	}

	/**
	 * Tests that the graph created is correct
	 */
	public void testGraph()
	{
		ArrayList<Player> players = new ArrayList<Player>();
		for (int i = 0; i < 4; i++){
			players.add(new Player("Player "+i));
		}
		MockRoundRobinTournament t = new MockRoundRobinTournament(-1, players, "test name", UUID.randomUUID(), getContext());

		BergerRoundGenerator g = new BergerRoundGenerator();
		Round round = g.generateRound(t.getRoundRobinPlayers(), t);

		List<Match> matches = round.getMatches();

		//check that it orders as expected with no scores set
		for (int i = 0; i < matches.size(); i++){
			Match m = matches.get(i);

			assertEquals(players.get(i), m.getPlayerOne());
			assertEquals(players.get(players.size()-1-i), m.getPlayerTwo());
		}

		ArrayList<ArrayList<UUID>> graph = t.getGraph();
		ArrayList<UUID> temp = new ArrayList<UUID>();
		temp.add(players.get(0).getUUID());
		temp.add(players.get(1).getUUID());
		temp.add(players.get(2).getUUID());
		temp.add(players.get(3).getUUID());

		//go through graph and verify correctness
		assertEquals(graph.get(0), temp);

		//make sure only 1 round generate
		assertEquals(graph.size(),1);
	}

	/**
	 * Tests that the graph created is correct
	 */
	public void testGraphWith8()
	{
		ArrayList<Player> players = new ArrayList<Player>();
		for (int i = 0; i < 8; i++){
			players.add(new Player("Player "+i));
		}
		MockRoundRobinTournament t = new MockRoundRobinTournament(-1, players, "test name", UUID.randomUUID(), getContext());

		BergerRoundGenerator g = new BergerRoundGenerator();
		Round round = g.generateRound(t.getRoundRobinPlayers(), t);

		List<Match> matches = round.getMatches();

		//check that it orders as expected with no scores set
		for (int i = 0; i < matches.size(); i++){
			Match m = matches.get(i);

			assertEquals(players.get(i), m.getPlayerOne());
			assertEquals(players.get(players.size()-1-i), m.getPlayerTwo());
		}

		ArrayList<ArrayList<UUID>> graph = t.getGraph();
		ArrayList<UUID> temp = new ArrayList<UUID>();
		temp.add(players.get(0).getUUID());
		temp.add(players.get(1).getUUID());
		temp.add(players.get(2).getUUID());
		temp.add(players.get(3).getUUID());
		temp.add(players.get(4).getUUID());
		temp.add(players.get(5).getUUID());
		temp.add(players.get(6).getUUID());
		temp.add(players.get(7).getUUID());

		//verify correctness
		assertEquals(graph.get(0), temp);

	}

	/**
	 * Tests adding a player for both cases
	 * 1. There is a BYE to replace
	 * 2. A new matchup needs to be added
	 */
	public void testAddPlayer()
	{
		
		//CASE 1:there is a BYE and this player can be put there
		ArrayList<Player> players = new ArrayList<Player>();
		for (int i = 0; i < 7; i++){
			players.add(new Player("Player "+i));
		}
		MockRoundRobinTournament t = new MockRoundRobinTournament(-1, players, "test name", UUID.randomUUID(), getContext());

		BergerRoundGenerator g = new BergerRoundGenerator();
		Round round = g.generateRound(t.getRoundRobinPlayers(), t);
		t.getRounds().add(round);
		ArrayList<ArrayList<UUID>> graph = t.getGraph();
		ArrayList<UUID> temp = new ArrayList<UUID>();
		temp.add(players.get(0).getUUID());
		temp.add(players.get(1).getUUID());
		temp.add(players.get(2).getUUID());
		temp.add(players.get(3).getUUID());
		temp.add(players.get(4).getUUID());
		temp.add(players.get(5).getUUID());
		temp.add(Player.BYE);
		temp.add(players.get(6).getUUID());

		//verify correctness
		assertEquals(graph.get(0), temp);
		
		
		//add player -> should fill bye
		players.add(new RoundRobinPlayer(new Player("A")));
		t.setPlayers(players);
		g.addPlayer((RoundRobinPlayer) players.get(7), t);
		
		temp.clear();
		temp.add(players.get(0).getUUID());
		temp.add(players.get(1).getUUID());
		temp.add(players.get(2).getUUID());
		temp.add(players.get(3).getUUID());
		temp.add(players.get(4).getUUID());
		temp.add(players.get(5).getUUID());
		temp.add(players.get(7).getUUID());
		temp.add(players.get(6).getUUID());

		//verify correctness
		assertEquals(graph.get(0), temp);
		
		//Case 2: no BYEs, and a new match must be added
		players.add(new RoundRobinPlayer(new Player("AB")));
		t.setPlayers(players);
		g.addPlayer((RoundRobinPlayer) players.get(8), t);
		
		
		temp.clear();
		temp.add(players.get(0).getUUID());
		temp.add(players.get(1).getUUID());
		temp.add(players.get(2).getUUID());
		temp.add(players.get(3).getUUID());
		
		temp.add(players.get(8).getUUID());
		temp.add(Player.BYE);
		
		temp.add(players.get(4).getUUID());
		temp.add(players.get(5).getUUID());
		temp.add(players.get(7).getUUID());
		temp.add(players.get(6).getUUID());

		//verify correctness
		assertEquals(graph.get(0), temp);
		
		
		
	}

	/**
	 * Tests that the graph extends correctly as the rounds progress
	 */
	public void testExtendGraph()
	{
		ArrayList<Player> players = new ArrayList<Player>();
		for (int i = 0; i < 4; i++){
			players.add(new Player(new UUID(0,i),"Player "+i));
		}
		MockRoundRobinTournament t = new MockRoundRobinTournament(-1, players, "test name", UUID.randomUUID(), getContext());

		BergerRoundGenerator g = new BergerRoundGenerator();
		Round round = g.generateRound(t.getRoundRobinPlayers(), t);

		List<Match> matches = round.getMatches();

		//check that it orders as expected with no scores set
		for (int i = 0; i < matches.size(); i++){
			Match m = matches.get(i);

			assertEquals(players.get(i), m.getPlayerOne());
			assertEquals(players.get(players.size()-1-i), m.getPlayerTwo());
		}

		ArrayList<ArrayList<UUID>> graph = t.getGraph();
		ArrayList<UUID> temp = new ArrayList<UUID>();
		temp.add(players.get(0).getUUID());
		temp.add(players.get(1).getUUID());
		temp.add(players.get(2).getUUID());
		temp.add(players.get(3).getUUID());

		//go through graph and verify correctness
		assertEquals(graph.get(0), temp);

		//make sure only size of 1
		assertEquals(graph.size(),1);

		//actually gen round
		t.getRounds().add(round);

		//verify state
		assertEquals(t.getRounds().size(),1);

		//generate next round (2)
		t.generateNextRound();
		assertEquals(t.getRounds().size(),2);


		temp.clear();
		temp.add(players.get(1).getUUID());
		temp.add(players.get(2).getUUID());
		temp.add(players.get(0).getUUID());
		temp.add(players.get(3).getUUID());

		//make sure only size of 2
		assertEquals(graph.size(),2);


		graph = t.getGraph();
		assertEquals(graph.get(1), temp);

		//generate next round
		t.generateNextRound();
		assertEquals(t.getRounds().size(),3);

		temp.clear();
		temp.add(players.get(2).getUUID());
		temp.add(players.get(0).getUUID());
		temp.add(players.get(1).getUUID());
		temp.add(players.get(3).getUUID());

		//make sure only size of 3
		assertEquals(graph.size(),3);
		assertEquals(graph.get(2), temp);
	}


}
