package playercommands;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Gatherable;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.QuestTemplate;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.PlayerCommand;

/**
 * @author Neon
 */
public class Id extends PlayerCommand {

	public Id() {
		super("id", "Shows item/quest/npc IDs.");

		// @formatter:off
		setSyntaxInfo(
			" - Shows the ID of the selected object.",
			"<item|quest> - Shows the ID of the specified item or quest."
		);
		// @formatter:on
	}

	@Override
	public void execute(Player player, String... params) {
		VisibleObject target = player.getTarget();

		if (params.length == 0) {
			if (target == null) {
				sendInfo(player);
				return;
			}

			if (!(target instanceof Npc) && !(target instanceof Gatherable)) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_INVALID_TARGET());
				return;
			}

			sendInfo(player, target.getClass().getSimpleName() + ": " + ChatUtil.path(target, true));
			return;
		} else {
			int id = ChatUtil.getItemId(params[0]);
			if (id != 0) {
				ItemTemplate template = DataManager.ITEM_DATA.getItemTemplate(id);
				if (template == null) {
					sendInfo(player, "Invalid item.");
					return;
				}
				sendInfo(player, "Item: " + template.getL10n() + "\nID: " + id);
				return;
			}

			id = ChatUtil.getQuestId(params[0]);
			if (id != 0) {
				QuestTemplate template = DataManager.QUEST_DATA.getQuestById(id);
				if (template == null) {
					sendInfo(player, "Invalid quest.");
					return;
				}
				sendInfo(player, "Quest: " + template.getL10n() + "\nID: " + id);
				return;
			}
		}

		sendInfo(player);
	}
}
