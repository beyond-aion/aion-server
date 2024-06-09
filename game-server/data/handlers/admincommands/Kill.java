package admincommands;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author ATracer, Wakizashi, Neon, Sykra
 */
public class Kill extends AdminCommand {

	public Kill() {
		super("kill", "Kills the specified NPC(s) or player.");

		// @formatter:off
		setSyntaxInfo(
			" - kills your target (can be NPC or player)",
			"<all> [neutral|enemy|npcId] - kills all NPCs in the surrounding area (default: all, optional: only neutral/hostile NPCs/specific NPC)",
			"<range (in meters)> [neutral|enemy|npcId] - kills NPCs in the specified radius around you (default: all, optional: only neutral/hostile NPCs/specific NPC)"
		);
		// @formatter:on
	}

	@Override
	public void execute(Player player, String... params) {
		VisibleObject target = player.getTarget();

		if (params.length > 2 || (params.length == 0 && target == null)) {
			sendInfo(player);
			return;
		}

		if (params.length == 0) {
			if (target instanceof Creature creature) {
				String targetInfo = target.getClass().getSimpleName().toLowerCase() + ": ";
				if (target instanceof Npc)
					targetInfo += ChatUtil.path(target, true);
				else
					targetInfo += StringUtils.capitalize(target.getName());
				if (kill(player, creature))
					sendInfo(player, "Killed " + targetInfo);
				else
					sendInfo(player, "Couldn't kill " + targetInfo);
			} else {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_INVALID_TARGET());
			}
		} else {
			int count = 0;
			float range;
			int npcId = 0;

			if (params[0].equalsIgnoreCase("all")) {
				range = -1;
			} else {
				range = Float.parseFloat(params[0]);
				if (range < 0) {
					sendInfo(player, "The given range must be larger than 0.");
					return;
				}
				// if input was integer, add 0.999 so it matches the clients displayed target distance (client doesn't round up at .5)
				if (range == Math.round(range))
					range += 0.999f;
			}

			if (params.length == 2 && NumberUtils.isDigits(params[1])) {
				npcId = Integer.parseInt(params[1]);
				if (DataManager.NPC_DATA.getNpcTemplate(npcId) == null) {
					sendInfo(player, npcId + " isn't a valid npcId.");
					return;
				}
			}

			for (VisibleObject obj : player.getKnownList().getKnownObjects().values()) {
				// is npc or summon
				if (obj instanceof Creature creature && !(obj instanceof Player)) {
					// is in range
					if (range == -1 || (range > 0 && PositionUtil.isInRange(player, obj, range))) {
						// is target
						if (params.length <= 1 || (params[1].equalsIgnoreCase("neutral") && !player.isEnemy(creature))
							|| (params[1].equalsIgnoreCase("enemy") && player.isEnemy(creature))
							|| (npcId != 0 && creature.getObjectTemplate().getTemplateId() == npcId)) {
							if (kill(player, creature))
								count += 1;
						}
					}
				}
			}
			sendInfo(player, count + " NPC(s) were killed.");
		}
	}

	private boolean kill(Player attacker, Creature target) {
		if (target.isDead() || target.getLifeStats().isAboutToDie())
			return false;

		target.getController().onAttack(target.isPvpTarget(attacker) && !target.isEnemy(attacker) ? target : attacker, target.getLifeStats().getMaxHp(),
			null);
		return true;
	}
}
