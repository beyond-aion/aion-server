package ai.instance.shugoImperialTomb;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.manager.WalkManager;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Estrayl
 */
@AIName("shugo_tomb_boss")
public class ShugoTombBossAI extends ShugoTombAttackerAI {

	private final AtomicInteger usedSkills = new AtomicInteger();
	private final AtomicBoolean isCasting = new AtomicBoolean();

	public ShugoTombBossAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		List<NpcSkillEntry> skills = getSkillList().getNpcSkills();
		if (getOwner().getEffectController().isInAnyAbnormalState(AbnormalState.CANT_ATTACK_STATE) || usedSkills.get() >= skills.size())
			return;

		NpcSkillEntry entry = skills.get(usedSkills.get());
		if (getLifeStats().getHpPercentage() <= entry.getTemplate().getMaxhp() && isCasting.compareAndSet(false, true)) {
			if (getMoveController().isInMove())
				WalkManager.stopWalking(this);

			SkillEngine.getInstance().getSkill(getOwner(), entry.getSkillId(), entry.getSkillLevel(), getOwner()).useWithoutPropSkill();
			usedSkills.incrementAndGet();
		}
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		if (skillTemplate.getSkillId() == 21266) {
			for (int i = 0; i < 4; i++) {
				rndSpawnInRange(219509, 2);
			}
		}
		isCasting.set(false);
		if (!canThink()) {
			ThreadPoolManager.getInstance().schedule(() -> {
				WalkManager.startWalking(this);
				getOwner().setState(CreatureState.WALK_MODE);
				PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.CHANGE_SPEED, 0, getObjectId()));
			}, 500);
		}
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case ALLOW_DECAY, REWARD_LOOT -> true;
			default -> super.ask(question);
		};
	}
}
