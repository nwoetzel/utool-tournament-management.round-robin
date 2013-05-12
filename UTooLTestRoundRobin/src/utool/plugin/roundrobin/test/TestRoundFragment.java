package utool.plugin.roundrobin.test;

import java.util.ArrayList;
import java.util.UUID;
import utool.plugin.Player;
import utool.plugin.activity.TournamentContainer;
import utool.plugin.roundrobin.RoundRobinTournament;
import utool.plugin.roundrobin.mocks.FragmentHolder;
import utool.plugin.roundrobin.mocks.MockRoundFragment;
import android.content.Intent;
import android.os.Bundle;
import android.test.ActivityUnitTestCase;

/**
 * Tests proper operation of the round fragment
 * @author waltzm
 * @version 5/4/2013
 */
public class TestRoundFragment extends ActivityUnitTestCase<FragmentHolder>{

	/**
	 * holds the tournament id
	 */
	private long tid = 12347;

	/**
	 * Required constructor for Activity Tests
	 * @since 10/11/2012
	 */
	public TestRoundFragment() {
		super(FragmentHolder.class);
	}

	/**
	 * The activity under test
	 */
	private FragmentHolder mActivity;
	
	/**
	 * tid
	 */
	private RoundRobinTournament tournament;

	@Override
	protected void setUp() throws Exception{
		super.setUp();

		//clear application data
		TournamentContainer.clearInstance(tid);
		Intent in = new Intent(getInstrumentation().getTargetContext(), FragmentHolder.class);
		mActivity = startActivity(in, (Bundle)null, (Object)null);

		ArrayList<Player> players = new ArrayList<Player>();
		for (int i = 0; i < 16; i++)
		{
			players.add(new Player("Player "+i));
		}

		tournament = (RoundRobinTournament) TournamentContainer.getInstance(tid);
		tournament = new RoundRobinTournament(tid, players, "blarg", new UUID(1,0), mActivity, mActivity);
		TournamentContainer.putInstance(tournament);
		tournament.generateNextRound();

		//initial settings
		MockRoundFragment m = mActivity.getRoundFragment();
		assertEquals(m.getTid(),0);
		assertEquals(m.getIsSelP1(),true);
		assertEquals(m.getRound(),0);
		assertEquals(m.getSelectedPlayerIndex(),-1);

		//set what is needed
		m.setTID(tid);
		m.setRound(1);

	}

	/**
	 * Tests that the various components are initialized properly
	 */
	public void testInitialization() 
	{		
		MockRoundFragment m = mActivity.getRoundFragment();
		assertTrue(m!=null);
		assertTrue(tournament!=null);

		//initial settings	
		assertEquals(m.getTid(),tid);
		assertEquals(m.getIsSelP1(),true);
		assertEquals(m.getRound(),1);
		assertEquals(m.getSelectedPlayerIndex(),-1);


	}
	
	/**
	 * tests the second player being clicked
	 */
	public void testOnSecondPlayerClick()
	{
		//select something
		MockRoundFragment m = mActivity.getRoundFragment();
		m.setSelectedPlayerIndex(0);
		m.setIsSelP1(true);
		
		m.onSecondPlayerClick(0, true);
		
		assertEquals(m.getSelectedPlayerIndex(),-1);
		
		m.setSelectedPlayerIndex(0);
		m.setIsSelP1(true);
		
		m.onSecondPlayerClick(1, true);
		
		assertEquals(m.getSelectedPlayerIndex(),-1);
		
		m.setSelectedPlayerIndex(0);
		m.setIsSelP1(true);
		
		m.onSecondPlayerClick(0, false);
		
		assertEquals(m.getSelectedPlayerIndex(),-1);
		
		m.setSelectedPlayerIndex(0);
		m.setIsSelP1(false);
		
		m.onSecondPlayerClick(0, false);
		
		assertEquals(m.getSelectedPlayerIndex(),-1);
				
	}
	
	/**
	 * Tests switching
	 */
	public void testdoSwitch()
	{
		MockRoundFragment m = mActivity.getRoundFragment();
		m.setSelectedPlayerIndex(0);
		m.setIsSelP1(true);
		m.doSwitch(0, false);
		assertEquals(m.getSelectedPlayerIndex(),-1);
		
		m.setSelectedPlayerIndex(0);
		m.setIsSelP1(true);
		
		m.doSwitch(0, true);
		
		assertEquals(m.getSelectedPlayerIndex(),-1);
		
		m.setSelectedPlayerIndex(0);
		m.setIsSelP1(true);
		
		m.doSwitch(1, true);
		
		assertEquals(m.getSelectedPlayerIndex(),-1);
		
		m.setSelectedPlayerIndex(0);
		m.setIsSelP1(true);
		
		m.doSwitch(0, false);
		
		assertEquals(m.getSelectedPlayerIndex(),-1);
		
		m.setSelectedPlayerIndex(0);
		m.setIsSelP1(false);
		
		m.doSwitch(0, false);
		
		assertEquals(m.getSelectedPlayerIndex(),-1);
	}
}
