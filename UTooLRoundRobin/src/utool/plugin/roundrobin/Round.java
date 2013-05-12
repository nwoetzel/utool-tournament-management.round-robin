package utool.plugin.roundrobin;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a single round in a Swiss System tournament.
 * @author Justin Kreier
 * @version 1/21/2013
 */
public class Round {
	
	/**
	 * The list of matches contained in the round
	 */
	private List<Match> matches;
	
	/**
	 * The round number
	 */
	private int roundNumber;
	
	/**
	 * The tournament holding this round
	 */
	private RoundRobinTournament tournament;
	
	/**
	 * Initializes a new round
	 * @param roundNumber The round's round number
	 * @param theTournament The tournament holding this round
	 */
	public Round(int roundNumber, RoundRobinTournament theTournament){
		this(roundNumber, new ArrayList<Match>(), theTournament);
	}
	
	/**
	 * Initializes a new round
	 * @param roundNumber The round's round number
	 * @param matches The round's matches
	 * @param theTournament The tournament holding this round
	 */
	public Round(int roundNumber, List<Match> matches, RoundRobinTournament theTournament){
		this.roundNumber = roundNumber;
		this.matches = matches;
		this.tournament = theTournament;
	}
	
	/**
	 * Returns the list of matches for the round
	 * @return The list of matches for the round
	 */
	public List<Match> getMatches(){
		return matches;
	}
	
	/**
	 * Sets the list of matches for the round
	 * @param matches The list of matches
	 */
	public void setMatches(List<Match> matches){
		this.matches = matches;
		notifyChanged();
	}
	
	/**
	 * Adds a new match to the back of the round
	 * @param m The match to add
	 */
	public void addMatch(Match m){
		matches.add(m);
		notifyChanged();
	}
	
	/**
	 * Returns the round number
	 * @return The round number
	 */
	public int getRoundNumber(){
		return roundNumber;
	}
	
	/**
	 * Checks if the round is complete. It is considered complete if each match has a decided match result.
	 * @return True if the round is complete
	 */
	public boolean isRoundComplete(){
		for (int i = 0; i < matches.size(); i++){
			if (matches.get(i).getMatchResult() == MatchResult.UNDECIDED){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Notifies the tournament that this has been changed
	 */
	public void notifyChanged(){
		tournament.notifyChanged();
	}

}
