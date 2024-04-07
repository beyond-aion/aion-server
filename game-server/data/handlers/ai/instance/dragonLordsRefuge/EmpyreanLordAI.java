package ai.instance.dragonLordsRefuge;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.ai.manager.EmoteManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.GeneralNpcAI;

/**
 * @author Bobobear, Estrayl
 */
@AIName("empyrean_lord")
public class EmpyreanLordAI extends GeneralNpcAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(50, 15);
	private int tiamatId;

	public EmpyreanLordAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		ThreadPoolManager.getInstance().schedule(() -> {
			getPosition().getWorldMapInstance().forEachNpc(npc -> {
				if (npc.getNpcId() >= 219532 && npc.getNpcId() <= 219539)
					npc.getController().die();
			});
		}, 9000);

		tiamatId = getPosition().getMapId() == 300520000 ? 219361 : 236276;
		switch (getNpcId()) {
			case 219488:
			case 856020:
				ThreadPoolManager.getInstance().schedule(() -> AIActions.useSkill(this, 20932), 8500);
				break;
			case 219491:
			case 856023:
				ThreadPoolManager.getInstance().schedule(() -> AIActions.useSkill(this, 20936), 8500);
				break;
			case 219489:
			case 856021:
				AIActions.targetCreature(this, getPosition().getWorldMapInstance().getNpc(tiamatId));
				ThreadPoolManager.getInstance().schedule(() -> AIActions.useSkill(this, 20929), 8500);
				break;
			case 219492:
			case 856024:
				AIActions.targetCreature(this, getPosition().getWorldMapInstance().getNpc(tiamatId));
				ThreadPoolManager.getInstance().schedule(() -> AIActions.useSkill(this, 20933), 2500);
				break;
		}
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		switch (skillTemplate.getSkillId()) {
			case 20932:
			case 20936:
				Npc tiamat = getPosition().getWorldMapInstance().getNpc(tiamatId);
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
		hpPhases.tryEnterNextPhase(this);
	}

	@Override
	public void handleHpPhase(int phaseHpPercent) {
		switch (getNpcId()) {
			case 219488, 856020, 219491, 856023 -> {
				switch (phaseHpPercent) {
					case 50 -> PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.IDTIAMAT_TIAMAT_GOD_HP_LOWER_THAN_50p());
					case 15 -> PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.IDTIAMAT_TIAMAT_GOD_HP_LOWER_THAN_15p());
				}
			}
		}
	}
}
