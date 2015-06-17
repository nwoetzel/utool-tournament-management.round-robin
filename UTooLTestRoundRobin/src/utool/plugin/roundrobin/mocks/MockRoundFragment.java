package utool.plugin.roundrobin.mocks;

import java.util.List;

import utool.plugin.activity.TournamentContainer;
import utool.plugin.roundrobin.Match;
import utool.plugin.roundrobin.MatchResult;
import utool.plugin.roundrobin.Round;
import utool.plugin.roundrobin.RoundFragment;
import utool.plugin.roundrobin.RoundRobinTournament;


public class MockRoundFragment extends RoundFragment{
	
	int selectedPlayerIndex =-1;
	public long getTid()
	{
		return tid;
	}
	
	public void setTID(long tid)
	{
		this.tid=tid;
	}
	
	public int getRound()
	{
		return round;
	}
	
	public void setRound(int round)
	{
		this.round=round;
	}
	
	public int getSelectedPlayerIndex()
	{
		return selectedPlayerIndex;
	}
	
	public void setSelectedPlayerIndex(int selectedPlayerIndex)
	{
		this.selectedPlayerIndex=selectedPlayerIndex;
	}
	
	public boolean getIsSelP1()
	{
		return isSelP1;
	}
	
	public void setIsSelP1(boolean isSelP1)
	{
		this.isSelP1=isSelP1;
	}

	/**
	 * Performs needed action if a second player is selected (the switch)
	 * @param pos the position of the second player
	 * @param p1 if the second player is p1
	 */
	public void onSecondPlayerClick(int pos, boolean p1)
	{
		try{
		super.onSecondPlayerClick(pos, p1);
		}
		catch(Exception e)
		{
			//thrown since Tournamnet Activity isnt set up
			this.savedP1=p1;
			this.savedPos=pos;
			RoundRobinTournament tourny = ((RoundRobinTournament)TournamentContainer.getInstance(tid));

			if(selectedPlayerIndex == pos && isSelP1 == p1)
			{
				//unselect current player
				selectedPlayerIndex = -1;				
			}
			else
			{
				//else switch this player and the selected player

				if(tourny.getRounds().size()>0)
				{
					//User verification if scores have been set
					List<Round> rounds = tourny.getRounds();
					List<Match> matches = rounds.get(round-1).getMatches();
					if(matches.get(pos).getMatchResult()!=MatchResult.UNDECIDED || matches.get(selectedPlayerIndex).getMatchResult()!=MatchResult.UNDECIDED)
					{
	
					}
					else
					{
						doSwitch(pos, p1);	
					}				
				}
			}
		}
	}
	
	public void doSwitch(int pos, boolean p1)
	{
		try
		{
		super.doSwitch(pos, p1);
		}
		catch(Exception e)
		{
			RoundRobinTournament tourny = ((RoundRobinTournament)TournamentContainer.getInstance(tid));
			//perform the switch
			tourny.switchPlayers(pos,p1,selectedPlayerIndex,isSelP1, round-1);

			selectedPlayerIndex = -1;	
		}
	}
}
