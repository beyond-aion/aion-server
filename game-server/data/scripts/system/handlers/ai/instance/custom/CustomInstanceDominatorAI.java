package ai.instance.custom;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.custom.instance.CustomInstanceRankEnum;
import com.aionemu.gameserver.custom.instance.CustomInstanceService;
import com.aionemu.gameserver.custom.instance.RoahCustomInstanceHandler;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.skill.QueuedNpcSkillEntry;
import com.aionemu.gameserver.model.templates.npcskill.QueuedNpcSkillTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FORCED_MOVE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMapInstance;

import ai.AggressiveNpcAI;

/**
 * @author Jo
 */
@AIName("custom_instance_dominator")
public class CustomInstanceDominatorAI extends AggressiveNpcAI {

	private int playerObjId, rank;
	private Future<?> debuffTask, playerPositionCheckTask;

	public CustomInstanceDominatorAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();

		WorldMapInstance wmi = getPosition().getWorldMapInstance();
		if (!(wmi.getInstanceHandler() instanceof RoahCustomInstanceHandler))
			return;

		playerObjId = wmi.getRegisteredObjects().iterator().next();
		rank = CustomInstanceService.getInstance().getPlayerRankObject(playerObjId).getRank();

		debuffTask = ThreadPoolManager.getInstance().schedule(this::debuffTarget, 1000);
		playerPositionCheckTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(this::checkPlayerPosition, 15000, 15000);
	}

	private void checkPlayerPosition() {
		if (!isDead() && getOwner().getGameStats().getLastAttackedTimeDelta() > 10) {
			Player p = getPosition().getWorldMapInstance().getPlayer(playerObjId);
			if (p != null && PositionUtil.isInRange(p, getOwner(), 60)) {
				double radian = Math.toRadians(PositionUtil.convertHeadingToAngle(p.getHeading()));
				float x = (float) (p.getX() + Math.cos(Math.PI * 1 + radian) * 2);
				float y = (float) (p.getY() + Math.sin(Math.PI * 1 + radian) * 2);
				World.getInstance().updatePosition(getOwner(), x, y, p.getZ(), (byte) 30);
				PacketSendUtility.broadcastPacketAndReceive(getOwner(), new SM_FORCED_MOVE(getOwner(), getOwner()));
			}
		}
	}

	private void debuffTarget() {
		if (!isDead()) {
			if (getOwner().getSkillCoolDowns() != null)
				getOwner().getSkillCoolDowns().clear(); // Make CD rank-dependent, not skill-dependent

			if (rank >= CustomInstanceRankEnum.ANCIENT.getValue())
				getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(3539, 65, 100))); // Ignite Aether

			if (rank >= CustomInstanceRankEnum.CERANIUM.getValue())
				getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(618, 65, 100))); // Ankle Snare
			else if (rank >= CustomInstanceRankEnum.GOLD.getValue())
				getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(1328, 65, 100))); // Restraint (^)

			if (rank >= CustomInstanceRankEnum.ANCIENT_PLUS.getValue())
				getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(3775, 65, 100))); // Fear
			else if (rank >= CustomInstanceRankEnum.GOLD.getValue())
				getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(1417, 65, 100))); // Curse Tree (^)

			// SILVER:
			getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(3581, 65, 100))); // Withering Gloom

			if (rank >= CustomInstanceRankEnum.PLATINUM.getValue())
				getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(3574, 65, 100))); // Shackle of Vulnerability
			else // SILVER:
				getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(3780, 65, 100))); // Root of Enervation (^)

			if (rank >= CustomInstanceRankEnum.MITHRIL.getValue())
				if (getOwner().getTarget() != null && getOwner().getTarget() instanceof Player
					&& ((Player) (getOwner().getTarget())).getPlayerClass() == PlayerClass.PHYSICAL_CLASS)
					getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(3571, 65, 100))); // Body Root
				else
					getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(3572, 65, 100))); // Sigil of Silence

			if (rank >= CustomInstanceRankEnum.CERANIUM.getValue())
				if (getOwner().getTarget() != null && getOwner().getTarget() instanceof Player
					&& ((Player) (getOwner().getTarget())).getPlayerClass() == PlayerClass.PHYSICAL_CLASS)
					getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(4135, 65, 100))); // Blinding Light
				else
					getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(1336, 65, 100))); // Curse of Weakness

			if (rank >= CustomInstanceRankEnum.ANCIENT.getValue())
				getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(4490, 65, 100))); // Paralysis Resonation

			debuffTask = ThreadPoolManager.getInstance().schedule(this::debuffTarget, 30000 - rank * 1000 / 3); // 30s ... 20s
		}

	}

	private void cancelTasks() {
		if (debuffTask != null && !debuffTask.isCancelled())
			debuffTask.cancel(true);
		if (playerPositionCheckTask != null && !playerPositionCheckTask.isCancelled())
			playerPositionCheckTask.cancel(true);
	}

	@Override
	protected void handleDied() {
		cancelTasks();
		super.handleDied();
	}

	@Override
	protected void handleDespawned() {
		cancelTasks();
		super.handleDespawned();
	}

}
