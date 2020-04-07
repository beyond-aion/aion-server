package admincommands;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.actions.AbstractItemAction;
import com.aionemu.gameserver.model.templates.item.actions.MegaphoneAction;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MEGAPHONE;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author ginho1, Neon
 */
public class Megaphone extends AdminCommand {

	private final List<MegaphoneChatColor> colors;

	public Megaphone() {
		super("megaphone", "Sends a message to the global faction chat (client must be started with -megaphone to show the megaphone chat window).");

		colors = collectColors();

		// @formatter:off
		setSyntaxInfo(
			"<none|elyos|asmo> <name> <message> - Sends the message with given sender name and faction prefix.",
			"<color ID> <none|elyos|asmo> <name> <message> - Sends the message in the color of given color ID.",
			"Color IDs: " + colorIds()
		);
		// @formatter:on
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length < 3) {
			sendInfo(admin);
			return;
		}

		int i = 0;
		int colorIndex = params[i].matches("\\d+") ? Integer.parseInt(params[i++]) - 1 : 0;
		if (colorIndex >= colors.size()) {
			sendInfo(admin, "Invalid color ID.");
			return;
		}
		int megaphoneItemId = colors.get(colorIndex).megaphoneItemId;
		String label = params[i++].toLowerCase();
		SM_MEGAPHONE.FactionLabel factionLabel;
		if ("none".startsWith(label))
			factionLabel = SM_MEGAPHONE.FactionLabel.NONE;
		else if ("elyos".startsWith(label))
			factionLabel = SM_MEGAPHONE.FactionLabel.ELYOS;
		else if ("asmodians".startsWith(label))
			factionLabel = SM_MEGAPHONE.FactionLabel.ASMODIANS;
		else {
			sendInfo(admin);
			return;
		}
		String sender = params[i++];
		String message = StringUtils.join(params, ' ', i, params.length);

		PacketSendUtility.broadcastToWorld(new SM_MEGAPHONE(factionLabel, sender, message, megaphoneItemId));
	}

	private List<MegaphoneChatColor> collectColors() {
		List<MegaphoneChatColor> colors = new ArrayList<>();
		for (ItemTemplate itemTemplate : DataManager.ITEM_DATA.getItemTemplates()) {
			if (itemTemplate.getActions() != null) {
				for (AbstractItemAction itemAction : itemTemplate.getActions().getItemActions()) {
					if (itemAction instanceof MegaphoneAction && colors.stream().noneMatch(c -> c.color == ((MegaphoneAction) itemAction).getColor()))
						colors.add(new MegaphoneChatColor(itemTemplate.getTemplateId(), ((MegaphoneAction) itemAction).getColor()));
				}
			}
		}
		colors.sort(Comparator.comparingInt((MegaphoneChatColor m) -> m.color).reversed());
		return colors;
	}

	private String colorIds() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < colors.size(); i++) {
			if (sb.length() > 0)
				sb.append(", ");
			sb.append(ChatUtil.color(i + 1 + " █", colors.get(i).color));
		}
		return sb.toString();
	}

	private class MegaphoneChatColor {

		private final int megaphoneItemId;
		private final int color;

		private MegaphoneChatColor(int megaphoneItemId, int color) {
			this.megaphoneItemId = megaphoneItemId;
			this.color = color;
		}
	}
}
