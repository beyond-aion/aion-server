package admincommands;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author ATracer, aionchs-, Wylovech, Neon
 */
public class Morph extends AdminCommand {

	public Morph() {
		super("morph", "Morphs a player into any NPC.", """
			 - morphs you into the NPC you are targeting.
			<id> - Morphs your target into the specified NPC (0 to cancel).
			""");
	}

	@Override
	public void execute(Player admin, String... params) {
		Player target = admin.getTarget() instanceof Player p ? p : admin;
		NpcTemplate npcTemplate;
		if (params.length == 0) {
			if (admin.getTarget() == null || admin.equals(admin.getTarget())) {
				sendInfo(admin);
				return;
			}
			if (!(admin.getTarget().getObjectTemplate() instanceof NpcTemplate t)) {
				PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_INVALID_TARGET());
				return;
			}
			npcTemplate = t;
		} else {
			int modelId = Integer.parseInt(params[0]);
			if (modelId == 0) {
				target.getTransformModel().apply(0);
				sendInfo(admin, "Cancelled" + (target.equals(admin) ? "" : " " + name(target) + "'s") + " morph.");
				return;
			}
			npcTemplate = DataManager.NPC_DATA.getNpcTemplate(modelId);
			if (npcTemplate == null) {
				sendInfo(admin, "Invalid ID.");
				return;
			}
		}
		target.getTransformModel().apply(npcTemplate.getTemplateId());
		sendInfo(admin, "You morphed" + (target.equals(admin) ? "" : " " + name(target)) + " into " + npcTemplate.getL10n() + ".");
		if (!target.equals(admin))
			sendInfo(target, name(admin) + " morphed you into " + npcTemplate.getL10n() + ".");
	}
}
