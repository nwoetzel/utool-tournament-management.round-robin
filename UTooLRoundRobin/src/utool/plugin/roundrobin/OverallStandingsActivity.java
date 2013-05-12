package utool.plugin.roundrobin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import utool.plugin.ImageDecodeTask;
import utool.plugin.Player;
import utool.plugin.activity.AbstractPluginCommonActivity;
import utool.plugin.activity.TournamentContainer;
import utool.plugin.roundrobin.TournamentActivity.HelpDialog;
import utool.plugin.roundrobin.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * This activity is the screen for displaying the players and their 
 * overall standings
 * @author waltzm
 * @version 2/2/2013
 */
public class OverallStandingsActivity extends AbstractPluginCommonActivity
{
	/**
	 * Order the list by the player rank
	 */
	public final static int ORDER_BY_RANK = 1;


	/**
	 * Order the list by the player name
	 */
	public final static int ORDER_BY_NAME = 2;

	/**
	 * Order the list by the number of round wins
	 */
	public final static int ORDER_BY_ROUND_WINS = 3;

	/**
	 * Order the list by the round losses
	 */
	public final static int ORDER_BY_ROUND_LOSSES = 4;

	/**
	 * Order the list by the round ties
	 */
	public final static int ORDER_BY_ROUND_TIES = 21;

	/**
	 * Order by the overall win score
	 */
	public final static int ORDER_BY_SCORE = 5;

	/**
	 * Holds whether or not the list is ordered ascending or descending
	 */
	private boolean isAscending = false;

	/**
	 * Holds which ordering scheme is selected
	 */
	private int currentSelection = ORDER_BY_RANK;

	/**
	 * Holds the listview adapter
	 */
	private OverallStandingsAdapter ad;

	/**
	 * Log tag to be used in this class
	 */
	private static final String LOG_TAG = "SS Overall Standings Activity";

	/**
	 * Holds the explanation for rank
	 */
	private static final String RANK_HELP = "Rank is how the player is doing in the tournament. A rank of one indicates that player is in first place.";

	/**
	 * Holds the explanation for name
	 */
	private static final String NAME_HELP = "Name is the name of the competing player. Long names may not be fully displayed on this screen.";

	/**
	 * Holds the explanation for wins
	 */
	private static final String WINS_HELP = "W stands for round wins. This column will have the total count of rounds the player has won.";

	/**
	 * Holds the explanation for losses
	 */
	private static final String LOSSES_HELP = "L stands for round losses. This column will have the total count of rounds the player has lost.";

	/**
	 * Holds the explanation for ties
	 */
	private static final String TIES_HELP = "T stands for round ties. This column will have the total count of rounds the player has tied.";

	/**
	 * Holds the explanation for score
	 */
	private static final String SCORE_HELP = "S stands for score. This column will hold the player's current score in the tournament.";

	/**
	 * Holds the first help text for the screen
	 */
	private static final String HELP_TEXT_1="This screen shows the current statistics for all players. Tap on a column header to order the list of players by that column. Hold down a column name to get a detailed explanation.";

	/**
	 * Holds the players of the tournament
	 */
	private List<RoundRobinPlayer> playerz;


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_standings_overall);

		//setup adapter
		ListView l = (ListView)findViewById(R.id.overall_standings_list);


		//get list from StandingsGenerator
		playerz = ((RoundRobinTournament)TournamentContainer.getInstance(getTournamentId())).getStandingsArray();

		//order arraylist  by rank ascending
		ArrayList<RoundRobinPlayer> players = this.orderByColumn(ORDER_BY_RANK);

		ad=new OverallStandingsAdapter(this, R.id.overall_standings_list, players);
		l.setAdapter(ad);

		//Setup ordering of the columns
		TextView rank = (TextView) findViewById(R.id.order_by_rank_overall);
		rank.setOnTouchListener(new FakeButtonOnTouchListener());
		rank.setOnClickListener(new OnClickListener()
		{

			public void onClick(View arg0)
			{
				arg0.setBackgroundColor(Color.BLACK);

				ArrayList<RoundRobinPlayer> players = orderByColumn(ORDER_BY_RANK);
				ad.setPlayers(players);
				ad.notifyDataSetChanged();
			}
		});

		TextView tie = (TextView) findViewById(R.id.order_by_t_overall);
		tie.setOnTouchListener(new FakeButtonOnTouchListener());
		tie.setOnClickListener(new OnClickListener()
		{

			public void onClick(View arg0)
			{
				arg0.setBackgroundColor(Color.BLACK);

				ArrayList<RoundRobinPlayer> players = orderByColumn(ORDER_BY_ROUND_TIES);
				ad.setPlayers(players);
				ad.notifyDataSetChanged();
			}
		});

		TextView name = (TextView) findViewById(R.id.order_by_name_overall);
		name.setOnTouchListener(new FakeButtonOnTouchListener());
		name.setOnClickListener(new OnClickListener()
		{

			public void onClick(View arg0)
			{
				arg0.setBackgroundColor(Color.BLACK);

				ArrayList<RoundRobinPlayer> players = orderByColumn(ORDER_BY_NAME);
				ad.setPlayers(players);
				ad.notifyDataSetChanged();
			}
		});

		TextView score = (TextView) findViewById(R.id.order_by_s_overall);
		score.setOnTouchListener(new FakeButtonOnTouchListener());
		score.setOnClickListener(new OnClickListener()
		{

			public void onClick(View arg0)
			{
				arg0.setBackgroundColor(Color.BLACK);

				ArrayList<RoundRobinPlayer> players = orderByColumn(ORDER_BY_SCORE);
				ad.setPlayers(players);
				ad.notifyDataSetChanged();
			}
		});

		TextView wins = (TextView) findViewById(R.id.order_by_w_overall);
		wins.setOnTouchListener(new FakeButtonOnTouchListener());
		wins.setOnClickListener(new OnClickListener()
		{

			public void onClick(View arg0)
			{
				arg0.setBackgroundColor(Color.BLACK);

				ArrayList<RoundRobinPlayer> players = orderByColumn(ORDER_BY_ROUND_WINS);
				ad.setPlayers(players);
				ad.notifyDataSetChanged();
			}
		});

		TextView losses = (TextView) findViewById(R.id.order_by_l_overall);
		losses.setOnTouchListener(new FakeButtonOnTouchListener());
		losses.setOnClickListener(new OnClickListener()
		{

			public void onClick(View arg0)
			{
				arg0.setBackgroundColor(Color.BLACK);

				ArrayList<RoundRobinPlayer> players = orderByColumn(ORDER_BY_ROUND_LOSSES);
				ad.setPlayers(players);
				ad.notifyDataSetChanged();
			}
		});


		//Register Items for context menu
		View r = findViewById(R.id.order_by_rank_overall);
		View n = findViewById(R.id.order_by_name_overall);
		View w = findViewById(R.id.order_by_w_overall);
		View ll = findViewById(R.id.order_by_l_overall);
		View t = findViewById(R.id.order_by_t_overall);
		View s = findViewById(R.id.order_by_s_overall);

		registerForContextMenu(r);
		registerForContextMenu(n);
		registerForContextMenu(w);
		registerForContextMenu(ll);
		registerForContextMenu(t);
		registerForContextMenu(s);


	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		//instead of bringing up a menu, show a dialog of explanations
		HelpDialog warning;
		if(v.getId() == R.id.order_by_rank_overall)
		{
			warning = new HelpDialog(RANK_HELP);
			warning.show(getSupportFragmentManager(), "Explanation of Rank");	
			
			ArrayList<RoundRobinPlayer> players = orderByColumn(ORDER_BY_RANK);
			ad.setPlayers(players);
			ad.notifyDataSetChanged();
		}
		else if(v.getId() == R.id.order_by_name_overall)
		{
			warning = new HelpDialog(NAME_HELP);
			warning.show(getSupportFragmentManager(), "Explanation of Name");	
			
			ArrayList<RoundRobinPlayer> players = orderByColumn(ORDER_BY_NAME);
			ad.setPlayers(players);
			ad.notifyDataSetChanged();
		}
		else if(v.getId() == R.id.order_by_w_overall)
		{
			warning = new HelpDialog(WINS_HELP);
			warning.show(getSupportFragmentManager(), "Explanation of Wins");	
			
			ArrayList<RoundRobinPlayer> players = orderByColumn(ORDER_BY_ROUND_WINS);
			ad.setPlayers(players);
			ad.notifyDataSetChanged();
		}
		else if(v.getId() == R.id.order_by_l_overall)
		{
			warning = new HelpDialog(LOSSES_HELP);
			warning.show(getSupportFragmentManager(), "Explanation of Losses");	
			
			ArrayList<RoundRobinPlayer> players = orderByColumn(ORDER_BY_ROUND_LOSSES);
			ad.setPlayers(players);
			ad.notifyDataSetChanged();
		}
		else if(v.getId() == R.id.order_by_t_overall)
		{
			warning = new HelpDialog(TIES_HELP);
			warning.show(getSupportFragmentManager(), "Explanation of Ties");	
			
			ArrayList<RoundRobinPlayer> players = orderByColumn(ORDER_BY_ROUND_TIES);
			ad.setPlayers(players);
			ad.notifyDataSetChanged();
		}
		else if(v.getId() == R.id.order_by_s_overall)
		{
			warning = new HelpDialog(SCORE_HELP);
			warning.show(getSupportFragmentManager(), "Explanation of Score");	
			
			ArrayList<RoundRobinPlayer> players = orderByColumn(ORDER_BY_SCORE);
			ad.setPlayers(players);
			ad.notifyDataSetChanged();
		}

	}


	/**
	 * Orders the list by column passed in. 
	 * @param columnClicked the column clicked
	 * @return new list of participants
	 */
	private ArrayList<RoundRobinPlayer> orderByColumn(int columnClicked)
	{
		ArrayList<RoundRobinPlayer> players = new ArrayList<RoundRobinPlayer>();
		for(int i=0;i<playerz.size();i++)
		{
			players.add(playerz.get(i));
		}
		//determine ascending or not
		if(currentSelection == columnClicked)
		{
			//column clicked again so swap order
			isAscending = !isAscending;
		}
		else
		{
			//new col clicked, so order ascending
			isAscending =true;
		}

		//save current selection
		this.currentSelection = columnClicked;
		switch(columnClicked)
		{
		case ORDER_BY_RANK:
			if(!isAscending)
			{
				Collections.reverse(players);
			}
			break;
		case ORDER_BY_NAME:
			Collections.sort(players, new ParticipantNameComparable(isAscending));
			break;
		case ORDER_BY_ROUND_WINS:
			Collections.sort(players, new ParticipantWinsComparable(isAscending));
			break;
		case ORDER_BY_ROUND_LOSSES:
			Collections.sort(players, new ParticipantLossesComparable(isAscending));
			break;
		case ORDER_BY_SCORE:
			Collections.sort(players, new ParticipantScoreComparable(isAscending));
			break;
		case ORDER_BY_ROUND_TIES:
			Collections.sort(players, new ParticipantTiesComparable(isAscending));
			break;

		}

		Log.d(LOG_TAG,"Ascending: "+isAscending);

		return players;
	}


	/**
	 * Sets up the popup help bubbles to cycle through
	 */
	private void setupHelpPopups()
	{
		DialogFragment warning;
		warning = new HelpDialog(HELP_TEXT_1);
		warning.show(getSupportFragmentManager(), "Help Dialog");

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.help:
			Log.d(LOG_TAG,"help clicked");
			//show help
			this.setupHelpPopups();	
			break;
		default:
			Log.d(LOG_TAG, "didnt find menu item");
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Comparable used for ranking according to player name
	 * @author waltzm
	 * @version 12/26/2012
	 */
	public class ParticipantNameComparable implements Comparator<RoundRobinPlayer>{

		/**
		 * Whether or not ascending
		 */
		private boolean ascending;

		/**
		 * Constructor for ranking on player rank
		 * @param ascending true if ascending
		 */
		public ParticipantNameComparable(boolean ascending)
		{
			this.ascending = ascending;
		}

		public int compare(RoundRobinPlayer p1, RoundRobinPlayer p2) 
		{
			int c = p1.getName().compareTo(p2.getName());
			if(ascending)
				return c;
			else
				return c*-1;

		}
	}

	/**
	 * Comparable used for ranking according to player wins
	 * @author waltzm
	 * @version 12/26/2012
	 */
	public class ParticipantWinsComparable implements Comparator<RoundRobinPlayer>{

		/**
		 * Whether or not ascending
		 */
		private boolean ascending;

		/**
		 * Constructor for ranking on player rank
		 * @param ascending true if ascending
		 */
		public ParticipantWinsComparable(boolean ascending)
		{
			this.ascending = ascending;
		}

		public int compare(RoundRobinPlayer p1, RoundRobinPlayer p2) 
		{		
			Integer i = getRoundWins(p1);
			Integer i2 = getRoundWins(p2);
			int c = i.compareTo(i2);
			if(!ascending)
				return c;
			else
				return c*-1;

		}
	}

	/**
	 * Calculates the number of round wins the player has so far
	 * @param p the swiss player
	 * @return the number of round wins
	 */
	private int getRoundWins(RoundRobinPlayer p)
	{
		int w=0;
		List<Match> matches = p.getMatchesPlayed();
		for(int i=0;i<matches.size();i++)
		{
			Match m = matches.get(i);
			if(m.getPlayerOne().equals(p)&&m.getMatchResult() == MatchResult.PLAYER_ONE)
			{
				w++;
			}
			else if(m.getPlayerTwo().equals(p)&&m.getMatchResult() == MatchResult.PLAYER_TWO)
			{
				w++;
			}
		}
		return w;
	}

	/**
	 * Comparable used for ranking according to player losses
	 * @author waltzm
	 * @version 12/26/2012
	 */
	public class ParticipantLossesComparable implements Comparator<RoundRobinPlayer>{

		/**
		 * Whether or not ascending
		 */
		private boolean ascending;

		/**
		 * Constructor for ranking on player rank
		 * @param ascending true if ascending
		 */
		public ParticipantLossesComparable(boolean ascending)
		{
			this.ascending = ascending;
		}

		public int compare(RoundRobinPlayer p1, RoundRobinPlayer p2) 
		{
			Integer i = getRoundLosses(p1);
			Integer i2 = getRoundLosses(p2);
			int c = i.compareTo(i2);
			if(!ascending)
				return c;
			else
				return c*-1;

		}
	}

	/**
	 * Calculates the number of round loses the player has so far
	 * @param p the swiss player
	 * @return number of round losses recorded
	 */
	private int getRoundLosses(RoundRobinPlayer p)
	{
		int l=0;
		List<Match> matches = p.getMatchesPlayed();
		for(int i=0;i<matches.size();i++)
		{
			Match m = matches.get(i);
			if(m.getPlayerOne().equals(p)&&m.getMatchResult() == MatchResult.PLAYER_TWO)
			{
				l++;
			}
			else if(m.getPlayerTwo().equals(p)&&m.getMatchResult() == MatchResult.PLAYER_ONE)
			{
				l++;
			}
		}
		return l;
	}

	/**
	 * Comparable used for ranking according to player losses
	 * @author waltzm
	 * @version 12/26/2012
	 */
	public class ParticipantTiesComparable implements Comparator<RoundRobinPlayer>{

		/**
		 * Whether or not ascending
		 */
		private boolean ascending;

		/**
		 * Constructor for ranking on player rank
		 * @param ascending true if ascending
		 */
		public ParticipantTiesComparable(boolean ascending)
		{
			this.ascending = ascending;
		}

		public int compare(RoundRobinPlayer p1, RoundRobinPlayer p2) 
		{
			Integer i = getRoundTies(p1);
			Integer i2 = getRoundTies(p2);
			int c = i.compareTo(i2);
			if(!ascending)
				return c;
			else
				return c*-1;

		}
	}

	/**
	 * Calculates the number of round loses the player has so far
	 * @param p the swiss player
	 * @return number of round losses recorded
	 */
	private int getRoundTies(RoundRobinPlayer p)
	{
		int t=0;
		List<Match> matches = p.getMatchesPlayed();
		for(int i=0;i<matches.size();i++)
		{
			Match m = matches.get(i);
			if(m.getMatchResult()==MatchResult.TIE)
			{
				t++;
			}
		}
		return t;
	}


	/**
	 * Comparable used for ranking according to player score
	 * @author waltzm
	 * @version 12/26/2012
	 */
	public class ParticipantScoreComparable implements Comparator<RoundRobinPlayer>{

		/**
		 * Whether or not ascending
		 */
		private boolean ascending;

		/**
		 * Constructor for ranking on player rank
		 * @param ascending true if ascending
		 */
		public ParticipantScoreComparable(boolean ascending)
		{
			this.ascending = ascending;
		}

		public int compare(RoundRobinPlayer p1, RoundRobinPlayer p2) 
		{
			Double i = p1.calculateScore();
			Double i2 = p2.calculateScore();
			int c = i.compareTo(i2);
			if(!ascending)
				return c;
			else
				return c*-1;

		}
	}

	/**
	 * This class is responsible for setting up the list of players to display in the list view
	 * @author waltzm
	 * @version 12/11/2012
	 */
	private class OverallStandingsAdapter extends ArrayAdapter<RoundRobinPlayer>{

		/**
		 * Holds the list of players
		 */
		private List<RoundRobinPlayer> players;

		/**
		 * Simple constructor to hide the annoying stuff
		 * @param context the application context
		 * @param textViewResourceId the list id
		 * @param players the players
		 */
		public OverallStandingsAdapter(Context context, int textViewResourceId, List<RoundRobinPlayer> players)
		{
			super(context, textViewResourceId, players);
			this.players = players;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			LayoutInflater inflater = getLayoutInflater();

			ImageView p1Portrait;
			if (convertView == null){
				convertView = inflater.inflate(R.layout.standings_overall_row, parent, false);
				p1Portrait = (ImageView)convertView.findViewById(R.id.prof_pic);
			} else {
				p1Portrait = (ImageView)convertView.findViewById(R.id.prof_pic);
				Object o = p1Portrait.getTag();
				if (o != null && o instanceof ImageDecodeTask){
					ImageDecodeTask t = (ImageDecodeTask)o;
					t.cancel(true);
				}

				convertView = inflater.inflate(R.layout.standings_overall_row, parent, false);
				p1Portrait = (ImageView)convertView.findViewById(R.id.prof_pic);
			}
			
			//Async load the portrait (p1)
			Player p1 = players.get(position);
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

			//setup player information
			TextView rank = (TextView)convertView.findViewById(R.id.rank_overall);

			rank.setText(players.get(position).getRank()+"");

			TextView name = (TextView)convertView.findViewById(R.id.name_overall);
			name.setText(""+players.get(position).getName());

			TextView wins = (TextView)convertView.findViewById(R.id.wins_overall);
			wins.setText(""+getRoundWins(players.get(position)));

			TextView losses = (TextView)convertView.findViewById(R.id.losses_overall);
			losses.setText(""+getRoundLosses(players.get(position)));

			TextView ties = (TextView)convertView.findViewById(R.id.ties_overall);
			ties.setText(""+getRoundTies(players.get(position)));

			TextView score = (TextView)convertView.findViewById(R.id.score_overall);

			//get and format text
			double pts = players.get(position).getScore();
			String ptz = pts+"";
			//round to 1 decimal place if over 10
			if(pts>=10)
			{
				ptz = roundOneDecimal(pts)+"";
			}
			else
			{
				ptz = roundTwoDecimal(pts)+"";
			}

			score.setText(ptz);

			convertView.invalidate();
			return convertView;
		}

		/**
		 * Sets the players
		 * Must notify this that stuff changed
		 * @param players the new player list
		 */
		public void setPlayers(ArrayList<RoundRobinPlayer> players)
		{
			this.players = players;
		}
	}


	/**
	 * Rounds decimal to one place
	 * @param d the double to round
	 * @return the rounded decimal
	 */
	public static String roundOneDecimal(double d) 
	{
		return String.format("%1$,.1f", d);
//		DecimalFormat twoDForm = new DecimalFormat("#.#");
//		twoDForm.setRoundingMode(RoundingMode.HALF_UP);
//		return Double.valueOf(twoDForm.format(d));
	}

	/**
	 * Rounds decimal to two place
	 * @param d the double to round
	 * @return the rounded decimal
	 */
	public static String roundTwoDecimal(double d) 
	{
		return String.format("%1$,.2f", d);
//		DecimalFormat twoDForm = new DecimalFormat("#.##");
//		twoDForm.setRoundingMode(RoundingMode.HALF_UP);
//		return Double.valueOf(twoDForm.format(d));
	}


	/**
	 * Makes the View look clickable by changing the background color
	 * when clicked. The onClick listener should be used to change
	 * the background color back to black for complete look.
	 * @author waltzm
	 * @version 12/27/12
	 */
	private class FakeButtonOnTouchListener implements OnTouchListener
	{

		public boolean onTouch(View arg0, MotionEvent event) 
		{
			if(event.getAction()==0)
			{
				arg0.setBackgroundColor(Color.GRAY);
			}
			else
			{
				arg0.setBackgroundColor(Color.BLACK);
			}
			return false;
		}

	}

}
