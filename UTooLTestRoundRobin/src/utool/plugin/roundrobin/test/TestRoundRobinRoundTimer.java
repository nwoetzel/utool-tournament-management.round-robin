package utool.plugin.roundrobin.test;

import java.util.ArrayList;

import junit.framework.TestCase;
import utool.plugin.Player;
import utool.plugin.roundrobin.RoundRobinConfiguration;
import utool.plugin.roundrobin.RoundRobinPlayer;

/**
 * Tests the roudn timer
 * @author waltzm
 * @version 3/17/2013
 */
public class TestRoundRobinRoundTimer extends TestCase{

	/**
	 * Holds a reference to the config
	 */
	RoundRobinConfiguration config;

	/**
	 * Holds first time users
	 */
	boolean firstTime = true;

	//This method is invoked before every test
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		ArrayList<RoundRobinPlayer> p = new ArrayList<RoundRobinPlayer>();
		p.add(new RoundRobinPlayer(new Player("John")));
		p.add(new RoundRobinPlayer(new Player("John")));
		p.add(new RoundRobinPlayer(new Player("John")));
		p.add(new RoundRobinPlayer(new Player("John")));

		config= new RoundRobinConfiguration(p, null);

		//test constructor and getters
		assertEquals(config.getStartTimerOnRoundChange(), false);
		assertEquals(config.getSecondsRemaining(), -1);
		assertEquals(config.getRoundTimerSeconds(), 50*60);

	}

	/**
	 * Tests that the start timer works as intended
	 */
	public void testStartTimer() 
	{
		//Testing setRoundTimerSeconds
		int time = 20*60+45;
		config.setRoundTimerSeconds(time);
		assertEquals(config.getRoundTimerSeconds(), time);

		//Testing getSecondsRemaining
		assertEquals(config.startTimer(), true);
		assertEquals(config.startTimer(), false);
		assertEquals(config.getSecondsRemaining(), 20*60+45);
	}

	/**
	 * Tests that the start timer with a delay works as intended
	 */
	public void testStartTimerDelay() 
	{
		//Testing setRoundTimerSeconds
		int time = 20*60+45;
		config.setRoundTimerSeconds(time);
		assertEquals(config.getRoundTimerSeconds(), time);

		//Testing getSecondsRemaining
		//assertEquals(config.startTimer(60*5), true);
		assertEquals(config.startTimer(60*20*1000), true);
		assertEquals(config.getSecondsRemaining(), 45);
	}

	/**
	 * Tests that setters and getters work
	 */
	public void testSettersGettersRoundTimer() 
	{
		//Testing setRoundTimerSeconds
		int time = 20*60+45;
		config.setRoundTimerSeconds(time);
		assertEquals(config.getRoundTimerSeconds(), time);

		//Testing getSecondsRemaining
		assertEquals(config.getSecondsRemaining(), -1);

		config.startTimer();
		assertEquals(config.getSecondsRemaining(), 20*60+45);

	}
}
