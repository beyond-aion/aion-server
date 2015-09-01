/*
 * Generatos Shield
 * The Illuminary Obelisk
 */
package ai.instance.illuminaryObelisk;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import ai.GeneralNpcAI2;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.manager.WalkManager;
import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_USE_OBJECT;
import com.aionemu.gameserver.services.NpcShoutsService;
import com.aionemu.gameserver.utils.PacketSendUtility;


/**
 * @author M.O.G. Dision
 */
@AIName("generator_shield_04")
public class GeneratorShield04AI2 extends GeneralNpcAI2 {

	protected boolean isInstanceDestroyed = false;
	private boolean isCancelled;
	private List<Npc> npcs = new ArrayList<Npc>();
	private Future<?> cancelSpawnTask;
	private int fazeTask = 0;
	private Npc gate;
  private int attackCount;

	
	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		attackCount++;
		if (attackCount == 10) {
			attackCount = 0;
			NpcShoutsService.getInstance().sendMsg(getOwner(), 1402220);
		}
	}

	
	@Override
	protected void handleDialogStart(Player player) {
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), DialogAction.SELECT_ACTION_1011.id()));
	}
	
	protected void handleUseItemFinish(Player player) {
		if (getOwner() == null || getOwner().getLifeStats().isAlreadyDead())
			return;
		if (player.getInventory().getItemCountByItemId(164000289) == 0) {
			NpcShoutsService.getInstance().sendMsg(getOwner(), 1402211);
			return;
		}
		else {
			if (fazeTask == 0) {
				player.getInventory().decreaseByItemId(164000289, 1);
				MsgGeneratorFaze();
				spawn(702224, 212.64922f, 254.5639f, 295.94763f, (byte) 60);
				fazeTask++;
			}	
			else if (fazeTask == 1) {
				player.getInventory().decreaseByItemId(164000289, 1);
				MsgGeneratorFaze();
				phaseAttack();
				spawn(702225, 212.64922f, 254.5639f, 295.94763f, (byte) 60);
				fazeTask++;
			}
			else if (fazeTask == 2) {
				player.getInventory().decreaseByItemId(164000289, 1);
				MsgGeneratorFazefinal();
				phaseAttack();
				spawn(702226, 212.64922f, 254.5639f, 295.94763f, (byte) 60);
				fazeTask++;
			}
			else if (fazeTask == 3) {
				MsgGeneratorFazeEnd();
			}
		}
	}
	
	
	protected void handleUseItemStart(final Player player) {
		final int delay = 15000;
		if (delay > 1) {
			final ItemUseObserver observer = new ItemUseObserver() {
				@Override
				public void abort() {
					player.getController().cancelTask(TaskId.ACTION_ITEM_NPC);
					PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.END_QUESTLOOT, 0, getObjectId()), true);
					PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), 0, 2));
					player.getObserveController().removeObserver(this);
				}

			};

			player.getObserveController().attach(observer);
			PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), delay, 1));
			PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.START_QUESTLOOT, 0, getObjectId()), true);
			player.getController().addTask(TaskId.ACTION_ITEM_NPC, ThreadPoolManager.getInstance().schedule(new Runnable() {
				@Override
				public void run() {
					PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.END_QUESTLOOT, 0, getObjectId()), true);
					PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), delay, 2));
					player.getObserveController().removeObserver(observer);
					handleUseItemFinish(player);
				}

			}, delay));
		}
		else {
			handleUseItemFinish(player);
		}
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogId, int questId, int extendedRewardIndex) {
		if (dialogId == 10000) {
			if (fazeTask == 0) {
				if (gate == null) {
					gate = (Npc)spawn(702015, 169.55626f, 254.52907f, 293.04276f, (byte) 0, 17);
					GateOpen();
					ThreadPoolManager.getInstance().schedule(new Runnable() {

						@Override
						public void run() {
							phaseAttack();
						}
					}, 5000);
				}
			}
			handleUseItemStart(player);
		}
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
		return false;
	}


	private void phaseAttack() {

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				sp(233723, 181.01f, 257.40f, 291.83f, (byte) 119, 1000, "4_left_301230000", false);
				sp(233724, 180.83f, 252.54f, 291.83f, (byte) 119, 1500, "4_right_301230000", false);
				sp(233725, 183.05f, 254.72f, 291.83f, (byte) 119, 2000, "4_center_301230000", false);
			}
		}, 1000);

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				sp(233726, 181.01f, 257.40f, 291.83f, (byte) 119, 1000, "4_left_301230000", false);
				sp(233727, 180.83f, 252.54f, 291.83f, (byte) 119, 1500, "4_right_301230000", false);
				sp(233728, 183.05f, 254.72f, 291.83f, (byte) 119, 2000, "4_center_301230000", false);
			}
		}, 10000);

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				sp(233729, 181.01f, 257.40f, 291.83f, (byte) 119, 1000, "4_left_301230000", false);
				sp(233722, 180.83f, 252.54f, 291.83f, (byte) 119, 1500, "4_right_301230000", false);
				sp(233721, 183.05f, 254.72f, 291.83f, (byte) 119, 2000, "4_center_301230000", false);
			}
		}, 20000);
	}

	private void GenertorAttack() {
		NpcShoutsService.getInstance().sendMsg(getOwner(), 1402223);
	}

	private void GateOpen() {
		NpcShoutsService.getInstance().sendMsg(getOwner(), 1402227);
	}

	private void MsgGeneratorFaze() {
		NpcShoutsService.getInstance().sendMsg(getOwner(), 1402138);
	}

	private void MsgGeneratorFazefinal() {
		NpcShoutsService.getInstance().sendMsg(getOwner(), 1402201);
	}

	private void MsgGeneratorFazeEnd() {
		NpcShoutsService.getInstance().sendMsg(getOwner(), 1402203);
	}

	private void MsgGeneratorNoIdium() {
		NpcShoutsService.getInstance().sendMsg(getOwner(), 1402211);
	}

	private void sp(final int npcId, final float x, final float y, final float z, final byte h, final int time, final String walkerId,
		final boolean isRun) {
		cancelSpawnTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isInstanceDestroyed && isCancelled == false) {
					Npc npc = (Npc) spawn(npcId, x, y, z, h);
					npcs.add(npc);
					npc.getSpawn().setWalkerId(walkerId);
					WalkManager.startWalking((NpcAI2) npc.getAi2());
					if (isRun) {
						npc.setState(1);
					}
					else {
						npc.setState(CreatureState.WALKING);
					}
					PacketSendUtility.broadcastPacket(npc, new SM_EMOTION(npc, EmotionType.START_EMOTE2, 0, npc.getObjectId()));
				}
			}
		}, time);
	}
	
	protected List<Npc> getNpcs(int npcId) {
		if (!isInstanceDestroyed) {
			return getNpcs(npcId);
		}
		return null;
	}

	private void deleteNpcs(List<Npc> npcs) {
		for (Npc npc : npcs) {
			if (npc != null) {
				npc.getController().onDelete();
			}
		}
	}
	
	
	@Override
	public boolean canThink() {
		return false;
	}
	
	
	@Override
	protected void handleDied() {
		super.handleDied();
		NpcShoutsService.getInstance().sendMsg(getOwner(), 1402142);
		Npc Fase01 = getPosition().getWorldMapInstance().getNpc(702224);
		Npc Fase02 = getPosition().getWorldMapInstance().getNpc(702225);
		Npc Fase03 = getPosition().getWorldMapInstance().getNpc(702226);
		if (gate != null)
			gate.getController().onDelete();
		for (Npc npc : npcs) {
			npc.getController().onDelete();
		}
		if (Fase01 != null)
			Fase01.getController().onDelete();
		if (Fase02 != null)
			Fase02.getController().onDelete();
		if (Fase03 != null)
			Fase03.getController().onDelete();
	}
}
