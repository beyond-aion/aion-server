package admincommands;

import java.util.List;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.AbsoluteStatOwner;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.calc.StatOwner;
import com.aionemu.gameserver.model.stats.calc.functions.IStatFunction;
import com.aionemu.gameserver.model.stats.calc.functions.StatFunctionProxy;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.network.aion.serverpackets.SM_STATS_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author MrPoke
 */
public class Stat extends AdminCommand {

	public Stat() {
		super("stat", "Shows stat info of your target.");

		// @formatter:off
		setSyntaxInfo(
			"<stat> [details] - Show active stat functions for the given stat (default: just the name, optional: detailed).",
			"<abs> <stat set id|cancel> - Apply fixed stats of the given stats_set ID from absolute_stats.xml or cancel them."
		);
		// @formatter:on
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			sendInfo(admin);
			return;
		}

		VisibleObject target = admin.getTarget();
		if (!(target instanceof Creature)) {
			PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_INVALID_TARGET());
			return;
		}
		Creature creature = (Creature) target;

		if (params.length == 1) {
			List<IStatFunction> stats = creature.getGameStats().getStatsSorted(StatEnum.valueOf(params[0]));
			for (IStatFunction stat : stats) {
				sendInfo(admin, stat.toString());
			}
		} else if (params.length == 2 && "details".equals(params[1])) {
			List<IStatFunction> stats = creature.getGameStats().getStatsSorted(StatEnum.valueOf(params[0]));
			for (IStatFunction stat : stats) {
				String details = collectDetails(stat);
				sendInfo(admin, details);
			}
		} else if ("abs".equals(params[0])) {
			if (!(target instanceof Player)) {
				sendInfo(admin, "Only players can be selected");
				return;
			}
			AbsoluteStatOwner absStats = ((Player) target).getAbsoluteStats();
			try {
				Integer templateId = Integer.parseInt(params[1]);
				absStats.setTemplate(templateId);
				absStats.apply();
				if (absStats.isActive()) {
					sendInfo(admin, "Successfully applied absolute stats");
				} else {
					sendInfo(admin, "No such template exists!");
				}
			} catch (NumberFormatException ex) {
				if (!"cancel".equalsIgnoreCase(params[1])) {
					sendInfo(admin, "Not a number");
					return;
				}
				if (!absStats.isActive()) {
					sendInfo(admin, "Nothing to cancel");
					return;
				}
				absStats.cancel();
				sendInfo(admin, "Successfully canceled absolute stats");
				PacketSendUtility.sendPacket((Player) target, new SM_STATS_INFO((Player) target));
			}
		} else {
			sendInfo(admin);
		}
	}

	private String collectDetails(IStatFunction stat) {
		StringBuilder sb = new StringBuilder();
		sb.append(stat.toString() + "\n");
		if (stat instanceof StatFunctionProxy) {
			StatFunctionProxy proxy = (StatFunctionProxy) stat;
			sb.append(" -- " + proxy.getProxiedFunction().toString());
		}
		StatOwner owner = stat.getOwner();
		if (owner instanceof Effect) {
			Effect effect = (Effect) owner;
			sb.append("\n -- skillId: " + effect.getSkillId());
			sb.append("\n -- skillName: " + effect.getSkillName());
		}
		return sb.toString();
	}

}
