package ai.instance.danuarReliquary;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.skill.QueuedNpcSkillEntry;
import com.aionemu.gameserver.model.templates.item.ItemAttackType;
import com.aionemu.gameserver.model.templates.npcskill.QueuedNpcSkillTemplate;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Ritsu
 * @reworked Estrayl October 29th, 2017.
 */
@AIName("modors_clone")
public class ModorsCloneAI extends AggressiveNpcAI {

	private AtomicBoolean canCancel = new AtomicBoolean();
	private AtomicBoolean isHome = new AtomicBoolean(true);

	public ModorsCloneAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		ThreadPoolManager.getInstance().schedule(() -> PacketSendUtility.broadcastMessage(getOwner(), 1500746), 3000);
	}

	@Override
	public int modifyOwnerDamage(int damage, Creature effected, Effect effect) {
		return Math.round(damage * 0.75f);
	}

	@Override
	public ItemAttackType modifyAttackType(ItemAttackType type) {
		return ItemAttackType.MAGICAL_WATER;
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (isHome.compareAndSet(true, false))
			handleSkillTask();
	}

	private void handleSkillTask() {
		getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21175, 60, 100, 14000, 15000)));
		getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21175, 60, 100, 14000, 20000)));
		getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21177, 60, 100, 14000, 15000)));
	}

	@Override
	public void onStartUseSkill(SkillTemplate skillTemplate) {
		if (skillTemplate.getSkillId() == 21177 && canCancel.compareAndSet(false, true))
			spawn(284386, 255.98627f, 259.0136f, 241.73842f, (byte) 0);
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate) {
		if (skillTemplate.getSkillId() == 21177 && canCancel.compareAndSet(true, false))
			handleSkillTask();
	}

	private void cancelVengefulOrb() {
		if (canCancel.get()) {
			Npc vengefulOrb = getPosition().getWorldMapInstance().getNpc(284386);
			if (vengefulOrb != null)
				vengefulOrb.getController().delete();
		}
	}

	@Override
	protected void handleDied() {
		cancelVengefulOrb();
		super.handleDied();
	}

	@Override
	protected void handleDespawned() {
		cancelVengefulOrb();
		super.handleDespawned();
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		isHome.set(true);
	}
}
