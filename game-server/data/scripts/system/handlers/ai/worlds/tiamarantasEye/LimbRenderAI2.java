package ai.worlds.tiamarantasEye;

import ai.GeneralNpcAI2;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.services.NpcShoutsService;


/**
 * @author cheatkiller
 *
 */
@AIName("limbrender")
public class LimbRenderAI2 extends GeneralNpcAI2 {
	
	int attackCount;
	
	@Override
	public void handleAttack(Creature creature) {
		super.handleAttack(creature);
		attackCount++;
		NpcShoutsService.getInstance().sendMsg(getOwner(), 1401462);
		if(attackCount == 100) {
			attackCount = 0;
			AI2Actions.useSkill(this, 20655);
			NpcShoutsService.getInstance().sendMsg(getOwner(), 1401463);
		}
	}

	@Override
	public int modifyDamage(Creature creature, int damage) {
		return 1;
	}
	
	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		NpcShoutsService.getInstance().sendMsg(getOwner(), 1401461);
	}
}