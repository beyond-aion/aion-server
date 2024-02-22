package ai.instance.theShugoEmperorsVault;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_USE_OBJECT;
import com.aionemu.gameserver.services.DialogService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.GeneralNpcAI;

/**
 * @author Yeats
 */
@AIName("emperorsVaultMorphNPC")
public class ShugoMorpher extends GeneralNpcAI {

	private AtomicBoolean started = new AtomicBoolean(false);

	public ShugoMorpher(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		// do nothing
	}

	protected void handleDialogStart(Player player) {
		if (DialogService.isInteractionAllowed(player, getOwner()) && started.compareAndSet(false, true)) {
			final int delay = 1000;
			final ItemUseObserver obs = new ItemUseObserver() {

				@Override
				public void abort() {
					started.set(false);
					player.getObserveController().removeObserver(this);
					player.getController().cancelTask(TaskId.ACTION_ITEM_NPC);
					PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.END_QUESTLOOT, 0, getObjectId()), true);
					PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), 0, 2));
				}

			};

			player.getObserveController().attach(obs);
			PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), delay, 1));
			PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.START_QUESTLOOT, 0, getObjectId()), true);
			player.getController().addTask(TaskId.ACTION_ITEM_NPC, ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.END_QUESTLOOT, 0, getObjectId()), true);
					PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), getObjectId(), delay, 2));
					player.getObserveController().removeObserver(obs);
					handleUseItemFinish(player);
				}

			}, delay));
		}
	}

	private void handleUseItemFinish(Player player) {
		Race race = player.getRace();
		boolean morphed = switch (getOwner().getNpcId()) {
			case 833491, 833494, 832935 -> SkillEngine.getInstance().getSkill(getOwner(), race == Race.ELYOS ? 21829 : 21832, 1, player).useSkill();
			case 833492, 833495, 832936 -> SkillEngine.getInstance().getSkill(getOwner(), race == Race.ELYOS ? 21830 : 21833, 1, player).useSkill();
			case 833493, 833496, 832937 -> SkillEngine.getInstance().getSkill(getOwner(), race == Race.ELYOS ? 21831 : 21834, 1, player).useSkill();
			default -> false;
		};

		if (morphed) {
			getOwner().getController().delete();
		} else {
			started.set(false);
		}
	}

	@Override
	public float modifyDamage(Creature attacker, float damage, Effect effect) {
		return 0;
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case REWARD_AP_XP_DP_LOOT, REWARD_LOOT -> false;
			default -> super.ask(question);
		};
	}
}
