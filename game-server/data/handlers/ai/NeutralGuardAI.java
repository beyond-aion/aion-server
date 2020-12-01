package ai;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.CreatureType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
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
		getOwner().overrideNpcType(CreatureType.SUPPORT);
	}

	@Override
	public void creatureNeedsHelp(Creature attacker) {
		if (PositionUtil.isInRange(attacker, getOwner(), 20) && getOwner().getType(attacker) != CreatureType.AGGRESSIVE
			&& attacker.getTarget() instanceof Player) {
			getOwner().overrideNpcType(CreatureType.AGGRESSIVE);
			getOwner().getAggroList().addHate(attacker, 1000);
		}
	}
}
