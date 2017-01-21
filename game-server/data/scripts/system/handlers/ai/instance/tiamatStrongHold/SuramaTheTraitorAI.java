package ai.instance.tiamatStrongHold;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.model.CreatureType;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CUSTOM_SETTINGS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.GeneralNpcAI;

/**
 * @author Cheatkiller
 */
@AIName("suramathetraitor")
public class SuramaTheTraitorAI extends GeneralNpcAI {

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		moveToRaksha();
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		PacketSendUtility.broadcastMessage(getOwner(), 390845, 2000);
	}

	private void moveToRaksha() {
		setStateIfNot(AIState.WALKING);
		getOwner().setState(CreatureState.ACTIVE, true);
		getMoveController().moveToPoint(651, 1319, 487);
		PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.START_EMOTE2, 0, getOwner().getObjectId()));
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				startDialog();
			}
		}, 10000);
	}

	private void startDialog() {
		final Npc raksha = getPosition().getWorldMapInstance().getNpc(219356);
		PacketSendUtility.broadcastMessage(getOwner(), 390841);
		PacketSendUtility.broadcastMessage(getOwner(), 390842, 3000);
		PacketSendUtility.broadcastMessage(raksha, 390843, 6000);
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				raksha.setTarget(getOwner());
				SkillEngine.getInstance().getSkill(raksha, 20952, 60, getOwner()).useNoAnimationSkill();
				changeNpcType(raksha, CreatureType.ATTACKABLE);
			}
		}, 8000);
	}

	private void changeNpcType(Npc npc, CreatureType newType) {
		npc.setNpcType(newType);
		npc.getKnownList().forEachPlayer(player -> {
			PacketSendUtility.sendPacket(player, new SM_CUSTOM_SETTINGS(npc.getObjectId(), 0, newType.getId(), 0));
		});
	}
}
