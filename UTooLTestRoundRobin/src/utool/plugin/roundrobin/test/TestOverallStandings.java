package utool.plugin.roundrobin.test;

import java.util.ArrayList;

import utool.plugin.Player;
import utool.plugin.activity.AbstractPluginCommonActivity;
import utool.plugin.activity.TournamentContainer;
import utool.plugin.roundrobin.MatchResult;
import utool.plugin.roundrobin.OptionsEmailTab;
import utool.plugin.roundrobin.OverallStandingsActivity;
import utool.plugin.roundrobin.R;
import utool.plugin.roundrobin.RoundRobinTournament;
import utool.plugin.roundrobin.TournamentActivity;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Bundle;
import android.test.ActivityUnitTestCase;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Tests the overall standings activity
 * @author waltzm
 * @version 2/2/2013
 */
public class TestOverallStandings extends ActivityUnitTestCase<OverallStandingsActivity>{

	/**
	 * holds the tournament id
	 */
	private long tournamentId = 150;

	/**
	 * Required constructor for Activity Tests
	 * @since 10/11/2012
	 */
	public TestOverallStandings() {
		super(OverallStandingsActivity.class);
	}


	/**
	 * The activity under test
	 */
	private OverallStandingsActivity mActivity;

	@Override
	protected void setUp() throws Exception{
		super.setUp();

		//clear application data
		AndroidTestHelperMethods.clearApplicationData(getInstrumentation().getTargetContext());


		Player local = new Player("Profile");
		Player p1 = new Player("Bob");
		//make a list of players
		ArrayList<Player> players = new ArrayList<Player>();
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
		
		ListView l = (ListView)mActivity.findViewById(R.id.overall_standings_list);
		assertNotNull(l);

		//two players in list
		assertEquals(2,l.getAdapter().getCount());
	}
	
	/**
	 * Tests that the order by column method
	 */
	@TargetApi(15)
	public void testOrderByColumn() 
	{		
		assertTrue(TournamentContainer.getInstance(tournamentId)!=null);
		assertTrue(((RoundRobinTournament)TournamentContainer.getInstance(tournamentId)).getAutomaticMessageHandler()!=null);
		
		//set mscores for match
		RoundRobinTournament t = (RoundRobinTournament) TournamentContainer.getInstance(tournamentId);
		t.generateNextRound();		
		//set some scores
		t.getRounds().get(0).getMatches().get(0).setScores(5, 3,MatchResult.PLAYER_ONE);		
		
		Intent i = new Intent(getInstrumentation().getTargetContext(), OptionsEmailTab.class);
		i.setClassName(AbstractPluginCommonActivity.UTOOL_TOURNAMENT_CONFIG_PACKAGE, AbstractPluginCommonActivity.UTOOL_TOURNAMENT_CONFIG_CLASS);
		i.putExtra(AbstractPluginCommonActivity.UTOOL_TOURNAMENT_ID_EXTRA_NAME, tournamentId);
		mActivity = startActivity(i, (Bundle)null, (Object)null);

		assertTrue(mActivity!=null);
		
		ListView l = (ListView)mActivity.findViewById(R.id.overall_standings_list);
		assertNotNull(l);

		//two players in list
		assertEquals(2,l.getAdapter().getCount());
		
		//test initial order (rank asc)
		assertEquals(t.getRounds().get(0).getMatches().get(0).getPlayerOne().getName(),((TextView)l.getAdapter().getView(0,null,null).findViewById(R.id.name_overall)).getText());
		assertEquals(t.getRounds().get(0).getMatches().get(0).getPlayerTwo().getName(),((TextView)l.getAdapter().getView(1,null,null).findViewById(R.id.name_overall)).getText());

		mActivity.findViewById(R.id.order_by_rank_overall).callOnClick();
		assertEquals(t.getRounds().get(0).getMatches().get(0).getPlayerOne().getName(),((TextView)l.getAdapter().getView(1,null,null).findViewById(R.id.name_overall)).getText());
		assertEquals(t.getRounds().get(0).getMatches().get(0).getPlayerTwo().getName(),((TextView)l.getAdapter().getView(0,null,null).findViewById(R.id.name_overall)).getText());
		
		//order by name
		mActivity.findViewById(R.id.order_by_name_overall).callOnClick();
		assertEquals("Bob",((TextView)l.getAdapter().getView(0,null,null).findViewById(R.id.name_overall)).getText());
		assertEquals("Profile",((TextView)l.getAdapter().getView(1,null,null).findViewById(R.id.name_overall)).getText());

		mActivity.findViewById(R.id.order_by_name_overall).callOnClick();
		assertEquals("Profile",((TextView)l.getAdapter().getView(0,null,null).findViewById(R.id.name_overall)).getText());
		assertEquals("Bob",((TextView)l.getAdapter().getView(1,null,null).findViewById(R.id.name_overall)).getText());

		
		//order by w
		mActivity.findViewById(R.id.order_by_w_overall).callOnClick();
		assertEquals(t.getRounds().get(0).getMatches().get(0).getPlayerOne().getName(),((TextView)l.getAdapter().getView(0,null,null).findViewById(R.id.name_overall)).getText());
		assertEquals(t.getRounds().get(0).getMatches().get(0).getPlayerTwo().getName(),((TextView)l.getAdapter().getView(1,null,null).findViewById(R.id.name_overall)).getText());
		
		mActivity.findViewById(R.id.order_by_w_overall).callOnClick();
		assertEquals(t.getRounds().get(0).getMatches().get(0).getPlayerOne().getName(),((TextView)l.getAdapter().getView(1,null,null).findViewById(R.id.name_overall)).getText());
		assertEquals(t.getRounds().get(0).getMatches().get(0).getPlayerTwo().getName(),((TextView)l.getAdapter().getView(0,null,null).findViewById(R.id.name_overall)).getText());
		
		//order by t
		mActivity.findViewById(R.id.order_by_t_overall).callOnClick();
		assertEquals(t.getRounds().get(0).getMatches().get(0).getPlayerOne().getName(),((TextView)l.getAdapter().getView(0,null,null).findViewById(R.id.name_overall)).getText());
		assertEquals(t.getRounds().get(0).getMatches().get(0).getPlayerTwo().getName(),((TextView)l.getAdapter().getView(1,null,null).findViewById(R.id.name_overall)).getText());
		
		mActivity.findViewById(R.id.order_by_t_overall).callOnClick();
		assertEquals(t.getRounds().get(0).getMatches().get(0).getPlayerOne().getName(),((TextView)l.getAdapter().getView(0,null,null).findViewById(R.id.name_overall)).getText());
		assertEquals(t.getRounds().get(0).getMatches().get(0).getPlayerTwo().getName(),((TextView)l.getAdapter().getView(1,null,null).findViewById(R.id.name_overall)).getText());
		
		//order by l
		mActivity.findViewById(R.id.order_by_l_overall).callOnClick();
		assertEquals(t.getRounds().get(0).getMatches().get(0).getPlayerOne().getName(),((TextView)l.getAdapter().getView(1,null,null).findViewById(R.id.name_overall)).getText());
		assertEquals(t.getRounds().get(0).getMatches().get(0).getPlayerTwo().getName(),((TextView)l.getAdapter().getView(0,null,null).findViewById(R.id.name_overall)).getText());
		
		mActivity.findViewById(R.id.order_by_l_overall).callOnClick();
		assertEquals(t.getRounds().get(0).getMatches().get(0).getPlayerOne().getName(),((TextView)l.getAdapter().getView(0,null,null).findViewById(R.id.name_overall)).getText());
		assertEquals(t.getRounds().get(0).getMatches().get(0).getPlayerTwo().getName(),((TextView)l.getAdapter().getView(1,null,null).findViewById(R.id.name_overall)).getText());
		
		//order by s
		mActivity.findViewById(R.id.order_by_s_overall).callOnClick();
		assertEquals(t.getRounds().get(0).getMatches().get(0).getPlayerOne().getName(),((TextView)l.getAdapter().getView(0,null,null).findViewById(R.id.name_overall)).getText());
		assertEquals(t.getRounds().get(0).getMatches().get(0).getPlayerTwo().getName(),((TextView)l.getAdapter().getView(1,null,null).findViewById(R.id.name_overall)).getText());
		
		
		mActivity.findViewById(R.id.order_by_s_overall).callOnClick();
		assertEquals(t.getRounds().get(0).getMatches().get(0).getPlayerOne().getName(),((TextView)l.getAdapter().getView(1,null,null).findViewById(R.id.name_overall)).getText());
		assertEquals(t.getRounds().get(0).getMatches().get(0).getPlayerTwo().getName(),((TextView)l.getAdapter().getView(0,null,null).findViewById(R.id.name_overall)).getText());
		
		
	}
	
	
	
	/**
	 * Tests the rounding methods
	 */
	public void testRounding() 
	{		
		assertTrue(TournamentContainer.getInstance(tournamentId)!=null);
		assertTrue(((RoundRobinTournament)TournamentContainer.getInstance(tournamentId)).getAutomaticMessageHandler()!=null);
		Intent i = new Intent(getInstrumentation().getTargetContext(), OptionsEmailTab.class);
		i.setClassName(AbstractPluginCommonActivity.UTOOL_TOURNAMENT_CONFIG_PACKAGE, AbstractPluginCommonActivity.UTOOL_TOURNAMENT_CONFIG_CLASS);
		i.putExtra(AbstractPluginCommonActivity.UTOOL_TOURNAMENT_ID_EXTRA_NAME, tournamentId);
		mActivity = startActivity(i, (Bundle)null, (Object)null);

		assertTrue(mActivity!=null);
		
		
		//TEST ROUND ONE DECIMAL
		double a = 1.9999;
		assertEquals(""+OverallStandingsActivity.roundOneDecimal(a), ""+2.0);
		a = 1.3333333;
		assertEquals(""+OverallStandingsActivity.roundOneDecimal(a)+"", ""+1.3);
		a = 2342;
		assertEquals(""+OverallStandingsActivity.roundOneDecimal(a)+"", ""+2342.0+"");

		//TEST ROUND TWO DECIMAL
		a = 1.9999;
		assertEquals(""+OverallStandingsActivity.roundTwoDecimal(a)+"", ""+2.00+"");
		a = 1.3333333;
		assertEquals(""+OverallStandingsActivity.roundTwoDecimal(a)+"", ""+1.33+"");
		a = 2342;
		assertEquals(""+OverallStandingsActivity.roundTwoDecimal(a)+"", ""+2342.00+"");

	}
	

}
