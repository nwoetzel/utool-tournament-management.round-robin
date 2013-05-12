package utool.plugin.roundrobin.test;

import java.util.ArrayList;
import utool.plugin.Player;
import utool.plugin.activity.AbstractPluginCommonActivity;
import utool.plugin.activity.TournamentContainer;
import utool.plugin.roundrobin.OptionsEmailTab;
import utool.plugin.roundrobin.OptionsPlayerTab;
import utool.plugin.roundrobin.R;
import utool.plugin.roundrobin.RoundRobinTournament;
import utool.plugin.roundrobin.TournamentActivity;
import android.content.Intent;
import android.os.Bundle;
import android.test.ActivityUnitTestCase;
import android.widget.ListView;

/**
 * This test class is meant to fully test the functionality of the Round Robin Option Player Tab
 * @author kreierj
 * @version 4/20/13
 */
public class TestRoundRobinOptionsPlayer extends ActivityUnitTestCase<OptionsPlayerTab>{

	/**
	 * holds the tournament id
	 */
	private long tournamentId = 10;

	/**
	 * Required constructor for Activity Tests
	 * @since 10/11/2012
	 */
	public TestRoundRobinOptionsPlayer() {
		super(OptionsPlayerTab.class);
	}

	/**
	 * The activity under test
	 */
	private OptionsPlayerTab mActivity;

	@Override
	protected void setUp() throws Exception{
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


	@Override
	protected void tearDown() throws Exception{
		super.tearDown();
		
		//clear application data
		AndroidTestHelperMethods.clearApplicationData(getInstrumentation().getTargetContext());
	}

	/**
	 * Tests that the various components are initialized properly
	 */
	public void testInitialization() 
	{		
		assertTrue(TournamentContainer.getInstance(tournamentId)!=null);
		assertTrue(((RoundRobinTournament)TournamentContainer.getInstance(tournamentId)).getAutomaticMessageHandler()!=null);
		Intent i = new Intent(getInstrumentation().getTargetContext(), OptionsEmailTab.class);
		i.setClassName(AbstractPluginCommonActivity.UTOOL_TOURNAMENT_CONFIG_PACKAGE, AbstractPluginCommonActivity.UTOOL_TOURNAMENT_CONFIG_CLASS);
		i.putExtra(AbstractPluginCommonActivity.UTOOL_TOURNAMENT_ID_EXTRA_NAME, tournamentId);
		mActivity = startActivity(i, (Bundle)null, (Object)null);

		assertTrue(mActivity!=null);

		ListView l = (ListView)mActivity.findViewById(R.id.option_list);
		assertNotNull(l);
		
		ArrayList<Player> players = TournamentContainer.getInstance(tournamentId).getPlayers();
		int count=0;
		for(int r=0;r<players.size();r++)
		{
			if(players.get(r).getPermissionsLevel()==Player.PARTICIPANT)
			{
				count++;
			}
		}
		//one players should be in list
		assertEquals(count,l.getAdapter().getCount());
	}

}