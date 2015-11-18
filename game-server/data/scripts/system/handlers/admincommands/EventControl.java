package admincommands;

import com.aionemu.gameserver.custom.BattleService;
import com.aionemu.gameserver.custom.GameEventType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author Woge
 */

public class EventControl extends AdminCommand {

	public EventControl() {
		super("evcmd");
	}

	@Override
	public void execute(Player player, String... params) {

		if (params.length < 2) {
			sendInfo(player);
			return;
		}
		

		if (params[0].equals("enable")) {
			
			GameEventType type = BattleService.getInstance().getEventType(params[1], true);
			if(type == null) {
				PacketSendUtility.sendMessage(player, "There is no such event. Please try again.");
			}
		} else if (params[0].equals("set")) {
			if(params[1].equals("test")) {
				BattleService.getInstance().setPublic(false);
			} else if(params[1].equals("public")) {
				BattleService.getInstance().setPublic(true);
			} else if(params[1].equals("reward")) {
				try {
					BattleService.getInstance().setReardID(Integer.parseInt(params[2]));
					PacketSendUtility.sendMessage(player, "Item set for Reward: " + params[2]);
				} catch(NumberFormatException ex) {
					PacketSendUtility.sendMessage(player, "Invalid Item ID!");
				}			
			}
		} else if (params[0].equals("invite")) {
			Player invited = World.getInstance().findPlayer(params[1]);
			if(invited == null) {
				PacketSendUtility.sendMessage(player, "Invited Player does not exist or is offline.");
				return;
			}
			if(BattleService.getInstance().invitePlayer(invited)) {
				PacketSendUtility.sendMessage(player, invited.getName() + " was succesfully invited.");
			} else {
				PacketSendUtility.sendMessage(player, invited.getName() + " was already invited.");
			}
		} else if (params[0].equals("uninvite")) {
			BattleService.getInstance().undoInvites();
			PacketSendUtility.sendMessage(player, "All invitations have been deleted.");
		}	
		
	}

	public void sendInfo(Player player) {
		PacketSendUtility
			.sendMessage(player, "# Welcome to our event manager! # \n This is a list of all existing Events avaiable to enable: ");
		PacketSendUtility.sendMessage(player, BattleService.getInstance().getAllAvaiableEvents());
	}
}
