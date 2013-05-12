package utool.plugin.roundrobin.tieresolver;

import java.util.List;

import android.util.Log;
import utool.plugin.roundrobin.Match;
import utool.plugin.roundrobin.RoundRobinPlayer;

/**
 * This tie resolver determines a winner by checking the scores of the players in relevant matches and who scored better.
 * @author Maria waltz
 * @version 3/23/2013
 */
public class RunDifferentialTieResolver implements TieResolver{

	@Override
	public RoundRobinPlayer resolveTie(RoundRobinPlayer p1, RoundRobinPlayer p2)
	{
		if(p1==null ||p2 == null)
		{
			Log.e("Run DifferentialTieResolver","Error: one of the user's was null");
			return null;
		}
		List<Match> matches = p1.getMatchesPlayed();

		int playerOnePoints = 0;
		int playerTwoPoints = 0;

		//find all matches they versed each other, and sum the points
		for (Match m : matches){
			if (m.getPlayerOne().equals(p1) && m.getPlayerTwo().equals(p2)){
				playerOnePoints+=m.getPlayerOneScore();
				playerTwoPoints+=m.getPlayerTwoScore();
			}
			else if (m.getPlayerTwo().equals(p1) && m.getPlayerOne().equals(p2)){
				playerTwoPoints+=m.getPlayerOneScore();
				playerOnePoints+=m.getPlayerTwoScore();
			}
		}

		if (playerOnePoints > playerTwoPoints){
			return p1;
		} else if (playerTwoPoints > playerOnePoints){
			return p2;
		}

		return null;
	}

}
