package ai.worlds.levinshor;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_USE_OBJECT;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.Visitor;

import ai.GeneralNpcAI2;

/**
 * @author Yeats
 */
@AIName("LDF4_Advance_Vocolith")
public class VocolithAI2 extends GeneralNpcAI2 {

	private AtomicBoolean used = new AtomicBoolean();

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
			player.getController().addTask(TaskId.ACTION_ITEM_NPC, ThreadPoolManager.getInstance().schedule((Runnable) () -> {
				PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.END_QUESTLOOT, 0, getObjectId()), true);
				PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), delay, 2));
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
				player.getObserveController().removeObserver(observer);
			}, delay));
		}
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogId, int questId, int extendedRewardIndex) {
		if (dialogId == 10000) {
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
			World.getInstance().getWorldMap(getOwner().getWorldId()).getMainWorldMapInstance().doOnAllPlayers(new Visitor<Player>() {

				@Override
				public void visit(Player player) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_FNAMED_SPAWN());
				}
			});
			spawnRandomBoss();
		} else {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_LDF4_ADVANCE_FNAMED_FAIL());
			used.set(false);
		}
	}

	private void spawnRandomBoss() {
		PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.DIE, 0, 0));
		ThreadPoolManager.getInstance().schedule(() -> getOwner().getController().onDieSilence(), 7000);
		ThreadPoolManager.getInstance().schedule((Runnable) () -> {
				int npcId = 235217 + Rnd.get(0, 3);
				SpawnTemplate spawn = SpawnEngine.addNewSingleTimeSpawn(getOwner().getWorldId(), npcId, getOwner().getX(), getOwner().getY(), getOwner()
					.getZ(), getOwner().getHeading());
				SpawnEngine.spawnObject(spawn, getOwner().getInstanceId());
		}, 2000);
	}
}
