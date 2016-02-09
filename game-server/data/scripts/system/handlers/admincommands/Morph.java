package admincommands;

import org.apache.commons.lang3.StringUtils;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author ATracer
 * @modified aionchs-, Wylovech, Neon
 */
public class Morph extends AdminCommand {

	public Morph() {
		super("morph", "Morphs a player into any npc.");

		setParamInfo(
			" - morphs you into the npc you are targeting.",
			"<id> - Morphs your target into the specified npc (0 to cancel)."
		);
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0 && !(admin.getTarget() instanceof Npc)) {
			sendInfo(admin);
			return;
		}

		Player target = admin;
		int npcId;

		if (admin.getTarget() instanceof Player)
			target = (Player) admin.getTarget();

		if (params.length == 0 && admin.getTarget() instanceof Npc) {
			npcId = ((Npc) admin.getTarget()).getNpcId();
		} else {
			try {
				npcId = Integer.parseInt(params[0]);
			} catch (NumberFormatException e) {
				sendInfo(admin);
				return;
			}
		}

		if (npcId < 0 || npcId > 0 && npcId < 200000) {
			sendInfo(admin, "Invalid ID.");
			return;
		}

		target.getTransformModel().apply(npcId);

		if (npcId == 0) {
			sendInfo(admin, "Cancelled" + (target.equals(admin) ? "" : " " + target.getName() + "'s") + " morph.");
		} else {
			NpcTemplate template = npcId > 0 ? DataManager.NPC_DATA.getNpcTemplate(npcId) : null;
			String name = template != null ? ChatUtil.path(StringUtils.capitalize(template.getName()) + " | " + npcId, npcId) : "unknown ID " + npcId;
			sendInfo(admin, "You morphed" + (target.equals(admin) ? "" : " " + target.getName()) + " into " + name + ".");
			if (!target.equals(admin))
				sendInfo(target, ChatUtil.name(admin) + " morphed you into an npc form.");
		}
	}
}
