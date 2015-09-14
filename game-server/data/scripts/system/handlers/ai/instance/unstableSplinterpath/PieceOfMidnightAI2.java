package ai.instance.unstableSplinterpath;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.MathUtil;

/**
 * @author Cheatkiller
 */
@AIName("pieceofmidnight")
public class PieceOfMidnightAI2 extends AggressiveNpcAI2 {

	@Override
	protected void handleCreatureSee(Creature creature) {
		checkDistance(this, creature);
	}

	@Override
	protected void handleCreatureMoved(Creature creature) {
		checkDistance(this, creature);
	}

	private void checkDistance(NpcAI2 ai, Creature creature) {
		Npc rukril = getPosition().getWorldMapInstance().getNpc(219551);
		Npc ebonsoul = getPosition().getWorldMapInstance().getNpc(219552);
		if (creature instanceof Npc) {
			if (MathUtil.isIn3dRange(getOwner(), rukril, 5) && rukril.getEffectController().hasAbnormalEffect(19266)) {
				rukril.getEffectController().removeEffect(19266);
				if (ebonsoul != null && ebonsoul.getEffectController().hasAbnormalEffect(19159))
					ebonsoul.getEffectController().removeEffect(19159);
			}
		}
	}
}
