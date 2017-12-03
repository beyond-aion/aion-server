package ai.instance.dragonLordsRefuge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Bobobear
 */
@AIName("gods")
public class GodsAI extends AggressiveNpcAI {

	private List<Integer> percents = new ArrayList<>();
	Npc tiamat;

	public GodsAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDeactivate() {
	}

	@Override
	public int modifyDamage(Creature attacker, int damage, Effect effect) {
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
					AIActions.targetCreature(GodsAI.this, tiamat);
					getAggroList().addHate(tiamat, 100000);
					PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.IDTIAMAT_TIAMAT_DRAKAN_BUFF_MSG());
					SkillEngine.getInstance().getSkill(getOwner(), (getNpcId() == 219488 ? 20931 : 20935), 60, tiamat).useNoAnimationSkill(); // adds 1mio hate
				}
			}, 12000);
		} else if (getNpcId() == 219489 || getNpcId() == 219492) {
			// empyrean lord (god) start final attack to Tiamat Dragon before became exhausted
			PacketSendUtility.broadcastToMap(getOwner(), (getNpcId() == 219489 ? SM_SYSTEM_MESSAGE.STR_MSG_IDTIAMAT_KAISINEL_2PHASE_DEADLYATK()
				: SM_SYSTEM_MESSAGE.STR_MSG_IDTIAMAT_MARCHUTAN_2PHASE_DEADLYATK()));
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					SkillEngine.getInstance().getSkill(getOwner(), (getNpcId() == 219489 ? 20929 : 20933), 100, tiamat).useNoAnimationSkill();
				}
			}, 2000);
		}
		addPercent();
	}

	private void addPercent() {
		percents.clear();
		Collections.addAll(percents, new Integer[] { 50, 15 });
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
			AIActions.targetCreature(this, tiamat);
			SkillEngine.getInstance().getSkill(getOwner(), (getNpcId() == 219488 ? 20931 : 20935), 60, tiamat).useSkill();
		}
	}

	private void checkPercentage(int hpPercentage) {
		if (percents.isEmpty())
			return;
		if (getOwner().getNpcId() == 219488 || getOwner().getNpcId() == 219491) {
			if (hpPercentage <= percents.get(0)) {
				switch (percents.remove(0)) {
					case 50:
						PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.IDTIAMAT_TIAMAT_GOD_HP_LOWER_THAN_50p());
						break;
					case 15:
						PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.IDTIAMAT_TIAMAT_GOD_HP_LOWER_THAN_15p());
						break;
				}
			}
		}
	}
}
