package admincommands;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author ATracer, aionchs-, Wylovech, Neon
 */
public class Morph extends AdminCommand {

	public Morph() {
		super("morph", "Morphs a player into any npc.");

		setSyntaxInfo(
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

		Player target = admin.getTarget() instanceof Player p ? p : admin;
		int npcId;

		if (params.length == 0 && admin.getTarget() instanceof Npc npc) {
			npcId = npc.getNpcId();
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
			sendInfo(admin, "You morphed" + (target.equals(admin) ? "" : " " + target.getName()) + " into " + ChatUtil.path(npcId, true) + ".");
			if (!target.equals(admin))
				sendInfo(target, ChatUtil.name(admin) + " morphed you into an npc form.");
		}
	}
}
