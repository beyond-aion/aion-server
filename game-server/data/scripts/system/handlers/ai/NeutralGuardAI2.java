package ai;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.CreatureType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CUSTOM_SETTINGS;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 * @author Cheatkiller
 */
@AIName("neutralguard")
public class NeutralGuardAI2 extends AggressiveNpcAI2 {

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		changeType(CreatureType.SUPPORT);
	}

	@Override
	public void creatureNeedsHelp(Creature attacker) {
		if (MathUtil.isIn3dRange(attacker, getOwner(), 20) && getOwner().getType(attacker) != CreatureType.AGGRESSIVE
			&& attacker.getTarget() instanceof Player) {
			changeType(CreatureType.AGGRESSIVE);
			getOwner().getAggroList().addHate(attacker, 1000);
		}
	}

	private void changeType(final CreatureType type) {
		getOwner().setNpcType(type);
		getKnownList().forEachPlayer(new Visitor<Player>() {

			@Override
			public void visit(Player player) {
				PacketSendUtility.sendPacket(player, new SM_CUSTOM_SETTINGS(getOwner().getObjectId(), 0, type.getId(), 0));
			}
		});
	}
}
