package utool.plugin.roundrobin.mocks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.content.Context;


import utool.plugin.Player;
import utool.plugin.observer.Observable;
import utool.plugin.observer.Observer;
import utool.plugin.roundrobin.Round;
import utool.plugin.roundrobin.RoundRobinPlayer;
import utool.plugin.roundrobin.RoundRobinTournament;

/**
 * A testable swiss tournament with access to protected methods
 * @author Justin Kreier
 * @version 1/25/2013
 */
public class MockRoundRobinTournament extends RoundRobinTournament{

	/**
	 * Required constructor
	 * @param tournamentId The tournament id
	 * @param playerList The player list
	 * @param tournamentName The tournament name
	 * @param profileId The profile id
	 * @param c The application context
	 */
	public MockRoundRobinTournament(long tournamentId, ArrayList<Player> playerList, String tournamentName, UUID profileId, Context c) {
		super(tournamentId, playerList, tournamentName, profileId, new Observer(){

			@Override
			public void updateObserver(Object observedObject) {
				//do nothing

			}

		}, c);
	}

	@Override
	public Round generateNextRound()
	{
		return super.generateNextRound();
	}

	/**
	 * Used to generate a non-random starting round
	 * @return teh round generated
	 */
	public Round generateNextRoundNotRandom()
	{		
		Round ret;

		//generating any other round
		List<RoundRobinPlayer> orderedPlayers = getStandingsArray();
		ret = generateRound(orderedPlayers);


		rounds.add(ret);
		observable.notifyChanged();

		//update email handler
		this.getAutomaticMessageHandler().sendOutNotifications();

		return ret;
	}

	/**
	 * Public access to the observable object
	 * @return The observable object
	 */
	public Observable<RoundRobinTournament> getObservable(){
		return super.observable;
	}

	/**
	 * Method to add a round to rounds without calling generate
	 * @param r The round to add
	 */
	public void addRound(Round r){
		super.rounds.add(r);
	}
}
