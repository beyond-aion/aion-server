package ai.instance.darkPoeta;

import java.util.List;
import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.handler.TalkEventHandler;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Ritsu
 */

@AIName("spaller_echtra")
public class SpallerEchtraAI extends AggressiveNpcAI {

	private Future<?> skillTask;

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkDirection();
	}

	private void checkDirection() {
		List<Npc> npcs = getPosition().getWorldMapInstance().getNpcs(281178);
		SkillTemplate paralyze = DataManager.SKILL_DATA.getSkillTemplate(8256);
		if (npcs != null) {
			for (Npc npc : npcs) {
				if (MathUtil.getDistance(getOwner(), npc) <= 2) {
					TalkEventHandler.onTalk(this, npc);
					AIActions.applyEffect(this, paralyze, getOwner());
					getOwner().getController().cancelCurrentSkill(null);
					getOwner().getMoveController().abortMove();
					getOwner().getEffectController().setAbnormal(AbnormalState.PARALYZE);
					skillTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

						@Override
						public void run() {
							SkillEngine.getInstance().getSkill(getOwner(), 18534, 50, getOwner()).useSkill();
							skillTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

								@Override
								public void run() {
									SkillEngine.getInstance().getSkill(getOwner(), 18574, 50, getOwner()).useSkill();
								}
							}, 3000);
						}
					}, 28000);
				}
			}
		}
	}

	private void cancelTask() {
		if (skillTask != null && !skillTask.isDone())
			skillTask.cancel(true);
	}

	@Override
	protected void handleBackHome() {
		cancelTask();
		super.handleBackHome();
	}

	@Override
	protected void handleDespawned() {
		cancelTask();
		super.handleDespawned();
	}

	@Override
	protected void handleDied() {
		cancelTask();
		super.handleDied();
	}
}
