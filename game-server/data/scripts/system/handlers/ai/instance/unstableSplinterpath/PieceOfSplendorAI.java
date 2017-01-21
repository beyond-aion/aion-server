package ai.instance.unstableSplinterpath;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.MathUtil;

import ai.AggressiveNpcAI;

/**
 * @author Cheatkiller
 */
@AIName("pieceofsplendor")
public class PieceOfSplendorAI extends AggressiveNpcAI {

	@Override
	protected void handleCreatureSee(Creature creature) {
		checkDistance(this, creature);
	}

	@Override
	protected void handleCreatureMoved(Creature creature) {
		checkDistance(this, creature);
	}

	private void checkDistance(NpcAI ai, Creature creature) {
		Npc rukril = getPosition().getWorldMapInstance().getNpc(219551);
		Npc ebonsoul = getPosition().getWorldMapInstance().getNpc(219552);
		if (creature instanceof Npc) {
			if (MathUtil.isIn3dRange(getOwner(), ebonsoul, 5) && ebonsoul.getEffectController().hasAbnormalEffect(19159)) {
				ebonsoul.getEffectController().removeEffect(19159);
				if (rukril != null && rukril.getEffectController().hasAbnormalEffect(19266))
					rukril.getEffectController().removeEffect(19266);
			}
		}
	}
}
