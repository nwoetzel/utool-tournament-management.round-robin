package utool.plugin.roundrobin;

/**
 * This class respresents a single match in a Swiss System tournament. 
 * @author Justin Kreier
 * @version 1/21/2013
 */
public class Match {

	/**
	 * The first player in the match
	 */
	private RoundRobinPlayer playerOne;

	/**
	 * The second player in the match
	 */
	private RoundRobinPlayer playerTwo;

	/**
	 * The score/points awarded to player 1
	 */
	private double playerOneScore;

	/**
	 * The score/points awarded to player 2
	 */
	private double playerTwoScore;

	/**
	 * The round holding this match
	 */
	private Round round;

	/**
	 * The result of this match
	 */
	private MatchResult result;

	/**
	 * Instantiates a new match
	 * @param p1 The first player participating in the match
	 * @param p2 The second player participating in the match
	 * @param r The round the match is occurring in
	 */
	public Match(RoundRobinPlayer p1, RoundRobinPlayer p2, Round r){
		if (p1 == null){
			throw new NullPointerException("Neither match participant may be null. If there" +
					"is no opponent, use SwissPlayer.BYE");
		}
		if (p2 == null){
			throw new NullPointerException("Neither match participant may be null. If there" +
					"is no opponent, use SwissPlayer.BYE");
		}
		if (r == null){
			throw new NullPointerException("There must be a valid round");
		}
		playerOne = p1;
		playerTwo = p2;
		round = r;

		result = MatchResult.UNDECIDED;

		playerOneScore = 0.0;
		playerTwoScore = 0.0;

		//add the matches to the players
		if (!p1.equals(RoundRobinPlayer.BYE)){
			p1.putMatch(this);
		}
		if (!p2.equals(RoundRobinPlayer.BYE)){
			p2.putMatch(this);
		}
	}

	/**
	 * Returns the result of this match
	 * @return The result of this match
	 */
	public MatchResult getMatchResult(){
		return result;
	}

	/**
	 * Returns the first player
	 * @return Player One
	 */
	public RoundRobinPlayer getPlayerOne(){
		return playerOne;
	}

	/**
	 * Returns the second player
	 * @return Player Two
	 */
	public RoundRobinPlayer getPlayerTwo(){
		return playerTwo;
	}

	/**
	 * Sets the scores for the match
	 * @param p1Score The score/points to be awarded to player one
	 * @param p2Score The score/points to be awarded to player two
	 * @param newResult The new result. If left null, will infer the result.
	 */
	public void setScores(double p1Score, double p2Score, MatchResult newResult){
		playerOneScore = p1Score;
		playerTwoScore = p2Score;
		if (newResult != null){
			result = newResult;
		} else if (p1Score > p2Score){
			result = MatchResult.PLAYER_ONE;
		} else if (p2Score > p1Score){
			result = MatchResult.PLAYER_TWO;
		} else {
			result = MatchResult.TIE;
		}


		notifyChanged();

	}

	/**
	 * Sets the scores for the match
	 * @param p1Score The score/points to be awarded to player one
	 * @param p2Score The score/points to be awarded to player two
	 */
	public void setScores(double p1Score, double p2Score){
		setScores(p1Score, p2Score, null);
	}




	/**
	 * Returns the round that the match is from 
	 * @return The round the match is from
	 */
	public Round getRound(){
		return round;
	}

	/**
	 * Returns the score/points awarded to player one
	 * @return The score/points awarded to player one
	 */
	public double getPlayerOneScore(){
		return playerOneScore;
	}

	/**
	 * Returns the score/points awarded to player two
	 * @return The score/points awarded to player two
	 */
	public double getPlayerTwoScore(){
		return playerTwoScore;
	}
	
	/**
	 * Gets the score from a player
	 * @param p The player to check the score for
	 * @return The player's score in the match
	 */
	public double getScore(RoundRobinPlayer p){
		if (playerOne.equals(p)){
			return getPlayerOneScore();
		} else if (playerTwo.equals(p)){
			return getPlayerTwoScore();
		} else{
			return 0;
		}
	}


	/**
	 * Notifies the round that this match has changed
	 */
	public void notifyChanged(){
		round.notifyChanged();
	}

	@Override
	public String toString(){
		return playerOne.getName()+" vs "+playerTwo.getName();
	}
}
