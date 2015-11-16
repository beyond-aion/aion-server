package playercommands;

import java.awt.Color;
import java.time.LocalDateTime;
import java.time.Month;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.StarterPackDAO;
import com.aionemu.gameserver.model.account.Account;
import com.aionemu.gameserver.model.account.PlayerAccountData;
import com.aionemu.gameserver.model.gameobjects.LetterType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.services.mail.SystemMailService;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.PlayerCommand;

/**
 * @author Estrayl
 * TODO: Remove me fast pls by sir!
 */
public class StarterPack extends PlayerCommand {
	
	private final LocalDateTime maxCreationTime = LocalDateTime.of(2015, Month.NOVEMBER, 15, 23, 59, 59);
	private final StarterPackDAO dao = DAOManager.getDAO(StarterPackDAO.class);
	private boolean isRewarded = false;

	public StarterPack() {
		super("starterpack", "Sends the starter pack to the specified player.\n" + ChatUtil.color("ATTENTION!", Color.RED) 
			+ " This will work only one time per account!");

		setParamInfo("<charname> - Sets the specified player as receiver of the pack.");
	}

	@Override
	protected void execute(Player player, String... params) {
		if (params.length == 0) {
			sendInfo(player);
			return;
		}
		
		int receivingPlayerId = dao.loadReceivingPlayer(player);
		int objectId = player.getObjectId();
		if (receivingPlayerId > 0 && objectId != receivingPlayerId)
			return;
		
		String name = Util.convertName(params[0]);
		
		Account acc = player.getClientConnection().getAccount();
		for (PlayerAccountData pad : acc.getSortedAccountsList()) {
			LocalDateTime creationTime = pad.getCreationDate().toLocalDateTime();
			if (!creationTime.isBefore(maxCreationTime))
				continue;
			PlayerCommonData pcd = pad.getPlayerCommonData();
			if (pcd == null)
				continue;
			if (pcd.getName().equals(name)) {
				sendReward(pcd);
				dao.storePlayer(player, objectId);
				PacketSendUtility.sendMessage(player, "Starter Pack successfully sent to character " + pcd.getName());
			}
		}
		
		if (!isRewarded)
			PacketSendUtility.sendMessage(player, "No player with the specified name was found on your account!");
	}
	
	private void sendReward(PlayerCommonData pcd) {
		SystemMailService.getInstance().sendMail("Beyond Aion",	pcd.getName(), "Starter Pack",
			"Greetings Daeva!\n\n" + "With this, you received your exclusive starter package which marks you as a backer of the Beyond Aion project."
				+ " We thank you for your support and wish you the best fun for the future of this server.\n\n" + "Enjoy your stay on Beyond Aion!"
				, 188051867, 1, 0, LetterType.EXPRESS);
		isRewarded = true;
	}
}
