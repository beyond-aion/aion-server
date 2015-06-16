package ai.instance.dragonLordsRefuge;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;


/**
 * @author Cheatkiller
 *
 */
@AIName("sinkingsandtiamat")
public class SinkingSandAI2 extends NpcAI2 {

	@Override
  protected void handleSpawned() {
  	super.handleSpawned();
  	useskill();
  }

  private void useskill() {
  	ThreadPoolManager.getInstance().schedule(new Runnable() {

  		@Override
  		public void run() {
  			if (getOwner().getNpcId() == 283136)
  				AI2Actions.useSkill(SinkingSandAI2.this, 20965);
  			getOwner().getController().die();
  		}
  	}, 10000);
  }
  
  @Override
	protected void handleDied() {
  	super.handleDied();
  	AI2Actions.deleteOwner(this);
	}
}