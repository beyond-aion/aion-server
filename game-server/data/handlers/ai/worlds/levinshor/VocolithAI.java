package ai.worlds.levinshor;

import static com.aionemu.gameserver.model.DialogAction.SETPRO1;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_USE_OBJECT;
import com.aionemu.gameserver.services.RespawnService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.GeneralNpcAI;

/**
 * @author Yeats, Neon
 */
@AIName("LDF4_Advance_Vocolith")
public class VocolithAI extends GeneralNpcAI {

	private AtomicBoolean used = new AtomicBoolean();

	public VocolithAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDialogStart(Player player) {
		if (!used.get()) {
			ItemUseObserver observer = new ItemUseObserver() {

				@Override
				public void abort() {
					player.getController().cancelTask(TaskId.ACTION_ITEM_NPC);
					PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.END_QUESTLOOT, 0, getObjectId()), true);
					PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), 0, 2));
					player.getObserveController().removeObserver(this);
				}
			};
			int delay = 1500;
			player.getObserveController().attach(observer);
			PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), delay, 1));
			PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.START_QUESTLOOT, 0, getObjectId()), true);
			player.getController().addTask(TaskId.ACTION_ITEM_NPC, ThreadPoolManager.getInstance().schedule(() -> {
				PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.END_QUESTLOOT, 0, getObjectId()), true);
				PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), delay, 2));
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
				player.getObserveController().removeObserver(observer);
			}, delay));
		}
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogActionId, int questId, int extendedRewardIndex) {
		if (dialogActionId == SETPRO1) {
			if (player.getInventory().getItemCountByItemId(185000216) == 0) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_FNAMED_FAIL());
				return false;
			} else if (used.compareAndSet(false, true)) {
				handleUsed(player);
			}
		}
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
		return true;
	}

	private void handleUsed(Player player) {
		if (player.getInventory().decreaseByItemId(185000216, 1)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_FNAMED_SPAWN_ITEM());
			spawnRandomBoss();
		} else {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_FNAMED_FAIL());
			used.set(false);
		}
	}

	private void spawnRandomBoss() {
		Npc vocolith = getOwner();
		AIActions.die(this); // kill vocolith
		RespawnService.scheduleDecayTask(vocolith, 7500); // schedule late despawn to show full death animation
		ThreadPoolManager.getInstance().schedule(() -> { // schedule boss spawn
			int npcId = 235217 + Rnd.get(0, 3);
			Npc boss = (Npc) spawn(npcId, vocolith.getX(), vocolith.getY(), vocolith.getZ(), vocolith.getHeading());
			PacketSendUtility.broadcastToMap(boss, SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_FNAMED_SPAWN());
		}, 5000);
	}
}
