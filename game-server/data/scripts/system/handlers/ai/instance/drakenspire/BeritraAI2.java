package ai.instance.drakenspire;

import java.util.concurrent.Future;

import ai.AggressiveNpcAI2;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;

@AIName("beritra")
public class BeritraAI2 extends AggressiveNpcAI2 {

	private Future<?> rebuff;
	private Future<?> buffCheck;

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
	}

	@Override
	protected void handleDied() {
		if (rebuff != null)
			rebuff.cancel(true);
		if (buffCheck != null)
			buffCheck.cancel(true);
		super.handleDied();
	}

	@Override
	protected void handleDespawned() {
		if (rebuff != null)
			rebuff.cancel(true);
		if (buffCheck != null)
			buffCheck.cancel(true);
		super.handleDespawned();
	}

	private void checkPercentage(int hpPercentage) {
		if (getOwner().getNpcId() == 236245 || getOwner().getNpcId() == 236246 || getOwner().getNpcId() == 236247) {
			if (hpPercentage == 100) {
				if (rebuff != null)
					rebuff.cancel(true);
			}
			if (hpPercentage < 100 && hpPercentage > 40) {
				if (rebuff == null) {
					rebuff = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

						@Override
						public void run() {
							switch (Rnd.get(1, 3)) {
								case 1:
									if (!getOwner().getEffectController().isAbnormalPresentBySkillId(21610))
										SkillEngine.getInstance().getSkill(getOwner(), (getNpcId() == 236245 ? 21610 : 21610), 60, getOwner()).useSkill();
									break;
								case 2:
									if (!getOwner().getEffectController().isAbnormalPresentBySkillId(21611))
										SkillEngine.getInstance().getSkill(getOwner(), (getNpcId() == 236245 ? 21611 : 21611), 60, getOwner()).useSkill();
									break;
								case 3:
									if (!getOwner().getEffectController().isAbnormalPresentBySkillId(21612))
										SkillEngine.getInstance().getSkill(getOwner(), (getNpcId() == 236245 ? 21612 : 21612), 60, getOwner()).useSkill();
									break;
							}
						}
					}, 90000, 90000);
				}
			}
			if (hpPercentage <= 40) {
				rebuff.cancel(true);
				if (rebuff == null) {
					rebuff = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

						@Override
						public void run() {
							switch (Rnd.get(1, 2)) {
								case 1:
									if (!getOwner().getEffectController().isAbnormalPresentBySkillId(21610))
										SkillEngine.getInstance().getSkill(getOwner(), (getNpcId() == 236245 ? 21610 : 21610), 60, getOwner()).useSkill();
									break;
								case 2:
									if (!getOwner().getEffectController().isAbnormalPresentBySkillId(21611))
										SkillEngine.getInstance().getSkill(getOwner(), (getNpcId() == 236245 ? 21611 : 21611), 60, getOwner()).useSkill();
									break;
							}
						}
					}, 90000, 90000);
				}
			}
			if (hpPercentage <= 25) {
				if (buffCheck == null) {
					buffCheck = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

						@Override
						public void run() {
							if (getOwner().getEffectController().isAbnormalPresentBySkillId(21612))
								SkillEngine.getInstance().getSkill(getOwner(), (getNpcId() == 236245 ? 21611 : 21611), 60, getOwner()).useSkill();
							else
								buffCheck.cancel(true);
						}
					}, 30000, 30000);

				}
			}

		}
	}
}
