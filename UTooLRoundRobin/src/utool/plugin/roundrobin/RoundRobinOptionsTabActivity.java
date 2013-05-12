package utool.plugin.roundrobin;

import java.util.List;

import utool.plugin.activity.AbstractPluginCommonTabActivity;
import utool.plugin.roundrobin.R;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

/**
 * Main activity for the tabs.
 * This class intentionally uses the deprecated Tabs instead of fragments due to their
 * relative ease of use, and better look
 * @author waltzm and kreierj
 * @version 4/20/2013
 */
public class RoundRobinOptionsTabActivity extends AbstractPluginCommonTabActivity {

	//tabs are deprecated, but look better/easier to use than the replacement
	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.roundrobin_options_main);

		//Hide keyboard
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		
		Resources ressources = getResources(); 
		TabHost tabHost = getTabHost(); 

		// Tournament Tab
		Intent intentSwiss = ((Intent) getIntent().clone()).setClass(this, OptionsTournamentTab.class);
		TabSpec tabSpecSwiss = tabHost
				.newTabSpec("Tournament")
				.setIndicator("", ressources.getDrawable(R.drawable.tournament_tab_icon))
				.setContent(intentSwiss);

		// Email tab
		Intent intentEmail = ((Intent) getIntent().clone()).setClass(this, OptionsEmailTab.class);
		TabSpec tabSpecEmail = tabHost
				.newTabSpec("Email")
				.setIndicator("", ressources.getDrawable(R.drawable.email_tab_icon))
				.setContent(intentEmail);

		// Players tab
		Intent intentPlayers =((Intent) getIntent().clone()).setClass(this, OptionsPlayerTab.class);
		TabSpec tabSpecPlayers = tabHost
				.newTabSpec("Players")
				.setIndicator("", ressources.getDrawable(R.drawable.players_tab_icon))
				.setContent(intentPlayers);


		// add all tabs 
		tabHost.addTab(tabSpecSwiss);
		tabHost.addTab(tabSpecPlayers);
		tabHost.addTab(tabSpecEmail);


		//set Windows tab as default (zero based)
		tabHost.setCurrentTab(0);
	}

	/**
	 * Returns a String representation of the tournament matches without html formating
	 * @param t reference to the tournament
	 * @return tournament matchups so far
	 */
	public static String getTournamentDataText(RoundRobinTournament t) 
	{
		String tdata = "";
		try
		{
		List<Round> rounds = t.getRounds();
		//for each write out results

		for(int r = 0;r<rounds.size();r++)
		{
			List<Match> m = rounds.get(r).getMatches();

			//round statement
			tdata+="Round "+(r+1)+": "+"\n";
			//matchups in round
			for(int i=0; i<m.size();i++)
			{
				//determine if a name should be bolded
				Match c = m.get(i);
				if(c.getMatchResult()==MatchResult.PLAYER_ONE)
				{
					//player 1 is winner therefore bold p1
					tdata+=c.getPlayerOne().getName() +" vs. "+c.getPlayerTwo().getName() +"\n";	
				}
				else if(c.getMatchResult()==MatchResult.PLAYER_TWO)
				{
					//player 2 is winner therefore bold p2
					tdata+=c.getPlayerOne().getName() +" vs. "+c.getPlayerTwo().getName()+"\n";	
				}
				else
				{
					//no winner
					tdata+=c.getPlayerOne().getName() +" vs. "+c.getPlayerTwo().getName()+"\n";
				}

			}

		}
		}
		catch(Exception e)
		{
			//problem in handeling
			tdata= "Error in processing Tournament state";
		}

		return tdata;
	}

	/**
	 * Returns a String representation of the tournament matches with html formating
	 * Winners will be bolded
	 * @param t reference to the tournament
	 * @return tournament matchups so far
	 */
	public static String getTournamentData(RoundRobinTournament t) 
	{
		String tdata = "";

		List<Round> rounds = t.getRounds();
		//for each write out results

		for(int r = 0;r<rounds.size();r++)
		{
			List<Match> m = rounds.get(r).getMatches();

			//round statement
			tdata+="<h2>Round "+(r+1)+": </h2>";
			//matchups in round
			for(int i=0; i<m.size();i++)
			{
				//determine if a name should be bolded
				Match c = m.get(i);
				if(c.getMatchResult()==MatchResult.PLAYER_ONE)
				{
					//player 1 is winner therefore bold p1
					tdata+="<b>"+c.getPlayerOne().getName()+"</b>" +" vs. "+c.getPlayerTwo().getName() +"<br>";	
				}
				else if(c.getMatchResult()==MatchResult.PLAYER_TWO)
				{
					//player 2 is winner therefore bold p2
					tdata+=c.getPlayerOne().getName() +" vs. "+"<b>"+c.getPlayerTwo().getName()+"</b>" +"<br>";	
				}
				else
				{
					//no winner
					tdata+=c.getPlayerOne().getName() +" vs. "+c.getPlayerTwo().getName() +"<br>";
				}

			}

		}

		return tdata;
	}
}