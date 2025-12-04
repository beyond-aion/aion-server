package admincommands;

import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
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
		super("set", "Changes various player attributes.");

		// @formatter:off
		setSyntaxInfo(
			"class <value> - Sets the class of the selected player.",
			"level <value> - Sets the level of the selected player.",
			"exp <value> - Sets the experience points of the selected player.",
			"ap <value> - Sets the abyss points of the selected player.",
			"gp <value> - Sets the glory points of the selected player."
		);
		// @formatter:on
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length < 2) {
			sendInfo(admin);
			return;
		}
		if (!(admin.getTarget() instanceof Player target)) {
			PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_INVALID_TARGET());
			return;
		}

		if (params[0].equals("class")) {
			PlayerClass playerClass = PlayerClass.valueOf(params[1].toUpperCase());
			ClassChangeService.setClass(target, playerClass, true, true);
		} else if (params[0].equals("level")) {
			int level = Math.min(GSConfig.PLAYER_MAX_LEVEL, Integer.parseInt(params[1]));
			target.getCommonData().setLevel(level);
			sendInfo(admin, "Set " + target.getName() + " level to " + target.getLevel());
		} else if (params[0].equals("exp")) {
			long exp = Long.parseLong(params[1]);
			target.getCommonData().setExp(exp);
			PacketSendUtility.sendMessage(admin, "Set exp of target to " + target.getCommonData().getExp());
		} else if (params[0].equals("ap")) {
			int ap = Integer.parseInt(params[1]);
			AbyssPointsService.addAp(target, ap - target.getAbyssRank().getAp());
			if (target != admin) {
				sendInfo(admin, "Set " + target.getName() + "'s abyss points to " + target.getAbyssRank().getAp() + ".");
				sendInfo(target, "Admin set your abyss points to " + target.getAbyssRank().getAp() + ".");
			}
		} else if (params[0].equals("gp")) {
			int gp = Integer.parseInt(params[1]);
			GloryPointsService.addGp(target.getObjectId(), gp - target.getAbyssRank().getCurrentGP());
			if (target != admin) {
				sendInfo(admin, "Set " + target.getName() + "'s glory points to " + target.getAbyssRank().getCurrentGP() + ".");
				sendInfo(target, "Admin set your glory points to " + target.getAbyssRank().getCurrentGP() + ".");
			}
		}
	}
}
