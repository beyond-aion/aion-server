package ai.instance.raksang;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author xTz
 */
@AIName("vasuki_lifespark")
public class VasukiLifesparkAI extends AggressiveNpcAI {

	private final AtomicBoolean startedEvent = new AtomicBoolean();
	private boolean think = false;

	public VasukiLifesparkAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean canThink() {
		return think;
	}

	@Override
	protected void handleSpawned() {
		if (getNpcId() == 217764) {
			think = true;
		} else {
			ThreadPoolManager.getInstance().schedule(() -> {
				if (!isDead())
					SkillEngine.getInstance().getSkill(getOwner(), 19126, 46, getOwner()).useNoAnimationSkill();
			}, 3000);
		}
		super.handleSpawned();
	}

	@Override
	protected void handleCreatureMoved(Creature creature) {
		if (creature instanceof Player player) {
			if (PositionUtil.getDistance(getOwner(), player) <= 30) {
				if (startedEvent.compareAndSet(false, true)) {
					final int level;
					final int shoutId;
					final int skill;
					switch (getNpcId()) {
						case 217760:
							skill = 19972;
							level = 45;
							shoutId = 1401107;
							break;
						case 217761:
							skill = 19972;
							level = 46;
							shoutId = 1401171;
							break;
						case 217763:
							skill = 20087;
							level = 46;
							shoutId = 0;
							break;
						default:
							skill = 20039;
							level = 46;
							shoutId = 1401110;
							break;
					}
					if (shoutId != 0) {
						PacketSendUtility.broadcastMessage(getOwner(), shoutId);
					}
					SkillEngine.getInstance().getSkill(getOwner(), skill, level, getOwner()).useNoAnimationSkill();
					if (getNpcId() != 217764) {
						ThreadPoolManager.getInstance().schedule(() -> {
							if (!isDead()) {
								if (getNpcId() == 217763) {
									getPosition().getWorldMapInstance().setDoorState(219, true);
								}
								SkillEngine.getInstance().getSkill(getOwner(), 19967, level, getOwner()).useNoAnimationSkill();
								ThreadPoolManager.getInstance().schedule(() -> {
									if (!isDead())
										AIActions.deleteOwner(VasukiLifesparkAI.this);
								}, 3500);

							}
						}, 3500);
					} else {
						SkillEngine.getInstance().getSkill(getOwner(), 19974, 46, getOwner()).useNoAnimationSkill();
					}
				}
			}
		}
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case IS_IMMUNE_TO_ABNORMAL_STATES -> true;
			default -> super.ask(question);
		};
	}

	@Override
	protected void handleDied() {
		if (getNpcId() == 217764) {
			PacketSendUtility.broadcastMessage(getOwner(), 1401111);
			Npc soul = getPosition().getWorldMapInstance().getNpc(217471);
			Npc sapping = getPosition().getWorldMapInstance().getNpc(217472);
			if (soul != null) {
				soul.getEffectController().removeEffect(19126);
			}
			if (sapping != null) {
				sapping.getEffectController().removeEffect(19126);
			}
			PacketSendUtility.broadcastToMap(getOwner(), 1401140);
		}
		super.handleDied();
	}

}
