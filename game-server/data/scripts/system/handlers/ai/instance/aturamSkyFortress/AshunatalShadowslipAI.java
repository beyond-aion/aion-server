package ai.instance.aturamSkyFortress;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.manager.WalkManager;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author xTz
 */
@AIName("ashunatal_shadowslip")
public class AshunatalShadowslipAI extends AggressiveNpcAI {

	private AtomicBoolean isHome = new AtomicBoolean(true);
	private boolean canThink = true;

	@Override
	public boolean canThink() {
		return canThink;
	}

	@Override
	protected void handleCreatureAggro(Creature creature) {
		super.handleCreatureAggro(creature);
		if (isHome.compareAndSet(true, false)) {
			getPosition().getWorldMapInstance().getDoors().get(2).setOpen(true); // this actually closes it on client side (wtf)
		}
	}

	@Override
	protected void handleBackHome() {
		isHome.set(true);
		super.handleBackHome();
		getPosition().getWorldMapInstance().getDoors().get(2).setOpen(false); // this actually opens it on client side (wtf)
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		// ashunatal got killed early, therefore she could not spawn her shadow which usually would open the door
		getPosition().getWorldMapInstance().getDoors().get(17).setOpen(false); // this actually opens it on client side (wtf)
		getPosition().getWorldMapInstance().getDoors().get(2).setOpen(false); // this actually opens it on client side (wtf)
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate) {
		super.onEndUseSkill(skillTemplate);
		if (skillTemplate.getSkillId() == 19417) {
			canThink = false;
			ThreadPoolManager.getInstance().schedule(() -> {
				getSpawnTemplate().setWalkerId("3002400001");
				getOwner().setState(CreatureState.ACTIVE, true);
				WalkManager.startWalking(this);
				PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.START_EMOTE2, 0, getObjectId()));
				ThreadPoolManager.getInstance().schedule(() -> getOwner().getController().delete(), 4000);
			}, 3000);
		}
	}

}
