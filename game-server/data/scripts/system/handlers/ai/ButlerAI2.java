package ai;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.DialogPage;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerScripts;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_HOUSE_SCRIPTS;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Rolandas
 * @reworked Neon
 */
@AIName("butler")
public class ButlerAI2 extends GeneralNpcAI2 {

	@Override
	public boolean onDialogSelect(Player player, int dialogId, int questId, int extendedRewardIndex) {
		return kickDialog(player, DialogPage.getPageByAction(dialogId));
	}

	private boolean kickDialog(Player player, DialogPage page) {
		if (page == DialogPage.NULL)
			return false;

		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getOwner().getObjectId(), page.id()));
		return true;
	}

	@Override
	protected void handleCreatureSee(Creature creature) {
		if (creature instanceof Player) {
			Player player = (Player) creature;
			House house = (House) getCreator();
			PlayerScripts scripts = house.getPlayerScripts();

			fixOwnerClientBug(player, house); // TODO: remove when it's not needed anymore...

			if (scripts.count() > 0)
				PacketSendUtility.sendPacket(player, new SM_HOUSE_SCRIPTS(house.getAddress().getId(), scripts));
		}
	}

	/**
	 * Fixes the display bug a house owner can have, when he logs in directly in his house. (Doesn't happen if he logs in outside and then walks in)<br>
	 * <br>
	 * If you log in to a house and were previously logged in with another char, each script of that char will appear, where the script ID is an ID you
	 * don't have set with this in char.<br>
	 * To avoid this, we send all possible IDs as if they were just deleted, before sending the actual scripts.
	 * 
	 * @param player
	 * @param house
	 */
	private void fixOwnerClientBug(Player player, House house) {
		// do only if player == owner and spawned inside (not walked in)
		if (player.getObjectId() == house.getOwnerId() && player.isProtectionActive()) {
			PlayerScripts emptyScriptContainer = new PlayerScripts(house.getObjectId());
			int[] ids = new int[PlayerScripts.getMaxCount()];
			for (int i = 0; i < PlayerScripts.getMaxCount(); i++)
				ids[i] = i; // this approach is not ideal, but it serves its purpose

			PacketSendUtility.sendPacket(player, new SM_HOUSE_SCRIPTS(house.getAddress().getId(), emptyScriptContainer, ids));
		}
	}
}
