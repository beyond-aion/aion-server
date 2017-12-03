package ai.walkers;

import org.apache.commons.lang3.ArrayUtils;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.handler.MoveEventHandler;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.utils.PositionUtil;

/**
 * @author Rolandas
 */
@AIName("polorserin")
public class PolorSerinAI extends WalkGeneralRunnerAI {

	static final int[] stopAdults = { 203129, 203132 };

	public PolorSerinAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleMoveArrived() {
		boolean adultsNear = false;
		for (VisibleObject object : getOwner().getKnownList().getKnownObjects().values()) {
			if (object instanceof Npc) {
				Npc npc = (Npc) object;
				if (!ArrayUtils.contains(stopAdults, npc.getNpcId()))
					continue;
				if (PositionUtil.isInRange(npc, getOwner(), getOwner().getAggroRange())) {
					adultsNear = true;
					break;
				}
			}
		}
		if (adultsNear) {
			MoveEventHandler.onMoveArrived(this);
			getOwner().unsetState(CreatureState.WEAPON_EQUIPPED);
		} else {
			super.handleMoveArrived();
			getOwner().setState(CreatureState.WEAPON_EQUIPPED);
		}
	}
}
