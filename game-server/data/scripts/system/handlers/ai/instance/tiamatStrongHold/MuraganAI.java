package ai.instance.tiamatStrongHold;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION.ActionType;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

import ai.GeneralNpcAI;

/**
 * @author Cheatkiller
 */
@AIName("muragan")
public class MuraganAI extends GeneralNpcAI {

	private boolean isMove;

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		if (getOwner().getNpcId() == 800438) {
			PacketSendUtility.broadcastMessage(getOwner(), 390852, 1000);
		}
	}

	@Override
	protected void handleCreatureSee(Creature creature) {
		checkDistance(this, creature);
	}

	@Override
	protected void handleCreatureMoved(Creature creature) {
		checkDistance(this, creature);
	}

	private void checkDistance(NpcAI ai, Creature creature) {
		if (creature instanceof Player) {
			if (MathUtil.isIn3dRange(getOwner(), creature, 15) && !isMove) {
				isMove = true;
				openSuramaDoor();
				startWalk((Player) creature);
			}
		}
	}

	private void startWalk(final Player player) {
		int owner = getOwner().getNpcId();
		if (owner == 800436 || owner == 800438)
			return;
		switch (owner) {
			case 800435:
				PacketSendUtility.broadcastMessage(getOwner(), 390837);
				PacketSendUtility.broadcastMessage(getOwner(), 390838, 4000);
				killGuardCaptain();
				break;
		}
		setStateIfNot(AIState.WALKING);
		getOwner().setState(CreatureState.ACTIVE, true);
		getMoveController().moveToPoint(838, 1317, 396);
		PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.START_EMOTE2, 0, getOwner().getObjectId()));
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				forQuest(player);
				AIActions.deleteOwner(MuraganAI.this);
			}
		}, 10000);
	}

	private void openSuramaDoor() {
		if (getOwner().getNpcId() == 800436) {
			PacketSendUtility.broadcastMessage(getOwner(), 390835);
			getPosition().getWorldMapInstance().getDoors().get(56).setOpen(true);
			AIActions.deleteOwner(this);
		}
	}

	private void killGuardCaptain() {
		WorldMapInstance instance = getOwner().getPosition().getWorldMapInstance();
		for (Npc npc : instance.getNpcs()) {
			if (npc.getNpcId() == 219392) {// 4.0
				spawn(283145, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading());// 4.0
				npc.getController().delete();
			}
		}
	}

	private void forQuest(Player player) {
		int quest = player.getRace().equals(Race.ELYOS) ? 30708 : 30758;
		final QuestState qs = player.getQuestStateList().getQuestState(quest);
		if (qs != null && qs.getQuestVarById(0) != 5) {
			qs.setQuestVar(qs.getQuestVarById(0) + 1);
			PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(ActionType.UPDATE, qs));
		}
	}
}
