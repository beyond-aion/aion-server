package ai.instance.dragonLordsRefuge;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.item.ItemAttackType;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.GeneralNpcAI;

/**
 * @author Luzien
 * @modified Estrayl March 10th, 2018
 */
@AIName("ultimate_atrocity")
public class UltimateAtrocityAI extends GeneralNpcAI {

	private Future<?> task;

	public UltimateAtrocityAI(Npc owner) {
		super(owner);
	}

	@Override
	public ItemAttackType modifyAttackType(ItemAttackType type) {
		return ItemAttackType.MAGICAL_FIRE;
	}

	@Override
	public int modifyOwnerDamage(int damage, Creature effected, Effect effect) {
		return damage / 4;
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		int skill;
		switch (getNpcId()) {
			case 283244:
				skill = 21160;
				break;
			case 283240:
				skill = 21156;
				break;
			case 283237:
			case 283241:
				skill = 20923;
				break;
			default:
				return;
		}

		task = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> AIActions.useSkill(this, skill), 500, 2000);

		ThreadPoolManager.getInstance().schedule(() -> AIActions.deleteOwner(this), 11000);
	}

	@Override
	public void handleDespawned() {
		task.cancel(true);
		super.handleDespawned();
	}

	@Override
	public boolean ask(AIQuestion question) {
		switch (question) {
			case SHOULD_DECAY:
			case SHOULD_RESPAWN:
			case SHOULD_REWARD:
				return false;
			default:
				return super.ask(question);
		}
	}
}
