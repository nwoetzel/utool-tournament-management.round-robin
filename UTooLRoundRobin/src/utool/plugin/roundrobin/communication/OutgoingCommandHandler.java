package utool.plugin.roundrobin.communication;

import java.util.List;

import utool.plugin.Player;
import utool.plugin.activity.AbstractOutgoingCommandHandler;
import utool.plugin.activity.AbstractTournament;

/**
 * This class is a concrete outgoing command handler for round robin
 * @author waltzm and kreierj
 * @version 1/25/2013
 */
public class OutgoingCommandHandler extends AbstractOutgoingCommandHandler{
	/**
	 * Constructor for the OutgoingCommandHandler
	 * @param tournamentLogic The tournament to associate this object with
	 */
	public OutgoingCommandHandler(AbstractTournament tournamentLogic)
	{
		super(tournamentLogic);
		//TODO I don't think the next line is needed, but don't want to remove
		this.tournament = tournamentLogic;
	}
	
	/**
	 * Handles the sending of players using a list using the super method.
	 * Allows for a list of players instead of an array like the super method.
	 * @param id The id
	 * @param players The list of players
	 */
	public void handleSendPlayers(long id, List<Player> players){
		//convert arraylist to array
		Player[] playerArray = players.toArray(new Player[players.size()]);
		
		super.handleSendPlayers(id, playerArray);
	}

}
