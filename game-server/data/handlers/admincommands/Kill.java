package admincommands;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

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
			"all [neutral|enemy|npcId] - kills all NPCs in the surrounding area (default: all, optional: only neutral/hostile NPCs/specific NPC)",
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
			Predicate<Creature> filter;
			if (params[0].equalsIgnoreCase("all")) {
				filter = _ -> true;
			} else {
				float range = Float.parseFloat(params[0]);
				if (range < 0) {
					sendInfo(player, "The given range must be larger than 0.");
					return;
				}
				// if input was integer, add 0.999 so it matches the client's displayed target distance (client doesn't round up at .5)
				float finalRange = range == Math.round(range) ? range + 0.999f : range;
				filter = creature -> PositionUtil.isInRange(player, creature, finalRange);
			}
			if (params.length == 2) {
				if (params[1].equalsIgnoreCase("neutral")) {
					filter = filter.and(creature -> !player.isEnemy(creature));
				} else if (params[1].equalsIgnoreCase("enemy")) {
					filter = filter.and(player::isEnemy);
				} else {
					int npcId = Integer.parseInt(params[1]);
					filter = filter.and(creature -> creature.getObjectTemplate().getTemplateId() == npcId);
				}
			}
			AtomicInteger count = new AtomicInteger();
			player.getKnownList().stream()
				.filter(obj -> obj.get() instanceof Creature creature && !(creature instanceof Player))
				.map(o -> (Creature) o.get())
				.filter(filter)
				.forEach(creature -> {
					if (kill(player, creature))
						count.incrementAndGet();
				});
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
