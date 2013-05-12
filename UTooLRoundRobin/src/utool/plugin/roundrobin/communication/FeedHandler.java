package utool.plugin.roundrobin.communication;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import utool.plugin.Player;
import utool.plugin.activity.AbstractIncomingCommandHandler;

/**
 * This class contains the actual logic for parsing, for the SaxFeedPaser.
 * It will call the appropriate IncomingCommandHandler method when it 
 * determines the received message.
 * 
 * General Strategy:
 * 1. save all the tags values and attributes as it goes through
 * 2. once an end tag is hit, determine what the tag was for
 * 	a. Determine if the correct information was received
 *  b. if so, call the correct command handler method
 *  
 * @author waltzm
 * @version 10/20/2012
 */
public class FeedHandler extends DefaultHandler
{
	/**
	 * Holds the list of messages in the document
	 */
	private List<ArrayList<Tag>> messages;

	//	private Class ch = new IncomingCommandHandler();
	/**
	 * Holds the message of the current tag
	 */
	private String tempVal;

	/**
	 * Holds the attribute of the current tag
	 */
	private String tempTag="";

	/**
	 * Holds the inner tags of each larger tag
	 */
	private ArrayList<Tag> tempMsg;

	/**
	 * Holds the incoming command handler to make calls to
	 */
	private AbstractIncomingCommandHandler handler;


	/**
	 * Constructor that takes in the handler to make calls to
	 * @param handler the Incoming Command Handler to make calls to
	 */
	public FeedHandler(AbstractIncomingCommandHandler handler)
	{
		this.handler = handler;	
	}
	/**
	 * Getter for the full list of received messages in the last/current document
	 * @return List of messages
	 */
	public List<ArrayList<Tag>> getMessages(){
		return this.messages;
	}


	//Event Handlers

	/**
	 * Called whenever a begin element tag is found in the document.
	 * The Value of the tag is stored in tempTag and tempVal is cleared, 
	 * if the tag is a command.
	 */
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		//reset tempval
		tempVal = "";
		if(localName.equalsIgnoreCase("command")) 
		{
			//	create a new instance of the command tag
			tempTag = attributes.getValue("type");
		}
	}

	/**
	 * tempVal receives the attribute
	 */
	public void characters(char[] ch, int start, int length) throws SAXException 
	{
		tempVal = new String(ch,start,length);
	}

	/**
	 * This method is called whenever an end tag is found in the document
	 * The type of command is then determined and handled if received correctly
	 */
	@SuppressWarnings("unchecked")
	public void endElement(String uri, String localName, String qName) throws SAXException {

		//Log.i("stuff", "uri: "+uri);
		//	Log.i("stuff", "local: "+localName);//correct one to use
		//Log.i("stuff", "qname: "+qName);

		if(localName.equalsIgnoreCase("command")) 
		{
			//add it to the list since the command tag has finished
			messages.add((ArrayList<Tag>)(tempMsg.clone()));

			//Handle the last tag
			//tempTag holds the type of the command
			//Step 1 determine proper handler(through long if else chain), retrieve info and call appropriate incomingCommandHandler
			if(tempTag.equals("sendMatchup"))
			{

				/* 
				 * Command of sendMatchup received
				 * passed in is the tournament id, the match id, the round, the table,
				 * and the two teams competing
				 */
				String matchup="-1";
				String id="-1";
				String round = "-1";
				String table = null;
				String teamName1 = null;
				String teamName2 = null;
				ArrayList<String> players1 = new ArrayList<String>();
				ArrayList<String> players2 = new ArrayList<String>();

				for(int i =tempMsg.size()-1;i>-1; i--)
				{
					if(tempMsg.get(i).getAttr().equals("Matchup"))
					{
						matchup = tempMsg.get(i).getMsg();
					}
					else if(tempMsg.get(i).getAttr().equals("id"))
					{
						id = tempMsg.get(i).getMsg();
					}
					else if(tempMsg.get(i).getAttr().equals("Round"))
					{
						round = tempMsg.get(i).getMsg();
					}
					else if(tempMsg.get(i).getAttr().equals("Table"))
					{
						table = tempMsg.get(i).getMsg();
					}
					else if(tempMsg.get(i).getAttr().equals("Team1"))
					{
						//go to next element
						i--;
						//pull out players and name until not either name or players
						while(i>-1&&(tempMsg.get(i).getAttr().equals("Name")||tempMsg.get(i).getAttr().equals("Player")))
						{
							if(tempMsg.get(i).getAttr().equals("Name"))
							{
								teamName1 = tempMsg.get(i).getMsg();
							}
							else
							{
								//attr is Player so add to list
								players1.add(tempMsg.get(i).getMsg());
							}
							i--;
						}

						//update i
						i++;

					}
					else if(tempMsg.get(i).getAttr().equals("Team2"))
					{
						//go to next element
						i--;
						//pull out players and name until not either name or players
						while(i>-1&&(tempMsg.get(i).getAttr().equals("Name")||tempMsg.get(i).getAttr().equals("Player")))
						{
							if(tempMsg.get(i).getAttr().equals("Name"))
							{
								teamName2 = tempMsg.get(i).getMsg();
							}
							else
							{
								//attr is Player so add to list
								players2.add(tempMsg.get(i).getMsg());
							}
							i--;
						}

						//update i
						i++;

					}
				}		

				handler.handleReceiveMatchup(Long.parseLong(id), Long.parseLong(matchup), teamName1, teamName2, players1, players2, Integer.parseInt(round), table);
			}
			else if(tempTag.equals("sendScore"))
			{
				/* 
				 * Command of sendScore received
				 * passed in is the tournament id, the match id, the round
				 * and the two teams competing and the scores
				 */
				String matchup="-1";
				String id="-1";
				String round = "-1";
				String teamName1 = "-1";
				String teamName2 = "-1";
				String score1="-1";
				String score2="-1";

				for(int i =tempMsg.size()-1;i>-1; i--)
				{
					if(tempMsg.get(i).getAttr().equals("Matchup"))
					{
						matchup = tempMsg.get(i).getMsg();
					}
					else if(tempMsg.get(i).getAttr().equals("id"))
					{
						id = tempMsg.get(i).getMsg();
					}
					else if(tempMsg.get(i).getAttr().equals("Round"))
					{
						round = tempMsg.get(i).getMsg();
					}
					else if(tempMsg.get(i).getAttr().equals("Team1"))
					{
						//go to next element
						i--;
						//pull out players and name until not either name or players
						while(i>-1&&(tempMsg.get(i).getAttr().equals("Name")||tempMsg.get(i).getAttr().equals("Score")))
						{
							if(tempMsg.get(i).getAttr().equals("Name"))
							{
								teamName1 = tempMsg.get(i).getMsg();
							}
							else
							{
								//attr is Score so add to list
								score1 = tempMsg.get(i).getMsg();
							}
							i--;
						}

						//update i
						i++;

					}
					else if(tempMsg.get(i).getAttr().equals("Team2"))
					{
						//go to next element
						i--;
						//pull out players and name until not either name or players
						while(i>-1&&(tempMsg.get(i).getAttr().equals("Name")||tempMsg.get(i).getAttr().equals("Score")))
						{
							if(tempMsg.get(i).getAttr().equals("Name"))
							{
								teamName2 = tempMsg.get(i).getMsg();
							}
							else
							{
								//attr is Score so add to list
								score2 = tempMsg.get(i).getMsg();
							}
							i--;
						}

						//update i
						i++;

					}
				}		

				handler.handleReceiveScore(Long.parseLong(id), Long.parseLong(matchup), teamName1, teamName2, score1, score2, Integer.parseInt(round));
			}
			else if(tempTag.equals("changeMatchup"))
			{
				//	Log.e("FH","Change matchup command");
				/* 
				 * Command of changeMatchup received
				 * passed in is the tournament id, the match id, the round, the table,
				 * and the two teams competing
				 */
				String matchup="-1";
				String id="-1";
				String round = "-1";
				String table = null;
				String teamName1 = null;
				String teamName2 = null;
				ArrayList<String> players1 = new ArrayList<String>();
				ArrayList<String> players2 = new ArrayList<String>();

				for(int i =tempMsg.size()-1;i>-1; i--)
				{
					//	Log.e("FH","in first loop. i: "+i+", attr: "+tempMsg.get(i).getAttr()+", Msg: "+tempMsg.get(i).getMsg());
					if(tempMsg.get(i).getAttr().equals("Matchup"))
					{
						matchup = tempMsg.get(i).getMsg();
					}
					else if(tempMsg.get(i).getAttr().equals("id"))
					{
						id = tempMsg.get(i).getMsg();
					}
					else if(tempMsg.get(i).getAttr().equals("Round"))
					{
						round = tempMsg.get(i).getMsg();
					}
					else if(tempMsg.get(i).getAttr().equals("Table"))
					{
						table = tempMsg.get(i).getMsg();
					}
					else if(tempMsg.get(i).getAttr().equals("Team1"))
					{
						i--;
						//pull out players and name until not either name or players
						while(i>-1&&(tempMsg.get(i).getAttr().equals("Name")||tempMsg.get(i).getAttr().equals("Player")))
						{
							if(tempMsg.get(i).getAttr().equals("Name"))
							{
								teamName1 = tempMsg.get(i).getMsg();
							}
							else
							{
								//attr is Player so add to list
								players1.add(tempMsg.get(i).getMsg());
							}
							i--;
							//	Log.i("FH","stuck in team 1");
						}

						//update i
						i++;

					}
					else if(tempMsg.get(i).getAttr().equals("Team2"))
					{
						i--;
						//pull out players and name until not either name or players
						while(i>-1&&(tempMsg.get(i).getAttr().equals("Name")||tempMsg.get(i).getAttr().equals("Player")))
						{
							if(tempMsg.get(i).getAttr().equals("Name"))
							{
								teamName2 = tempMsg.get(i).getMsg();
							}
							else
							{
								//attr is Player so add to list
								players2.add(tempMsg.get(i).getMsg());
							}
							i--;
							//		Log.i("FH","stuck in team 2");
						}

						//update i
						i++;

					}
				}		

				handler.handleChangeMatchup(Long.parseLong(id), Long.parseLong(matchup), teamName1, teamName2, players1, players2, Integer.parseInt(round), table);
			}
			else if(tempTag.equals("sendPlayers"))
			{
				/* 
				 * Command of send players received
				 * passed in is the tournament id, 
				 * and the players
				 */

				String id="-1";

				ArrayList<Player> players = new ArrayList<Player>();

				for(int i =tempMsg.size()-1;i>-1; i--)
				{
					if(tempMsg.get(i).getAttr().equals("id"))
					{
						id = tempMsg.get(i).getMsg();
					}
					else if(tempMsg.get(i).getAttr().equals("Player"))
					{
						//pull out the player information
						UUID playerid = null;
						String name=null;
						boolean isGhost=false;
						int seed =-1;
						int perm=Player.PARTICIPANT;
						//go to next element
						i--;
						while(i>-1&&(tempMsg.get(i).getAttr().equals("PlayerId")||tempMsg.get(i).getAttr().equals("Name")||tempMsg.get(i).getAttr().equals("isGhost")||tempMsg.get(i).getAttr().equals("Seed")||tempMsg.get(i).getAttr().equals("permissionLevel")))
						{
							if(tempMsg.get(i).getAttr().equals("Name"))
							{
								name = tempMsg.get(i).getMsg();
							}
							else if(tempMsg.get(i).getAttr().equals("PlayerId"))
							{
								playerid = UUID.fromString(tempMsg.get(i).getMsg());
							}
							else if(tempMsg.get(i).getAttr().equals("isGhost"))
							{
								isGhost = Boolean.parseBoolean(tempMsg.get(i).getMsg());
							}
							else if(tempMsg.get(i).getAttr().equals("Seed"))
							{
								seed = Integer.parseInt(tempMsg.get(i).getMsg());
							}
							else if(tempMsg.get(i).getAttr().equals("permissionLevel"))
							{
								perm = Integer.parseInt(tempMsg.get(i).getMsg());
							}
							i--;

						}
						
						
						Log.e("FH","Id: "+playerid);
						
						//create and add player
						Player p = new Player(playerid, name, isGhost, seed);
						
						p.setPermissionsLevel(perm);
						players.add(p);
						//update i
						i++;

					}

				}		

				handler.handleReceivePlayers(id, players);

			}
			else if(tempTag.equals("sendFinalStandings"))
			{
				/* 
				 * Command of sendFinalStandings received
				 * passed in is the tournament id, 
				 * and the players. Each player index corresponds to the wins
				 * losses, round wins, round losses and final standing
				 */
				String id="-1";

				ArrayList<String> players = new ArrayList<String>();
				ArrayList<Double> w = new ArrayList<Double>();
				ArrayList<Double> l = new ArrayList<Double>();
				ArrayList<Integer> rw = new ArrayList<Integer>();
				ArrayList<Integer> rl = new ArrayList<Integer>();
				ArrayList<Integer> s = new ArrayList<Integer>();
				int index=0;

				for(int i =tempMsg.size()-1;i>-1; i--)
				{					
					if(tempMsg.get(i).getAttr().equals("id"))
					{
						id = tempMsg.get(i).getMsg();
					}
					else if(tempMsg.get(i).getAttr().equals("Player"))
					{
						//pull out the player information
						//increase i since tempMsg[i] was Player
						i--;
						while(i>-1&&(tempMsg.get(i).getAttr().equals("PlayerId")||tempMsg.get(i).getAttr().equals("Wins")||tempMsg.get(i).getAttr().equals("Losses")||tempMsg.get(i).getAttr().equals("RoundWins")||tempMsg.get(i).getAttr().equals("RoundLosses")||tempMsg.get(i).getAttr().equals("Standing")))
						{

							if(tempMsg.get(i).getAttr().equals("Wins"))
							{
								w.add(index, Double.parseDouble(tempMsg.get(i).getMsg()));
							}
							else if(tempMsg.get(i).getAttr().equals("PlayerId"))
							{
								players.add(index, tempMsg.get(i).getMsg());
							}
							else if(tempMsg.get(i).getAttr().equals("Losses"))
							{
								l.add(index, Double.parseDouble(tempMsg.get(i).getMsg()));
							}
							else if(tempMsg.get(i).getAttr().equals("RoundWins"))
							{
								rw.add(index, Integer.parseInt(tempMsg.get(i).getMsg()));
							}
							else if(tempMsg.get(i).getAttr().equals("RoundLosses"))
							{
								rl.add(index, Integer.parseInt(tempMsg.get(i).getMsg()));
							}
							else if(tempMsg.get(i).getAttr().equals("Standing"))
							{
								s.add(index, Integer.parseInt(tempMsg.get(i).getMsg()));
							}
							i--;
						}



						//increment index since player added
						index++;

						//update i
						i++;
					}

				}		


				handler.handleReceiveFinalStandings(id, players, w, l, rw, rl, s);
			}
			else if(tempTag.equals("sendRoundTimerAmount"))
			{
				/*
				 * Command of sendRoundTimerAmount received
				 * passed in is the tournament id
				 * and the time amount
				 */
				String time="-1";
				String id="-1";

				for(int i =0;i<tempMsg.size(); i++)
				{
					if(tempMsg.get(i).getAttr().equals("Time"))
					{
						time = tempMsg.get(i).getMsg();
					}
					else if(tempMsg.get(i).getAttr().equals("id"))
					{
						id = tempMsg.get(i).getMsg();
					}

				}		

				handler.handleReceiveRoundTimerAmount(Long.parseLong(id), time);
			}
			else if(tempTag.equals("sendClear"))
			{
				/*
				 * Command of sendClear received
				 * passed in is the tournament id
				 */
				String id="-1";

				for(int i =0;i<tempMsg.size(); i++)
				{
					if(tempMsg.get(i).getAttr().equals("id"))
					{
						id = tempMsg.get(i).getMsg();
					}

				}		

				handler.handleReceiveClear(Long.parseLong(id));
			}
			else if(tempTag.equals("sendTournamentName"))
			{
				/*
				 * Command of sendTournamentName received
				 * passed in is the tournament id
				 * and the name of the tournament
				 */
				String name="-1";
				String id="-1";

				for(int i =0;i<tempMsg.size(); i++)
				{
					if(tempMsg.get(i).getAttr().equals("Name"))
					{
						name = tempMsg.get(i).getMsg();
					}
					else if(tempMsg.get(i).getAttr().equals("id"))
					{
						id = tempMsg.get(i).getMsg();
					}

				}		

				handler.handleReceiveTournamentName(Long.parseLong(id), name);
			}
			else if(tempTag.equals("sendBeginNewRound"))
			{
				/*
				 * Command of sendBeginRound received
				 * passed in is the tournament id
				 * and the round that is beginning
				 */
				String round="-1";
				String id="-1";

				for(int i =0;i<tempMsg.size(); i++)
				{
					if(tempMsg.get(i).getAttr().equals("Round"))
					{
						round = tempMsg.get(i).getMsg();
					}
					else if(tempMsg.get(i).getAttr().equals("id"))
					{
						id = tempMsg.get(i).getMsg();
					}

				}		

				handler.handleReceiveBeginNewRound(Long.parseLong(id), Integer.parseInt(round));
			}
			else if(tempTag.equals("sendError"))
			{
				/*
				 * Command of sendRoundTimerAmount received
				 * passed in is the tournament id
				 * and the time amount
				 */
				String playerid="-1";
				String id="-1";
				String errorName="-1";
				String errorMessage="-1";

				for(int i =0;i<tempMsg.size(); i++)
				{
					if(tempMsg.get(i).getAttr().equals("PlayerId"))
					{
						playerid = tempMsg.get(i).getMsg();
					}
					else if(tempMsg.get(i).getAttr().equals("id"))
					{
						id = tempMsg.get(i).getMsg();
					}
					else if(tempMsg.get(i).getAttr().equals("ErrorName"))
					{
						errorName = tempMsg.get(i).getMsg();
					}
					else if(tempMsg.get(i).getAttr().equals("ErrorMessage"))
					{
						errorMessage = tempMsg.get(i).getMsg();
					}

				}		

				handler.handleReceiveError(Long.parseLong(id), playerid, errorName, errorMessage);
			}

		}else 
		{
			//is a sub tag and therefore should be saved
			//add a tag with the type and msg
			tempMsg.add(new Tag(localName,tempVal.trim()));
		}

	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		//clear lists
		messages = new ArrayList<ArrayList<Tag>>();
		tempMsg = new ArrayList<Tag>();
	}




}
