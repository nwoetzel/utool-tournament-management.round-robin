package utool.plugin.roundrobin.roundgenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.util.Log;
import utool.plugin.Player;
import utool.plugin.roundrobin.Match;
import utool.plugin.roundrobin.Round;
import utool.plugin.roundrobin.RoundRobinPlayer;
import utool.plugin.roundrobin.RoundRobinTournament;

/**
 * This round generator is responible for creating a round using
 * the Berger table algorithm
 * 
 *   //TODO: Possible enhancement: randomize match order to make it less 
 *   obvious that a person is being held constant
 * @author Maria Waltz
 * @version 3/23/2013
 */
public class BergerRoundGenerator extends RoundGenerator{

	@Override
	public Round generateRound(List<RoundRobinPlayer> orderedPlayers, RoundRobinTournament t) 
	{	
		if(orderedPlayers==null || t ==null)
		{
			Log.e("BergerRoundGEnerator","Error: Orderedplayers or passed in tournaemnt was null");
			return null;
		}

		int round = t.getRounds().size();
		Round ret = new Round(round, t);
		ArrayList<Match> matches = new ArrayList<Match>();

		//Handle less than 2 players
		if(orderedPlayers.size()==1)
		{
			matches.add(new Match(orderedPlayers.get(0), RoundRobinPlayer.BYE, ret));
			ret.setMatches(matches);
			return ret;
		}
		else if(orderedPlayers.size()<1)
		{
			ret.setMatches(matches);
			return ret;
		}

		if(round==0)
		{
			//change ordered players into integers
			List<UUID> indicies = new ArrayList<UUID>(orderedPlayers.size());
			for(int i=0;i<orderedPlayers.size();i++)
			{
				indicies.add(orderedPlayers.get(i).getUUID());
			}

			//remove last player of list
			UUID fixed = indicies.remove(indicies.size()-1);

			//generating the graph

			//create the graph
			ArrayList<ArrayList<UUID>> graph = t.getGraph();

			//if indicies is even, add a bye indicies
			if(indicies.size()%2==0)
			{
				indicies.add(Player.BYE);
			}

			//create the rounds
			{
				//create the rounds
				ArrayList<UUID> r = new ArrayList<UUID>(indicies.size());
				r.addAll(indicies);
				r.add(fixed);

				//add to the graph
				graph.add(r);

				//update indicies n/2-1 shifts
				int n = (indicies.size()+1)/2-1;
				for(int f=0;f<n;f++)
				{
					UUID temp = indicies.remove(0);
					indicies.add(temp);
				}


			}

			ArrayList<UUID> gr = graph.get(round);

			t.setGraph(graph);
			int index=gr.size()-1;
			//create the list of matches
			Log.e("LT", "Graph round: "+graph.get(round));


			for(int m=0;m<gr.size()/2;m++)
			{
				RoundRobinPlayer p1=null ;
				RoundRobinPlayer p2 =null;

				if(gr.get(m).equals(Player.BYE))
				{
					p1 = RoundRobinPlayer.BYE;
				}
				else
				{
					for(int i = 0;i<orderedPlayers.size();i++)
					{
						if(orderedPlayers.get(i).getUUID().equals(gr.get(m)))
						{
							p1 = orderedPlayers.get(i);
							break;
						}
					}

				}
				if(gr.get(index).equals(Player.BYE))
				{
					p2 = RoundRobinPlayer.BYE;
				}
				else
				{
					for(int i = 0;i<orderedPlayers.size();i++)
					{
						if(orderedPlayers.get(i).getUUID().equals(gr.get(index)))
						{
							p2 = orderedPlayers.get(i);
							break;
						}
					}

				}
				Log.e("LT", "Match added: "+p1.getName()+","+p2.getName());
				matches.add(new Match(p1, p2, ret));
				index--;
			}

		}
		else
		{
			//after first round
			//retrieve the graph
			ArrayList<ArrayList<UUID>> graph = t.getGraph();
			
			//determine if the graph is big enough
			while(graph.size()<=round)
			{
				//graph needs to be expanded
				graph = this.extendGraph(graph);
			}
			
			ArrayList<UUID> gr = graph.get(round);

			
			int index=gr.size()-1;
			//create the list of matches
			for(int m=0;m<gr.size()/2;m++)
			{

				RoundRobinPlayer p1 =null;
				RoundRobinPlayer p2 =null;
				if(gr.get(m).equals(Player.BYE))
				{
					p1 = RoundRobinPlayer.BYE;
				}
				else
				{
					for(int i = 0;i<orderedPlayers.size();i++)
					{
						if(orderedPlayers.get(i).getUUID().equals(gr.get(m)))
						{
							p1 = orderedPlayers.get(i);
							break;
						}
					}
				}
				if(gr.get(index).equals(Player.BYE))
				{
					p2 = RoundRobinPlayer.BYE;
				}
				else
				{
					for(int i = 0;i<orderedPlayers.size();i++)
					{
						if(orderedPlayers.get(i).getUUID().equals(gr.get(index)))
						{
							p2 = orderedPlayers.get(i);
							break;
						}
					}
				}

				if(round%2==1&&m==0)
				{
					//swap p1 and p2
					//if it is an odd round and its the first matchup
					RoundRobinPlayer temp = p1;
					p1=p2;
					p2=temp;
				}
				matches.add(new Match(p1, p2, ret));
				index--;
			}
		}

		ret.setMatches(matches);
		return ret;
	}

	/**
	 * Extends the graph to an extra round
	 * @param graph the graph to extend
	 * @return the new graph
	 */
	private ArrayList<ArrayList<UUID>> extendGraph(ArrayList<ArrayList<UUID>> graph) 
	{

		//update the graph
		int round = graph.size();
		//pull out the indicies
		List<UUID> indicies = graph.get(round-1);

		//remove last player of list
		UUID fixed = indicies.remove(indicies.size()-1);

		//if indicies is even, add a bye indicies
		if(indicies.size()%2==0)
		{
			indicies.add(Player.BYE);
		}
		
		//update indicies n/2-1 shifts
		int n = (indicies.size()+1)/2-1;
		for(int f=0;f<n;f++)
		{
			UUID temp = indicies.remove(0);
			indicies.add(temp);
		}
		
		//create the round
		ArrayList<UUID> r = new ArrayList<UUID>(indicies.size());
		r.addAll(indicies);
		r.add(fixed);

		//add to the graph
		graph.add(r);

		return graph;
	}
	@Override
	public void addPlayer(RoundRobinPlayer add,RoundRobinTournament tournament) {

		ArrayList<ArrayList<UUID>> graph = tournament.getGraph();
		//two cases: 

		//Case 1: Either there is a BYE and this player can be put there
		List<Match> matches = tournament.getRounds().get(tournament.getRounds().size()-1).getMatches();
		boolean byeFound = false;
		for(int j=0;j<matches.size();j++)
		{
			if(matches.get(j).getPlayerOne().equals(RoundRobinPlayer.BYE))
			{
				//Player 1 is a Bye
				//add = new SwissPlayer(p.get(i));
				Match temp = matches.get(j);
				//remove temp p 2 old match
				temp.getPlayerTwo().getMatchesPlayed().remove(temp.getPlayerTwo().getMatchesPlayed().size()-1);
				matches.set(j,new Match(add, temp.getPlayerTwo(), tournament.getRounds().get(tournament.getRounds().size()-1)));

				tournament.notifyChanged();
				byeFound = true;

				//Systematically replace the BYE with the new player
				//bye location is same as match number since first player
				int byeIndex = j;
				
				//starting at current place in graph, update
				graph = tournament.getGraph();
				int round = tournament.getRounds().size()-1;
				
				
				tournament.printGraph();
				
				for(int i=round;i<graph.size();i++)
				{
					//update graph
					graph.get(i).set(byeIndex, add.getUUID());
					byeIndex--;
					if(byeIndex ==-1)
					{
						byeIndex = graph.size()-2;
					}
				}
				tournament.printGraph();
				
				
				break;
			}
			else if(matches.get(j).getPlayerTwo().equals(RoundRobinPlayer.BYE))
			{
				//Player 2 is a Bye
				//SwissPlayer add = new SwissPlayer(p.get(i));
				Match temp = matches.get(j);
				//remove temp p 1 old match
				temp.getPlayerOne().getMatchesPlayed().remove(temp.getPlayerOne().getMatchesPlayed().size()-1);
				matches.set(j,new Match(temp.getPlayerOne(), add, tournament.getRounds().get(tournament.getRounds().size()-1)));

				tournament.notifyChanged();

				byeFound = true;
				//Systematically replace the BYE with the new player
				//bye location is same as match number since first player
				int byeIndex = matches.size()*2-j-1;
				
				//starting at current place in graph, update
				graph = tournament.getGraph();
				int round = tournament.getRounds().size()-1;
				
				
				tournament.printGraph();
				
				for(int i=round;i<graph.size();i++)
				{
					//update graph
					graph.get(i).set(byeIndex, add.getUUID());
					byeIndex--;
					if(byeIndex ==-1)
					{
						byeIndex = graph.size()-2;
					}
				}
				tournament.printGraph();

				break;
			}

		}

		//Case 2: no BYEs, and a new match must be added
		if(!byeFound)
		{
			//SwissPlayer add = new SwissPlayer(p.get(i));
			matches.add(new Match(add, RoundRobinPlayer.BYE, tournament.getRounds().get(tournament.getRounds().size()-1)));
			//update graph
			//need to generate current loc in graph to num rounds
			int numRounds = tournament.conf.getNumRounds();
			int round = tournament.getRounds().size()-1;

			//pull out the indicies
			List<UUID> indicies = graph.get(round);

			//remove wrong parts of graph
			for(int i=round;i<graph.size();i++)
			{
				graph.remove(i);
				i--;
			}
			

			//add the new player and bye to middle of list
			indicies.add(indicies.size()/2,Player.BYE);
			indicies.add(indicies.size()/2,add.getUUID());
			

			//remove last player of list
			UUID fixed = indicies.remove(indicies.size()-1);

			//create the rounds
			for(int i=round;i<numRounds;i++)
			{
				//create the rounds
				ArrayList<UUID> r = new ArrayList<UUID>(indicies.size());
				r.addAll(indicies);
				r.add(fixed);

				//add to the graph
				graph.add(r);
				
				//update indicies n/2-1 shifts
				int n = (indicies.size()+1)/2-1;
				for(int f=0;f<n;f++)
				{
					UUID temp = indicies.remove(0);
					indicies.add(temp);
				}

			}

			tournament.setGraph(graph);
			tournament.printGraph();
		}
	}
}
