package ai.instance.unstableSplinterpath;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.PositionUtil;

import ai.AggressiveNpcAI;

/**
 * @author Cheatkiller
 */
@AIName("pieceofmidnight")
public class PieceOfMidnightAI extends AggressiveNpcAI {

	public PieceOfMidnightAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleCreatureSee(Creature creature) {
		checkDistance(creature);
	}

	@Override
	protected void handleCreatureMoved(Creature creature) {
		checkDistance(creature);
	}

	private void checkDistance(Creature creature) {
		if (creature instanceof Npc) {
			Npc rukril = getPosition().getWorldMapInstance().getNpc(219551);
			if (rukril != null && PositionUtil.isInRange(getOwner(), rukril, 5) && rukril.getEffectController().hasAbnormalEffect(19266)) {
				rukril.getEffectController().removeEffect(19266);
				Npc ebonsoul = getPosition().getWorldMapInstance().getNpc(219552);
				if (ebonsoul != null && ebonsoul.getEffectController().hasAbnormalEffect(19159))
					ebonsoul.getEffectController().removeEffect(19159);
			}
		}
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case REWARD_LOOT, REWARD_AP -> false;
			default -> super.ask(question);
		};
	}
}
