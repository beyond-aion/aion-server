package ai.instance.raksang;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;

import ai.AggressiveNpcAI;

/**
 * @author xTz
 */
@AIName("seal_generator")
public class SealGeneratorAI extends AggressiveNpcAI {

	private final AtomicBoolean startedEvent = new AtomicBoolean();

	public SealGeneratorAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	protected void handleCreatureMoved(Creature creature) {
		if (creature instanceof Player player) {
			if (PositionUtil.getDistance(getOwner(), player) <= 30 && startedEvent.compareAndSet(false, true))
				PacketSendUtility.broadcastToMap(getOwner(), 1401156);
		}
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case IS_IMMUNE_TO_ABNORMAL_STATES -> true;
			default -> super.ask(question);
		};
	}

	@Override
	public float modifyDamage(Creature attacker, float damage, Effect effect) {
		return 1;
	}
}
