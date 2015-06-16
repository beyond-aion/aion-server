package ai.instance.unstableSplinterpath;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.MathUtil;


/**
 * @author Cheatkiller
 *
 */
@AIName("pieceofsplendor")
public class PieceOfSplendorAI2 extends AggressiveNpcAI2 {

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
    	if (MathUtil.isIn3dRange(getOwner(), ebonsoul, 5) && ebonsoul.getEffectController().hasAbnormalEffect(19159)) {
    		ebonsoul.getEffectController().removeEffect(19159);
    		if(rukril != null && rukril.getEffectController().hasAbnormalEffect(19266))
    			rukril.getEffectController().removeEffect(19266);
    	}
    }
  }
}
