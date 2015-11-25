package com.aionemu.gameserver.custom;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * @author Woge
 */

public class BattleService {

	public static BattleService instance = new BattleService();
	private final static Logger log = LoggerFactory.getLogger(BattleService.class);
	private boolean isPublic = false;
	private List<Player> invitedPlayers = new LinkedList<Player>();
	private Map<Player, GameEvent> registeredPlayers = new HashMap<Player, GameEvent>();
	private int rewardID = 0;
	

	public BattleService() {
	}
	
	public int getRewardId() {
		return rewardID;
	}
	
	public void setReardID(int reward) {
		this.rewardID = reward;
	}
	
	public void undoInvites() {
		invitedPlayers.clear();
	}

	public GameEventType getEventType (String gameEventType, boolean createNew) {
		gameEventType = "EVENT_" + gameEventType;

		GameEventType type = GameEventType.valueOf(gameEventType);

		if (type == null) {
			return null;
		}

		if (createNew) {
			type.createGameEvent();
			announceToAllPlayers("A new " + type.getViewableName() + " will start Soon! \n Make sure to join your faction by using .evreg!");
		}
		return type;

	}
	
	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
		log("Battlegrounds have been set to " + (isPublic ? "Public" : "Invitation"));
	}
	
	public void onPlayerLogout(Player player) {
		invitedPlayers.remove(player);
		unregisterPlayer(player);
	}
	
	public void unregisterPlayer(Player player) {
		if(registeredPlayers.containsKey(player)) {
			GameEvent event = registeredPlayers.get(player);
			event.unregisterParticipant(player);
		}
	}
	
	public boolean isPublic() {
		return isPublic;
	}
	
	public void destroyEvents() {
		for(GameEventType type : GameEventType.values()) {
			for(GameEvent event : type.getActiveSubEvents()) {
				type.unsetActiveEvent(event);
			}
		}
	}
	
	public boolean invitePlayer(Player player) {
		if(invitedPlayers.contains(player)) {
			return false;
		}
		PacketSendUtility.sendMessage(player, "You have been Invited to an Event Test! \n"
			+ " Wait until the Event is Announced and use the .evreg command to register by typing .evreg <event id>");
		return invitedPlayers.add(player);
	}
	
	public boolean isInvited(Player player) {
		if(invitedPlayers.contains(player)) {
			return true;
		} else {
			return false;
		}
	}
	
	public void announceToAllPlayers(String message) {
		if(!this.isPublic()) {
			for(Player player : this.invitedPlayers) {
				PacketSendUtility.sendBrightYellowMessageOnCenter(player, message);
			}
			return;
		}
				
		Iterator<Player> iter = World.getInstance().getPlayersIterator();

		while (iter.hasNext()) {
			PacketSendUtility.sendBrightYellowMessageOnCenter(iter.next(), message);
		}
	}

	public String getCurrentEvents(Player player) {
		String result = "";
		for (GameEventType type : GameEventType.values()) {
			if(type.getActiveSubEvents().size() > 0) {
				result += "# " + type.getViewableName() + " [" + type.getID() + "] \n";
			}		
		}
		return result;
	}

	public void registerPlayer(Player player, GameEventType eventType) {
		GameEvent matchingEvent = eventType.getGameEventByPriority(player);
		if (matchingEvent == null) {
			PacketSendUtility.sendMessage(player, "Sorry, there is no such event avaiable at the moment");
			return;
		}
		if (!matchingEvent.registerPlayer(player)) {
			PacketSendUtility.sendMessage(player, "There is a ZergRush on the way in this Event. Currently no Place for you in there!");
		} else {
			PacketSendUtility.sendMessage(player, "You succesfully registered for the Event. Waiting for more Players to Join...");
		  registeredPlayers.put(player, matchingEvent);
		}
	}

	public static void sendBattleNotice(Player player, String notice) {
		sendBattleNotice(player, notice, "Beyond Aion");
	}
	
	public static void sendBattleNotice(Player player, String notice, String sender) {
		PacketSendUtility.sendPacket(player, new SM_MESSAGE(0, sender, notice, ChatType.COMMAND));
	}
	
	public String getAllAvaiableEvents() {
		String result = "";
		for(GameEventType type : GameEventType.values()) {
			result += "# " + type.getViewableName() + "[ " + type.getID() + "] \n";
		}
		return result;
	}

	public void log(String entry) {
		log.info("[BattleService] " + entry);
	}

	public void logError(String entry, Exception ex) {
		log.error("[BattleService] " + entry, ex);
	}

	public static final BattleService getInstance() {
		return instance;
	}

}
