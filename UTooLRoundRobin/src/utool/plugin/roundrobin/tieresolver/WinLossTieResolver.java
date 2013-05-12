package utool.plugin.roundrobin.tieresolver;

import java.util.List;
import android.util.Log;
import utool.plugin.roundrobin.Match;
import utool.plugin.roundrobin.MatchResult;
import utool.plugin.roundrobin.RoundRobinPlayer;

/**
 * This tie resolver returns the player that won more matches
 * @author Maria Waltz
 * @version 4/3/2013
 */
public class WinLossTieResolver implements TieResolver{

	@Override
	public RoundRobinPlayer resolveTie(RoundRobinPlayer p1, RoundRobinPlayer p2) {
		if(p1==null ||p2 == null)
		{
			Log.e("ScoreTieResolver","Error: one of the user's was null");
			return null;
		}

		int p1wl = 0;	
		int p2wl = 0;

		//sum wins and subtract losses
		List<Match> matches = p1.getMatchesPlayed();
		for (Match m : matches){
			if (m.getPlayerOne().equals(p1)){
				if(m.getMatchResult().equals(MatchResult.PLAYER_ONE))
				{
					p1wl++;
				}
				else if(m.getMatchResult().equals(MatchResult.PLAYER_TWO))
				{
					p1wl--;
				}

			}
			else if (m.getPlayerTwo().equals(p1)){
				if(m.getMatchResult().equals(MatchResult.PLAYER_ONE))
				{
					p1wl--;
				}
				else if(m.getMatchResult().equals(MatchResult.PLAYER_TWO))
				{
					p1wl++;
				}

			}

		}

		//sum wins and subtract losses
		matches = p2.getMatchesPlayed();
		for (Match m : matches){
			if (m.getPlayerOne().equals(p2)){
				if(m.getMatchResult().equals(MatchResult.PLAYER_ONE))
				{
					p2wl++;
				}
				else if(m.getMatchResult().equals(MatchResult.PLAYER_TWO))
				{
					p2wl--;
				}

			}
			else if (m.getPlayerTwo().equals(p2)){
				if(m.getMatchResult().equals(MatchResult.PLAYER_ONE))
				{
					p2wl--;
				}
				else if(m.getMatchResult().equals(MatchResult.PLAYER_TWO))
				{
					p2wl++;
				}

			}
		}
		
		if (p1wl > p2wl){
			return p1;
		} else if (p2wl > p1wl){
			return p2;
		}

		return null;
	}

}
