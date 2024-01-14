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

	private static final char ITEM_ICON = '\uE054'; // bag

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

			if (!player.isStaff() && !(target instanceof Npc) && !(target instanceof Gatherable)) { // regular players only see IDs of npcs and gatherables
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_INVALID_TARGET());
				return;
			}

			String msg = target.getClass().getSimpleName() + ": " + ChatUtil.path(target, true);
			if (player.isStaff()) {
				int staticId = target.getSpawn() == null ? 0 : target.getSpawn().getStaticId();
				msg += " (Object ID: " + target.getObjectId() + (staticId == 0 ? "" : ", Static ID: " + staticId) + ")";
			}
			sendInfo(player, msg);
			return;
		} else {
			int id = ChatUtil.getItemId(params[0]);
			if (id != 0) {
				ItemTemplate template = DataManager.ITEM_DATA.getItemTemplate(id);
				if (template == null) {
					sendInfo(player, "Invalid item.");
					return;
				}
				sendInfo(player, "Item:" + ITEM_ICON + template.getL10n() + "\nID: " + id);
				return;
			}

			id = ChatUtil.getQuestId(params[0]);
			if (id != 0) {
				QuestTemplate template = DataManager.QUEST_DATA.getQuestById(id);
				if (template == null) {
					sendInfo(player, "Invalid quest.");
					return;
				}
				sendInfo(player, "Quest:" + getQuestIcon(template) + template.getL10n() + "\nID: " + id);
				return;
			}
		}

		sendInfo(player);
	}

	private char getQuestIcon(QuestTemplate template) {
		switch (template.getCategory()) {
			case EVENT:
				return '\uE039'; // pink
			case MISSION:
				return '\uE037'; // golden
			case IMPORTANT:
			case SIGNIFICANT:
				return '\uE03F'; // dark blue
		}
		return '\uE034'; // light blue
	}
}
