package admincommands;

import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.AbsoluteStatOwner;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.calc.StatOwner;
import com.aionemu.gameserver.model.stats.calc.functions.IStatFunction;
import com.aionemu.gameserver.model.stats.calc.functions.StatFunctionProxy;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.network.aion.serverpackets.SM_STATS_INFO;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author MrPoke
 */
public class Stat extends AdminCommand {

	private static final Logger log = LoggerFactory.getLogger(Stat.class);

	/**
	 * @param alias
	 */
	public Stat() {
		super("stat");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length >= 1) {
			VisibleObject target = admin.getTarget();
			if (target == null) {
				PacketSendUtility.sendMessage(admin, "No target selected");
				return;
			}
			if (target instanceof Creature) {
				Creature creature = (Creature) target;

				if (params.length == 1) {
					TreeSet<IStatFunction> stats = creature.getGameStats().getStatsByStatEnum(StatEnum.valueOf(params[0]));
					for (IStatFunction stat : stats) {
						PacketSendUtility.sendMessage(admin, stat.toString());
					}
				} else if (params.length == 2 && "details".equals(params[1])) {
					TreeSet<IStatFunction> stats = creature.getGameStats().getStatsByStatEnum(StatEnum.valueOf(params[0]));
					for (IStatFunction stat : stats) {
						String details = collectDetails(stat);
						PacketSendUtility.sendMessage(admin, details);
						log.info(details);
					}
				} else if (params.length > 0 && "abs".equals(params[0])) {
					if (!(target instanceof Player)) {
						PacketSendUtility.sendMessage(admin, "Only players can be selected");
						return;
					}
					AbsoluteStatOwner absStats = ((Player) target).getAbsoluteStats();
					try {
						Integer templateId = Integer.parseInt(params[1]);
						absStats.setTemplate(templateId);
						absStats.apply();
						if (absStats.isActive()) {
							PacketSendUtility.sendMessage(admin, "Successfully applied absolute stats");
						} else {
							PacketSendUtility.sendMessage(admin, "No such template exists!");
						}
					} catch (NumberFormatException ex) {
						if (!"cancel".equalsIgnoreCase(params[1])) {
							PacketSendUtility.sendMessage(admin, "Not a number");
							return;
						}
						if (!absStats.isActive()) {
							PacketSendUtility.sendMessage(admin, "Nothing to cancel");
							return;
						}
						absStats.cancel();
						PacketSendUtility.sendMessage(admin, "Successfully canceled absolute stats");
						PacketSendUtility.sendPacket((Player) target, new SM_STATS_INFO((Player) target));
					}
				}
			}
		}
	}

	private String collectDetails(IStatFunction stat) {
		StringBuffer sb = new StringBuffer();
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

	@Override
	public void info(Player player, String message) {
		// TODO Auto-generated method stub

	}

}
