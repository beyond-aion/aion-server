package admincommands;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.MailDAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;


/**
 * @author ViAl
 *
 */
public class CleanMail extends AdminCommand {

	private static final String SYNTAX = "Syntax: //cleanmail player_name";

	public CleanMail() {
		super("cleanmail");
	}

	@Override
	public void execute(Player player, String... params) {
		try {
			String pName = params[0];
			if(DAOManager.getDAO(MailDAO.class).cleanMail(pName))
				PacketSendUtility.sendMessage(player, "Mailbox for "+pName+" cleaned.");
			else
				PacketSendUtility.sendMessage(player, SYNTAX);
		}
		catch(Exception e) {
			PacketSendUtility.sendMessage(player, SYNTAX);
		}
	}

}
