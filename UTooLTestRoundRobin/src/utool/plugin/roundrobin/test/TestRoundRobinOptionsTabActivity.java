package utool.plugin.roundrobin.test;

import java.util.ArrayList;
import utool.plugin.Player;
import utool.plugin.activity.TournamentContainer;
import utool.plugin.roundrobin.MatchResult;
import utool.plugin.roundrobin.RoundRobinOptionsTabActivity;
import utool.plugin.roundrobin.RoundRobinTournament;
import utool.plugin.roundrobin.TournamentActivity;
import android.test.ActivityUnitTestCase;

/**
 * This test class is meant to fully test the functionality of the Round Robin Tab Activity
 * @author kreierj
 * @version 4/20/13
 */
public class TestRoundRobinOptionsTabActivity extends ActivityUnitTestCase<RoundRobinOptionsTabActivity>{

	/**
	 * holds the tournament id
	 */
	private long tournamentId = 10;

	/**
	 * Required constructor for Activity Tests
	 * @since 10/11/2012
	 */
	public TestRoundRobinOptionsTabActivity() {
		super(RoundRobinOptionsTabActivity.class);
	}

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();

		//clear application data
		AndroidTestHelperMethods.clearApplicationData(getInstrumentation().getTargetContext());


		Player local = new Player("Profile");
		Player p1 = new Player("Bob");
		p1.setPermissionsLevel(Player.PARTICIPANT);
		Player p2 = new Player("Tim");
		//make a list of players
		ArrayList<Player> players = new ArrayList<Player>();
		players.add(p2);
		players.add(p1);
		players.add(local);

		//start the activity
		TournamentContainer.putInstance(new RoundRobinTournament(tournamentId, players, "t1", local.getUUID(), new TournamentActivity(),this.getInstrumentation().getTargetContext()));

	}
	/**
	 * Tests the getTournamentData
	 */
	public void testGetTournamentData() 
	{		
		//create tournament
		Player local = new Player("Profile");
		Player p1 = new Player("Bob");
		Player p2 = new Player("Tim");
		ArrayList<Player> players = new ArrayList<Player>();
		players.add(p2);
		players.add(p1);
		players.add(local);
		TournamentContainer.putInstance(new RoundRobinTournament(tournamentId, players, "t1", local.getUUID(), new TournamentActivity(),this.getInstrumentation().getTargetContext()));

		//test data
		RoundRobinTournament t = (RoundRobinTournament) TournamentContainer.getInstance(tournamentId);
		assertEquals("",RoundRobinOptionsTabActivity.getTournamentData(t));
		
		t.generateNextRound();
		assertEquals("<h2>Round 1: </h2>"+t.getRounds().get(0).getMatches().get(0).getPlayerOne().getName()+" vs. "+t.getRounds().get(0).getMatches().get(0).getPlayerTwo().getName()+"<br>"+t.getRounds().get(0).getMatches().get(1).getPlayerOne().getName()+" vs. "+t.getRounds().get(0).getMatches().get(1).getPlayerTwo().getName()+"<br>",RoundRobinOptionsTabActivity.getTournamentData(t));
		
		//set some scores
		t.getRounds().get(0).getMatches().get(0).setScores(1, 2,MatchResult.PLAYER_TWO);
		t.getRounds().get(0).getMatches().get(1).setScores(4, 3,MatchResult.PLAYER_ONE);
		
		assertEquals("<h2>Round 1: </h2>"+t.getRounds().get(0).getMatches().get(0).getPlayerOne().getName()+" vs. <b>"+t.getRounds().get(0).getMatches().get(0).getPlayerTwo().getName()+"</b><br><b>"+t.getRounds().get(0).getMatches().get(1).getPlayerOne().getName()+"</b> vs. "+t.getRounds().get(0).getMatches().get(1).getPlayerTwo().getName()+"<br>",RoundRobinOptionsTabActivity.getTournamentData(t));

	}

}