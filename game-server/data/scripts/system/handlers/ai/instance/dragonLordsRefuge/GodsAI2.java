package ai.instance.dragonLordsRefuge;

import ai.AggressiveNpcAI2;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.services.NpcShoutsService;
import com.aionemu.gameserver.skillengine.SkillEngine;

/**
 * @author Bobobear
 */

@AIName("gods")
public class GodsAI2 extends AggressiveNpcAI2 {

	Npc tiamat;

	@Override
	protected void handleDeactivate() {
	}

	@Override
	public int modifyDamage(Creature creature, int damage) {
		return 6000;
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		tiamat = getPosition().getWorldMapInstance().getNpc(219361);
		if (getNpcId() == 219488 || getNpcId() == 219491) {
			// empyrean lord (god) debuff all players before start attack Tiamat
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					SkillEngine.getInstance().getSkill(getOwner(), (getOwner().getNpcId() == 219488 ? 20932 : 20936), 100, getOwner()).useSkill();
				}
			}, 8000);
			// empyrean lord (god) start attack Tiamat Dragon
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					AI2Actions.targetCreature(GodsAI2.this, tiamat);
					getAggroList().addHate(tiamat, 100000);
					NpcShoutsService.getInstance().sendMsg(getOwner(), 1401550);
					SkillEngine.getInstance().getSkill(getOwner(), (getNpcId() == 219488 ? 20931 : 20935), 60, tiamat).useNoAnimationSkill(); // adds 1mio hate
				}
			}, 12000);
		} else if (getNpcId() == 219489 || getNpcId() == 219492) {
			// empyrean lord (god) start final attack to Tiamat Dragon before became exausted
			NpcShoutsService.getInstance().sendMsg(getOwner(), (getNpcId() == 219489 ? 1401538 : 1401539));
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					SkillEngine.getInstance().getSkill(getOwner(), (getNpcId() == 219489 ? 20929 : 20933), 100, tiamat).useNoAnimationSkill();
				}
			}, 2000);
		}
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
	}

	@Override
	protected void handleActivate() {
		super.handleActivate();
		tiamat = getPosition().getWorldMapInstance().getNpc(219361);
		if (getOwner().getNpcId() == 219488 || getOwner().getNpcId() == 219491) {
			AI2Actions.targetCreature(GodsAI2.this, tiamat);
			SkillEngine.getInstance().getSkill(getOwner(), (getNpcId() == 219488 ? 20931 : 20935), 60, tiamat).useSkill();
		}
	}

	private void checkPercentage(int hpPercentage) {
		if (getOwner().getNpcId() == 219488 || getOwner().getNpcId() == 219488) {
			if (hpPercentage == 50) {
				NpcShoutsService.getInstance().sendMsg(getOwner(), 1401548);
			}
			if (hpPercentage == 15) {
				NpcShoutsService.getInstance().sendMsg(getOwner(), 1401549);
			}
			if (hpPercentage < 5) {
				NpcShoutsService.getInstance().sendMsg(getOwner(), 1401548);
				NpcShoutsService.getInstance().sendMsg(getOwner(), 1401549);
			}
		}
	}
}
