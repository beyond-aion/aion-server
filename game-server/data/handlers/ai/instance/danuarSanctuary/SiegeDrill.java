package ai.instance.danuarSanctuary;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.handler.MoveEventHandler;
import com.aionemu.gameserver.ai.manager.WalkManager;
import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_USE_OBJECT;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author xTz
 */
@AIName("siege_drill")
public class SiegeDrill extends NpcAI {

	protected int startBarAnimation = 1;
	protected int cancelBarAnimation = 2;
	private AtomicBoolean isUsed = new AtomicBoolean(false);

	public SiegeDrill(Npc owner) {
		super(owner);
	}

	@Override
	public float modifyDamage(Creature attacker, float damage, Effect effect) {
		return 0;
	}

	@Override
	protected void handleDialogStart(Player player) {
		if (isUsed.get()) {
			return;
		}
		handleUseItemStart(player);
	}

	@Override
	protected void handleMoveArrived() {
		super.handleMoveArrived();
		MoveEventHandler.onMoveArrived(this);
	}

	@Override
	protected void handleAttack(Creature creature) {
		// do nothing
	}

	protected void handleUseItemStart(final Player player) {
		final int delay = getTalkDelay();
		if (delay > 1) {
			final ItemUseObserver observer = new ItemUseObserver() {

				@Override
				public void abort() {
					player.getController().cancelTask(TaskId.ACTION_ITEM_NPC);
					PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.END_QUESTLOOT, 0, getObjectId()), true);
					PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), 0, cancelBarAnimation));
					player.getObserveController().removeObserver(this);
				}

			};

			player.getObserveController().attach(observer);
			PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), getTalkDelay(), startBarAnimation));
			PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.START_QUESTLOOT, 0, getObjectId()), true);
			player.getController().addTask(TaskId.ACTION_ITEM_NPC, ThreadPoolManager.getInstance().schedule(() -> {
				PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.END_QUESTLOOT, 0, getObjectId()), true);
				PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), getTalkDelay(), cancelBarAnimation));
				player.getObserveController().removeObserver(observer);
				handleUseItemFinish(player);
			}, delay));
		} else {
			handleUseItemFinish(player);
		}
	}

	protected void handleUseItemFinish(Player player) {
		if (!player.getInventory().decreaseByItemId(185000174, 1)) {
			PacketSendUtility.broadcastToMap(getOwner(), 1401932);
			return;
		}
		if (isUsed.compareAndSet(false, true)) {
			getOwner().getSpawn().setWalkerId("2A2D7F6EA351DCCEAA3CD097B9311ED308DCD2EC");
			WalkManager.startWalking(this);
			getOwner().setState(CreatureState.ACTIVE, true);
			PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.CHANGE_SPEED, 0, getObjectId()));
			ThreadPoolManager.getInstance().schedule(() -> {
				Npc npc = getPosition().getWorldMapInstance().getNpc(233189);
				if (npc != null) {
					getOwner().getSpawn().setWalkerId(null);
					WalkManager.stopWalking(this);
					getOwner().setTarget(npc);

					SkillEngine.getInstance().getSkill(getOwner(), 20778, 65, npc).useSkill();
					ThreadPoolManager.getInstance().schedule(() -> AIActions.deleteOwner(SiegeDrill.this), 5000);
				}
			}, 4800);
		}
	}

	protected int getTalkDelay() {
		return getObjectTemplate().getTalkDelay() * 1000;
	}

}
