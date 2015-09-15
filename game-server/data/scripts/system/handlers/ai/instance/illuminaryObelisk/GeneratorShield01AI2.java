package ai.instance.illuminaryObelisk;

import java.util.List;

import javolution.util.FastTable;
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
@AIName("generator_shield_01")
public class GeneratorShield01AI2 extends GeneralNpcAI2 {

	protected boolean isInstanceDestroyed = false;
	private boolean isCancelled;
	private List<Npc> npcs = new FastTable<Npc>();
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
		} else {
			if (fazeTask == 0) {
				player.getInventory().decreaseByItemId(164000289, 1);
				MsgGeneratorFaze();
				spawn(702218, 255.53876f, 297.46393f, 321.375f, (byte) 30);
				fazeTask++;
			} else if (fazeTask == 1) {
				player.getInventory().decreaseByItemId(164000289, 1);
				MsgGeneratorFaze();
				phaseAttack();
				spawn(702219, 255.53876f, 297.46393f, 321.375f, (byte) 30);
				fazeTask++;
			} else if (fazeTask == 2) {
				player.getInventory().decreaseByItemId(164000289, 1);
				MsgGeneratorFazefinal();
				phaseAttack();
				spawn(702220, 255.53876f, 297.46393f, 321.375f, (byte) 30);
				fazeTask++;
			} else if (fazeTask == 3) {
				MsgGeneratorFazeEnd();
			}
		}
	}

	protected void handleUseItemStart(final Player player, int usageTime) {
		if (usageTime >= 1000) {
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
			PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), usageTime, 1));
			PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.START_QUESTLOOT, 0, getObjectId()), true);
			player.getController().addTask(TaskId.ACTION_ITEM_NPC, ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.END_QUESTLOOT, 0, getObjectId()), true);
					PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), usageTime, 2));
					player.getObserveController().removeObserver(observer);
					handleUseItemFinish(player);
				}

			}, usageTime));
		} else {
			handleUseItemFinish(player);
		}
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogId, int questId, int extendedRewardIndex) {
		if (dialogId == 10000) {
			if (fazeTask == 0) {
				if (gate == null) {
					gate = (Npc) spawn(702017, 255.7926f, 338.22058f, 325.56473f, (byte) 0, 60);
					GateOpen();
					ThreadPoolManager.getInstance().schedule(new Runnable() {

						@Override
						public void run() {
							phaseAttack();
						}
					}, 5000);
				}
			}
			handleUseItemStart(player, 15000);
		}
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
		return false;
	}

	private void phaseAttack() {

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				sp(233720, 257.31f, 328.03f, 325.00f, (byte) 91, 1000, "1_left_301230000", false);
				sp(233721, 253.57f, 328.10f, 325.00f, (byte) 91, 1500, "1_right_301230000", false);
				sp(233722, 255.40f, 326.54f, 325.00f, (byte) 91, 2000, "1_center_301230000", false);
			}
		}, 1000);

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				sp(233723, 257.31f, 328.03f, 325.00f, (byte) 91, 1000, "1_left_301230000", false);
				sp(233724, 253.57f, 328.10f, 325.00f, (byte) 91, 1500, "1_right_301230000", false);
				sp(233725, 255.40f, 326.54f, 325.00f, (byte) 91, 2000, "1_center_301230000", false);
			}
		}, 10000);

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				sp(233726, 257.31f, 328.03f, 325.00f, (byte) 91, 1000, "1_left_301230000", false);
				sp(233727, 253.57f, 328.10f, 325.00f, (byte) 91, 1500, "1_right_301230000", false);
				sp(233728, 255.40f, 326.54f, 325.00f, (byte) 91, 2000, "1_center_301230000", false);
			}
		}, 20000);
	}

	// private void GenertorAttack() {
	// NpcShoutsService.getInstance().sendMsg(getOwner(), 1402220);
	// }

	private void GateOpen() {
		NpcShoutsService.getInstance().sendMsg(getOwner(), 1402224);
	}

	private void MsgGeneratorFaze() {
		NpcShoutsService.getInstance().sendMsg(getOwner(), 1402135);
	}

	private void MsgGeneratorFazefinal() {
		NpcShoutsService.getInstance().sendMsg(getOwner(), 1402198);
	}

	private void MsgGeneratorFazeEnd() {
		NpcShoutsService.getInstance().sendMsg(getOwner(), 1402203);
	}

	// private void MsgGeneratorNoIdium() {
	// NpcShoutsService.getInstance().sendMsg(getOwner(), 1402211);
	// }

	private void sp(final int npcId, final float x, final float y, final float z, final byte h, final int time, final String walkerId,
		final boolean isRun) {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isInstanceDestroyed && isCancelled == false) {
					Npc npc = (Npc) spawn(npcId, x, y, z, h);
					npcs.add(npc);
					npc.getSpawn().setWalkerId(walkerId);
					WalkManager.startWalking((NpcAI2) npc.getAi2());
					if (isRun) {
						npc.setState(1);
					} else {
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
		NpcShoutsService.getInstance().sendMsg(getOwner(), 1402139);
		Npc Fase01 = getPosition().getWorldMapInstance().getNpc(702218);
		Npc Fase02 = getPosition().getWorldMapInstance().getNpc(702219);
		Npc Fase03 = getPosition().getWorldMapInstance().getNpc(702220);
		if (gate != null)
			gate.getController().onDelete();
		deleteNpcs(npcs);
		if (Fase01 != null)
			Fase01.getController().onDelete();
		if (Fase02 != null)
			Fase02.getController().onDelete();
		if (Fase03 != null)
			Fase03.getController().onDelete();
	}
}
