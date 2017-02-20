package admincommands;

import org.apache.commons.lang3.StringUtils;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author ATracer, Wakizashi
 * @modified Neon
 */
public class Kill extends AdminCommand {

	public Kill() {
		super("kill", "Kills the specified NPC(s) or player.");

		// @formatter:off
		setParamInfo(
			" - kills your target (can be NPC or player)",
			"<all> [neutral|enemy] - kills all NPCs in the surrounding area (default: all, optional: only neutral/hostile NPCs)",
			"<range (in meters)> [neutral|enemy] - kills NPCs in the specified radius around you (default: all, optional: only neutral/hostile NPCs)"
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
			if (target instanceof Creature) {
				String targetInfo = target.getClass().getSimpleName().toLowerCase() + ": ";
				if (target instanceof Npc)
					targetInfo += ChatUtil.path((Npc) target);
				else
					targetInfo += StringUtils.capitalize(target.getName());
				if (kill(player, (Creature) target))
					sendInfo(player, "Killed " + targetInfo);
				else
					sendInfo(player, "Couldn't kill " + targetInfo);
			} else {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_INVALID_TARGET());
			}
		} else {
			int count = 0;
			float range = 0;

			if (params[0].equalsIgnoreCase("all")) {
				range = -1;
			} else {
				try {
					range = Float.parseFloat(params[0]);
					if (range < 0) {
						sendInfo(player);
						return;
					}
					// if input was integer, add 0.999 so it matches the clients displayed target distance (client doesn't round up at .5)
					if (range == Math.round(range))
						range += 0.999;
				} catch (NumberFormatException e) {
					sendInfo(player, "Invalid range parameter.");
					return;
				}
			}

			for (VisibleObject obj : player.getKnownList().getKnownObjects().values()) {
				// is npc or summon
				if (obj instanceof Creature && !(obj instanceof Player)) {
					// is in range
					if (range == -1 || (range > 0 && MathUtil.isIn3dRange(player, obj, range))) {
						// is target
						if (params.length <= 1 || (params[1].equalsIgnoreCase("neutral") && !player.isEnemy((Creature) obj))
							|| (params[1].equalsIgnoreCase("enemy") && player.isEnemy((Creature) obj))) {
							if (kill(player, (Creature) obj))
								count += 1;
						}
					}
				}
			}
			sendInfo(player, count + " NPC(s) were killed.");
		}
	}

	private boolean kill(Player attacker, Creature target) {
		if (target.getLifeStats().isAlreadyDead() || target.getLifeStats().isAboutToDie())
			return false;

		target.getController().onAttack(target.isPvpTarget(attacker) && !target.isEnemy(attacker) ? target : attacker, target.getLifeStats().getMaxHp(),
			null);
		return true;
	}
}
