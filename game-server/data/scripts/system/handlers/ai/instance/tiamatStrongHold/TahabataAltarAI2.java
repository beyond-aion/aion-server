package ai.instance.tiamatStrongHold;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.poll.AIAnswer;
import com.aionemu.gameserver.ai2.poll.AIAnswers;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.MathUtil;


/**
 * @author Cheatkiller
 *
 */
@AIName("tahabataaltar")
public class TahabataAltarAI2 extends NpcAI2 {

  @Override
  protected void handleCreatureSee(Creature creature) {
      checkDistance(creature);
  }
  
  @Override
  protected void handleCreatureMoved(Creature creature) {
      checkDistance(creature);
  }

  private void checkDistance(Creature creature) {
  	int owner = getNpcId();
  	int debuff = 0;
  	switch(owner) {
  		case 283116:
  			debuff = 20970;
  			break;
  		case 283118:
  			debuff = 20971;
  			break;
  	}
     if (creature instanceof Player) {
    	 if (getNpcId() == 283253 && MathUtil.isIn3dRangeLimited(getOwner(), creature, 25, 37)
     		|| getNpcId() == 283255 && MathUtil.isIn3dRangeLimited(getOwner(), creature, 20, 25)) {
     		  if(!creature.getEffectController().hasAbnormalEffect(debuff)) {
    		     AI2Actions.useSkill(this, debuff);
     		  }
    	 }
    }
  }
  
  @Override
  protected AIAnswer pollInstance(AIQuestion question) {
      switch (question) {
          case SHOULD_DECAY:
              return AIAnswers.NEGATIVE;
          case SHOULD_RESPAWN:
              return AIAnswers.NEGATIVE;
          case SHOULD_REWARD:
              return AIAnswers.NEGATIVE;
          default:
              return null;
      }
   }
}
