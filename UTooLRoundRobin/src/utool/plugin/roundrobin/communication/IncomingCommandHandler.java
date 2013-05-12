package utool.plugin.roundrobin.communication;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;
import android.util.Log;
import utool.plugin.Player;
import utool.plugin.activity.AbstractIncomingCommandHandler;
import utool.plugin.activity.AbstractTournament;
import utool.plugin.roundrobin.Match;
import utool.plugin.roundrobin.MatchResult;
import utool.plugin.roundrobin.Round;
import utool.plugin.roundrobin.RoundRobinConfiguration;
import utool.plugin.roundrobin.RoundRobinPlayer;
import utool.plugin.roundrobin.RoundRobinTournament;
import utool.plugin.roundrobin.TournamentActivity;

/**
 * Implementation of the AbstractIncomingCommandHandler for the Round robin tournament.
 * @author Justin Kreier
 * @version 2/7/2013
 */
public class IncomingCommandHandler extends AbstractIncomingCommandHandler{

	/**
	 * True if the handler believes it is out of sync and waiting for an error 
	 */
	private boolean inErrorState;
	
	/**
	 * holds the log tag for this class
	 */
	private static final String LOG_TAG = "Incoming Command Handler RR";

	/**
	 * Code for if the clear is being sent as part of error code handling
	 */
	public static final long RESEND_CLEAR = 235;

	/**
	 * Required constructor
	 * @param t The tournament it is acting on
	 */
	public IncomingCommandHandler(AbstractTournament t) {
		super(t);
		inErrorState = false;
	}

	@Override
	public void handleReceiveMatchup(long id, long matchid, String team1name, String team2name, ArrayList<String> team1, ArrayList<String> team2, int round, String table){
		super.handleReceiveMatchup(id, matchid, team1name, team2name, team1, team2, round, table);
		//if player is not host, update
		if (t.getPermissionLevel() == Player.PARTICIPANT || t.getPermissionLevel() == Player.MODERATOR){
			if (!inErrorState){
				try{
					RoundRobinTournament rr = (RoundRobinTournament)t;

					//get the round the match is meant for
					Round r = rr.getRounds().get(round);

					//retrieve the local players from the tournament
					List<RoundRobinPlayer> players = rr.getRoundRobinPlayers();
					UUID p1 = UUID.fromString(team1.get(0));
					UUID p2 = UUID.fromString(team2.get(0));


					RoundRobinPlayer playerOne = null;
					RoundRobinPlayer playerTwo = null;

					if (p1.equals(RoundRobinPlayer.BYE.getUUID())){
						playerOne = RoundRobinPlayer.BYE;
					}
					if (p2.equals(RoundRobinPlayer.BYE.getUUID())){
						playerTwo = RoundRobinPlayer.BYE;
					}

					for (RoundRobinPlayer p : players){
						if (p.getUUID().equals(p1)){
							playerOne = p;
						}
						if (p.getUUID().equals(p2)){
							playerTwo = p;
						}
						if (playerOne != null && playerTwo != null){
							break;
						}
					}

					Match m = new Match(playerOne, playerTwo, r);
					r.addMatch(m);

				} catch (Exception e){
					Log.e(LOG_TAG, "Exception in handleReceiveMatchup", e);
					sendError(TournamentActivity.RESEND_ERROR_CODE);
				}
			}
		}
	}

	@Override
	public void handleReceiveBeginNewRound(long id, int round){
		super.handleReceiveBeginNewRound(id, round);

		if (t.getPermissionLevel() == Player.PARTICIPANT || t.getPermissionLevel() == Player.MODERATOR){
			if (!inErrorState){
				try{
					RoundRobinTournament rr = (RoundRobinTournament)t;
					List<Round> rounds = rr.getRounds();

					if (round == rounds.size()){
						//means we're adding the correct round
						rounds.add(new Round(round,rr));
						((RoundRobinTournament)t).notifyChanged();
						
						//restart round timer
						if(((RoundRobinTournament) t).getRoundRobinConfiguration().getStartTimerOnRoundChange())
						{
							Log.e(LOG_TAG,"Starting timer for new round");
							((RoundRobinTournament) t).getRoundRobinConfiguration().startTimer();
						}

					} else {
						//something got messed up, throw an exception and panic
						throw new RuntimeException("Tournament desynchronization in handleSendBeginNewRound");
					}
				} catch (Exception e){
					Log.e(LOG_TAG, "Exception in handleReceiveBeginNewRound", e);
					sendError(TournamentActivity.RESEND_ERROR_CODE);
				}
			}
		}
	}

	@Override
	public void handleReceiveScore(long id, long matchid, String team1name, String team2name, String score1, String score2, int round){
		super.handleReceiveScore(id, matchid, team1name, team2name, score1, score2, round);

		if (t.getPermissionLevel() == Player.PARTICIPANT || t.getPermissionLevel() == Player.MODERATOR){
			if (!inErrorState){
				try{
					RoundRobinTournament rr = (RoundRobinTournament)t;
					//get the round and match
					Round r = rr.getRounds().get(round);
					Match m = r.getMatches().get((int)matchid);

					m.setScores(Double.parseDouble(score1), Double.parseDouble(score2));

				} catch (Exception e){
					Log.e(LOG_TAG, "Exception in handleReceiveScore", e);
					sendError(TournamentActivity.RESEND_ERROR_CODE);
				}
			}
		}
		else
		{
			//score from moderator received
			try{
				RoundRobinTournament rr = (RoundRobinTournament)t;
				//get the round and match
				Round r = rr.getRounds().get(round);
				Match m = r.getMatches().get((int)matchid);
				
				//setting scores will re-send it out to connected
				m.setScores(Double.parseDouble(score1), Double.parseDouble(score2));

			} catch (Exception e){
				Log.e(LOG_TAG, "Exception in handleReceiveScore", e);
			}
		}
	}

	@Override
	public void handleReceiveClear(long tid){
		super.handleReceiveClear(tid);
		if (tid == RESEND_CLEAR){
			inErrorState = false;
		}
		if (t.getPermissionLevel() == Player.PARTICIPANT || t.getPermissionLevel() == Player.MODERATOR){
			if (!inErrorState){
				RoundRobinTournament rr = (RoundRobinTournament)t;
				rr.clearTournament();
			}
		}
	}

	@Override
	public void handleReceiveError(long id, String playerid, String name, String message){
		super.handleReceiveError(id, playerid, name, message);
		if ((int)id == TournamentActivity.RESEND_ERROR_CODE && t.getPermissionLevel() == Player.HOST){
			OutgoingCommandHandler out = new OutgoingCommandHandler(t);
			out.handleSendClear(RESEND_CLEAR);
			out.handleSendPlayers(-1, t.getPlayers());

			RoundRobinTournament rr = (RoundRobinTournament)t;

			List<Round> rounds = rr.getRounds(); 

			//for each round
			for (Round r : rounds){
				//send new round
				out.handleSendBeginNewRound(-1, r.getRoundNumber());

				List<Match> matches = r.getMatches();

				//for each match in round
				for (int i = 0; i < matches.size(); i++){
					Match m = matches.get(i);

					RoundRobinPlayer playerOne = m.getPlayerOne();
					RoundRobinPlayer playerTwo = m.getPlayerTwo();

					String[] team1 = new String[1];
					String[] team2 = new String[1];

					team1[0] = playerOne.getUUID().toString();
					team2[0] = playerTwo.getUUID().toString();

					//send new match
					out.handleSendMatchup(-1, i, null, null, team1, team2, r.getRoundNumber(), null);

					//send match score
					if (m.getMatchResult() != MatchResult.UNDECIDED){
						out.handleSendScore(0, i, playerOne.getUUID().toString(), playerTwo.getUUID().toString(), m.getPlayerOneScore(), m.getPlayerTwoScore(), r.getRoundNumber());
					}
				}
			}
		}
	}

	@Override
	public void handleReceivePlayers(String id, ArrayList<Player> players){
		super.handleReceivePlayers(id, players);
		if (t.getPermissionLevel() == Player.PARTICIPANT || t.getPermissionLevel() == Player.MODERATOR){
			if (!inErrorState){
				ArrayList<Player> pNew = RoundRobinTournament.playerListToRoundRobinPlayers(players);
				List<RoundRobinPlayer> old = ((RoundRobinTournament)(t)).getRoundRobinPlayers();
				//instead take new list, iterate through old list, and if player is found
				for(int i=0;i<pNew.size();i++)
				{
					RoundRobinPlayer play = (RoundRobinPlayer) pNew.get(i);

					//check if play is in old list
					for(int j=0;j<old.size();j++)
					{
						//if so
						if(play.equals(old.get(j)))
						{
							//copy over the scores etc. into the new player
							List<Match> matches = play.getMatchesPlayed();
							matches.clear();
							matches.addAll(old.get(j).getMatchesPlayed());
						}
					}
				}
				
				t.setPlayers(pNew);

				//update local pid perrmission level if changed
				for(int i=0;i<players.size();i++)
				{
					if(players.get(i).getUUID().equals(t.getPID()))
					{
						int perm = players.get(i).getPermissionsLevel();
						if(perm==Player.DEVICELESS)
						{
							//connect as is leaving people as deviceless
							//temporary fix
							perm=Player.PARTICIPANT;
						}
						t.setPermissionLevel(perm);
						Log.d("Incoming Command Handler RR", "Player is now at permission level: "+t.getPermissionLevel());
					}
				}
			}
		}
	}

	/**
	 * Responsible for sending an error with an error code to the receiver
	 * @param errorCode The error code to send
	 */
	private void sendError(int errorCode){
		inErrorState = true;
		OutgoingCommandHandler out = new OutgoingCommandHandler(t);
		out.handleSendError(errorCode, "", "", "");
	}

	/**
	 * Notifies the plugin of the time limit for each round, beginning at the next round message.
	 * If any of the times are negative, indicates no round timer.
	 * @param milliElapsedSoFar of the tournament
	 * @param time the time in hh:mm:ss
	 */
	public void handleReceiveRoundTimerAmount(long milliElapsedSoFar, String time)
	{
		super.handleReceiveRoundTimerAmount(milliElapsedSoFar, time);

		RoundRobinConfiguration config = ((RoundRobinTournament)t).getRoundRobinConfiguration();
		StringTokenizer s = new StringTokenizer(time,":");
		if(s.countTokens()!=3)
		{
			//time is malformed
			Log.e(LOG_TAG,"Time received is malformed: "+time);
			return;
		}
		try
		{
			int hour = Integer.parseInt(s.nextToken());
			int min = Integer.parseInt(s.nextToken());
			int sec = Integer.parseInt(s.nextToken());

			config.setRoundTimerSeconds(hour*60*60+min*60+sec);

			config.startTimer(milliElapsedSoFar);

			((RoundRobinTournament)t).notifyChanged();
		}
		catch(Exception e)
		{
			//number formating went wrong, don't do anything
			Log.e(LOG_TAG,"Number formating was incorrect for round timer: "+time);

		}
	}
}
