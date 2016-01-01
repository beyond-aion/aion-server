package ai.instance.aturamSkyFortress;

import java.util.concurrent.atomic.AtomicBoolean;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.AIState;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author xTz
 */
@AIName("ashunatal_shadowslip")
public class AshunatalShadowslipAI2 extends AggressiveNpcAI2 {

	private AtomicBoolean isHome = new AtomicBoolean(true);
	private boolean isSummoned;
	private boolean canThink = true;

	@Override
	public boolean canThink() {
		return canThink;
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (isHome.compareAndSet(true, false)) {
			getPosition().getWorldMapInstance().getDoors().get(17).setOpen(true);
			getPosition().getWorldMapInstance().getDoors().get(2).setOpen(true);
		}
		checkPercentage(getLifeStats().getHpPercentage());
	}

	private void checkPercentage(int hpPercentage) {
		if (hpPercentage <= 50 && !isSummoned) {
			isSummoned = true;
			SkillEngine.getInstance().getSkill(getOwner(), 19428, 1, getOwner()).useNoAnimationSkill();
			doSchedule();
		}
	}

	@Override
	protected void handleBackHome() {
		isHome.set(true);
		isSummoned = false;
		super.handleBackHome();
		getPosition().getWorldMapInstance().getDoors().get(17).setOpen(true);
		getPosition().getWorldMapInstance().getDoors().get(2).setOpen(false);
		Npc npc = getPosition().getWorldMapInstance().getNpc(219186);
		if (npc != null) {
			npc.getController().onDelete();
		}
	}

	private void doSchedule() {
		if (!isAlreadyDead()) {
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					if (!isAlreadyDead()) {
						SkillEngine.getInstance().getSkill(getOwner(), 19417, 49, getOwner()).useNoAnimationSkill();
						ThreadPoolManager.getInstance().schedule(new Runnable() {

							@Override
							public void run() {
								if (!isAlreadyDead()) {
									WorldPosition p = getPosition();
									spawn(219186, p.getX(), p.getY(), p.getZ(), p.getHeading());
									canThink = false;
									getSpawnTemplate().setWalkerId("3002400001");
									setStateIfNot(AIState.WALKING);
									think();
									getOwner().setState(1);
									PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.START_EMOTE2, 0, getObjectId()));
									ThreadPoolManager.getInstance().schedule(new Runnable() {

										@Override
										public void run() {
											if (!isAlreadyDead()) {
												despawn();
											}
										}

									}, 4000);
								}
							}

						}, 3000);
					}
				}

			}, 2000);

		}
	}

	private void despawn() {
		AI2Actions.deleteOwner(this);
	}
}
