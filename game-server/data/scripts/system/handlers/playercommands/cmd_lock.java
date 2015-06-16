package playercommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.loginserver.LoginServer;
import com.aionemu.gameserver.network.loginserver.serverpackets.SM_CHANGE_ALLOWED_HDD_SERIAL;
import com.aionemu.gameserver.network.loginserver.serverpackets.SM_CHANGE_ALLOWED_IP;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.PlayerCommand;


/**
 * @author ViAl
 *
 */
public class cmd_lock extends PlayerCommand {
	
	private static final String SYNTAX = ".lock enable ip\n.lock enable pc\n.lock disable ip\n.lock disable pc";
	
	public cmd_lock() {
		super("lock");
	}
	
	@Override
	public void execute(Player player, String... params) {
		try {
			String cmd = params[0];
			String mode = params[1];
			if(cmd.equalsIgnoreCase("enable")) {
				if(mode.equalsIgnoreCase("ip")) {
					LoginServer.getInstance().sendPacket(new SM_CHANGE_ALLOWED_IP(player.getPlayerAccount().getId(), player.getClientConnection().getIP()));
					PacketSendUtility.sendMessage(player, "You account was locked on "+player.getClientConnection().getIP()+" ip. You will not be able to login on this account with different IP address more.");
				}
				else if(mode.equalsIgnoreCase("pc")) {
					LoginServer.getInstance().sendPacket(new SM_CHANGE_ALLOWED_HDD_SERIAL(player.getPlayerAccount().getId(), player.getClientConnection().getHddSerial()));
					PacketSendUtility.sendMessage(player, "You account was locked on PC which you are logged from now. You will not be able to login on this account from other PC more.");
				}
				else {
					PacketSendUtility.sendMessage(player, SYNTAX);
				}
			}
			else if(cmd.equalsIgnoreCase("disable")) {
				if(mode.equalsIgnoreCase("ip")) {
					LoginServer.getInstance().sendPacket(new SM_CHANGE_ALLOWED_IP(player.getPlayerAccount().getId(), ""));
					PacketSendUtility.sendMessage(player, "IP lock disabled.");
				}
				else if(mode.equalsIgnoreCase("pc")) {
					LoginServer.getInstance().sendPacket(new SM_CHANGE_ALLOWED_HDD_SERIAL(player.getPlayerAccount().getId(), ""));
					PacketSendUtility.sendMessage(player, "PC lock disabled.");
				}
				else {
					PacketSendUtility.sendMessage(player, SYNTAX);
				}
			}
			else {
				PacketSendUtility.sendMessage(player, SYNTAX);
			}
		}
		catch(Exception e) {
			PacketSendUtility.sendMessage(player, SYNTAX);
		}
	}
}
