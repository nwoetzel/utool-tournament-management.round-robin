package utool.plugin.roundrobin;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import utool.plugin.ImageDecodeTask;
import utool.plugin.Player;
import utool.plugin.activity.AbstractPluginCommonActivity;
import utool.plugin.activity.TournamentContainer;
import utool.plugin.roundrobin.communication.OutgoingCommandHandler;
import utool.plugin.roundrobin.R;

/**
 * This activity is responsible for allowing the user to set the scores for a
 * Swiss tournament match.
 * @author Justin Kreier
 * @version 1/30/2013
 */
public class ScoresActivity extends AbstractPluginCommonActivity{

	/**
	 * Reference to the tournament
	 */
	private RoundRobinTournament tournament;

	/**
	 * Reference to the match
	 */
	private Match match;

	/**
	 * The current round number (zero based)
	 */
	private int roundNum;

	/**
	 * The current match number (zero based)
	 */
	private int matchNum;

	/**
	 * The match result
	 */
	private MatchResult result;

	/**
	 * Timer for the round
	 */
	private Timer timer;


	/* (non-Javadoc)
	 * @see utool.plugin.activity.AbstractPluginCommonActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//Hide keyboard
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		setContentView(R.layout.activity_scores);

		//retrieve the Swiss tournament
		tournament = (RoundRobinTournament) TournamentContainer.getInstance(this.getTournamentId());

		//retrieve the round and match number from extras
		Intent intent = getIntent();
		roundNum = intent.getIntExtra("round", 0);
		matchNum = intent.getIntExtra("match", 0);

		//get the match from the above info
		Round round = tournament.getRounds().get(roundNum);
		match = round.getMatches().get(matchNum);

		//display match and round number scores to text views
		TextView matchName = (TextView)findViewById(R.id.matchName);
		if (tournament.getRoundRobinConfiguration().getNumRounds() > roundNum){
			matchName.setText("Round "+(roundNum+1)+" : Match "+(matchNum+1));
		} else {
			matchName.setText("Final Round ("+(roundNum+1)+") : Match "+(matchNum+1));
		}

		TextView p1Name = (TextView)findViewById(R.id.playerOneNameDisplay);
		p1Name.setText(match.getPlayerOne().getName());

		//async load p1 portrait
		ImageView p1Portrait = (ImageView)findViewById(R.id.playerOnePortraitDisplay);
		Player p1 = match.getPlayerOne();
		if (p1.hasPortraitChanged()){
			p1Portrait.setImageResource(R.drawable.silhouette);
			ImageDecodeTask task = new ImageDecodeTask(p1Portrait);
			p1Portrait.setTag(task);
			task.execute(p1);
		} else {
			Bitmap bm = p1.getPortrait();
			if (bm != null){
				p1Portrait.setImageBitmap(bm);
			} else {
				p1Portrait.setImageResource(R.drawable.silhouette);
			}
		}
		//end async load

		EditText p1Score = (EditText)findViewById(R.id.playerOneScore);
		p1Score.setText(match.getPlayerOneScore()+"");

		TextView p2Name = (TextView)findViewById(R.id.playerTwoNameDisplay);
		p2Name.setText(match.getPlayerTwo().getName());

		//async load p2 portrait
		ImageView p2Portrait = (ImageView)findViewById(R.id.playerTwoPortraitDisplay);
		Player p2 = match.getPlayerTwo();
		if (p2.hasPortraitChanged()){
			p2Portrait.setImageResource(R.drawable.silhouette);
			ImageDecodeTask task = new ImageDecodeTask(p2Portrait);
			p2Portrait.setTag(task);
			task.execute(p2);
		} else {
			Bitmap bm = p2.getPortrait();
			if (bm != null){
				p2Portrait.setImageBitmap(bm);
			} else {
				p2Portrait.setImageResource(R.drawable.silhouette);
			}
		}
		//end async load

		EditText p2Score = (EditText)findViewById(R.id.playerTwoScore);
		p2Score.setText(match.getPlayerTwoScore()+"");


		//hide round timer
		TextView r = (TextView)findViewById(R.id.roundTimer);
		r.setVisibility(View.GONE);

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

		//initialize listeners
		initializeListeners();


	}

	/**
	 * Displays the help messages for the user.
	 * Different help is displayed for participant vs. host/moderator
	 */
	private void setupHelp() 
	{
		// Create and show the help dialog.
		final Dialog dialog = new Dialog(ScoresActivity.this);
		if(tournament.getPermissionLevel()== Player.HOST|| tournament.getPermissionLevel()== Player.MODERATOR)
		{
			dialog.setContentView(R.layout.roundrobin_scores_help);
		}
		else
		{
			dialog.setContentView(R.layout.roundrobin_scores_part_help);
		}
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
		getMenuInflater().inflate(R.menu.options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.help:
			setupHelp();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		Log.e("Scores","Scores On destroy");

		//stop timer
		if(timer!=null)
		{
			timer.cancel();
			timer.purge();
			timer=null;
		}
	}

	/**
	 * Initializes listeners for UI elements
	 */
	private void initializeListeners(){
		//save listener
		Button saveButton = (Button)findViewById(R.id.saveButton);
		//if host/mod activate save button
		if(tournament.getPermissionLevel()==Player.HOST||tournament.getPermissionLevel()==Player.MODERATOR)
		{

			saveButton.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					saveButtonPressed();
				}
			});

		}
		//if participant, disable save
		else
		{
			//TODO disable or hide or change text
			saveButton.setEnabled(false);
		}



		EditText p1Score = (EditText)findViewById(R.id.playerOneScore);
		EditText p2Score = (EditText)findViewById(R.id.playerTwoScore);

		//if round has been completed
		if (tournament.getRounds().size()-1 > roundNum){
			//disable the buttons
			Button b = (Button)findViewById(R.id.playerOneAdd);
			b.setEnabled(false);
			b = (Button)findViewById(R.id.playerOneSubtract);
			b.setEnabled(false);
			b = (Button)findViewById(R.id.playerTwoAddButton);
			b.setEnabled(false);
			b = (Button)findViewById(R.id.playerTwoSubtractButton);
			b.setEnabled(false);
			p1Score.setFocusable(false);
			p2Score.setFocusable(false);
		} else {
			//set up add and subtract listeners
			Button b = (Button)findViewById(R.id.playerOneAdd);
			b.setOnClickListener(new AddButtonPressedListener(p1Score));

			b = (Button)findViewById(R.id.playerOneSubtract);
			b.setOnClickListener(new SubtractButtonPressedListener(p1Score));

			b = (Button)findViewById(R.id.playerTwoAddButton);
			b.setOnClickListener(new AddButtonPressedListener(p2Score));

			b = (Button)findViewById(R.id.playerTwoSubtractButton);
			b.setOnClickListener(new SubtractButtonPressedListener(p2Score));
		}

		//group listeners
		Button playerOneWin = (Button)findViewById(R.id.playerOneWinButton);
		Button playerTwoWin = (Button)findViewById(R.id.playerTwoWinButton);
		Button tie = (Button)findViewById(R.id.tieButton);

		if (!(tournament.getRounds().size()-1 > roundNum)){
			playerOneWin.setOnClickListener(new ResultChangedListener(R.id.playerOneWinButton, p1Score, p2Score));
			playerTwoWin.setOnClickListener(new ResultChangedListener(R.id.playerTwoWinButton, p1Score, p2Score));
			tie.setOnClickListener(new ResultChangedListener(R.id.tieButton, p1Score, p2Score));
		}
		//store the match result
		result = match.getMatchResult();

		//alter the button display if winner is already set
		if (result == MatchResult.PLAYER_ONE){
			playerOneWin.setEnabled(false);
		} else if (result == MatchResult.TIE){
			tie.setEnabled(false);
		} else if (result == MatchResult.PLAYER_TWO){
			playerTwoWin.setEnabled(false);
		}

	}

	/**
	 * Handler for when the save button is pressed
	 */
	private void saveButtonPressed(){
		if(tournament.getPermissionLevel()==Player.HOST||tournament.getPermissionLevel()==Player.MODERATOR)
		{
			//get player 1 and player 2 scores

			EditText p1ScoreView = (EditText)findViewById(R.id.playerOneScore);
			EditText p2ScoreView = (EditText)findViewById(R.id.playerTwoScore);

			double p1Score;
			double p2Score;

			try{
				p1Score = Double.parseDouble(p1ScoreView.getText().toString());
			} catch (NumberFormatException e){
				p1Score = match.getPlayerOneScore();
			}

			try{
				p2Score = Double.parseDouble(p2ScoreView.getText().toString());
			} catch (NumberFormatException e){
				p2Score = match.getPlayerTwoScore();
			}

			//set the scores and match result for the match
			match.setScores(p1Score, p2Score, result);

			//send scores to participants
			OutgoingCommandHandler out = new OutgoingCommandHandler(tournament);
			out.handleSendScore(-1, matchNum, match.getPlayerOne().getUUID().toString(), match.getPlayerTwo().getUUID().toString(), p1Score, p2Score, roundNum);

			//end the activity
			finish();
		}
		else
		{
			//do nothing
		}
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

				//Log.e(LOG_TAG,hours+":"+mins+":"+time%60+":"+sec);

				if(time<0)
				{
					//timer is stopped so stop timer
					timer.cancel();
					//hide tv
					tv.setText("");
				}
				else if(time==0)
				{
					//timer is finished, go off
					tv.setText("0:00:00");
				}
				else if(time<60)
				{
					if(time<9)
					{
						tv.setText("0:00:0"+time);
					}
					else
					{
						tv.setText("0:00:"+time);
					}

				}
				else if(time<=60*5 && time>=60*5-1 ) //eg (4:49)
				{
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



	/**
	 * Handler for when an add button is pressed
	 * @author Justin Kreier
	 * @version 1/29/2013
	 */
	public class AddButtonPressedListener implements OnClickListener{

		/**
		 * The textField to modify
		 */
		private EditText textField;

		/**
		 * Constructor for the listener
		 * @param text The edit text to modify
		 */
		public AddButtonPressedListener(EditText text){
			textField = text;
		}

		@Override
		public void onClick(View v) {
			//get the text's double value
			double score = 0.0;
			try{
				score = Double.parseDouble(textField.getText().toString());
			} catch (NumberFormatException e){
				//bad data, ignore and let it stay at zero
			}

			//increase the score by one
			score = score + 1;

			//set the text's text to the new value
			textField.setText(score+"");
		}

	}

	/**
	 * Listener and handler for when the subtract button is pressed
	 * @author Justin Kreier
	 * @version 1/29/2013
	 */
	public class SubtractButtonPressedListener implements OnClickListener{

		/**
		 * The textField to modify
		 */
		private EditText textField;

		/**
		 * Constructor for the listener
		 * @param text The edit text to modify
		 */
		public SubtractButtonPressedListener(EditText text){
			textField = text;
		}

		@Override
		public void onClick(View v) {
			//get the text's double value
			double score = 0.0;
			try{
				score = Double.parseDouble(textField.getText().toString());
			} catch (NumberFormatException e){
				//ignore, let it stay at 0
			}

			//increase the score by one
			score = score - 1;

			//set the text's text to the new value
			textField.setText(score+"");
		}
	}

	/**
	 * Listener for when the match result is set
	 * @author Justin Kreier
	 * @version 1/29/2013
	 */
	public class ResultChangedListener implements OnClickListener{

		/**
		 * The id of the item
		 */
		private int id;

		/**
		 * Reference to the player one score field
		 */
		private EditText p1Score;

		/**
		 * Reference to the player two score field
		 */
		private EditText p2Score;

		/**
		 * Constructor
		 * @param itemId The item to listen for
		 * @param p1 Player one's score edit text
		 * @param p2 Player two's score edit text
		 */
		public ResultChangedListener(int itemId, EditText p1, EditText p2){
			id = itemId;
			p1Score = p1;
			p2Score = p2;
		}

		@Override
		public void onClick(View v) {
			//get which item is checked
			switch(id){
			case R.id.playerOneWinButton:
				result = MatchResult.PLAYER_ONE;
				break;
			case R.id.tieButton:
				result = MatchResult.TIE;
				break;
			case R.id.playerTwoWinButton:
				result = MatchResult.PLAYER_TWO;
				break;
			default:
				result = MatchResult.UNDECIDED;
			}

			Button p1Win = (Button)findViewById(R.id.playerOneWinButton);
			Button p2Win = (Button)findViewById(R.id.playerTwoWinButton);
			Button tie = (Button)findViewById(R.id.tieButton);

			//update the display
			if (result == MatchResult.PLAYER_ONE){
				p1Win.setEnabled(false);
				tie.setEnabled(true);
				p2Win.setEnabled(true);

				p1Score.setText(tournament.getRoundRobinConfiguration().getWinScore()+"");
				p2Score.setText(tournament.getRoundRobinConfiguration().getLossScore()+"");

			} else if (result == MatchResult.TIE){
				p1Win.setEnabled(true);
				tie.setEnabled(false);
				p2Win.setEnabled(true);

				p1Score.setText(tournament.getRoundRobinConfiguration().getTieScore()+"");
				p2Score.setText(tournament.getRoundRobinConfiguration().getTieScore()+"");

			} else if (result == MatchResult.PLAYER_TWO){
				p1Win.setEnabled(true);
				tie.setEnabled(true);
				p2Win.setEnabled(false);

				p1Score.setText(tournament.getRoundRobinConfiguration().getLossScore()+"");
				p2Score.setText(tournament.getRoundRobinConfiguration().getWinScore()+"");
			} else {
				p1Win.setEnabled(true);
				tie.setEnabled(true);
				p2Win.setEnabled(true);
			}
		}

	}
}
