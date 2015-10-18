package playercommands;

import org.apache.commons.lang3.StringUtils;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.QuestTemplate;
import com.aionemu.gameserver.model.templates.VisibleObjectTemplate;
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

		setParamInfo(
			" - Shows the ID of the selected npc.",
			"<item|quest> - Shows the ID of the specified item or quest."
		);
	}

	@Override
	public void execute(Player player, String... params) {
		VisibleObject target = player.getTarget();

		if (params.length == 0) {
			if (target == null) {
				sendInfo(player);
				return;
			}

			if (!(target instanceof Npc)) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_INVALID_TARGET);
				return;
			}

			VisibleObjectTemplate template = target.getObjectTemplate();
			sendInfo(player, target.getClass().getSimpleName() + ": "
				+ ChatUtil.path(StringUtils.capitalize(template.getName()) + " | " + template.getTemplateId(), template.getTemplateId()));
			return;
		} else {
			int id = ChatUtil.getItemId(params[0]);
			if (id != 0) {
				ItemTemplate template = DataManager.ITEM_DATA.getItemTemplate(id);
				if (template == null) {
					sendInfo(player, "Invalid item.");
					return;
				}
				sendInfo(player, "Item name: " + template.getName() + "\nID: " + id);
				return;
			}

			id = ChatUtil.getQuestId(params[0]);
			if (id != 0) {
				QuestTemplate template = DataManager.QUEST_DATA.getQuestById(id);
				if (template == null) {
					sendInfo(player, "Invalid quest.");
					return;
				}
				sendInfo(player, "Quest name: " + template.getName() + "\nID: " + id);
				return;
			}
		}

		sendInfo(player);
	}
}
