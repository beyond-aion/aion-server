package admincommands;

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
		super("heal", "Restores HP, MP, DP, flight time and energy of repose.", """
			 - Heals your target's HP, MP and removes soul sickness.
			dp - Heals your target's DP.
			fp - Heals your target's flight time.
			repose - Heals your target's energy of repose.
			<number> - Heals your target's HP by given amount.
			<number%> - Heals your target's HP by given percentage.
			""");
	}

	@Override
	public void execute(Player player, String... params) {
		VisibleObject target = player.getTarget();
		if (target == null) {
			sendInfo(player);
			return;
		}
		if (!(target instanceof Creature creature)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_INVALID_TARGET());
			return;
		}
		if (params.length == 0) {
			creature.getLifeStats().increaseHp(TYPE.HP, creature.getLifeStats().getMaxHp());
			creature.getLifeStats().increaseMp(TYPE.HEAL_MP, creature.getLifeStats().getMaxMp(), 0, LOG.MPHEAL);
			creature.getEffectController().removeByDispelSlotType(DispelSlotType.SPECIAL2);
			if (!player.equals(creature))
				sendInfo(player, name(creature) + " has been refreshed.");
		} else if (params[0].equalsIgnoreCase("dp") && creature instanceof Player targetPlayer) {
			targetPlayer.getCommonData().setDp(targetPlayer.getGameStats().getMaxDp().getCurrent());
			if (!player.equals(creature))
				sendInfo(player, name(targetPlayer) + "'s DP have been fully refreshed.");
		} else if (params[0].equalsIgnoreCase("fp") && creature instanceof Player targetPlayer) {
			targetPlayer.getLifeStats().setCurrentFp(targetPlayer.getLifeStats().getMaxFp());
			if (!player.equals(creature))
				sendInfo(player, name(targetPlayer) + "'s flight time has been fully refreshed.");
		} else if (params[0].equalsIgnoreCase("repose") && creature instanceof Player targetPlayer) {
			PlayerCommonData pcd = targetPlayer.getCommonData();
			pcd.setCurrentReposeEnergy(pcd.getMaxReposeEnergy());
			PacketSendUtility.sendPacket(targetPlayer,
				new SM_STATUPDATE_EXP(pcd.getExpShown(), pcd.getExpRecoverable(), pcd.getExpNeed(), pcd.getCurrentReposeEnergy(), pcd.getMaxReposeEnergy()));
			if (!player.equals(creature))
				sendInfo(player, name(targetPlayer) + "'s Energy of Repose has been fully refreshed.");
		} else {
			int value;
			if (params[0].endsWith("%")) {
				int hpPercent = Integer.parseInt(params[0], 0, params[0].length() - 1, 10);
				value = Math.clamp((int) (hpPercent / 100f * creature.getLifeStats().getMaxHp()), 0, creature.getLifeStats().getMaxHp());
			} else
				value = Integer.parseInt(params[0]);
			creature.getLifeStats().increaseHp(TYPE.HP, value);
			if (!player.equals(creature))
				sendInfo(player, name(creature) + " has been healed by " + value + " health points.");
		}
	}
}
