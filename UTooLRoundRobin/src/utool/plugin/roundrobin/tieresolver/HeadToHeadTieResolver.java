package utool.plugin.roundrobin.tieresolver;

import java.util.List;
import android.util.Log;
import utool.plugin.roundrobin.Match;
import utool.plugin.roundrobin.MatchResult;
import utool.plugin.roundrobin.RoundRobinPlayer;

/**
 * This tie resolver determines a winner by checking if the two players have played
 * against one another, and the winner of that. The winner of their match wins the tie, otherwise Null is returned
 * @author Maria waltz and kreierj
 * @version 4/21/2013
 */
public class HeadToHeadTieResolver implements TieResolver{
	
	
	@Override
	public RoundRobinPlayer resolveTie(RoundRobinPlayer p1, RoundRobinPlayer p2) {
		
		if(p1==null ||p2 == null)
		{
			Log.e("Run DifferentialTieResolver","Error: one of the user's was null");
			return null;
		}
		
		int playerOneWins = countWins(p1,p2);
		int playerTwoWins = countWins(p2,p1);
		
		List<RoundRobinPlayer> p1Opponents = p1.getOpponents();
		for (int i = 0; i < p1Opponents.size(); i++){
			RoundRobinPlayer opponent = p1Opponents.get(i);
			//if my opponent has played against a different opponent I've played
			if (opponent.countPlayedAgainstPlayer(p2) == 0){  
				p1Opponents.remove(i);
				i--;
			}
		}
		
		
		for (RoundRobinPlayer opponent : p1Opponents){
			int opponentScoreP1 = countWins(opponent, p1); //number of times opponent beat p1
			int p1Score = countWins(p1, opponent);
			int opponentScoreP2 = countWins(opponent, p2); //number of times opponent beat p2
			int p2Score = countWins(p2, opponent);
			
			if (p2Score > opponentScoreP2 && p1Score < opponentScoreP1){
				p2Score -= p1Score;
				playerOneWins -= p2Score;
			}
		}
		
		
		
		List<RoundRobinPlayer> p2Opponents = p2.getOpponents();
		for (int i = 0; i < p2Opponents.size(); i++){
			RoundRobinPlayer opponent = p2Opponents.get(i);
			//if my opponent has played against a different opponent I've played
			if (opponent.countPlayedAgainstPlayer(p1) == 0){  
				p2Opponents.remove(i);
				i--;
			}
		}
		
		
		for (RoundRobinPlayer opponent : p2Opponents){
			int opponentScoreP1 = countWins(opponent, p1); //number of times opponent beat p1
			int p1Score = countWins(p1, opponent);
			int opponentScoreP2 = countWins(opponent, p2); //number of times opponent beat p2
			int p2Score = countWins(p2, opponent);
			
			if (p1Score > opponentScoreP1 && p2Score < opponentScoreP2){
				p1Score -= p2Score;
				playerTwoWins -= p1Score;
			}
		}
		
		
		
		if (playerOneWins > playerTwoWins){
			return p1;
		} else if (playerTwoWins > playerOneWins){
			return p2;
		}
		
		return null;
	}
	
	/**
	 * Counts how many times p1 has beaten p2
	 * @param p1 Player 1
	 * @param p2 Player 2
	 * @return The number of times p1 has beaten p2
	 */
	private int countWins(RoundRobinPlayer p1, RoundRobinPlayer p2){
		if(p1==null ||p2 == null)
		{
			Log.e("Run DifferentialTieResolver","Error: one of the user's was null");
			return 0;
		}
		
		List<Match> matches = p1.getMatchesPlayed();
		
		int playerOneWins = 0;
		
		//find all matches they versed each other, and sum the wins
		for (Match m : matches){
			if (m.getPlayerOne().equals(p1) && m.getPlayerTwo().equals(p2)){
				if (m.getMatchResult().equals(MatchResult.PLAYER_ONE)){
					playerOneWins++;
				}
			}
			else if (m.getPlayerTwo().equals(p1) && m.getPlayerOne().equals(p2)){
				if (m.getMatchResult().equals(MatchResult.PLAYER_TWO)){
					playerOneWins++;
				}
			}
		}
		
		return playerOneWins;
	}

}
