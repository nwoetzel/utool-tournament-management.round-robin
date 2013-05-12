package utool.plugin.roundrobin;

import java.util.ArrayList;
import java.util.List;
import utool.plugin.activity.AbstractPluginCommonActivity;
import utool.plugin.activity.TournamentContainer;
import utool.plugin.roundrobin.TournamentActivity.HelpDialog;
import utool.plugin.roundrobin.R;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Round robin Tournament Options Screen for Participants
 * Decide Ties
		List of possible tie breakers
		Able to select/order tie breakers to use
		Default: Head to Head,run differential, lazy 
 * @author waltzm and kreierj
 * @version 4/20/2013
 */
public class OptionsTournamentTabPart extends AbstractPluginCommonActivity 
{

	/**
	 * Holds the arrayAdapter for tie breaking
	 */
	private OptionsTieBreakerAdapter ad;


	/**
	 * Holds the configuration object
	 */
	private RoundRobinConfiguration conf;

	/**
	 * Holds the tie help text string
	 */
	private static final String TIE_HELP= "All selected tie breakers will be used in descending order.\n"+
	 "Head to Head: If the players have versed each other, the winner will win the tie.\n"+
	 "Run Differential: The player who scored higher cumulatively in the tied players matchups.\n" +
	 "Lazy: Winner is randomly selected.";
	

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.roundrobin_options_tournament_part);

		//pull out configuration object
		conf = ((RoundRobinTournament)TournamentContainer.getInstance(getTournamentId())).getRoundRobinConfiguration();

		//Set tie handling
		List<Integer> ties = conf.getTieBreakers();
		int place = ties.size()-1;
		//ties holds all of the tie breakers being used
		//now to add the rest that aren't checked
		ArrayList<Integer> clone = new ArrayList<Integer>();
		clone.addAll(ties);
		clone.addAll(conf.getUnusedtieBreakers());

		//setup adapter
		ListView l = (ListView)findViewById(R.id.tie_list_view);
		ad=new OptionsTieBreakerAdapter(this, R.id.option_list,place, clone );
		l.setAdapter(ad);

		//Register Items for context menu
		View t = findViewById(R.id.tie_tv);
		registerForContextMenu(t);


	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		//instead of bringing up a menu, show a dialog of explanations
		HelpDialog warning;
		if(v.getId() == R.id.tie_tv)
		{
			warning = new HelpDialog(TIE_HELP);
			warning.show(getSupportFragmentManager(), "Explaination of Tie Handling");
		}
	}

	@Override
	public void onPause()
	{
		super.onPause();

		//Set tie handling
		conf.setTieBreakers(ad.getListTieBreakers(),ad.getListUnusedTieBreakers());

	}

	/**
	 * Displays the help messages for the user
	 */
	private void setupHelp() 
	{
		// Create and show the help dialog.
		final Dialog dialog = new Dialog(OptionsTournamentTabPart.this);
		dialog.setContentView(R.layout.roundrobin_options_help_part);
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
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.help:
			this.setupHelp();
		default:
			return super.onOptionsItemSelected(item);
		}
	}


	/**
	 * This class is responsible for setting up the list of tie breakers to display in the list view
	 * @author waltzm
	 * @version 1/29/2013
	 */
	private class OptionsTieBreakerAdapter extends ArrayAdapter<Integer>
	{

		/**
		 * Holds the list of players
		 */
		private ArrayList<Integer> tieBreakers;

		/**
		 * Holds the point in tieBreakers of the last checked one
		 */
		private int place;

		/**
		 * Simple constructor to hide the annoying stuff
		 * @param context the application context
		 * @param textViewResourceId the list id
		 * @param place the place in the array of the last checked tie
		 * @param tieBreakers the full list of tiebreakers
		 */
		public OptionsTieBreakerAdapter(Context context, int textViewResourceId, int place, ArrayList<Integer> tieBreakers)
		{
			super(context, textViewResourceId, tieBreakers);
			this.tieBreakers = tieBreakers;
			this.place = place;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			LayoutInflater inflater = getLayoutInflater();
			View row = inflater.inflate(R.layout.roundrobin_options_tie_row, parent, false);

			TextView tName = (TextView)row.findViewById(R.id.tie_option_name);
			tName.setText(RoundRobinConfiguration.getTieBreakerName(tieBreakers.get(position)));


			//set the checkbox listener
			final CheckBox pCheck = (CheckBox)row.findViewById(R.id.tie_check_box);

			//update checkedness of the tieBreaker
			if(position<=place)
			{
				pCheck.setChecked(true);
			}
			else
			{
				pCheck.setChecked(false);
			}
			pCheck.setOnCheckedChangeListener(new OnCheckChangedListener_Tie_Options(tieBreakers.get(position)));

			//up and down arrows
			ImageButton up = (ImageButton)row.findViewById(R.id.options_tie_increase);
			up.setOnClickListener(new OnIncreaseClickListener(tieBreakers.get(position)));

			ImageButton down = (ImageButton)row.findViewById(R.id.options_tie_decrease);
			down.setOnClickListener(new OnDecreaseClickListener(tieBreakers.get(position)));
			if(position>place)
			{
				//disable up and down arrows and the name since turned off
				down.setEnabled(false);
				up.setEnabled(false);
				row.setEnabled(false);
				tName.setEnabled(false);
			}

			pCheck.invalidate();
			row.invalidate();
			return row;
		}

		/**
		 * Getter for the selected tie breakers in order
		 * @return list of used tie breakers in order
		 */
		public List<Integer> getListTieBreakers()
		{
			ArrayList<Integer> clone = new ArrayList<Integer>();
			for(int i=0;i<=place;i++)
			{
				clone.add(tieBreakers.get(i));
			}
			return clone;
		}

		/**
		 * Getter for the unselected tie breakers
		 * @return list of unused tiebreakers
		 */
		public List<Integer> getListUnusedTieBreakers()
		{
			ArrayList<Integer> clone = new ArrayList<Integer>();
			for(int i=place+1;i<tieBreakers.size();i++)
			{
				clone.add(tieBreakers.get(i));
			}
			return clone;
		}

		/**
		 * Updates the list of tiebreakers based on the check status.
		 * Checked items are near the top of the list.
		 * unchecked items are placed to the bottom
		 * @param isChecked whether or not the tiebreaker is checked
		 * @param tieBreaker the tiebreaker to update
		 */
		protected void changeCheckTieBreaker(boolean isChecked, int tieBreaker)
		{
			//remove old place of the tie breaker
			int index =tieBreakers.indexOf(tieBreaker);
			tieBreakers.remove(index);

			if(isChecked)
			{			
				//move to bottom of checked list
				place++;
				tieBreakers.add(place,tieBreaker);
			}
			else
			{
				//move to bottom of full list
				tieBreakers.add(tieBreaker);
				if(index<=place)
				{
					place--;
				}
			}

			this.notifyDataSetChanged();
		}

		/**
		 * Signifies up arrow pressed on th tiebreaker passed in
		 * @param tieBreaker the tiebreaker
		 */
		protected void upPressed(int tieBreaker) 
		{
			int index =tieBreakers.indexOf(tieBreaker);
			//make sure not at top of unchecked or at top of list
			if(index!=place+1&&index>0)
			{
				tieBreakers.remove(index);
				tieBreakers.add(index-1,tieBreaker);
			}
			this.notifyDataSetChanged();
		}

		/**
		 * Signifies down arrow pressed on the tiebreaker passed in
		 * @param tieBreaker the tiebreaker
		 */
		protected void downPressed(int tieBreaker) 
		{
			int index =tieBreakers.indexOf(tieBreaker);
			//make sure not at ottom of checked, or at bottom of list
			if(index!=place&&index<tieBreakers.size()-1)
			{
				tieBreakers.remove(index);
				tieBreakers.add(index+1,tieBreaker);
			}
			this.notifyDataSetChanged();
		}
	}

	/**
	 * Click Listener for the up arrow on a tie breaker.
	 * @author waltzm
	 * @version 1/30/2013
	 */
	private class OnIncreaseClickListener implements OnClickListener
	{
		/**
		 * Holds the tiebreaker in the row
		 */
		private int tieBreaker;

		/**
		 * Creates the listener
		 * @param tieBreaker the tie breaker of this row
		 */
		OnIncreaseClickListener(int tieBreaker)
		{
			this.tieBreaker = tieBreaker;
		}

		@Override
		public void onClick(View arg0) {
			ad.upPressed(tieBreaker);	
		}
	}

	/**
	 * Click Listener for the down arrow on a tie breaker.
	 * @author waltzm
	 * @version 1/30/2013
	 */
	private class OnDecreaseClickListener implements OnClickListener
	{
		/**
		 * Holds the tiebreaker in the row
		 */
		private int tieBreaker;

		/**
		 * Creates the listener
		 * @param tieBreaker the tie breaker of this row
		 */
		OnDecreaseClickListener(int tieBreaker)
		{
			this.tieBreaker = tieBreaker;
		}

		@Override
		public void onClick(View arg0) {
			ad.downPressed(tieBreaker);	
		}
	}
	/**
	 * Custom listener to update the player based on if the check box is checked
	 * If checked the player is set to Moderator, otherwise it is set to Participant
	 * @author waltzm
	 *
	 */
	private class OnCheckChangedListener_Tie_Options implements OnCheckedChangeListener
	{

		/**
		 * The position of the tie breaker to update
		 */
		private int tieBreaker;

		/**
		 * Creates a OnCheckListener responsible for a tiebreaker
		 * @param tieBreaker the tieBreaker of this listener
		 */
		public OnCheckChangedListener_Tie_Options(int tieBreaker)
		{
			this.tieBreaker = tieBreaker;
		}

		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
		{
			ad.changeCheckTieBreaker(isChecked, tieBreaker);
		}

	}

}