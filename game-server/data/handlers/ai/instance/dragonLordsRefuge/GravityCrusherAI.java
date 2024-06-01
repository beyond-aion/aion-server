package ai.instance.dragonLordsRefuge;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

import ai.AggressiveNpcAI;

/**
 * @author Cheatkiller, Estrayl
 */
@AIName("gravity_crusher")
public class GravityCrusherAI extends AggressiveNpcAI {

	public GravityCrusherAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		final WorldMapInstance instance = getPosition().getWorldMapInstance();
		ThreadPoolManager.getInstance().schedule(() -> {
			AIActions.targetCreature(this, Rnd.get(instance.getPlayersInside()));
			setStateIfNot(AIState.WALKING);
			getOwner().setState(CreatureState.ACTIVE, true);
			getMoveController().moveToTargetObject();
			PacketSendUtility.broadcastToMap(getOwner(), new SM_EMOTION(getOwner(), EmotionType.WALK));
			transform();
		}, 2000);
	}

	private void transform() {
		ThreadPoolManager.getInstance().schedule(() -> {
			if (!isDead())
				getOwner().queueSkill(getNpcId() == 283141 ? 20967 : 21900, 1);
		}, 30000);
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		switch (skillTemplate.getSkillId()) {
			case 20967:
			case 21900:
				spawn(getOwner().getLevel() == 65 ? getNpcId() + 1 : getNpcId() - 1, getOwner().getX(), getOwner().getY(), getOwner().getZ(),
					getOwner().getHeading());
				AIActions.deleteOwner(this);
				break;
		}
	}

	@Override
	public void handleMoveArrived() {
		super.handleMoveArrived();
		getOwner().queueSkill(20987, 1);
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case ALLOW_DECAY, ALLOW_RESPAWN, REWARD_AP_XP_DP_LOOT -> false;
			default -> super.ask(question);
		};
	}
}
