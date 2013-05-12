package utool.plugin.roundrobin.test;

import java.util.ArrayList;

import utool.plugin.Player;
import utool.plugin.activity.AbstractPluginCommonActivity;
import utool.plugin.activity.TournamentContainer;
import utool.plugin.roundrobin.OptionsEmailTab;
import utool.plugin.roundrobin.OptionsTournamentTab;
import utool.plugin.roundrobin.R;
import utool.plugin.roundrobin.RoundRobinTournament;
import utool.plugin.roundrobin.TournamentActivity;
import android.content.Intent;
import android.os.Bundle;
import android.test.ActivityUnitTestCase;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;

/**
 * Tests the tournament tab
 * @author kreierj
 * @version 4/20/2013
 */
public class TestRoundRobinOptionsTournamentTab extends ActivityUnitTestCase<OptionsTournamentTab>{

	/**
	 * holds the tournament id
	 */
	private long tournamentId = 123;

	/**
	 * Required constructor for Activity Tests
	 * @since 10/11/2012
	 */
	public TestRoundRobinOptionsTournamentTab() {
		super(OptionsTournamentTab.class);
	}

	/**
	 * The activity under test
	 */
	private OptionsTournamentTab mActivity;

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

		//ties
		ListView l = (ListView)mActivity.findViewById(R.id.tie_list_view);
		assertNotNull(l);
		assertEquals(3,l.getAdapter().getCount());
		
		//matching
		RadioButton r = (RadioButton)mActivity.findViewById(R.id.closest_score);
		assertEquals(false,r.isChecked());
		RadioButton r2 = (RadioButton)mActivity.findViewById(R.id.new_opponent);
		assertEquals(true,r2.isChecked());
		
		//scoring
		EditText w = (EditText)mActivity.findViewById(R.id.points_awarded_win);
		assertEquals("1.0",w.getText().toString());
		EditText t = (EditText)mActivity.findViewById(R.id.points_awarded_tie);
		assertEquals("0.5",t.getText().toString());
		EditText ll = (EditText)mActivity.findViewById(R.id.points_awarded_loss);
		assertEquals("0.0",ll.getText().toString());
		
		//rounds
		EditText nr = (EditText)mActivity.findViewById(R.id.number_of_rounds);
		assertEquals("3",nr.getText().toString());
	}
}