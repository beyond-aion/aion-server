package ai.worlds.kaldor;

import static com.aionemu.gameserver.model.DialogAction.SETPRO1;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.controllers.NpcController;
import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.model.DialogPage;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

/**
 * @author Ritsu, Estrayl, Neon
 */
@AIName("anohas_sword")
public class AnohasSwordAI extends NpcAI {

	public AnohasSwordAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDialogStart(Player player) {
		int fortressLegionId = SiegeService.getInstance().getFortress(7011).getLegionId();
		if (player.getLegion() != null && player.getLegion().getLegionId() == fortressLegionId && player.getLegionMember().isBrigadeGeneral())
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 10));
		else
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogActionId, int questId, int extendedRewardIndex) {
		if (dialogActionId != SETPRO1)
			return false;

		if (!player.getInventory().decreaseByItemId(185000215, 1)) {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), DialogPage.NO_RIGHT.id()));
			return false;
		}

		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 2375));
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_LDF5_FORTRESS_NAMED_SPAWN_ITEM());
		World.getInstance().forEachPlayer(receiver -> {
			PacketSendUtility.sendPacket(receiver, SM_SYSTEM_MESSAGE.STR_MSG_LDF5_FORTRESS_NAMED_SPAWN());
			PacketSendUtility.sendPacket(receiver, SM_SYSTEM_MESSAGE.STR_MSG_LDF5_FORTRESS_NAMED_SPAWN_SYSTEM());
		});
		Race raceSummoned = player.getRace();
		Npc sword = getOwner();
		NpcController swordController = sword.getController();
		AIActions.die(this); // kill sword (starts animation)
		swordController.addTask(TaskId.DECAY, ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			PacketSendUtility.broadcastPacket(sword, new SM_EMOTION(sword, EmotionType.RESURRECT));
			PacketSendUtility.broadcastPacket(sword, new SM_EMOTION(sword, EmotionType.DIE));
		}, 10 * 1000, 10 * 1000)); // disable despawn, repeat special die animation instead until anoha spawns
		VisibleObject flag = spawn(702618, 791.38214f, 488.93655f, 143.47617f, (byte) 30); // map icon
		ThreadPoolManager.getInstance().schedule(() -> {
			Npc berserkAnoha = (Npc) spawn(855263, 791.38214f, 488.93655f, 143.47517f, (byte) 30); // Berserk Anoha
			berserkAnoha.getObserveController().addObserver(new ActionObserver(ObserverType.DEATH) {

				@Override
				public void died(Creature creature) {
					flag.getController().delete();
				}
			});
			PacketSendUtility.broadcastToWorld(SM_SYSTEM_MESSAGE.STR_MSG_ANOHA_SPAWN());
			swordController.delete(); // despawn sword
		}, 30 * 60000);
		return true;
	}

	@Override
	public boolean ask(AIQuestion question) {
		return question == AIQuestion.ALLOW_RESPAWN ? false : super.ask(question);
	}
}
