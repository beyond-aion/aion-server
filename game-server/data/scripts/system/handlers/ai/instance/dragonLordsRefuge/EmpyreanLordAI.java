package ai.instance.dragonLordsRefuge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.manager.EmoteManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.GeneralNpcAI;

/**
 * @author Bobobear
 * @reworked Estrayl March 10th, 2018
 */
@AIName("empyrean_lord")
public class EmpyreanLordAI extends GeneralNpcAI {

	private List<Integer> percents = new ArrayList<>();
	private Npc tiamat;

	public EmpyreanLordAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		ThreadPoolManager.getInstance().schedule(() -> {
			for (Npc npc : getPosition().getWorldMapInstance().getNpcs())
				if (npc != null && npc.getNpcId() >= 219532 && npc.getNpcId() <= 219539)
					npc.getController().die();
		}, 9000);
		tiamat = getPosition().getWorldMapInstance().getNpc(219361);
		switch (getNpcId()) {
			case 219488:
			case 219491:
				ThreadPoolManager.getInstance().schedule(() -> AIActions.useSkill(this, getNpcId() == 219488 ? 20932 : 20936), 8500);
				break;
			case 219489:
			case 219492:
				AIActions.targetCreature(this, tiamat);
				ThreadPoolManager.getInstance().schedule(() -> AIActions.useSkill(this, getNpcId() == 219489 ? 20929 : 20933), 2500);
				break;
		}
		addPercent();
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate) {
		switch (skillTemplate.getSkillId()) {
			case 20932:
			case 20936:
				AIActions.targetCreature(this, tiamat);
				getAggroList().addHate(tiamat, Integer.MAX_VALUE / 4);
				setStateIfNot(AIState.FIGHT);
				EmoteManager.emoteStartAttacking(getOwner(), tiamat);
				break;
		}
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
	}

	private void checkPercentage(int hpPercentage) {
		if (percents.isEmpty())
			return;
		switch (getNpcId()) {
			case 219488:
			case 219491:
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
				break;
		}
	}

	private void addPercent() {
		percents.clear();
		Collections.addAll(percents, new Integer[] { 50, 15 });
	}

}
