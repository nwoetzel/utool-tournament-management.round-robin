package utool.plugin.roundrobin.roundgenerator;

import java.util.List;

import utool.plugin.roundrobin.Round;
import utool.plugin.roundrobin.RoundRobinPlayer;
import utool.plugin.roundrobin.RoundRobinTournament;

/**
 * This interface defines a round generator for a Round Robin System tournament
 * @author Justin Kreier
 * @version 1/31/2013
 */
public abstract class RoundGenerator {

	/**
	 * Generates a round from the list of round robin players
	 * @param orderedPlayers The list of round robin players
	 * @param tournament Reference to the tournament the round is being generated for
	 * @return The next round
	 */
	public abstract Round generateRound(List<RoundRobinPlayer> orderedPlayers, RoundRobinTournament tournament);
	


	/**
	 * Adds a player to the tournament starting in the current round
	 * @param player the player to add
	 * @param tournament Reference to the tournament
	 */
	public abstract void addPlayer(RoundRobinPlayer player, RoundRobinTournament tournament);
	

}
