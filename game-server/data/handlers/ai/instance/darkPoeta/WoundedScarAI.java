package ai.instance.darkPoeta;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.manager.WalkManager;
import com.aionemu.gameserver.model.CreatureType;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldPosition;

import ai.GeneralNpcAI;

/**
 * @author Estrayl
 */
@AIName("wounded_scar")
public class WoundedScarAI extends GeneralNpcAI {

	private AtomicBoolean isDestinationReached = new AtomicBoolean();

	public WoundedScarAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	protected void handleDialogStart(Player player) {
		Npc owner = getOwner();
		owner.getSpawn().setWalkerId("9C671D72B623B88FDFAFF9E2AF491976B84AE720");
		owner.overrideNpcType(CreatureType.INVULNERABLE);
		WalkManager.startWalking((NpcAI) owner.getAi());
	}

	@Override
	protected void handleMoveArrived() {
		super.handleMoveArrived();
		if (getOwner().getMoveController().isStop() && isDestinationReached.compareAndSet(false, true)) {
			WorldPosition p = getPosition();
			spawn(281116, p.getX(), p.getY(), p.getZ(), p.getHeading());
			AIActions.deleteOwner(this);
			return;
		}
		switch (getMoveController().getCurrentStep().getStepIndex()) {
			case 8:
				SkillEngine.getInstance().applyEffectDirectly(18532, getOwner(), getOwner());
				spawn(281148, 648.8706f, 1167.4102f, 143.6956f, (byte) 2);
				spawn(281148, 649.7064f, 1164.4081f, 143.4077f, (byte) 6);
				spawn(281148, 651.4119f, 1161.6421f, 143.6618f, (byte) 11);
				getOwner().setState(CreatureState.ACTIVE, true);
				PacketSendUtility.broadcastToMap(getOwner(), new SM_EMOTION(getOwner(), EmotionType.RUN));
				break;
			case 15:
				SkillEngine.getInstance().applyEffectDirectly(18532, getOwner(), getOwner());
				spawn(281149, 530.7089f, 1179.5717f, 138.0460f, (byte) 7);
				break;
			case 25:
				SkillEngine.getInstance().applyEffectDirectly(18532, getOwner(), getOwner());
				spawn(281150, 508.9217f, 1050.1077f, 123.8376f, (byte) 87);
				break;
			case 26:
				SkillEngine.getInstance().applyEffectDirectly(18532, getOwner(), getOwner());
				spawn(281151, 504.4358f, 1064.9199f, 126.04446f, (byte) 53);
				spawn(281151, 506.9358f, 1066.3199f, 126.04446f, (byte) 53);
				break;
		}

	}
}
