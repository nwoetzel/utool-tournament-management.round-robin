package utool.plugin.roundrobin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import utool.plugin.Player;
import utool.plugin.activity.AbstractTournament;
import utool.plugin.observer.Observable;
import utool.plugin.observer.Observer;
import utool.plugin.roundrobin.communication.AutomaticMessageHandler;
import utool.plugin.roundrobin.communication.IncomingCommandHandler;

/**
 * This class is responsible for maintaining the state of an entire Round robin Tournament.
 * @author Justin Kreier
 * @author waltzm 
 * * @version 3/24/2013
 */
public class RoundRobinTournament extends AbstractTournament{

	/**
	 * The list of rounds
	 */
	protected List<Round> rounds;

	/**
	 * Observable implementation for Round robinTournament
	 */
	protected Observable<RoundRobinTournament> observable;

	/**
	 * Holds a reference to the tournament's email handler
	 */
	protected volatile AutomaticMessageHandler aeh;

	/**
	 * Holds the round robin configuration object
	 */
	public RoundRobinConfiguration conf;

	/**
	 * holds the logger tag
	 */
	private static final String LOG_TAG = "Round roRinTo urnaemte";


	/**
	 * Holds the graph of the tournament matchups
	 */
	private ArrayList<ArrayList<UUID>> graph; 

	/**
	 * Instantiates a new Round robinTournament
	 * @param tournamentId The tournament id
	 * @param playerList The list of players
	 * @param tournamentName The tournament name
	 * @param profileId The profile id
	 * @param o The object observing this tournament
	 * @param c The application context
	 */
	public RoundRobinTournament(long tournamentId, List<Player> playerList, String tournamentName, UUID profileId, Observer o, Context c) {
		super(tournamentId, playerListToRoundRobinPlayers(playerList), 
				(String) nullChecker(tournamentName, 
						new NullPointerException("Tournament name cannot be null")),
						(UUID)nullChecker(profileId,
								new NullPointerException("Profile id cannot be null")));


		rounds = new ArrayList<Round>();

		observable = new Observable<RoundRobinTournament>(this);
		observable.registerObserver(o);

		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(c);
		conf = new RoundRobinConfiguration(getRoundRobinPlayers(),p);

		//setup graph
		graph = new  ArrayList<ArrayList<UUID>>();
	}

	/**
	 * Checks if an object is null, and throws the exception if it is
	 * @param objectToCheck The object to check
	 * @param e The exception to throw
	 * @return The object if it is not null
	 */
	private static Object nullChecker(Object objectToCheck, RuntimeException e){
		if(objectToCheck == null){
			throw e;
		} else {
			return objectToCheck;
		}
	}

	/**
	 * Converts the player list into a list of Round robin players (performing necessary initialization 
	 * and preventing type cast exceptions later on)
	 * @param playerList The player list to convert
	 * @return The list of Round robinPlayers
	 */
	public static ArrayList<Player> playerListToRoundRobinPlayers(List<Player> playerList){
		ArrayList<Player> ret = new ArrayList<Player>();
		for (Player p : playerList){
			ret.add(new RoundRobinPlayer(p));
		}
		return ret;
	}


	/**
	 * Generates a new round based on current round information
	 * @return The newly generated round
	 */
	public Round generateNextRound(){
		Round ret;
		if (rounds.size() == 0){
			//randomize the list of players
			ArrayList<Player> tempPlayers = new ArrayList<Player>(this.getPlayers());
			ArrayList<RoundRobinPlayer> randomizedPlayers = new ArrayList<RoundRobinPlayer>();

			Random r = new Random();
			while (tempPlayers.size() > 0){
				randomizedPlayers.add((RoundRobinPlayer)tempPlayers.remove(r.nextInt(tempPlayers.size())));
			}

			//round size is zero, so we're generating the first round
			ret = generateRound(randomizedPlayers);
		} else{
			//generating any other round
			List<RoundRobinPlayer> orderedPlayers = getStandingsArray();
			ret = generateRound(orderedPlayers);
		}

		rounds.add(ret);
		observable.notifyChanged();

		//update email handler
		this.getAutomaticMessageHandler().sendOutNotifications();

		return ret;
	}

	/**
	 * Generates a round using the appropriate round generator from a list of players whose
	 * scores have been calculated
	 * @param orderedPlayers The list of players to generate a round for
	 * @return the new round
	 */
	public Round generateRound(List<RoundRobinPlayer> orderedPlayers)
	{
		Round r = conf.getRoundGenerator().generateRound(orderedPlayers, this);
		return r;
	}

	/**
	 * Returns the list of all rounds in the tournament
	 * @return The list of all rounds in the tournament
	 */
	public List<Round> getRounds(){
		return rounds;
	}

	/**
	 * Gets the player list as a list of round robin players
	 * @return The player list as a list of round robin players
	 */
	public List<RoundRobinPlayer> getRoundRobinPlayers(){
		List<Player> p = this.getPlayers();
		List<RoundRobinPlayer> player = new ArrayList<RoundRobinPlayer>();
		for (int i = 0; i < p.size(); i++){
			player.add((RoundRobinPlayer)p.get(i));
		}
		return player;
	}

	/**
	 * Returns the list of players ordered by their calculated score
	 * @return The list of players ordered by their score 
	 */
	public List<RoundRobinPlayer> getStandingsArray(){
		ArrayList<RoundRobinPlayer> players = new ArrayList<RoundRobinPlayer>();

		for (int i = 0; i < getPlayers().size(); i++){
			RoundRobinPlayer player = (RoundRobinPlayer)getPlayers().get(i);
			player.calculateScore();
			players.add(player);
		}

		//sort the players
		Collections.sort(players, new Comparator<RoundRobinPlayer>(){

			public int compare(RoundRobinPlayer p1, RoundRobinPlayer p2) {
				if (p1.getScore() > p2.getScore()){
					return -1;
				} else if (p1.getScore() < p2.getScore()){
					return 1;
				} else {

					RoundRobinPlayer tieWinner = RoundRobinPlayer.resolveTie(p1, p2, conf.getTieResolver());

					if (tieWinner == null){
						return 0;
					} else if (tieWinner.equals(p1)){
						return -1;
					} else {
						return 1;
					}
				}
			}

		});

		RoundRobinPlayer lastPlayer = null;
		int rank = 0;
		for (int i = 0; i < players.size(); i++){
			RoundRobinPlayer player = players.get(i);


			//if last player > player, rank = i+1
			if (lastPlayer != null && lastPlayer.getScore() > player.getScore()){
				rank = i+1;
				player.setRank(rank);
			}
			//if there is a last player, and when we compare the two players they are considered tied, then set their rank to be the same
			else if (lastPlayer != null && RoundRobinPlayer.resolveTie(player, lastPlayer, conf.getTieResolver()) == null){
				player.setRank(rank);
			} else {
				rank = i+1;
				player.setRank(rank);
			}

			lastPlayer = player;
		}

		return players;
	}

	/**
	 * Notifies the observable that it has been changed
	 */
	public void notifyChanged(){
		observable.notifyChanged();
	}

	/**
	 * Get the tournament's email handler 
	 * @return Instance of an email handler
	 */
	public AutomaticMessageHandler getAutomaticMessageHandler(){
		if (aeh == null){
			synchronized (this) {
				if (aeh == null){
					aeh = new AutomaticMessageHandler(tournamentId);
				}
			}
		}
		return aeh;
	}

	/**
	 * Get the tournament's Round robinConfiguration 
	 * @return Instance of an Round robinConfiguration
	 */
	public RoundRobinConfiguration getRoundRobinConfiguration(){
		return conf;
	}

	/**
	 * Clears a tournament's information
	 */
	public void clearTournament(){
		rounds.clear();
		List<RoundRobinPlayer> rr = this.getRoundRobinPlayers();
		for (Player p : rr){
			RoundRobinPlayer s = (RoundRobinPlayer)p;
			s.getMatchesPlayed().clear();
		}
		notifyChanged();
	}

	/**
	 * Switches s1 with s2 in the given round
	 * @param p1Match the match id of s1
	 * @param isP1First if S1 is first player in match
	 * @param p2Match the match id of s2
	 * @param isP2First if s2 is the first player in match
	 * @param round the round of the tournament (0 is round 1)
	 * @return true if ideal was met, false if not
	 */
	public boolean switchPlayers(int p1Match, boolean isP1First, int p2Match, boolean isP2First, int round) 
	{
		List<Round> rounds = getRounds();
		if(rounds.size()>0&&round>-1)
		{
			List<Match> matches = rounds.get(round).getMatches();

			//Find p1 and p2
			RoundRobinPlayer p1=null;
			RoundRobinPlayer p2=null;

			if(isP1First)
			{
				p1 = matches.get(p1Match).getPlayerOne();
			}
			else
			{
				p1 = matches.get(p1Match).getPlayerTwo();
			}

			if(isP2First)
			{
				p2 = matches.get(p2Match).getPlayerOne();
			}
			else
			{
				p2 = matches.get(p2Match).getPlayerTwo();
			}

			//remove unplayed match from p1 and p2
			Log.d(LOG_TAG,"P1 Matches prior: "+p1.getMatchesPlayed().toString());
			Log.d(LOG_TAG,"P2 Matches prior: "+p2.getMatchesPlayed().toString());
			if(p1.getMatchesPlayed().size()>0)
			{
				p1.getMatchesPlayed().remove(p1.getMatchesPlayed().size()-1);
			}
			if(p2.getMatchesPlayed().size()>0)
			{
				p2.getMatchesPlayed().remove(p2.getMatchesPlayed().size()-1);
			}

			Log.d(LOG_TAG,"P1 Matches after: "+p1.getMatchesPlayed().toString());
			Log.d(LOG_TAG,"P2 Matches after: "+p2.getMatchesPlayed().toString());
			//switch p1 and p2
			//get p1 opponent
			RoundRobinPlayer temp;
			if(isP1First)
			{
				//remove if both are BYE's
				temp = matches.get(p1Match).getPlayerTwo();
				if(p2.equals(RoundRobinPlayer.BYE)&&temp.equals(RoundRobinPlayer.BYE))
				{
					matches.remove(p1Match);
				}
				else
				{
					matches.set(p1Match, new Match(p2, temp, this.rounds.get(round)));
				}
			}
			else
			{
				//remove if both are BYE's
				temp = matches.get(p1Match).getPlayerOne();
				if(p2.equals(RoundRobinPlayer.BYE)&&temp.equals(RoundRobinPlayer.BYE))
				{
					matches.remove(p1Match);
				}
				else
				{		
					matches.set(p1Match, new Match(temp, p2, this.rounds.get(round)));
				}
			}
			//get p2 opponent
			RoundRobinPlayer temp2;
			if(isP2First)
			{
				temp2 = matches.get(p2Match).getPlayerTwo();
				//remove if both are byes
				if(p1.equals(RoundRobinPlayer.BYE)&&temp2.equals(RoundRobinPlayer.BYE))
				{
					matches.remove(p2Match);
				}
				else
				{
					matches.set(p2Match, new Match(p1, temp2, this.rounds.get(round)));
				}
			}
			else
			{
				temp2 = matches.get(p2Match).getPlayerOne();
				//remove if both are byes
				if(p1.equals(RoundRobinPlayer.BYE)&&temp2.equals(RoundRobinPlayer.BYE))
				{
					matches.remove(p2Match);
				}
				else
				{
					matches.set(p2Match, new Match(temp2, p1, this.rounds.get(round)));
				}
			}

			//notify of change
			this.notifyChanged();		
			//notify connected players of the change
			IncomingCommandHandler inc = new IncomingCommandHandler(this);
			inc.handleReceiveError(TournamentActivity.RESEND_ERROR_CODE, "", "", "");


			//regenerate the graph to ideal if it is round 1
			if(this.getRounds().size()==1)
			{
				ArrayList<UUID> g =new ArrayList<UUID>();
				g = graph.get(0);

				//swap players
				int indexP1=0;
				int indexP2 = 0;

				for(int i=0;i<g.size();i++)
				{
					if(g.get(i).equals(p1.getUUID()))
					{
						indexP1 = i;
					}
					else if(g.get(i).equals(p2.getUUID()))
					{
						indexP2 = i;
					}
				}

				g.set(indexP1, p2.getUUID());
				g.set(indexP2, p1.getUUID());

				//remove rest of graph and let it regenerate itself as it goes
				graph = new ArrayList<ArrayList<UUID>>();
				graph.add(g);
				return true;
			}
			else
			{
				//cant get ideal
				return false;
			}

		}	
		
		return false;
	}

	/**
	 * Getter for the graph of matches
	 * @return a reference to the graph
	 */
	public ArrayList<ArrayList<UUID>> getGraph()
	{
		return graph;
	}

	/**
	 * Setter for the graph
	 * @param graph the graph
	 */
	public void setGraph(ArrayList<ArrayList<UUID>> graph)
	{
		this.graph = graph;
	}


	/**
	 * Removes the player with UUID rem form the graph
	 * @param rem the UUID fo the player to remove
	 */
	public void removePlayerFromGraph(UUID rem) 
	{
		Log.d(LOG_TAG,"Graph before remove: "+graph);
		//go through graph removing all instances of the player
		//also decrement every index
		for(int i=0;i<graph.size();i++)
		{
			//pull out row
			ArrayList<UUID> row = graph.get(i);
			for(int j=0;j<row.size();j++)
			{
				if(row.get(j).equals(rem))
				{
					row.set(j,Player.BYE);
				}
			}
		}
		Log.d(LOG_TAG,"Graph: "+graph);

	}

	/**
	 * helper method to tostring the graph for debugging
	 */
	public void printGraph()
	{
		ArrayList<Player> p = getPlayers();

		for(int c = 0;c<graph.size();c++)
		{
			ArrayList<UUID> indicies = graph.get(c);
			String s = "";
			for(int z=0;z<indicies.size();z++)
			{
				String name="";
				for(int j=0;j<p.size();j++)
				{
					if(p.get(j).getUUID().equals(indicies.get(z)))
					{
						name = p.get(j).getName();
					}
				}
				if(name=="")
				{
					name = "BYE";
				}
				s+=name+", ";
			}

			Log.e("row"+c,"Indicies: "+s);
		}
	}
}

