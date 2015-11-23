package playercommands;

import com.aionemu.gameserver.custom.BattleService;
import com.aionemu.gameserver.custom.GameEventType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.PlayerCommand;

/**
 * 
 * @author Woge
 * 
 */

public class Evreg extends PlayerCommand {

	public Evreg() {
		super("evreg", "Register for an Event!");
		setParamInfo(".evreg <event name>");
	}

	@Override
	public void execute(Player player, String... params) {
		
		if(!BattleService.getInstance().isInvited(player)) {
			PacketSendUtility.sendMessage(player, "Sorry, you are not invited to the Tests.");
			return;
		}

		if(params.length < 1) {
			sendInfo(player);
			return;
		}
				
		BattleService service = BattleService.getInstance();	
		GameEventType playerChoose = service.getEventType(params[0], false);
		if(playerChoose == null) {
			PacketSendUtility.sendMessage(player, "The choosen Event does not exist.");
		}
		service.registerPlayer(player, playerChoose);

	}

	public void sendInfo(Player player) {
		PacketSendUtility.sendMessage(player,
			"# Welcome to our event manager! # \n Type .evreg <event name> to become part of an event!"
				+ "\n The following events are avaiable at this moment:");
		PacketSendUtility.sendMessage(player, BattleService.getInstance().getCurrentEvents(player));
	}
}
