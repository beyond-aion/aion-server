package ai.instance.danuarReliquary;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNoLootNpcAI;

/**
 * @author Ritsu, Estrayl, Yeats
 */
@AIName("modors_clone")
public class ModorsCloneAI extends AggressiveNoLootNpcAI {

	private float modifier = 1.35f;

	public ModorsCloneAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		if (getNpcId() == 284383 || getNpcId() == 855244) {
			ThreadPoolManager.getInstance().schedule(() -> PacketSendUtility.broadcastMessage(getOwner(), 348682), 1000);
		} else {
			SkillEngine.getInstance().applyEffectDirectly(21181, getOwner(), getOwner()); // Malevolence
			ThreadPoolManager.getInstance().schedule(() -> PacketSendUtility.broadcastMessage(getOwner(), 348683), 1000);
		}
	}

	@Override
	public float modifyOwnerDamage(float damage, Creature effected, Effect effect) {
		return damage * modifier;
	}

	@Override
	public void onStartUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		switch (skillTemplate.getSkillId()) {
			case 21177:
				if (getNpcId() == 284383 || getNpcId() == 855244) {
					PacketSendUtility.broadcastMessage(getOwner(), 348677);
					spawn(284387, 255.98627f, 259.0136f, 242.73842f, (byte) 0);
				} else {
					PacketSendUtility.broadcastMessage(getOwner(), 348680);
				}
				break;
			case 21372:
			case 21174:
				if (getNpcId() == 284383 || getNpcId() == 855244) {
					PacketSendUtility.broadcastMessage(getOwner(), 348678);
				} else {
					PacketSendUtility.broadcastMessage(getOwner(), 348679);
				}
				break;
			case 21175:
				break;
		}
		if (getNpcId() == 284383 || getNpcId() == 855244) {
			for (VisibleObject obj : getOwner().getKnownList().getKnownObjects().values()) {
				if (obj instanceof Npc && !((Npc) obj).isDead() && (((Npc) obj).getNpcId() == 855245 || ((Npc) obj).getNpcId() == 284384)) {
					VisibleObject target = obj.getTarget();
					if (target instanceof Creature && ((Creature) target).isDead()) {
						target = getTarget();
						obj.setTarget(target);
					}
					SkillEngine.getInstance().getSkill(((Npc) obj), skillTemplate.getSkillId(), skillLevel, target).useWithoutPropSkill();
				}
			}
		}
		modifier = 0.2f;
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		modifier = 1.35f;
	}

	private void cancelVengefulOrb() {
		Npc vengefulOrb = getPosition().getWorldMapInstance().getNpc(284387);
		if (vengefulOrb != null) {
			vengefulOrb.getController().cancelCurrentSkill(null);
			vengefulOrb.getController().delete();
		}
	}

	@Override
	public int modifyInitialSkillDelay(int delay) {
		return 3000;
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
}
