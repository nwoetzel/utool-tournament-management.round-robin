package utool.plugin.roundrobin;

import java.util.ArrayList;
import utool.plugin.Player;
import utool.plugin.activity.AbstractPluginCommonActivity;
import utool.plugin.activity.TournamentContainer;
import utool.plugin.roundrobin.TournamentActivity.HelpDialog;
import utool.plugin.roundrobin.communication.OutgoingCommandHandler;
import utool.plugin.roundrobin.R;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Tab responsible for adjusting player permissions
 * Should be accessible by hosts only
 * @author waltzm and kreierj
 * @version 4/20/2013
 */
public class OptionsPlayerTab extends AbstractPluginCommonActivity {

	/**
	 * Holds the arrayAdapter
	 */
	private OptionsPlayersAdapter ad;

	/**
	 * Holds reference to the tournament
	 */
	private RoundRobinTournament t;

	/**
	 * Holds the logger text for this activity
	 */
	private static final String LOG_TAG = "Options Player Tab";

	/**
	 * first help text of the options player tab
	 */
	private static final String HELP_TEXT_1="This tab allows you to adjust the connected player's permissions. Players with moderator permissions are able to record scores on their device.";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.roundrobin_options_player_tab);


		//get list of players
		t = (RoundRobinTournament) TournamentContainer.getInstance(getTournamentId());
		ArrayList<Player> players = t.getPlayers();

		//Shorten the player list to only include players with permission level of participant or moderator
		ArrayList<Player> parts = new ArrayList<Player>();
		Log.e(LOG_TAG ,"Playerz: ");
		for(int i=0;i<players.size();i++)
		{
			Log.e(LOG_TAG ,players.get(i).getName()+": "+players.get(i).getPermissionsLevel());
			if(players.get(i).getPermissionsLevel()==Player.PARTICIPANT||players.get(i).getPermissionsLevel()==Player.MODERATOR)
			{
				//add to list
				parts.add(players.get(i));
			}
		}

		//hide either no_player_text or Player and Moderator
		if(parts.size()<1)
		{
			//no players
			findViewById(R.id.no_player_text).setVisibility(View.VISIBLE);
			findViewById(R.id.op_mod_header).setVisibility(View.INVISIBLE);
			findViewById(R.id.op_player_header).setVisibility(View.INVISIBLE);
		}
		else
		{
			//players
			//Don't want the no_player_text taking up room in the layout
			findViewById(R.id.no_player_text).setVisibility(View.GONE);
			findViewById(R.id.op_mod_header).setVisibility(View.VISIBLE);
			findViewById(R.id.op_player_header).setVisibility(View.VISIBLE);
		}

		//setup adapter
		ListView l = (ListView)findViewById(R.id.option_list);
		ad=new OptionsPlayersAdapter(this, R.id.option_list, parts);
		l.setAdapter(ad);
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		//save all of the permissions done automatically since player list is updated
		//re-send player list with updated permissions to connected devices
		ArrayList<Player> p = t.getPlayers();//ad.getPlayers();

		Player[] l = new Player[p.size()];

		for(int i=0;i<p.size();i++)
		{
			l[i] = p.get(i);
		}

		//notifies connected of permission change
		OutgoingCommandHandler och = new OutgoingCommandHandler(t);
		och.handleSendPlayers(getTournamentId(), l);
	}

	/**
	 * Sets up the popup help bubbles to cycle through
	 */
	private void setupHelpPopups() 
	{
		DialogFragment warning;

		warning = new HelpDialog(HELP_TEXT_1);
		warning.show(getSupportFragmentManager(), "Help Dialog for Player Options");
	}


	/**
	 * This class is responsible for setting up the list of players to display in the list view
	 * @author waltzm
	 * @version 1/29/2013
	 */
	private class OptionsPlayersAdapter extends ArrayAdapter<Player>{

		/**
		 * Holds the list of players
		 */
		private ArrayList<Player> players;

		/**
		 * Simple constructor to hide the annoying stuff
		 * @param context the application context
		 * @param textViewResourceId the list id
		 * @param players the players
		 */
		public OptionsPlayersAdapter(Context context, int textViewResourceId, ArrayList<Player> players)
		{
			super(context, textViewResourceId, players);
			this.players = players;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			LayoutInflater inflater = getLayoutInflater();
			View row = inflater.inflate(R.layout.roundrobin_options_player_row, parent, false);

			// set name to players name
			TextView pName = (TextView)row.findViewById(R.id.se_options_name);
			pName.setText(players.get(position).getName());

			//set the player image to the imageview
			ImageView pPic = (ImageView)row.findViewById(R.id.se_options_pic);
			if(players.get(position).getPortrait()!=null)
			{
				pPic.setImageBitmap((players.get(position).getPortrait()));
			}

			//set the checkbox listener
			final CheckBox pCheck = (CheckBox)row.findViewById(R.id.se_options_check);
			pCheck.setOnCheckedChangeListener(new OnCheckChangedListener_Options(players.get(position)));
			int p = players.get(position).getPermissionsLevel();
			if(p==Player.HOST)
			{
				//checked and unchangeable
				pCheck.setChecked(true);
				//pCheck.setClickable(false);
				pCheck.setEnabled(false);

				pCheck.setOnClickListener(new OnClickListener(){

					public void onClick(View v) {
						pCheck.setChecked(true);
					}

				});

			}
			else if(p==Player.PARTICIPANT)
			{
				//unchecked
				pCheck.setChecked(false);
			}
			else if(p == Player.MODERATOR)
			{
				//checked
				pCheck.setChecked(true);

			}
			pCheck.invalidate();
			row.invalidate();
			return row;
		}
	}

	/**
	 * Custom listener to update the player based on if the check box is checked
	 * If checked the player is set to Moderator, otherwise it is set to Participant
	 * @author waltzm
	 *
	 */
	private class OnCheckChangedListener_Options implements OnCheckedChangeListener
	{

		/**
		 * The player to update
		 */
		private Player player;

		/**
		 * Creates a OnCheckListener responsible for keeping the permissions
		 * of Player p in synch with the checkboxes
		 * @param player the player to be edited
		 */
		public OnCheckChangedListener_Options(Player player)
		{
			this.player = player;
		}

		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
		{
			if(isChecked)
			{
				player.setPermissionsLevel(Player.MODERATOR);
			}
			else
			{
				player.setPermissionsLevel(Player.PARTICIPANT);
			}

		}

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
			this.setupHelpPopups();
		default:
			return super.onOptionsItemSelected(item);
		}
	}


}