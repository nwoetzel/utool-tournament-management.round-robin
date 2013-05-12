package utool.plugin.roundrobin.test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import utool.plugin.Player;
import utool.plugin.activity.AbstractPluginCommonActivity;
import utool.plugin.activity.TournamentContainer;
import utool.plugin.roundrobin.MatchResult;
import utool.plugin.roundrobin.R;
import utool.plugin.roundrobin.RoundRobinConfiguration;
import utool.plugin.roundrobin.RoundRobinPlayer;
import utool.plugin.roundrobin.RoundRobinTournament;
import utool.plugin.roundrobin.TournamentActivity;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.test.ActivityUnitTestCase;
import android.widget.Button;
import android.widget.TextView;

/**
 * Tests the tournament activity of Swiss
 * @author waltzm
 * @version 2/2/2013
 */
public class TestTournamentActivity extends ActivityUnitTestCase<TournamentActivity>{

	/**
	 * holds the tournament id
	 */
	private long tournamentId = 1246;

	/**
	 * Required constructor for Activity Tests
	 * @since 10/11/2012
	 */
	public TestTournamentActivity() {
		super(TournamentActivity.class);
	}

	/**
	 * The activity under test
	 */
	private TournamentActivity mActivity;

	@Override
	protected void setUp() throws Exception{
		super.setUp();

		//clear application data
		TournamentContainer.clearInstance(tournamentId);
		AndroidTestHelperMethods.clearApplicationData(getInstrumentation().getTargetContext());

		Intent i = new Intent(getInstrumentation().getTargetContext(), TournamentActivity.class);
		i.setClassName(AbstractPluginCommonActivity.UTOOL_TOURNAMENT_CONFIG_PACKAGE, AbstractPluginCommonActivity.UTOOL_TOURNAMENT_CONFIG_CLASS);
		i.putExtra(AbstractPluginCommonActivity.UTOOL_TOURNAMENT_ID_EXTRA_NAME, tournamentId);
		i.putExtra("tournamentName", "Tournament 1");
		i.putExtra("permissionLevel", Player.HOST);
		Player local = new Player("Profile");
		Player p1 = new Player("Bob");
		p1.setPermissionsLevel(Player.PARTICIPANT);
		Player p2 = new Player("Tim");
		//make a list of players
		ArrayList<Player> players = new ArrayList<Player>();
		players.add(p2);
		players.add(p1);
		players.add(local);
		i.putExtra("playerList", players);
		i.putExtra("pid", new UUID(2,1).toString());

		mActivity = startActivity(i, (Bundle)null, (Object)null);
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
		assertTrue(mActivity!=null);

		RoundRobinTournament t = (RoundRobinTournament) TournamentContainer.getInstance(tournamentId);

		//make sure tournament has been created
		assertNotNull(t);
		assertEquals(t.getPlayers().size(),3);

		//make sure GUI is correct
		assertEquals("Tournament 1",((Button)mActivity.findViewById(R.id.tournament_name)).getText().toString());
		assertEquals("Round 1",((TextView)mActivity.findViewById(R.id.tournament_round)).getText().toString());

		//tournament should have 1 round
		assertEquals(t.getRounds().size(),1);
		//check on pager: should have one round
		ViewPager mPager = (ViewPager) mActivity.findViewById(R.id.pager);
		PagerAdapter ad = mPager.getAdapter();
		assertEquals(ad.getCount(),1);
	}

	/**
	 * Tests that setting scores updates the GUI
	 */
	@TargetApi(15)
	public void testSettingScores()
	{		
		assertTrue(mActivity!=null);

		RoundRobinTournament t = (RoundRobinTournament) TournamentContainer.getInstance(tournamentId);

		//make sure tournament has been created
		assertNotNull(t);
		assertEquals(t.getPlayers().size(),3);

		t.getRounds().get(0).getMatches().get(0).setScores(5, 3, MatchResult.PLAYER_ONE);
		t.getRounds().get(0).getMatches().get(1).setScores(3, 6, MatchResult.PLAYER_TWO);
		//check on pager: should have updated scores
		ViewPager mPager = (ViewPager) mActivity.findViewById(R.id.pager);
		PagerAdapter ad = mPager.getAdapter();
		assertEquals(ad.getCount(),1);

		//hit next
		mActivity.findViewById(R.id.right_arrow).callOnClick();
		//should now have two pages
		assertEquals(ad.getCount(),2);

	}

	/**
	 * Tests that setting scores updates the GUI
	 */
	public void testUpdatePlayerList() 
	{		
		assertTrue(mActivity!=null);

		RoundRobinTournament t = (RoundRobinTournament) TournamentContainer.getInstance(tournamentId);

		//make sure tournament has been created
		assertNotNull(t);
		assertEquals(t.getPlayers().size(),3);	
	}

	/**
	 * Tests the setup of the round timer
	 */
	public void testRoundTimer()
	{
		RoundRobinConfiguration config = ((RoundRobinTournament) TournamentContainer.getInstance(tournamentId)).getRoundRobinConfiguration();
		TextView tv = (TextView)mActivity.findViewById(R.id.roundTimer);


		//Test that initially turned off and null
		assertEquals(mActivity.getTimer(),null);
		assertEquals(config.getSecondsRemaining(),-1);
		assertEquals(tv.getText(),"");

		//check the start of the timer
		mActivity.startTimer(1, 5);
		assertEquals(config.getSecondsRemaining(),1*60*60+5*60);
		assertTrue(mActivity.getTimer()!=null);

		mActivity.updateTimer();

		//check that the view is visible
		assertEquals(tv.getText(),"1:05:00");

	}

	/**
	 * tests the adding and removing of players
	 */
	public void testRoundFragment()
	{
		RoundRobinTournament t = (RoundRobinTournament) TournamentContainer.getInstance(tournamentId);
		List<RoundRobinPlayer> newPlayers  = t.getRoundRobinPlayers();

		Intent data = new Intent();
		data.putExtra("playerList", newPlayers.toArray());

		//fake a return with same list of players
		mActivity.onActivityResult(TournamentActivity.TOURNAMENT_CONFIGURATION_ACTIVITY_REQUEST_CODE, Activity.RESULT_OK, data);
		assertEquals(newPlayers.size(), t.getRoundRobinPlayers().size());

		//make sure list is same
		for(int i=0;i<newPlayers.size();i++)
		{
			assertEquals(newPlayers.get(i), t.getRoundRobinPlayers().get(i));
		}

		//fake a return with player added
		newPlayers.add( new RoundRobinPlayer(new Player("bob")));
		data.putExtra("playerList", newPlayers.toArray());
		mActivity.onActivityResult(TournamentActivity.TOURNAMENT_CONFIGURATION_ACTIVITY_REQUEST_CODE, Activity.RESULT_OK, data);
		assertEquals(newPlayers.size(), t.getRoundRobinPlayers().size());

		//make sure list is same
		for(int i=0;i<newPlayers.size();i++)
		{
			assertEquals(newPlayers.get(i), t.getRoundRobinPlayers().get(i));
		}
		
		//fake a return with a player removed
		newPlayers.remove(newPlayers.size()-1);
		data.putExtra("playerList", newPlayers.toArray());
		mActivity.onActivityResult(TournamentActivity.TOURNAMENT_CONFIGURATION_ACTIVITY_REQUEST_CODE, Activity.RESULT_OK, data);
		assertEquals(newPlayers.size(), t.getRoundRobinPlayers().size());

		//make sure list is same
		for(int i=0;i<newPlayers.size();i++)
		{
			assertEquals(newPlayers.get(i), t.getRoundRobinPlayers().get(i));
		}

	}
}