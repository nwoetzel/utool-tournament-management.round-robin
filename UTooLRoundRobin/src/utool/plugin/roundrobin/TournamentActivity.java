package utool.plugin.roundrobin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import utool.networking.XmlMessageTypeException;
import utool.networking.packet.PlayerMessage;
import utool.networking.packet.PluginTerminationMessage;
import utool.networking.packet.PlayerMessage.MessageType;
import utool.plugin.Player;
import utool.plugin.activity.IPluginServiceActivity;
import utool.plugin.activity.PluginCommonActivityHelper;
import utool.plugin.activity.PluginMainActivityHelper;
import utool.plugin.activity.TournamentContainer;
import utool.plugin.observer.Observer;
import utool.plugin.roundrobin.communication.IncomingCommandHandler;
import utool.plugin.roundrobin.communication.OutgoingCommandHandler;
import utool.plugin.roundrobin.communication.SaxFeedParser;
import utool.plugin.roundrobin.R;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.Vibrator;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This activity houses the matchups of the tournament and serves as the Round Robin PluginMainActivity.
 * @author Maria Waltz
 * @version 2/2/2013
 */
@SuppressLint("ValidFragment")
public class TournamentActivity extends FragmentActivity implements Observer, IPluginServiceActivity
{
	/**
	 * Data reception threads
	 */
	private static HashMap<Long, Thread> receiveThreads = new HashMap<Long, Thread>();

	/**
	 * Tag for logging to console
	 */
	private static final String LOG_TAG = "RoundRobin.TournamentActivity";

	/**
	 * The PluginMainActivityHelper this activity is using for all special operations
	 */
	protected PluginMainActivityHelper pluginHelper;

	/**
	 * Holds the adapter for the pager
	 */
	private RoundFragmentAdapter mAdapter;

	/**
	 * Holds a reference to the pager holding the fragments
	 */
	private ViewPager mPager;

	/**
	 * The instance of the Swiss Tournament that we will work with
	 */
	private RoundRobinTournament tournament;

	/**
	 * Request code used when going to tournament configuration screen
	 */
	public static final int TOURNAMENT_CONFIGURATION_ACTIVITY_REQUEST_CODE = 33;

	/**
	 * Timer for the round
	 */
	private Timer timer;

	/**
	 * Holds the index of the selected player.
	 */
	public int selectedPlayerIndex=56;


	/**
	 * Runnable used by thread to receive messages
	 */
	Runnable receiveRunnable = new Runnable() 
	{
		public void run() 
		{
			try {
				while (true)
				{
					String msg = pluginHelper.mICore.receive();
					if (msg != null && !msg.equals("")){
						PlayerMessage message = null;
						try {
							message = new PlayerMessage(msg);
						} catch (XmlMessageTypeException e) {

						}

						if (msg.equals(PluginCommonActivityHelper.UTOOL_SOCKET_CLOSED_MESSAGE)){
							return;
						} else if (PluginTerminationMessage.isPluginTerminationMessage(msg)){
							terminatePlugin();
						} else if (message!=null&&message.getMessageType()==MessageType.PlayerRegister){
							updatePlayerList(pluginHelper.mICore.getPlayerList());
						} else {
							SaxFeedParser s = new SaxFeedParser(new IncomingCommandHandler(tournament));
							s.parse(msg);
						}

					}
				}
			} catch (RemoteException e) {
				try
				{
					Log.e("ReceiveRunnable", "Exception when receiving a runnable", e);
					Toast.makeText(TournamentActivity.this, "Error in connection, try reconnecting.", Toast.LENGTH_LONG).show();

				}
				catch(Exception e2)
				{
					//couldnt notify user
				}

			}
		}
	};

	/**
	 * Determine and handle if a player is added through the msg
	 * @param data The message string
	 * @return true if it was a player being added
	 */
	public boolean DetermineIfMsgIsAddPlayer(String data){
		if(pluginHelper.getPermissionLevel()==Player.HOST)
		{
			Log.e(LOG_TAG,"Determining if player");
			try {
				PlayerMessage message = new PlayerMessage(data);
				if (message.getMessageType() == MessageType.PlayerRegister){
					Log.e(LOG_TAG,"Found player");
					RoundRobinPlayer player = new RoundRobinPlayer(message.getPlayer());
					player.setPermissionsLevel(Player.PARTICIPANT);
					ArrayList<Player> pt = tournament.getPlayers();
					ArrayList<Player> p = new ArrayList<Player>();
					p.addAll(pt);
					p.add(player);
					Log.e(LOG_TAG,"adding player");
					Log.e(LOG_TAG,"p:"+p);
					Log.e(LOG_TAG,"pt: "+pt);
					this.updatePlayerList(p);
					return true;
				}
			} catch (XmlMessageTypeException e){
			}
			if (PluginTerminationMessage.isPluginTerminationMessage(data)){
				try {
					pluginHelper.mICore.close();
				} catch (Exception e) {
					//Catch all exceptions so the plugin doesn't crash
				}
				TournamentContainer.clearInstance(pluginHelper.getTournamentId());
				finish();
			}
		}
		return false;
	}

	/**
	 * holds whether or not a round generation is in progress
	 */
	private boolean isGenerating=false;

	/**
	 * Error code indicating that the tournament needs to be resynchronized
	 */
	public static final int RESEND_ERROR_CODE = 101;

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_tournament);

		//hide the new match frame layout
		findViewById(R.id.frameLayout).setVisibility(View.GONE);
		pluginHelper = new PluginMainActivityHelper(TournamentActivity.this, TournamentActivity.this);
		
		try{
			if (pluginHelper.isNewInstance()){
				myOnCreate();
			}
		} catch (Exception e){
			//not sure if this code is actually safe....
		}
	}


	/**
	 * time delayed oncreate to be called once the service is connected.
	 */
	public void myOnCreate(){

		Log.d("ON CREATE FROM SERVICE", "YOOOOOOOOOOO");


		//Retrieve or instantiate the tournament
		//Create tournament instances
		tournament = (RoundRobinTournament) TournamentContainer.getInstance(pluginHelper.getTournamentId());
		if (tournament == null){

			tournament = new RoundRobinTournament(pluginHelper.getTournamentId(), pluginHelper.getPlayerList(), pluginHelper.getTournamentName(), pluginHelper.getPid(), TournamentActivity.this, TournamentActivity.this);

			TournamentContainer.putInstance(tournament);

			if (pluginHelper.getPermissionLevel() == Player.HOST){
				tournament.generateNextRound();
			}

		} else {

			updatePlayerList(pluginHelper.getPlayerList());


		}

		tournament.setPermissionLevel(pluginHelper.getPermissionLevel());
		//setup bridge
		tournament.getBridge().setMainActivity(TournamentActivity.this);

		//SETUP ACTIVITY MAIN SCREEN
		//tournament name
		Button tname = (Button)findViewById(R.id.tournament_name);

		tname.setText(pluginHelper.getTournamentName());

		//holding tournament name
		registerForContextMenu(tname);
		tname.setOnClickListener(new OnClickListener(){
			public void onClick(View v) 
			{
				//Go to overall standings
				Intent i = pluginHelper.getNewIntent(TournamentActivity.this, OverallStandingsActivity.class);
				startActivity(i);
			}	
		});




		//set round textview
		TextView round = (TextView)findViewById(R.id.tournament_round);
		round.setText("Round 1");

		//setup listener's for the <- -> arrows
		ImageButton left =(ImageButton)findViewById(R.id.left_arrow);
		left.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v) 
			{
				Log.d(LOG_TAG, "Left arrow clicked");
				mPager.setCurrentItem(mPager.getCurrentItem()-1);
			}	
		});

		ImageButton right =(ImageButton)findViewById(R.id.right_arrow);
		right.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v) 
			{
				Log.d(LOG_TAG, "Right arrow clicked");
				//determine if round should get generated
				if(canGenerateNextRound())
				{
					generateNextRound();
				}

				mAdapter.notifyDataSetChanged();
				mPager.setCurrentItem(mPager.getCurrentItem()+1);	


			}	
		});

		//SETUP FRAGMENTS AND PAGER
		mAdapter = new RoundFragmentAdapter(getSupportFragmentManager());
		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setAdapter(mAdapter);

		//Setup support for swiping generating the next round
		mPager.setOnPageChangeListener(new OnPageChangeListener(){
			@Override
			public void onPageScrollStateChanged(int arg0) {
				//Called when the use begins a scroll event
				if(canGenerateNextRound())
				{
					generateNextRound();
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageSelected(int arg0) {
			}

		});

		//round timer: default to not there
		TextView r = (TextView)findViewById(R.id.roundTimer);
		r.setText("");

		//setup timer
		RoundRobinConfiguration config = tournament.getRoundRobinConfiguration();
		if(config.getSecondsRemaining()!=-1)
		{
			r.setVisibility(View.VISIBLE);
			//update GUI every minute
			TimerTask task = new TimerTask(){
				public void run() {
					updateTimer();
				}
			};
			timer = new Timer(true);
			timer.scheduleAtFixedRate(task, 0, 1000);
		}


		//just in case set somehow, restore to default
		config.setTimerTerminatedPrematurely(false);





		mAdapter.notifyDataSetChanged();	

		if (tournament.getPermissionLevel() != Player.HOST){
			tournament.clearTournament();
			OutgoingCommandHandler och =  new OutgoingCommandHandler(tournament);
			och.handleSendError(TournamentActivity.RESEND_ERROR_CODE, "", "Resend", "plox update me");
		}
	}




	@Override
	public void onBackPressed(){

		Log.e("adiao","Sel:"+this.selectedPlayerIndex);
		if(this.selectedPlayerIndex==-1){
			super.onBackPressed();
		}
		else
		{
			//fragment.unselect();
			this.selectedPlayerIndex=-1;
			this.mAdapter.notifyDataSetChanged();
		}
	}


	/**
	 * Updates the player list
	 * Called either when clicked on from core main screen, or from returning from core configuration screen
	 * @param p the list to add to merge with the existing player list
	 */
	private void updatePlayerList(List<Player> p)
	{
		//update the player list
		Log.e(LOG_TAG,"Updating playerlist");

		//old list of players
		ArrayList<Player> pt = tournament.getPlayers();

		//boolean array to hold if each player in pt is in p
		boolean[] isPlayerInPt = new boolean[p.size()];

		//go through each player in pt and see if they are in p. If yes, then update old player to new player info
		for(int i=0;i<pt.size();i++)
		{
			int index = -1;
			for(int j = 0;j<p.size();j++)
			{

				if(pt.get(i).equals(p.get(j)))
				{
					//player in new passed in list matches id of old player
					Player t = pt.get(i);
					t.setPortrait(p.get(j).getPortrait());
					t.setName(p.get(j).getName());
					index = j;
					isPlayerInPt[j]=true;
				}
			}

			if(index==-1)
			{
				//remove player
				Log.e(LOG_TAG,"Removed Player: "+pt.get(i).getName());
				//player i was removed from pt in edit screen

				//remove player completely
				Player rem = pt.remove(i);
				i--;
				//find their match and remove them
				List<Match> mts = tournament.getRounds().get(tournament.getRounds().size()-1).getMatches();

				for(int m = 0;m<mts.size();m++)
				{
					if(mts.get(m).getPlayerOne().getUUID().equals(rem.getUUID()))
					{
						Match t = mts.get(m);
						//remove the old match from the player's internal list
						if(t.getPlayerTwo().getMatchesPlayed().size()>0)
						{
							t.getPlayerTwo().getMatchesPlayed().remove(t.getPlayerTwo().getMatchesPlayed().size()-1);

						}
						mts.set(m, new Match(RoundRobinPlayer.BYE, t.getPlayerTwo(), tournament.getRounds().get(tournament.getRounds().size()-1)));						
						tournament.notifyChanged();
						break;
					}
					else if(mts.get(m).getPlayerTwo().getUUID().equals(rem.getUUID()))
					{
						Match t = mts.get(m);
						//remove the old match from the player's internal list
						if(t.getPlayerOne().getMatchesPlayed().size()>0)
						{
							t.getPlayerOne().getMatchesPlayed().remove(t.getPlayerOne().getMatchesPlayed().size()-1);
						}
						mts.set(m, new Match(t.getPlayerOne(), RoundRobinPlayer.BYE, tournament.getRounds().get(tournament.getRounds().size()-1)));
						tournament.notifyChanged();
						break;
					}
				}

				//update graph
				tournament.removePlayerFromGraph(rem.getUUID());

				//set tournament players to 
				tournament.setPlayers(pt);

			}
		}
		boolean playerAdded = false;

		//check for addition
		for(int i =0;i<isPlayerInPt.length;i++)
		{
			if(!isPlayerInPt[i])
			{
				//Player at i must be added
				Log.e(LOG_TAG,"Added Player: "+p.get(i).getName());
				RoundRobinPlayer add=new RoundRobinPlayer(p.get(i));
				pt.add(add);
				playerAdded = true;
				tournament.conf.getRoundGenerator().addPlayer(add, tournament);
			}
		}

		//update number of rounds if needed
		if(playerAdded)
		{
			RoundRobinConfiguration c = tournament.getRoundRobinConfiguration();
			final int r = c.getNumRounds();
			double d=pt.size()-1;
			int n = (int)d;
			if(d>n)
			{
				n++;
			}

			final double n_fin = n;
			//set tournament players to 
			tournament.setPlayers(pt);


			//if the recommended number of rounds has changed...
			if(r!=d)
			{
				this.runOnUiThread(new Runnable(){

					public void run(){
						//Create alert asking if they want to increase the number of rounds 
						//due to the increase in the number of players

						new AlertDialog.Builder(TournamentActivity.this)
						.setTitle("Warning")
						.setMessage("Since the playerlist has been altered, would you like to change the number of rounds from "+r+" to the recommended number of "+((int)n_fin)+"?")
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int whichButton) 
							{
								//Yes clicked -> increase num rounds in rr config
								RoundRobinConfiguration c = tournament.getRoundRobinConfiguration();
								double d=tournament.getPlayers().size()-1;
								if(tournament.getPlayers().size()%2!=0)
								{
									d++;//account for bye
								}

								c.setNumRounds((int)d);
							}})
							.setNegativeButton("No",  null).show();
					}
				});
			}
		}

		//notify connected players of the change
		IncomingCommandHandler inc = new IncomingCommandHandler(tournament);
		inc.handleReceiveError(TournamentActivity.RESEND_ERROR_CODE, "", "", "");

	}

	public void onResume()
	{
		//update the gui on resume in case its needed
		super.onResume();

		if (pluginHelper.mICore != null){
			if(mAdapter!=null)
			{
				mAdapter.notifyDataSetChanged();	
			}
			if (tournament.getPermissionLevel() != Player.HOST){
				tournament.clearTournament();
				OutgoingCommandHandler och =  new OutgoingCommandHandler(tournament);
				och.handleSendError(TournamentActivity.RESEND_ERROR_CODE, "", "Resend", "plox update me");
				this.updateObserver(null);
			}
		}
	}

	/**
	 * Will generate the next round if applicable. Only will generate if the user is on the last
	 * fragment and the last fragment has finished setting scores
	 * @return true if able to be generated
	 */
	private boolean canGenerateNextRound() {
		if(isGenerating)
		{
			return false;
		}
		//determine if on last fragment generated thus far
		if(mPager.getCurrentItem()+1==tournament.getRounds().size())
		{
			//determine if reached the round limit
			if( tournament.getRounds().size()<tournament.getRoundRobinConfiguration().getNumRounds())
			{

				//determine if round is finished
				List<Match> matches = tournament.getRounds().get(mPager.getCurrentItem()).getMatches();
				//	boolean isFinished = true;
				for(int i=0;i<matches.size();i++)
				{
					if(matches.get(i).getMatchResult() == MatchResult.UNDECIDED)
					{
						//make sure its not a bye
						if(matches.get(i).getPlayerTwo().getUUID().equals(Player.BYE)||matches.get(i).getPlayerOne().getUUID().equals(Player.BYE))
						{
							//there is a bye in the match so ignore
						}
						else
						{
							return false;
						}

					}
				}

				//if here was reached all rounds must be done

				return true;
			}
		}

		return false;
	}

	/**
	 * Only call once canGenerateNextRound returns true
	 * Will set all bye match scores that haven't been set yet
	 * Will generate the next round and update the GUI
	 */
	private void generateNextRound()
	{
		//ONLY HOSTS can generate the next round
		if(tournament.getPermissionLevel()==Player.HOST)
		{
			isGenerating = true;

			Log.d("sdf","Generating next round: "+tournament.getRounds().size());
			List<Match> matches = tournament.getRounds().get(mPager.getCurrentItem()).getMatches();

			//set all bye matches to the opposing player as winner
			for(int i=0;i<matches.size();i++)
			{
				if(matches.get(i).getMatchResult() == MatchResult.UNDECIDED)
				{
					double win = tournament.getRoundRobinConfiguration().getWinScore();
					double loss = tournament.getRoundRobinConfiguration().getLossScore();
					OutgoingCommandHandler out = new OutgoingCommandHandler(tournament);
					//determine which player is the bye
					if(matches.get(i).getPlayerTwo().equals(RoundRobinPlayer.BYE))
					{
						//set player one as winner
						matches.get(i).setScores(win, loss, MatchResult.PLAYER_ONE);
						//Send to connected
						out.handleSendScore(-1, i, matches.get(i).getPlayerOne().getUUID().toString(), matches.get(i).getPlayerTwo().getUUID().toString(), win, loss, mPager.getCurrentItem());
						
					}
					else
					{
						//set player two as winner
						matches.get(i).setScores(loss, win, MatchResult.PLAYER_TWO);
						//send to connected
						out.handleSendScore(-1, i, matches.get(i).getPlayerOne().getUUID().toString(), matches.get(i).getPlayerTwo().getUUID().toString(), loss, win, mPager.getCurrentItem());
					}
				}
			}

			tournament.generateNextRound();

			//round timer 
			//if timer had been running, start it again
			if(tournament.getRoundRobinConfiguration().getStartTimerOnRoundChange())
			{
				tournament.getRoundRobinConfiguration().startTimer();
				//update GUI every minute
				TimerTask task = new TimerTask(){
					public void run() {
						updateTimer();
					}
				};
				timer = new Timer(true);
				timer.scheduleAtFixedRate(task, 0, 1000);
			}

			mAdapter.notifyDataSetChanged();


			//notify participant connections of new round
			OutgoingCommandHandler out = new OutgoingCommandHandler(tournament);
			int roundnum = tournament.getRounds().size()-1;
			out.handleSendBeginNewRound(-1, tournament.getRounds().size()-1);

			matches = tournament.getRounds().get(tournament.getRounds().size()-1).getMatches();
			for (int i = 0; i < matches.size(); i++){
				String[] team1 = new String[1];
				String[] team2 = new String[1];

				team1[0] = matches.get(i).getPlayerOne().getUUID().toString();
				team2[0] = matches.get(i).getPlayerTwo().getUUID().toString();
				out.handleSendMatchup(-1l, i, null, null, team1, team2, roundnum, null);
			}

			isGenerating=false;
		}
	}

	/**
	 * Displays the help messages for the user
	 */
	private void setupHelp() 
	{
		// Create and show the help dialog.
		final Dialog dialog = new Dialog(TournamentActivity.this);
		dialog.setContentView(R.layout.roundrobin_tournament_help);
		dialog.setTitle("UTooL Round Robin Help");
		dialog.setCancelable(true);
		Button closeButton = (Button) dialog.findViewById(R.id.help_close_button);
		closeButton.setOnClickListener(new Button.OnClickListener() {      
			public void onClick(View view) { 
				dialog.dismiss();     
			}
		});
		dialog.show();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(tournament.getPermissionLevel()==Player.HOST)
		{
			getMenuInflater().inflate(R.menu.activity_tournament, menu);
		}
		else
		{
			getMenuInflater().inflate(R.menu.activity_tournament_part, menu);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_edit:
			//Go to tournament configuration					
			Intent in = new Intent();
			in.setAction("utool.core.intent.TOURNAMENT_CONFIG");
			in.putExtra("tournamentId", tournament.getTournamentId());
			startActivityForResult(in, TOURNAMENT_CONFIGURATION_ACTIVITY_REQUEST_CODE);
			break;
		case R.id.menu_terminate:
			terminatePlugin();
			break;
		case R.id.menu_help:
			setupHelp();
			break;
		case R.id.menu_options:
			//transition to options screen
			if(tournament.getPermissionLevel()==Player.HOST)
			{
				Intent i = pluginHelper.getNewIntent(TournamentActivity.this, RoundRobinOptionsTabActivity.class);
				startActivity(i);
			}
			break;
		case R.id.menu_restart:
			tournament.clearTournament();
			tournament.generateNextRound();
			this.updateObserver(null);
			IncomingCommandHandler inc = new IncomingCommandHandler(tournament);
			inc.handleReceiveError(TournamentActivity.RESEND_ERROR_CODE, "", "", "");
			break;
		case R.id.options_part:
			if(tournament.getPermissionLevel()!=Player.HOST)
			{
				Intent i = pluginHelper.getNewIntent(TournamentActivity.this, ParticipantOptions.class);
				startActivity(i);
			}
			break;
		case R.id.quit_part:
			try {
				if(timer!=null)
				{
					timer.cancel();
				}
				RoundRobinConfiguration config = tournament.getRoundRobinConfiguration();
				config.setRoundTimerSeconds(0);
				pluginHelper.mICore.close();
			} catch (Exception e) {
			}
			TournamentContainer.clearInstance(pluginHelper.getTournamentId());
			finish();
			break;
		case R.id.help_part:
			setupHelp();
			break;
		case R.id.refresh_part:
			//send an error to the tournament
			//clear self
			tournament.clearTournament();
			OutgoingCommandHandler och =  new OutgoingCommandHandler(tournament);
			och.handleSendError(TournamentActivity.RESEND_ERROR_CODE, "", "Resend", "plox update me");
			this.updateObserver(null);
			break;
		case R.id.round_timer_item:
			//round timer
			RoundRobinConfiguration config = this.tournament.getRoundRobinConfiguration();
			OnTimeSetListener list  = new TimePickerDialog.OnTimeSetListener() {
				@Override
				public void onTimeSet(android.widget.TimePicker view, int hourOfDay, int minute) {
					startTimer(hourOfDay, minute);
				}
			};

			TimePickerDialog t = new TimePickerDialog(this, 0, list , 0, (int)(config.getRoundTimerSeconds()/60), true);
			t.setTitle("Set round timer amount:");

			t.show();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Terminate this plugin instance
	 */
	public void terminatePlugin(){
		TournamentContainer.clearInstance(pluginHelper.getTournamentId());
		try {
			if (timer != null){
				timer.cancel();
			}
			RoundRobinConfiguration config = tournament.getRoundRobinConfiguration();
			if (config != null){
				config.setTimerTerminatedPrematurely(true);
				config.setRoundTimerSeconds(0);
			}
			pluginHelper.mICore.close();
		} catch (Exception e){
			Log.e(LOG_TAG, e.toString());
		}
		finish();
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		//stop timer
		if(timer!=null)
		{
			timer.cancel();
			timer.purge();
			timer=null;
		}

		pluginHelper.unbindService();
	}


	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_tournament, menu);
		Log.d(LOG_TAG,"Inflating Menu");
	}

	/**
	 * Starts the timer
	 * Should only be called from the time picker and tests
	 * @param hourOfDay the hour of the timer
	 * @param minute the min of the timer
	 */
	public void startTimer(int hourOfDay, int minute)
	{
		//stop timer
		if(timer!=null)
		{
			timer.cancel();
			timer.purge();
			timer=null;
		}

		Log.i("",""+hourOfDay+":"+minute);
		RoundRobinConfiguration config = tournament.getRoundRobinConfiguration();
		config.setRoundTimerSeconds(hourOfDay *60*60+minute*60);
		config.startTimer();
		//update GUI every minute
		TimerTask task = new TimerTask(){
			public void run() {
				updateTimer();
			}
		};
		timer = new Timer(true);
		timer.scheduleAtFixedRate(task, 0, 1000);

		//notify connected of timer
		OutgoingCommandHandler och = new OutgoingCommandHandler(tournament);
		och.handleSendRoundTimerAmount(0, hourOfDay, minute, 0);
	}

	/**
	 * Updates the timer according to the time remaining
	 */
	public void updateTimer()
	{	
		runOnUiThread(new Runnable() {
			public void run() 
			{
				RoundRobinConfiguration config = tournament.getRoundRobinConfiguration();
				TextView tv = (TextView)findViewById(R.id.roundTimer);
				long time = config.getSecondsRemaining();

				String sec = time%60+"";
				if(time%60<10)
				{
					sec = "0"+time%60;
				}

				int mins = (int)(time/60)%60;
				int hours = (int)(time/60/60);

				//		Log.e(LOG_TAG,hours+":"+mins+":"+time%60+":"+sec);

				if(time<0)
				{
					//timer is stopped so stop timer
					if(timer!=null)
					{
						timer.cancel();
					}
					timer=null;
					//hide tv
					tv.setText("");
				}
				else if(time==0)
				{
					if(!config.wasTimerTerminated())
					{
						//timer is finished, go off
						Toast.makeText(TournamentActivity.this, "Round is finished!", Toast.LENGTH_SHORT).show();
						//Vibrate
						Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
						// 2. Vibrate in a Pattern with 500ms on, 500ms off for 5 times
						long[] pattern = { 0, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500};
						v.vibrate(pattern, -1);

						tv.setText("0:00:00");
					}
					else
					{
						Log.e("Timer","Vib surpressed");
					}

				}
				else if(time<60)
				{
					if(time<10)
					{
						tv.setText("0:00:0"+time);
					}
					else
					{
						tv.setText("0:00:"+time);
					}

				}
				else if(time == 60*5)//5 minutes
				{
					Log.e(LOG_TAG,"5 minutes!!!!" +time);
					Toast.makeText(TournamentActivity.this, "5 minutes remaining!", Toast.LENGTH_SHORT).show();
					//vibrate
					Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
					// 1. Vibrate for 1000 milliseconds
					long milliseconds = 1000;
					v.vibrate(milliseconds);

					tv.setText(hours+":0"+mins+":"+sec);
				}
				else
				{
					if(mins<10)
					{
						tv.setText(hours+":0"+mins+":"+sec);	
					}
					else
					{
						tv.setText(hours+":"+mins+":"+sec);			
					}
				}
			}
		});	
	}


	@Override
	public boolean onContextItemSelected(MenuItem item){
		return onOptionsItemSelected(item);
	}

	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		//If returning from the core edit screen
		if (requestCode == TOURNAMENT_CONFIGURATION_ACTIVITY_REQUEST_CODE)
		{
			if(resultCode == RESULT_OK)
			{
				//correctly returned, now to get new list of players
				if(data != null)
				{
					Bundle b = data.getExtras();
					Object[] resultArray = (Object[])b.get("playerList");

					ArrayList<Player> newPlayerlist = new ArrayList<Player>();
					for(Object o : resultArray){
						newPlayerlist.add((Player)o);
					}

					this.updatePlayerList(newPlayerlist);
				}

			}
		}
	}

	/**
	 * The is an adapter for the fragment pager
	 * @author waltzm
	 * @version 1/24/2013
	 */
	private class RoundFragmentAdapter extends FragmentPagerAdapter {

		/**
		 * Basic constructor for a fragment pager adapter
		 * @param fm the fragment manager
		 */
		public RoundFragmentAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() 
		{
			runOnUiThread(new Runnable(){

				@Override
				public void run() {
					//					Log.d(LOG_TAG,"Size: "+tournament.getRounds().size());
					//update the round textview
					TextView round = (TextView)findViewById(R.id.tournament_round);
					//determine if final round
					if((tournament.getRounds().size()<tournament.getRoundRobinConfiguration().getNumRounds()))
					{
						round.setText("Round "+(mPager.getCurrentItem()+1));
					}
					else
					{
						//tournament completed
						//determins if displaying final round
						if(mPager.getCurrentItem()+1==tournament.getRounds().size() && tournament.getPermissionLevel() == Player.HOST)
						{
							round.setText("Final Round ("+(mPager.getCurrentItem()+1)+")");
						}
						else
						{
							round.setText("Round "+(mPager.getCurrentItem()+1));
						}
					}

					//show the right arrow if the next screen can be generated, or not on the last screen
					if(mPager.getCurrentItem()<tournament.getRounds().size()-1||canGenerateNextRound())
					{
						//enable going to the right
						findViewById(R.id.right_arrow).setVisibility(View.VISIBLE);
					}
					else
					{
						findViewById(R.id.right_arrow).setVisibility(View.INVISIBLE);
					}

					if(mPager.getCurrentItem()==0)
					{
						//disable going to the left
						findViewById(R.id.left_arrow).setVisibility(View.INVISIBLE);
					}
					else
					{
						findViewById(R.id.left_arrow).setVisibility(View.VISIBLE);
					}
					//Log.d(LOG_TAG, "Size: "+tournament.getRounds().size());
				}

			});

			return tournament.getRounds().size();
		}

		@Override
		public Fragment getItem(int position) {
			//	Log.d(LOG_TAG,"Returning Fragment pos: "+position);
			return RoundFragment.newInstance(tournament.getTournamentId(),position+1);
		}

		@Override
		public int getItemPosition(Object object)
		{
			return POSITION_NONE;
		}

	}

	/**
	 * Called by round fragments to go to scores screen
	 * @param mid the match id of the match to set scores for
	 */
	protected void goToScores(int mid)
	{
		Intent i = pluginHelper.getNewIntent(TournamentActivity.this, ScoresActivity.class);
		i.putExtra("round", (mPager.getCurrentItem()));
		i.putExtra("match", mid);
		startActivity(i);
	}


	@Override
	public void runOnServiceConnected() {

		Log.d("ON CREATE FROM SERVICE", "YOOOOOOOOOOO2");
		try {

			myOnCreate();

			//create thread to get received messages
			Log.d(LOG_TAG, "Service connected, isNewInstance=" + pluginHelper.isNewInstance());
			if (pluginHelper.isNewInstance()){

				//this.updatePlayerList(pluginHelper.getPlayerList());

				// Host/Client specific code
				if (pluginHelper.mICore.isClient()){
					//send request code
					OutgoingCommandHandler out = new OutgoingCommandHandler(tournament);
					out.handleSendError(RESEND_ERROR_CODE, "", "", "");

				} else {
					OutgoingCommandHandler out = new OutgoingCommandHandler(tournament);
					out.handleSendClear(IncomingCommandHandler.RESEND_CLEAR);
					out.handleSendPlayers(-1, tournament.getPlayers());

					RoundRobinTournament swiss = tournament;

					List<Round> rounds = swiss.getRounds(); 

					//for each round
					for (Round r : rounds){
						//send new round
						out.handleSendBeginNewRound(-1, r.getRoundNumber());

						List<Match> matches = r.getMatches();

						//for each match in round
						for (int i = 0; i < matches.size(); i++){
							Match m = matches.get(i);

							RoundRobinPlayer playerOne = m.getPlayerOne();
							RoundRobinPlayer playerTwo = m.getPlayerTwo();

							String[] team1 = new String[1];
							String[] team2 = new String[1];

							team1[0] = playerOne.getUUID().toString();
							team2[0] = playerTwo.getUUID().toString();

							//send new match
							out.handleSendMatchup(-1, i, null, null, team1, team2, r.getRoundNumber(), null);

							//send match score
							if (m.getMatchResult() != MatchResult.UNDECIDED){
								out.handleSendScore(0, i, playerOne.getUUID().toString(), playerTwo.getUUID().toString(), m.getPlayerOneScore(), m.getPlayerTwoScore(), r.getRoundNumber());
							}
						}
					}


				}

			}

			Thread receiveThread = receiveThreads.get(pluginHelper.getTournamentId());
			if (pluginHelper.isNewInstance() || receiveThread == null || (receiveThread != null && !receiveThread.isAlive())){
				Log.d("KOTH", "Creating receive thread");
				receiveThread = new Thread(receiveRunnable);
				receiveThread.start();
				receiveThreads.put(pluginHelper.getTournamentId(), receiveThread);
			}

		} catch (RemoteException e) {
			Log.e(LOG_TAG, e.toString());
			Toast t = new Toast(TournamentActivity.this);
			t.setText("Error connecting to core service");
			t.show();
		}

	}

	@Override
	public void runOnServiceDisconnected()
	{
		Log.e(LOG_TAG, "Service has unexpectedly disconnected");
		//If the service has disconnected, either the plugin is closing or the core has died. Do some cleanup.
		TournamentContainer.clearInstance(pluginHelper.getTournamentId());
	}

	/**
	 * A notification fragment which will display text to a user with a clickable "Ok" box
	 * @author Justin Kreier
	 * @version 1/18/2013
	 */
	public static class HelpDialog extends DialogFragment{

		/**
		 * The message to display
		 */
		private String message;

		/**
		 * Constructor
		 * @param message The message you want to display when show() is called
		 */
		public HelpDialog(String message){
			this.message = message;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState){
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(message);

			builder.setPositiveButton("Ok", null);

			return builder.create();
		}
	}


	@Override
	public void updateObserver(Object observedObject)
	{
		//		if (observedObject instanceof RoundRobinTournament){
		//SwissTournament tournament = (SwissTournament)observedObject;
		if(mAdapter!=null)
		{
			this.runOnUiThread(new Runnable(){
				@Override
				public void run() {
					try{
						//update round timer if needed
						if(tournament.getRoundRobinConfiguration().getSecondsRemaining()!=-1&&timer==null)
						{
							Log.d(LOG_TAG,"Updating round timer");
							//create the timer
							TimerTask task = new TimerTask(){
								public void run() {
									updateTimer();
								}
							};
							timer = new Timer(true);
							timer.scheduleAtFixedRate(task, 0, 1000);
						}
						//update the gui
						mAdapter.notifyDataSetChanged();
					} catch (IllegalStateException e){
						Log.e("Observer", "Exception thrown from mAdapter", e);
					}
				}
			});
		}
		//		}
	}

	/**
	 * Getter for the timer. Used in testing
	 * @return the timer
	 */
	public Timer getTimer()
	{
		return timer;
	}

	@Override
	public boolean sendMessage(String message) {
		try{
			return pluginHelper.sendMessage(message);
		}
		catch(Exception e)
		{
			return false;
		}

	}
}
