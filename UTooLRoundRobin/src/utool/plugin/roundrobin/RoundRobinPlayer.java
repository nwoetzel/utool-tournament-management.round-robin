package utool.plugin.roundrobin;

import java.util.ArrayList;
import java.util.List;

import utool.plugin.Player;
import utool.plugin.roundrobin.tieresolver.TieResolver;

/**
 * This class represents a single participating player in a Swiss System 
 * Tournament
 * @author Justin Kreier
 * @version 1/21/2013
 */
public class RoundRobinPlayer extends Player{

	/**
	 * Publicly available player indicating the round is a bye round
	 */
	public static final RoundRobinPlayer BYE = new RoundRobinPlayer(new Player(Player.BYE,"BYE"));
	
	/**
	 * The list of matches that the player has participated in
	 */
	private List<Match> matchesPlayed;
	
	/**
	 * The most recently calculated score for the player
	 */
	private double calculatedScore;
	
	/**
	 * The player's rank in the standings
	 */
	private int playerRanking;
	
	/**
	 * Constructor that converts a Player into a SwissPlayer
	 * @param fromPlayer The Player to convert into a SwissPlayer
	 */
	public RoundRobinPlayer(Player fromPlayer){
		super(fromPlayer.getUUID(), fromPlayer.getName(), fromPlayer.isGhost(),
				fromPlayer.getSeedValue(), fromPlayer.getPortraitFilepath(), 
				fromPlayer.getPortrait(), fromPlayer.getPermissionsLevel());
		
		matchesPlayed = new ArrayList<Match>();
		calculatedScore = 0.0;
		playerRanking = -1;
	}
	
	/**
	 * Returns the matches the player has participated in
	 * @return The matches the player has participated in
	 */
	public List<Match> getMatchesPlayed(){
		return matchesPlayed;
	}
	
	/**
	 * Returns the match this player played in during the desired round number
	 * @param roundNumber The round number of the match to retrieve
	 * @return The match this player played in during the desired round number, 
	 * or null if no round exists
	 */
	public Match getMatch(int roundNumber){
		for (int i = 0; i < matchesPlayed.size(); i++){
			if (matchesPlayed.get(i).getRound().getRoundNumber() == roundNumber){
				return matchesPlayed.get(i);
			}
		}
		return null;
	}
	
	/**
	 * Returns the match this player played in during the desired round
	 * @param round The round the player played in
	 * @return The match this player played in during the desired round
	 */
	public Match getMatch(Round round){
		return getMatch(round.getRoundNumber());
	}

	/**
	 * Calculates the player's score based on their match wins and losses
	 * @return The player's calculated score
	 */
	public double calculateScore(){
		double totalScore = 0.0;
		for(int i = 0; i < matchesPlayed.size(); i++){
			Match m = matchesPlayed.get(i);
			if (m.getPlayerOne().equals(this) && m.getMatchResult() != MatchResult.UNDECIDED){
				totalScore += m.getPlayerOneScore();
			} else if (m.getPlayerTwo().equals(this) && m.getMatchResult() != MatchResult.UNDECIDED){
				totalScore += m.getPlayerTwoScore();
			}
		}
		calculatedScore = totalScore;
		return totalScore;
	}
	
	/**
	 * Adds a match to the list of matches played
	 * @param m The match to add
	 */
	public void putMatch(Match m){
		//replace the match if it exists already
		for (int i = 0; i < matchesPlayed.size(); i++){
			if (matchesPlayed.get(i).getRound().getRoundNumber() == m.getRound().getRoundNumber()){
				matchesPlayed.set(i, m);
				return;
			}
		}
		matchesPlayed.add(m);
	}
	
	/**
	 * Returns the player's most recently calculated score.
	 * @return The most recently calculated score.
	 */
	public double getScore(){
		return calculatedScore;
	}
	
	/**
	 * Counts the number of byes this player has received
	 * @return The total number of byes this player has received
	 */
	public int countByes(){
		int count = 0;
		for (int i = 0; i < matchesPlayed.size(); i++){
			Match m = matchesPlayed.get(i);
			if (m.getPlayerOne().equals(RoundRobinPlayer.BYE) || m.getPlayerTwo().equals(RoundRobinPlayer.BYE)){
				count++;
			}
		}
		return count;
	}
	
	/**
	 * Determines if this player has played against another specific player
	 * @param opponent The player to check against
	 * @return true if this player has played against that opponent
	 */
	public int countPlayedAgainstPlayer(RoundRobinPlayer opponent){
		int count = 0;
		for (int i = 0; i < matchesPlayed.size(); i++){
			Match m = matchesPlayed.get(i);
			if (m.getPlayerOne().equals(opponent)){
				count++;
			} else if (m.getPlayerTwo().equals(opponent)){
				count++;
			}
		}
		return count;
	}
	
	/**
	 * Gets the opponents this player has played against
	 * @return The list of players this player has played against
	 */
	public List<RoundRobinPlayer> getOpponents(){
		List<RoundRobinPlayer> opponents = new ArrayList<RoundRobinPlayer>();
		for (Match m : matchesPlayed){
			if (!this.equals(m.getPlayerOne())){
				if (!opponents.contains(m.getPlayerOne())){
					opponents.add(m.getPlayerOne());
				}
			} else if (!opponents.contains(m.getPlayerTwo())){
				opponents.add(m.getPlayerTwo());
			}
		}
		
		return opponents;
	}
	
	/**
	 * Getter for the rank of a player
	 * @return the player ranking
	 */
	public int getRank(){
		return playerRanking;
	}
	
	/**
	 * Setter for the rank of the player
	 * @param newRank the new rank of the player
	 */
	public void setRank(int newRank){
		playerRanking = newRank;
	}
	
	/**
	 * Resolves a tie between two players
	 * @param p1 The first player to compare
	 * @param p2 The second player to compare
	 * @param resolver The method of tie resolution to use
	 * @return The player who wins the tie-break
	 */
	public static RoundRobinPlayer resolveTie(RoundRobinPlayer p1, RoundRobinPlayer p2, TieResolver resolver){
		return resolver.resolveTie(p1, p2);
	}
	

}
