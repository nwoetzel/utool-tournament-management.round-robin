package utool.plugin.roundrobin.test;

import java.util.ArrayList;

import utool.plugin.email.Contact;
import utool.plugin.roundrobin.communication.AutomaticMessageHandler;
import junit.framework.TestCase;

/**
 * Tests the automatic email handler
 * @author waltzm
 * @version 1/20/2013
 */
public class TestAutomaticEmailHandler extends TestCase{


	/**
	 * holds tid
	 */
	private long tournamentId =2390;

	/**
	 * Holds a reference to the OOT
	 */
	AutomaticMessageHandler aeh;

	/**
	 * Holds first time users
	 */
	boolean firstTime = true;

	//This method is invoked before every test
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		aeh = new AutomaticMessageHandler(tournamentId);
		
		//test constructor
		assertEquals(aeh.getPossibleSubscribers().size(), 0);
		assertEquals(aeh.getSubscribers().size(), 0);
	}

	/**
	 * Tests that the set and get subscribers works
	 */
	public void testSetSubscribers() 
	{
		ArrayList<Contact> subs = new ArrayList<Contact>();
		subs.add(new Contact("Ben", Contact.EMAIL_ADDRESS));
		subs.add(new Contact("Randy", Contact.PHONE_NUMBER));

		aeh.setSubscribers(subs);
		assertEquals(aeh.getSubscribers(),subs);

		//make sure safe from mutation
		subs.add(new Contact("Tom", Contact.EMAIL_ADDRESS));
		assertNotSame(aeh.getSubscribers(),subs);

		//make sue set works after adding
		aeh.setSubscribers(subs);
		assertEquals(aeh.getSubscribers(),subs);

	}

	/**
	 * Tests that the set and get possible subscribers works
	 */
	public void testSetPossibleSubscribers() 
	{
		ArrayList<Contact> subs = new ArrayList<Contact>();
		subs.add(new Contact("Randy", Contact.EMAIL_ADDRESS));
		subs.add(new Contact("Bill", Contact.EMAIL_ADDRESS));

		aeh.setPossibleSubscribers(subs);
		assertEquals(aeh.getPossibleSubscribers(),subs);

		//make sure safe from mutation
		subs.add(new Contact("Tom", Contact.EMAIL_ADDRESS));
		assertNotSame(aeh.getPossibleSubscribers(),subs);

		//make sue set works after adding
		aeh.setPossibleSubscribers(subs);
		assertEquals(aeh.getPossibleSubscribers(),subs);

	}




}
