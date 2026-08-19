package admincommands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_STATUPDATE_EXP;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.model.DispelSlotType;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Mrakobes, Loxo
 */
public class Heal extends AdminCommand {

	public Heal() {
		super("heal", "Restores HP, MP, DP, flight time and energy of repose.");

		// @formatter:off
		setSyntaxInfo(
			" - Heals your targets HP, MP and removes soul sickness.",
			"<dp> - Heals your targets DP.",
			"<fp> - Heals your targets flight time.",
			"<repose> - Heals your targets energy of repose.",
			"<number> - Heals your targets HP by given amount.",
			"<number%> - Heals your targets HP by given percentage."
		);
		// @formatter:on
	}

	@Override
	public void execute(Player player, String... params) {
		VisibleObject target = player.getTarget();
		if (!(target instanceof Creature)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_INVALID_TARGET());
			return;
		}

		Creature creature = (Creature) target;

		if (params.length == 0) {
			creature.getLifeStats().increaseHp(TYPE.HP, creature.getLifeStats().getMaxHp());
			creature.getLifeStats().increaseMp(TYPE.HEAL_MP, creature.getLifeStats().getMaxMp(), 0, LOG.MPHEAL);
			creature.getEffectController().removeByDispelSlotType(DispelSlotType.SPECIAL2);
			if (!player.equals(creature))
				sendInfo(player, creature.getName() + " has been refreshed.");
		} else if (params[0].equalsIgnoreCase("dp") && creature instanceof Player) {
			Player targetPlayer = (Player) creature;
			targetPlayer.getCommonData().setDp(targetPlayer.getGameStats().getMaxDp().getCurrent());
			if (!player.equals(creature))
				sendInfo(player, targetPlayer.getName() + "'s DP have been fully refreshed.");
		} else if (params[0].equalsIgnoreCase("fp") && creature instanceof Player) {
			Player targetPlayer = (Player) creature;
			targetPlayer.getLifeStats().setCurrentFp(targetPlayer.getLifeStats().getMaxFp());
			if (!player.equals(creature))
				sendInfo(player, targetPlayer.getName() + "'s flight time has been fully refreshed.");
		} else if (params[0].equalsIgnoreCase("repose") && creature instanceof Player) {
			Player targetPlayer = (Player) creature;
			PlayerCommonData pcd = targetPlayer.getCommonData();
			pcd.setCurrentReposeEnergy(pcd.getMaxReposeEnergy());
			PacketSendUtility.sendPacket(targetPlayer,
				new SM_STATUPDATE_EXP(pcd.getExpShown(), pcd.getExpRecoverable(), pcd.getExpNeed(), pcd.getCurrentReposeEnergy(), pcd.getMaxReposeEnergy()));
			if (!player.equals(creature))
				sendInfo(player, targetPlayer.getName() + "'s Energy of Repose has been fully refreshed.");
		} else {
			try {
				Matcher result = Pattern.compile("(.+)%").matcher(params[0]);
				int value;

				if (result.find()) {
					int hpPercent = Integer.parseInt(result.group(1));

					if (hpPercent < 100)
						value = (int) (hpPercent / 100f * creature.getLifeStats().getMaxHp());
					else
						value = creature.getLifeStats().getMaxHp();
				} else
					value = Integer.parseInt(params[0]);
				creature.getLifeStats().increaseHp(TYPE.HP, value);
				if (!player.equals(creature))
					sendInfo(player, creature.getName() + " has been healed by " + value + " health points!");
			} catch (Exception ex) {
				sendInfo(player);
			}
		}
	}
}
