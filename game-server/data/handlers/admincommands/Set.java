package admincommands;

import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_TITLE_INFO;
import com.aionemu.gameserver.services.ClassChangeService;
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
import com.aionemu.gameserver.services.abyss.GloryPointsService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Nemiroff, ATracer, IceReaper, Sarynth, Artur
 */
public class Set extends AdminCommand {

	public Set() {
		super("set");
	}

	@Override
	public void execute(Player admin, String... params) {
		Player target = null;
		VisibleObject creature = admin.getTarget();

		if (admin.getTarget() instanceof Player) {
			target = (Player) creature;
		}

		if (target == null) {
			PacketSendUtility.sendMessage(admin, "You should select a target first!");
			return;
		}

		if (params.length < 2) {
			PacketSendUtility.sendMessage(admin, "You should enter second params!");
			return;
		}
		String paramValue = params[1];

		if (params[0].equals("class")) {
			byte newClass;
			try {
				newClass = Byte.parseByte(paramValue);
			} catch (NumberFormatException e) {
				PacketSendUtility.sendMessage(admin, "You should enter valid second params!");
				return;
			}

			ClassChangeService.setClass(target, PlayerClass.getPlayerClassById(newClass), true, true);
		} else if (params[0].equals("exp")) {
			long exp;
			try {
				exp = Long.parseLong(paramValue);
			} catch (NumberFormatException e) {
				PacketSendUtility.sendMessage(admin, "You should enter valid second params!");
				return;
			}

			target.getCommonData().setExp(exp);
			PacketSendUtility.sendMessage(admin, "Set exp of target to " + target.getCommonData().getExp());
		} else if (params[0].equals("ap")) {
			int ap;
			try {
				ap = Integer.parseInt(paramValue);
			} catch (NumberFormatException e) {
				PacketSendUtility.sendMessage(admin, "You should enter valid second params!");
				return;
			}

			AbyssPointsService.setAp(target, ap);
			if (target == admin) {
				PacketSendUtility.sendMessage(admin, "Set your Abyss Points to " + ap + ".");
			} else {
				PacketSendUtility.sendMessage(admin, "Set " + target.getName() + " Abyss Points to " + ap + ".");
				PacketSendUtility.sendMessage(target, "Admin set your Abyss Points to " + ap + ".");
			}
		} else if (params[0].equals("gp")) {
			int gp;
			try {
				gp = Integer.parseInt(paramValue);
			} catch (NumberFormatException e) {
				PacketSendUtility.sendMessage(admin, "You should enter valid second params!");
				return;
			}
			GloryPointsService.modifyGpBy(target.getObjectId(), gp, false, false);
			if (target == admin) {
				PacketSendUtility.sendMessage(admin, "Set your Glory Points to " + gp + ".");
			} else {
				PacketSendUtility.sendMessage(admin, "Set " + target.getName() + " Glory Points to " + gp + ".");
				PacketSendUtility.sendMessage(target, "Admin set your Glory Points to " + gp + ".");
			}
		} else if (params[0].equals("level")) {
			int level;
			try {
				level = Integer.parseInt(paramValue);
			} catch (NumberFormatException e) {
				PacketSendUtility.sendMessage(admin, "You should enter valid second params!");
				return;
			}

			Player player = target;

			if (level <= GSConfig.PLAYER_MAX_LEVEL)
				player.getCommonData().setLevel(level);

			PacketSendUtility.sendMessage(admin, "Set " + player.getCommonData().getName() + " level to " + player.getLevel());
		} else if (params[0].equals("title")) {
			int titleId;
			try {
				titleId = Integer.parseInt(paramValue);
			} catch (NumberFormatException e) {
				PacketSendUtility.sendMessage(admin, "You should enter valid second params!");
				return;
			}

			Player player = target;
			if (titleId <= 160)
				setTitle(player, titleId);
			PacketSendUtility.sendMessage(admin, "Set " + player.getCommonData().getName() + " title to " + titleId);

		}
	}

	private void setTitle(Player player, int value) {
		PacketSendUtility.sendPacket(player, new SM_TITLE_INFO(value));
		PacketSendUtility.broadcastPacket(player, (new SM_TITLE_INFO(player, value)));
		player.getCommonData().setTitleId(value);
	}

	@Override
	public void info(Player player, String message) {
		PacketSendUtility.sendMessage(player, "syntax //set <class|exp|ap|level|title>");
	}
}
