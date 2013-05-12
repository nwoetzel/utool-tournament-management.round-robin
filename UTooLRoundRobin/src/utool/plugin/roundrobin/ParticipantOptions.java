package utool.plugin.roundrobin;

import utool.plugin.activity.AbstractPluginCommonTabActivity;
import utool.plugin.roundrobin.R;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

/**
 * Class for holding the participant options
 * Only holds two tabs (Tournament and Email)
 * @author waltzm
 * @version 2/8/2012
 */
public class ParticipantOptions extends AbstractPluginCommonTabActivity {

	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.roundrobin_options_main);

		Resources ressources = getResources(); 
		TabHost tabHost = getTabHost(); 

		//Tournament Tab: Only shows tie breakers
		Intent intentSwiss = ((Intent) getIntent().clone()).setClass(this, OptionsTournamentTabPart.class);
		TabSpec tabSpecSwiss = tabHost
				.newTabSpec("Tournament")
				.setIndicator("", ressources.getDrawable(R.drawable.tournament_tab_icon))
				.setContent(intentSwiss);

		// Email tab: Same as host
		Intent intentEmail = ((Intent) getIntent().clone()).setClass(this, OptionsEmailTab.class);
		TabSpec tabSpecEmail = tabHost
				.newTabSpec("Email")
				.setIndicator("", ressources.getDrawable(R.drawable.email_tab_icon))
				.setContent(intentEmail);

		// add all tabs 
		tabHost.addTab(tabSpecSwiss);
		tabHost.addTab(tabSpecEmail);


		//set Windows tab as default (zero based)
		tabHost.setCurrentTab(0);
	}
}