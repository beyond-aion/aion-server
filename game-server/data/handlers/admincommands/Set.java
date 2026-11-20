package admincommands;

import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.dao.AbyssRankDAO;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.ClassChangeService;
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
import com.aionemu.gameserver.services.abyss.GloryPointsService;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.utils.stats.AbyssRankEnum;

/**
 * @author Nemiroff, ATracer, IceReaper, Sarynth, Artur
 */
public class Set extends AdminCommand {

	public Set() {
		super("set", "Changes various player attributes.", """
			class <value> - Sets the class of the selected player.
			level <value> - Sets the level of the selected player.
			exp <value> - Sets the experience points of the selected player.
			dp <value> - Sets the DP of the selected player.
			ap <value> - Sets the abyss points of the selected player.
			gp <value> - Sets the glory points of the selected player.
			rank <1-18> - Sets the abyss rank of the selected player directly.
			Note: Any actions default to your character, if no player is targeted.
			""");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length < 2) {
			sendInfo(admin);
			return;
		}
		Player target = admin.getTarget() instanceof Player player ? player : admin;

		if (params[0].equals("class")) {
			PlayerClass playerClass = PlayerClass.valueOf(params[1].toUpperCase());
			ClassChangeService.setClass(target, playerClass, true, true);
		} else if (params[0].equals("level")) {
			int level = Math.min(GSConfig.PLAYER_MAX_LEVEL, Integer.parseInt(params[1]));
			target.getCommonData().setLevel(level);
			sendInfo(admin, "Set " + name(target) + "'s level to " + target.getLevel());
		} else if (params[0].equals("exp")) {
			long exp = Long.parseLong(params[1]);
			target.getCommonData().setExp(exp);
			sendInfo(admin, "Set exp of target to " + target.getCommonData().getExp());
		} else if (params[0].equals("dp")) {
			int dp = Math.max(0, Integer.parseInt(params[1]));
			target.getCommonData().setDp(dp);
			if (target != admin)
				sendInfo(admin, "Set " + name(target) + "'s DP to " + target.getCommonData().getDp() + ".");
		} else if (params[0].equals("ap")) {
			int ap = Integer.parseInt(params[1]);
			AbyssPointsService.addAp(target, ap - target.getAbyssRank().getAp());
			if (target != admin) {
				sendInfo(admin, "Set " + name(target) + "'s abyss points to " + target.getAbyssRank().getAp() + ".");
				sendInfo(target, "Admin set your abyss points to " + target.getAbyssRank().getAp() + ".");
			}
		} else if (params[0].equals("gp")) {
			int gp = Integer.parseInt(params[1]);
			GloryPointsService.addGp(target.getObjectId(), gp - target.getAbyssRank().getCurrentGP());
			if (target != admin) {
				sendInfo(admin, "Set " + name(target) + "'s glory points to " + target.getAbyssRank().getCurrentGP() + ".");
				sendInfo(target, "Admin set your glory points to " + target.getAbyssRank().getCurrentGP() + ".");
			}
		} else if (params[0].equals("rank")) {
			AbyssRankEnum rank;
			try {
				rank = AbyssRankEnum.getRankById(Integer.parseInt(params[1]));
			} catch (IllegalArgumentException e) {
				sendInfo(admin, "Unknown abyss rank: " + params[1]);
				return;
			}
			target.getAbyssRank().setRank(rank);
			AbyssRankDAO.updateAbyssRank(target.getObjectId(), rank);
			AbyssPointsService.onRankChanged(target, false, true, null);
			if (target != admin) {
				sendInfo(admin, "Set " + name(target) + "'s abyss rank to " + rank + ".");
				sendInfo(target, "Admin set your abyss rank to " + rank + ".");
			}
		} else {
			sendInfo(admin);
		}
	}
}
