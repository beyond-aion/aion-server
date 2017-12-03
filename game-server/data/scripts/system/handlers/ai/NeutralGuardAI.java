package ai;

import java.util.function.Consumer;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.CreatureType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CUSTOM_SETTINGS;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;

/**
 * @author Cheatkiller
 */
@AIName("neutralguard")
public class NeutralGuardAI extends AggressiveNpcAI {

	public NeutralGuardAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		changeType(CreatureType.SUPPORT);
	}

	@Override
	public void creatureNeedsHelp(Creature attacker) {
		if (PositionUtil.isInRange(attacker, getOwner(), 20) && getOwner().getType(attacker) != CreatureType.AGGRESSIVE
			&& attacker.getTarget() instanceof Player) {
			changeType(CreatureType.AGGRESSIVE);
			getOwner().getAggroList().addHate(attacker, 1000);
		}
	}

	private void changeType(final CreatureType type) {
		getOwner().setNpcType(type);
		getKnownList().forEachPlayer(new Consumer<Player>() {

			@Override
			public void accept(Player player) {
				PacketSendUtility.sendPacket(player, new SM_CUSTOM_SETTINGS(getOwner().getObjectId(), 0, type.getId(), 0));
			}
		});
	}
}
