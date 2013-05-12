package utool.plugin.roundrobin;

/**
 * This enumeration defines the result of a match. 
 * @author Justin Kreier
 * @version 1/21/2013
 */
public enum MatchResult {

	/**
	 * The result indicating that player one was the victor
	 */
	PLAYER_ONE,
	
	/**
	 * The result indicating that player two was the victor
	 */
	PLAYER_TWO,
	
	/**
	 * The result indicating that the match ended in a tie
	 */
	TIE,
	
	/**
	 * The result indicating that the match has not yet been decided
	 */
	UNDECIDED
}
