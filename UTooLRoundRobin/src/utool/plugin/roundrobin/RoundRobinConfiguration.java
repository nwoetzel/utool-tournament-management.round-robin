package utool.plugin.roundrobin;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import android.content.SharedPreferences;
import android.util.Log;
import utool.plugin.roundrobin.roundgenerator.BergerRoundGenerator;
import utool.plugin.roundrobin.roundgenerator.KirkmanRoundGenerator;
import utool.plugin.roundrobin.roundgenerator.RoundGenerator;
import utool.plugin.roundrobin.tieresolver.HeadToHeadTieResolver;
import utool.plugin.roundrobin.tieresolver.LazyTieResolver;
import utool.plugin.roundrobin.tieresolver.MultipleTieResolver;
import utool.plugin.roundrobin.tieresolver.RunDifferentialTieResolver;
import utool.plugin.roundrobin.tieresolver.TieResolver;

/**
 * Holds the configuration of the Round Robin Tournament
 * @author waltzm
 * @version 1/31/2013
 */
public class RoundRobinConfiguration 
{
	/**
	 * Log tag to be used in this class
	 */
	private static final String LOG_TAG = "Round Robin Configuration";

	/**
	 * true only if the timer was stopped prematurely and therefore should not go off
	 */
	private boolean terminatedPrematurely=false;

	
	/**
	 * Holds the number of rounds in the tournament
	 */
	private int numRounds;

	/**
	 * Holds the list of tie breakers to use in order
	 */
	private List<Integer> tieBreakers;

	/**
	 * Holds the corresponding tie resolver
	 */
	private TieResolver resolver;

	/**
	 * Reference to the round generator
	 */
	private RoundGenerator generator;

	/**
	 * Holds the list of tie breakers not being used
	 */
	private List<Integer> unusedtieBreakers;

	/**
	 * Ties are broken by head to head algorithms
	 */
	public static final int HEAD_TO_HEAD_TIE_BREAKER = 1;
	
	/**
	 * Ties are broken by run differential
	 */
	public static final int RUN_DIFFERENTIAL_TIE_BREAKER = 23;

	/**
	 * Ties are broken lazily
	 */
	public static final int LAZY_TIE_BREAKER = 4;

	/**
	 * Holds the number of points to be awarded on a win
	 */
	private float winScore;

	/**
	 * Holds the number of points to be awarded on a loss
	 */
	private float lossScore;

	/**
	 * Holds the number of points to be awarded on a tie
	 */
	private float tieScore;

	/**
	 * Holds the pairing algorithm to use
	 */
	private int pairingAlgorithm;


	/**
	 * Shared preferences that is used to save the rr options
	 * any time something is set
	 */
	public SharedPreferences pref;

	/**
	 * Pairs players with the opponent they have the closest score to
	 */
	public static final int KIRKMAN_TOURNAMENT_PAIRING_ALGORITHM = 15;

	/**
	 * Pairs players with the person they have the closest score to unless 
	 * there is a player they have versed less times.
	 */
	public static final int BERGER_TOURNAMENT_PAIRING_ALGORITHM = 16;

	//Following are for saving to preferences
	/**
	 * String used to save the default scoring for a win permanently
	 */
	private static final String SHARED_PREF_DEFAULT_SCORING_WINS = "default_wins";

	/**
	 * String used to save the default scoring for a loss permanently
	 */
	private static final String SHARED_PREF_DEFAULT_SCORING_LOSSES = "default_losses";

	/**
	 * String used to save the default scoring for a tie permanently
	 */
	private static final String SHARED_PREF_DEFAULT_SCORING_TIES = "default_ties";

	/**
	 * String used to save the default rounds permanently
	 */
	private static final String SHARED_PREF_DEFAULT_MATCHING_ALG = "default_rounds";

	/**
	 * String used to save the default tie handling permanently
	 */
	private static final String SHARED_PREF_TIE_HANDLING = "default_tie_handling";

	/**
	 * Total number of seconds per round
	 */
	private long roundTimerSeconds = 50*60;//50 mins

	/**
	 * time at start of the round
	 */
	private long timerStartTimer = -1;

	/**
	 * True if timer is started
	 */
	private boolean isTimerStarted = false;

	/**
	 * true if a previous round had the timer
	 */
	private boolean startTimerOnRoundChange=false;

	/**
	 * Creates a default Swiss Configuration object
	 * @param p player list in the tournament
	 * @param pref The share preferences to save/load from
	 */
	public RoundRobinConfiguration(List<RoundRobinPlayer> p, SharedPreferences pref)
	{
		this.pref = pref;

		//load defaults from Preferences if there

		//calculate default number of rounds
		this.numRounds = p.size()-1;
		if(p.size()%2!=0)
		{
			this.numRounds++;//account for bye
		}
		
		//default tie breakers
		tieBreakers = new ArrayList<Integer>();
		unusedtieBreakers = new ArrayList<Integer>();

		if(pref!=null)
		{
			String s = pref.getString(SHARED_PREF_TIE_HANDLING, "");

			if(s.equals(""))
			{
				//default
				tieBreakers.add(HEAD_TO_HEAD_TIE_BREAKER);
				tieBreakers.add(RUN_DIFFERENTIAL_TIE_BREAKER);
				tieBreakers.add(LAZY_TIE_BREAKER);
			}
			else
			{
				try
				{
					Log.d(LOG_TAG,"Begining loading in");
					StringTokenizer t = new StringTokenizer(s,"|");
					String ties = t.nextToken();
					String unused_ties = t.nextToken();

					StringTokenizer used = new StringTokenizer(ties, ",");
					while(used.hasMoreTokens())
					{
						tieBreakers.add(Integer.parseInt(used.nextToken()));
						Log.d(LOG_TAG,"Used Added: "+tieBreakers.get(tieBreakers.size()-1));
					}

					StringTokenizer unused = new StringTokenizer(unused_ties, ",");
					while(unused.hasMoreTokens())
					{
						unusedtieBreakers.add(Integer.parseInt(unused.nextToken()));
						Log.d(LOG_TAG,"Unused Added: "+tieBreakers.get(tieBreakers.size()-1));
					}

				}
				catch(Exception e)
				{
					Log.d(LOG_TAG,"ERROR",e);
					//reset to default if something goes wrong
					tieBreakers.clear();
					unusedtieBreakers.clear();
					//default
					tieBreakers.add(HEAD_TO_HEAD_TIE_BREAKER);
					tieBreakers.add(RUN_DIFFERENTIAL_TIE_BREAKER);
					tieBreakers.add(LAZY_TIE_BREAKER);
				}
			}
			//setup MultipleTieResolver
			resolver = new MultipleTieResolver(this.determineListResolvers());

			//scoring defaults	
			winScore = pref.getFloat(SHARED_PREF_DEFAULT_SCORING_WINS, 1); 
			lossScore = pref.getFloat(SHARED_PREF_DEFAULT_SCORING_LOSSES, 0); 
			tieScore = pref.getFloat(SHARED_PREF_DEFAULT_SCORING_TIES, .5f); 

			//matchup pairing alg
			setPairingAlgorithm(pref.getInt(SHARED_PREF_DEFAULT_MATCHING_ALG, RoundRobinConfiguration.KIRKMAN_TOURNAMENT_PAIRING_ALGORITHM));
		}
		else
		{
			//no prefs passed in so revert to defaults
			tieBreakers.clear();
			unusedtieBreakers.clear();
			//default
			tieBreakers.add(HEAD_TO_HEAD_TIE_BREAKER);
			tieBreakers.add(RUN_DIFFERENTIAL_TIE_BREAKER);
			tieBreakers.add(LAZY_TIE_BREAKER);
			winScore = 1;
			lossScore = 0; 
			tieScore = .5f; 
			resolver = new MultipleTieResolver(this.determineListResolvers());
			setPairingAlgorithm(RoundRobinConfiguration.KIRKMAN_TOURNAMENT_PAIRING_ALGORITHM);

		}
	}

	/**
	 * Returns the list of resolvers corresponding to tieBreakers
	 * @return the list of resolvers
	 */
	private List<TieResolver> determineListResolvers() 
	{
		List<TieResolver> resolvers = new ArrayList<TieResolver>();
		//go through tiebreakers adding to resolver
		for(int i=0;i<tieBreakers.size();i++)
		{
			switch(tieBreakers.get(i))
			{
			case HEAD_TO_HEAD_TIE_BREAKER:
				resolvers.add(new HeadToHeadTieResolver());
				break;
			case LAZY_TIE_BREAKER:
				resolvers.add(new LazyTieResolver());
				break;
			case RUN_DIFFERENTIAL_TIE_BREAKER:
				resolvers.add(new RunDifferentialTieResolver());
				break;

			}
		}
		return resolvers;
	}


	/**
	 * Getter for number of rounds
	 * @return number of rounds
	 */
	public int getNumRounds() {
		return numRounds;
	}


	/**
	 * Setter for number of rounds
	 * @param numRounds new number of rounds
	 */
	public void setNumRounds(int numRounds) 
	{
		this.numRounds = numRounds;
	}


	/**
	 * Returns the points to be awarded on a win
	 * @return score of a win
	 */
	public float getWinScore() {
		return winScore;
	}

	/**
	 * Setter for the points to be awarded on a win
	 * @param winScore points for a win
	 */
	public void setWinScore(float winScore) {
		this.winScore = winScore;
		//update preferences
		pref.edit().putFloat(SHARED_PREF_DEFAULT_SCORING_WINS, winScore).commit();
	}

	/**
	 * Returns the points to be awarded on a loss
	 * @return score of a loss
	 */
	public float getLossScore() {
		return lossScore;
	}

	/**
	 * Setter for the points to be awarded on a loss
	 * @param lossScore points for a loss
	 */
	public void setLossScore(float lossScore) {
		this.lossScore = lossScore;
		//update preferences
		pref.edit().putFloat(SHARED_PREF_DEFAULT_SCORING_LOSSES, lossScore).commit();
	}

	/**
	 * Returns the points to be awarded on a tie
	 * @return score of a tie
	 */
	public float getTieScore() {
		return tieScore;
	}

	/**
	 * Setter for the points to be awarded on a tie
	 * @param tieScore points for a tie
	 */
	public void setTieScore(float tieScore) {
		this.tieScore = tieScore;
		//update preferences
		pref.edit().putFloat(SHARED_PREF_DEFAULT_SCORING_TIES, tieScore).commit();

	}

	/**
	 * Gets the round generator defined by the configuration
	 * @return The round generator
	 */
	public RoundGenerator getRoundGenerator(){
		return generator;
	}

	/**
	 * Getter for the pairing algorithm
	 * Returns one of the static finals declared in this class
	 * @return pairing algorithm.
	 */
	public int getPairingAlgorithm() {
		return pairingAlgorithm;
	}

	/**
	 * Setter for the pairing algorithm.
	 * Should be one of the pairing algorithm's defined in this class
	 * @param pairingAlgorithm the new scoring algorithm to use
	 */
	public void setPairingAlgorithm(int pairingAlgorithm) 
	{
		this.pairingAlgorithm = pairingAlgorithm;
		//update preferences
		if(pref!=null)
		{
			pref.edit().putInt(SHARED_PREF_DEFAULT_MATCHING_ALG, pairingAlgorithm).commit();
		}
		switch(pairingAlgorithm){
		case KIRKMAN_TOURNAMENT_PAIRING_ALGORITHM:
			generator = new KirkmanRoundGenerator();
			break;
		case BERGER_TOURNAMENT_PAIRING_ALGORITHM:
			generator = new BergerRoundGenerator();
			break;
		default:
			throw new RuntimeException("Pairing algorithm called with invalid value: "+pairingAlgorithm);
		}
	}


	/**
	 * Getter for the tie breakers to use in order
	 * @return list of tie breakers
	 */
	public List<Integer> getTieBreakers() {
		return tieBreakers;
	}

	/**
	 * Setter for the list of tie breakers in order
	 * Note: all tie breakers must be in either
	 * the list of tie breakers, or the list of
	 * unused tie breakers!
	 * @param tieBreakers list of tie breakers
	 * @param unusedtieBreakers list of unused tie breakers
	 */
	public void setTieBreakers(List<Integer> tieBreakers,List<Integer> unusedtieBreakers)
	{
		this.tieBreakers = tieBreakers;
		this.unusedtieBreakers = unusedtieBreakers;
		//update preferences
		String tb = "";
		for(int i=0;i<tieBreakers.size();i++)
		{
			tb+=tieBreakers.get(i);
			if(i+1<tieBreakers.size())
			{
				tb+=",";
			}
		}
		tb+= "|";
		for(int i=0;i<unusedtieBreakers.size();i++)
		{
			tb+=unusedtieBreakers.get(i);
			if(i+1<unusedtieBreakers.size())
			{
				tb+=",";
			}
		}
		Log.d(LOG_TAG,"SharedPref adding: "+tb);
		pref.edit().putString(SHARED_PREF_TIE_HANDLING, tb).commit();

		//update MultipleTieResolver
		resolver = new MultipleTieResolver(this.determineListResolvers());
	}	

	/**
	 * Getter for the list of unused tie breakers
	 * @return the list of unused tiebreakers
	 */
	public List<Integer> getUnusedtieBreakers() {
		return unusedtieBreakers;
	}

	/**
	 * Getter for the tie resolver
	 * @return the tie resolver
	 */
	public TieResolver getTieResolver() {
		return resolver;
	}

	/**
	 * Setter for the tie resolver
	 * @param resolver the tie resolver
	 */
	public void setTieResolver(TieResolver resolver) {
		this.resolver = resolver;
	}

	/**
	 * Returns a string name for the public static int passed in
	 * @param tie the final int of the tie
	 * @return name of the tie
	 */
	public static String getTieBreakerName(int tie)
	{
		switch(tie)
		{
		case HEAD_TO_HEAD_TIE_BREAKER:
			return "Head To Head";
		case LAZY_TIE_BREAKER:
			return "Lazy";
		case RUN_DIFFERENTIAL_TIE_BREAKER:
			return "Run Differential";
		}
		return "No idea";
	}

	/**
	 * Getter for the amount of seconds per round
	 * @return the roundTimerSeconds
	 */
	public long getRoundTimerSeconds() {
		return roundTimerSeconds;
	}

	/**
	 * Starts the timer
	 * @return true is started
	 */
	public boolean startTimer()
	{
		startTimerOnRoundChange = true;
		if(!isTimerStarted)
		{
			//start the timer

			//grab start time
			this.timerStartTimer = System.currentTimeMillis();
			isTimerStarted=true;

			return true;
		}
		else
		{
			this.timerStartTimer = System.currentTimeMillis();
			return false;//already started
		}
	}

	/**
	 * Starts the timer, putting the start time milliElapsedSoFar milliseconds ago
	 * @param milliElapsedSoFar milliseconds since timer should have started
	 * @return false if timer was already started
	 */
	public boolean startTimer(long milliElapsedSoFar) {
		startTimerOnRoundChange = true;
		if(!isTimerStarted)
		{
			//start the timer

			//grab start time
			this.timerStartTimer = System.currentTimeMillis()-milliElapsedSoFar;
			isTimerStarted=true;

			return true;
		}
		else
		{
			this.timerStartTimer = System.currentTimeMillis()-milliElapsedSoFar;
			return false;//already started
		}

	}

	/**
	 * Setter for the amount of seconds per round
	 * @param roundTimerSeconds the roundTimerSeconds to set
	 */
	public void setRoundTimerSeconds(int roundTimerSeconds) 
	{
		this.roundTimerSeconds = roundTimerSeconds;
	}

	/**
	 * Get the number of seconds left in the current round
	 * returns -1 if timer isn't started
	 * return 0 if just finished
	 * @return the seconds Remaining
	 */
	public long getSecondsRemaining() 
	{
		if(isTimerStarted)
		{
			long time = System.currentTimeMillis();

			long dif = time-this.timerStartTimer;

			long timePassed = dif/1000;

			long remaining  = this.roundTimerSeconds - timePassed;

			if(remaining<=0)
			{
				//time expired
				isTimerStarted = false;
				return 0;
			}
			else
			{
				return remaining;
			}
		}
		else
		{
			return -1;
		}
	}

	/**
	 * Getter for if a round timer has been started for a previous round
	 * @return true if on round change a tiemr should be started
	 */
	public boolean getStartTimerOnRoundChange()
	{
		return startTimerOnRoundChange;
	}
	
	/**
	 * Setter for if the timer is killed before going off.
	 * This makes it so the timer doesn't go off (vibrate)
	 * @param t true if yes
	 */
	public void setTimerTerminatedPrematurely(boolean t)
	{
		this.terminatedPrematurely=t;
	}

	/**
	 * return true if timer terminated early
	 * @return true if timer killed set 
	 */
	public boolean wasTimerTerminated() {
		return this.terminatedPrematurely;
	}
}
