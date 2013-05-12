package utool.plugin.roundrobin;

import java.util.ArrayList;
import java.util.List;
import utool.plugin.ImageDecodeTask;
import utool.plugin.Player;
import utool.plugin.activity.TournamentContainer;
import utool.plugin.roundrobin.communication.IncomingCommandHandler;
import utool.plugin.roundrobin.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
/**
 * Fragment that hold the matches for a given round in a list
 * @author waltzm
 * @version 2/2/2013
 */
public class RoundFragment extends ListFragment
{
	/**
	 * Holds a reference to the round adapter
	 */
	private RoundAdapter ad;

	/**
	 * Holds the tournament id of the tournament to connect to
	 */
	protected long tid;

	/**
	 * Holds the round this is
	 */
	protected int round;

	/**
	 * Holds if the selected player is the first or second in the match
	 */
	protected boolean isSelP1 = true;

	/**
	 * Holds the log tag for this class
	 */
	private static final String LOG_TAG = "RoundFragment";

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		tid = getArguments().getLong("tournamentId");
		round = getArguments().getInt("round");
		//unselect player by default
		((TournamentActivity)getActivity()).selectedPlayerIndex = -1;
	}

	@Override
	public void onPause()
	{
		super.onPause();
		//close frame layout
		getActivity().findViewById(R.id.frameLayout).setVisibility(View.GONE);

		//un-select player
		((TournamentActivity)getActivity()).selectedPlayerIndex=-1;

	}

	/**
	 * Create a new instance of RoundFragment
	 * @param round the round of the fragment
	 * @param tid the tournament id
	 * @return created fragment
	 */
	public static RoundFragment newInstance(long tid,int round)
	{
		RoundFragment f = new RoundFragment();
		Bundle args = new Bundle();
		args.putLong("tournamentId", tid);
		args.putInt("round", round);
		f.setArguments(args);
		return f;
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) 
	{
		List<Round> rounds = ((RoundRobinTournament)TournamentContainer.getInstance(tid)).getRounds();
		if(rounds.size()>0)
		{
			List<Match> matches = rounds.get(round-1).getMatches();
			ad = new RoundAdapter(getActivity(),android.R.layout.simple_list_item_1, matches);
			setListAdapter(ad);
		}
		else
		{
			List<Match> matches = new ArrayList<Match>();
			ad = new RoundAdapter(getActivity(),android.R.layout.simple_list_item_1, matches);
			setListAdapter(ad);
		}
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	/**
	 * Updates the information stored/displayed
	 */
	public void update()
	{
		this.getActivity().runOnUiThread(new Runnable(){

			@Override
			public void run() {
				List<Match> matches = ((RoundRobinTournament)TournamentContainer.getInstance(tid)).getRounds().get(round-1).getMatches();

				ad.setMatches(matches);
				ad.notifyDataSetChanged();
			}
			
		});
		
	}

	/**
	 * This class is responsible for setting up the list of players to display in the list view
	 * @author waltzm
	 * @version 12/11/2012
	 */
	private class RoundAdapter extends ArrayAdapter<Match>{

		/**
		 * Holds the list of matches
		 */
		private List<Match> matches;

		/**
		 * Simple constructor to hide the annoying stuff
		 * @param context the application context
		 * @param textViewResourceId the list id
		 * @param matches2 the matches of the round
		 */
		public RoundAdapter(Context context, int textViewResourceId, List<Match> matches2)
		{
			super(context, textViewResourceId, matches2);
			this.matches = matches2;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			LayoutInflater inflater = getLayoutInflater(getArguments());

			ImageView p1Portrait;
			ImageView p2Portrait;
			if (convertView == null){
				convertView = inflater.inflate(R.layout.matchup_row, parent, false);
				p1Portrait = (ImageView)convertView.findViewById(R.id.player_one_portrait);
				p2Portrait = (ImageView)convertView.findViewById(R.id.player_two_portait);
			} else {
				p1Portrait = (ImageView)convertView.findViewById(R.id.player_one_portrait);
				Object o = p1Portrait.getTag();
				if (o != null && o instanceof ImageDecodeTask){
					ImageDecodeTask t = (ImageDecodeTask)o;
					t.cancel(true);
				}
				
				p2Portrait = (ImageView)convertView.findViewById(R.id.player_two_portait);
				o = p2Portrait.getTag();
				if (o != null && o instanceof ImageDecodeTask){
					ImageDecodeTask t = (ImageDecodeTask)o;
					t.cancel(true);
				}

				convertView = inflater.inflate(R.layout.matchup_row, parent, false);
				p1Portrait = (ImageView)convertView.findViewById(R.id.player_one_portrait);
				p2Portrait = (ImageView)convertView.findViewById(R.id.player_two_portait);
			}
			
			//Async load the portrait (player 1)
			Player p1 = matches.get(position).getPlayerOne();
			
			if (p1.hasPortraitChanged()){
				p1Portrait.setImageResource(R.drawable.silhouette);
				ImageDecodeTask task = new ImageDecodeTask(p1Portrait);
				p1Portrait.setTag(task);
				task.execute(p1);
			} else {
				Bitmap bm = p1.getPortrait();
				if (bm != null && !bm.isRecycled()){
					p1Portrait.setImageBitmap(bm);
				} else {
					p1Portrait.setImageResource(R.drawable.silhouette);
				}
			}
			//end async load
			
			//Async load the portrait (player 2)
			Player p2 = matches.get(position).getPlayerTwo();
			
			if (p2.hasPortraitChanged()){
				p2Portrait.setImageResource(R.drawable.silhouette);
				ImageDecodeTask task = new ImageDecodeTask(p2Portrait);
				p1Portrait.setTag(task);
				task.execute(p2);
			} else {
				Bitmap bm = p2.getPortrait();
				if (bm != null && !bm.isRecycled()){
					p2Portrait.setImageBitmap(bm);
				} else {
					p2Portrait.setImageResource(R.drawable.silhouette);
				}
			}
			//end async load

			convertView.setOnClickListener(new RowClickListener(position));
			convertView.setClickable(true);

			//set background
			String local = TournamentContainer.getInstance(tid).getPID().toString();
			if(matches.get(position).getPlayerOne().getUUID().toString().equals(local)||matches.get(position).getPlayerTwo().getUUID().toString().equals(local) )
			{
				((ImageView)convertView.findViewById(R.id.background_row)).setBackgroundResource(R.drawable.blue_row);
			}
			else
			{
				if((position & 1) == 1)//checking for oddness for position
				{
					((ImageView)convertView.findViewById(R.id.background_row)).setBackgroundResource(R.drawable.gray_row);
				}
				else
				{
					((ImageView)convertView.findViewById(R.id.background_row)).setBackgroundResource(R.drawable.black_row);
				}
			}

			//Set Player names
			TextView name = (TextView)convertView.findViewById(R.id.player_one_name);
			name.setText(""+matches.get(position).getPlayerOne().getName());

			TextView tname = (TextView)convertView.findViewById(R.id.player_two_name);
			tname.setText(""+matches.get(position).getPlayerTwo().getName());

			//Set Player scores
			TextView s1 = (TextView)convertView.findViewById(R.id.player_one_score);
			s1.setText(""+matches.get(position).getPlayerOneScore());

			TextView s2 = (TextView)convertView.findViewById(R.id.player_two_score);
			s2.setText(""+matches.get(position).getPlayerTwoScore());

			//Make it so that players can be moved around
			name.setOnLongClickListener(new PlayerOnClickListener(position, true));
			tname.setOnLongClickListener(new PlayerOnClickListener(position, false));

			//setup color of textfields: yellow if selected, transparent otherwise
			if(position == ((TournamentActivity)getActivity()).selectedPlayerIndex)
			{
				if(isSelP1)
				{
					//set name to look selected
					name.setBackgroundColor(Color.parseColor("#00C3F7"));//blueish
					name.setTextColor(Color.WHITE);
					tname.setBackgroundColor(Color.TRANSPARENT);
					tname.setTextColor(Color.BLACK);
				}
				else
				{
					tname.setBackgroundColor(Color.parseColor("#00C3F7"));//blueish
					tname.setTextColor(Color.WHITE);
					name.setBackgroundColor(Color.TRANSPARENT);
					name.setTextColor(Color.BLACK);
				}
			}
			else
			{
				tname.setBackgroundColor(Color.TRANSPARENT);
				tname.setTextColor(Color.BLACK);
				name.setBackgroundColor(Color.TRANSPARENT);
				name.setTextColor(Color.BLACK);
			}

			name.setOnClickListener(new NameClickListener(position, true));
			tname.setOnClickListener(new NameClickListener(position, false));

			//set player star
			convertView.findViewById(R.id.player_one_star).setVisibility(ImageView.INVISIBLE);
			convertView.findViewById(R.id.player_two_star).setVisibility(ImageView.INVISIBLE);

			if(matches.get(position).getMatchResult().equals(MatchResult.PLAYER_ONE))
			{
				//player 1 won
				ImageView star = (ImageView)convertView.findViewById(R.id.player_one_star);
				star.setVisibility(ImageView.VISIBLE);
			}
			else if(matches.get(position).getMatchResult().equals(MatchResult.PLAYER_TWO))
			{
				//player 2 won
				ImageView star = (ImageView)convertView.findViewById(R.id.player_two_star);
				star.setVisibility(ImageView.VISIBLE);
			}
			
			//Set match id
			TextView mid = (TextView)convertView.findViewById(R.id.mid);
			mid.setText((position+1)+"");

			convertView.invalidate();
			return convertView;

		}

		/**
		 * Listener for a row click
		 * @author waltzm
		 * @version 1/31/2013
		 */
		private class RowClickListener implements OnClickListener
		{
			/**
			 * Holds the match id
			 */
			private int match_id;

			/**
			 * Constructor
			 * @param mid the id of the match
			 */
			public RowClickListener(int mid)
			{
				this.match_id=  mid;
			}
			@Override
			public void onClick(View arg0) 
			{
				TournamentActivity a = (TournamentActivity) getActivity();
				a.goToScores(match_id);
			}

		}

		/**
		 * Listener for a textview click. Performs the same as row click
		 * However if something is selected, behaves differently
		 * @author waltzm
		 * @version 3/9/2013
		 */
		protected class NameClickListener implements OnClickListener
		{
			/**
			 * Holds the match id
			 */
			private int mid;

			/**
			 * Holds if the player is first in the slot
			 */
			private boolean isFirstPlayer;

			/**
			 * Constructor
			 * @param mid the id of the match
			 * @param isFirstPlayer if the textview is in the first or second slot
			 */
			public NameClickListener(int mid,boolean isFirstPlayer)
			{
				this.mid=  mid;
				this.isFirstPlayer = isFirstPlayer;
			}
			@Override
			public void onClick(View arg0) 
			{
				//Close up the frame layout
				getActivity().findViewById(R.id.frameLayout).setVisibility(View.GONE);

				if(((TournamentActivity)getActivity()).selectedPlayerIndex>-1)
				{
					//switch player/unselect player
					onSecondPlayerClick(mid,isFirstPlayer);
				}
				else
				{
					//go to scores
					TournamentActivity a = (TournamentActivity) getActivity();
					a.goToScores(mid);
				}
			}

		}


		/**
		 * Sets the players
		 * Must notify this that stuff changed
		 * @param matches2 the new matches
		 */
		public void setMatches(List<Match> matches2)
		{
			this.matches = matches2;
		}
	}

	/**
	 * OnClickListener for when a player is selected. This allows for moving around of players
	 * @author waltzm
	 * @version 3/9/2013
	 */
	private class PlayerOnClickListener implements OnLongClickListener
	{

		/**
		 * Holds the place in Matches of the player
		 */
		private int pos;

		/**
		 * True if p1, false if p2 in the match
		 */
		private boolean isP1;

		/**
		 * Creates a player on click listener for the position
		 * @param position the position of the match the player is in
		 * @param isFirstPlayer true if the player is p1, false if p2
		 */
		public PlayerOnClickListener(int position, boolean isFirstPlayer)
		{
			this.pos = position;	
			this.isP1 = isFirstPlayer;
		}

		@Override
		public boolean onLongClick(View v) 
		{
			RoundRobinTournament tourny = ((RoundRobinTournament)TournamentContainer.getInstance(tid));
			//Log.e(LOG_TAG,"In on click; Onclick of pos:"+pos);
			//If the player isn't host, they can't access this functionality
			if(tourny.getPermissionLevel()!=Player.HOST)
			{
				return false;
			}
			//if a player isn't already selected, select this player
			if(((TournamentActivity)getActivity()).selectedPlayerIndex <0)
			{
				if(round ==tourny.getRounds().size())
				{
					((TournamentActivity)getActivity()).selectedPlayerIndex = pos;
					isSelP1 = isP1;


					//Open up the frame layout
					getActivity().findViewById(R.id.frameLayout).setVisibility(View.VISIBLE);
					//setup the button listener
					Button b = (Button) getActivity().findViewById(R.id.newMatch);
					b.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) 
						{
							RoundRobinTournament tourny = ((RoundRobinTournament)TournamentContainer.getInstance(tid));
							List<Match> m = tourny.getRounds().get(tourny.getRounds().size()-1).getMatches();


							//remove selected player from current match
							Match temp =  m.get(((TournamentActivity)getActivity()).selectedPlayerIndex);
							if(isSelP1)
							{
								//check if second is BYE
								if(temp.getPlayerTwo().getUUID().equals(Player.BYE))
								{
									//would make a double bye, therefore remove match completely
									m.remove(((TournamentActivity)getActivity()).selectedPlayerIndex);
								}
								else
								{
									m.set(((TournamentActivity)getActivity()).selectedPlayerIndex, new Match(new RoundRobinPlayer(new Player(Player.BYE, "BYE")), temp.getPlayerTwo(), tourny.getRounds().get(tourny.getRounds().size()-1)));
								}
							}
							else
							{
								//check if first is BYE
								if(temp.getPlayerOne().getUUID().equals(Player.BYE))
								{
									//would make a double bye, therefore remove match completely
									m.remove(((TournamentActivity)getActivity()).selectedPlayerIndex);
								}
								else
								{
									m.set(((TournamentActivity)getActivity()).selectedPlayerIndex, new Match(temp.getPlayerOne(), RoundRobinPlayer.BYE, tourny.getRounds().get(tourny.getRounds().size()-1)));
								}
							}
							//take selected player and put in new match against bye
							if(isSelP1)
							{
								//Make sure selected player isn't a BYE
								if(!temp.getPlayerOne().equals(RoundRobinPlayer.BYE))
								{
									m.add(new Match(temp.getPlayerOne(), RoundRobinPlayer.BYE, tourny.getRounds().get(tourny.getRounds().size()-1)));

								}	
							}
							else
							{
								//Make sure selected player isn't a BYE
								if(!temp.getPlayerTwo().equals(RoundRobinPlayer.BYE))
								{
									m.add(new Match(temp.getPlayerTwo(), RoundRobinPlayer.BYE, tourny.getRounds().get(tourny.getRounds().size()-1)));
								}
							}
							//notify of update
							tourny.notifyChanged();
							//close frame layout
							getActivity().findViewById(R.id.frameLayout).setVisibility(View.GONE);

							//un-select player
							((TournamentActivity)getActivity()).selectedPlayerIndex=-1;

							//notify connected players of the change
							IncomingCommandHandler inc = new IncomingCommandHandler(tourny);
							inc.handleReceiveError(TournamentActivity.RESEND_ERROR_CODE, "", "", "");
						}

					});
					ad.notifyDataSetChanged();
				}

			}
			else 
			{
				//Close up the frame layout
				getActivity().findViewById(R.id.frameLayout).setVisibility(View.GONE);
				//one player already selected
				onSecondPlayerClick(pos, isP1);
			}
			return true;

		}
	}

	/**
	 * Holds for onclick
	 */
	protected int savedPos;

	/**
	 * Holds for onclick
	 */
	protected boolean savedP1;

	/**
	 * Performs needed action if a second player is selected (the switch)
	 * @param pos the position of the second player
	 * @param p1 if the second player is p1
	 */
	protected void onSecondPlayerClick(int pos, boolean p1)
	{
		Log.e(LOG_TAG,"Second click: "+pos +" and "+p1+" [and] "+((TournamentActivity)getActivity()).selectedPlayerIndex +" and "+isSelP1);

		this.savedP1=p1;
		this.savedPos=pos;
		RoundRobinTournament tourny = ((RoundRobinTournament)TournamentContainer.getInstance(tid));

		if(((TournamentActivity)getActivity()).selectedPlayerIndex == pos && isSelP1 == p1)
		{
			//unselect current player
			((TournamentActivity)getActivity()).selectedPlayerIndex = -1;				
			//Log.e(LOG_TAG,"Unselect; Onclick of pos:"+pos);
			if(ad!=null)
				ad.notifyDataSetChanged();
		}
		else
		{
			//else switch this player and the selected player

			if(tourny.getRounds().size()>0)
			{
				//User verification if scores have been set
				List<Round> rounds = tourny.getRounds();
				List<Match> matches = rounds.get(round-1).getMatches();
				if(matches.get(pos).getMatchResult()!=MatchResult.UNDECIDED || matches.get(((TournamentActivity)getActivity()).selectedPlayerIndex).getMatchResult()!=MatchResult.UNDECIDED)
				{
					new AlertDialog.Builder(getActivity())
					.setTitle("Warning")
					.setMessage("Completing this switch will erase the scores aready set. Do you want to continue with this action?")
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int whichButton) 
						{
							doSwitch(savedPos, savedP1);		
						}})
						.setNegativeButton("No",  new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int whichButton) 
							{
								//un-select on the cancel
								((TournamentActivity)getActivity()).selectedPlayerIndex = -1;	
								ad.notifyDataSetChanged();
							}}).show();
				}
				else
				{
					doSwitch(pos, p1);	
				}				
			}
		}
		if(ad!=null)
			ad.notifyDataSetChanged();

		Log.d(LOG_TAG, "Rounds:"+((RoundRobinTournament)TournamentContainer.getInstance(tid)).getRounds().get(round-1).getMatches());
	}

	/**
	 * Private method for handling the switching of the players
	 * @param pos the position of the player
	 * @param p1 if the player is first player or not
	 */
	protected void doSwitch(int pos, boolean p1)
	{
		Log.e(LOG_TAG,"Switch: "+pos +" and "+p1+" [and] "+((TournamentActivity)getActivity()).selectedPlayerIndex +" and "+isSelP1);

		RoundRobinTournament tourny = ((RoundRobinTournament)TournamentContainer.getInstance(tid));
		//perform the switch
		if(!tourny.switchPlayers(pos,p1,((TournamentActivity)getActivity()).selectedPlayerIndex,isSelP1, round-1))
		{
			//notify user matchups are unideal
			getActivity().runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(getActivity(), "Warning: Swapping of players has resulted in non-ideal matchups.", Toast.LENGTH_SHORT).show();
				}
			});
		}
		if(ad!=null)
			ad.notifyDataSetChanged();

		((TournamentActivity)getActivity()).selectedPlayerIndex = -1;	
	}
}