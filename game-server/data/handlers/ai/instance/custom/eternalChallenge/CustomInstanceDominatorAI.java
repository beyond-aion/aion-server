package ai.instance.custom.eternalChallenge;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AttackIntention;
import com.aionemu.gameserver.custom.instance.CustomInstanceRankEnum;
import com.aionemu.gameserver.custom.instance.CustomInstanceService;
import com.aionemu.gameserver.custom.instance.RoahCustomInstanceHandler;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureSeeState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FORCED_MOVE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.geo.GeoService;

import ai.AggressiveNoLootNpcAI;

/**
 * @author Jo
 */
@AIName("custom_instance_dominator")
public class CustomInstanceDominatorAI extends AggressiveNoLootNpcAI {

	private int challengerObjId;
	private int challengerRank;
	private Future<?> debuffTask;

	public CustomInstanceDominatorAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();

		WorldMapInstance wmi = getPosition().getWorldMapInstance();
		if (!(wmi.getInstanceHandler() instanceof RoahCustomInstanceHandler))
			return;

		challengerObjId = wmi.getRegisteredObjects().iterator().next();
		challengerRank = CustomInstanceService.getInstance().loadOrCreateRank(challengerObjId).getRank();

		debuffTask = ThreadPoolManager.getInstance().schedule(this::debuffTarget, 1000);
		getOwner().setSeeState(CreatureSeeState.SEARCH2);
	}

	@Override
	public AttackIntention chooseAttackIntention() {
		VisibleObject target = getTarget();
		if (!isDead() && target != null) {
			if (!GeoService.getInstance().canSee(getOwner(), target)) {
				World.getInstance().updatePosition(getOwner(), target.getX(), target.getY(), target.getZ(), (byte) 30);
				PacketSendUtility.broadcastPacketAndReceive(getOwner(), new SM_FORCED_MOVE(getOwner(), getOwner()));
			}
		}
		return super.chooseAttackIntention();
	}

	private void debuffTarget() {
		if (!isDead()) {
			if (getOwner().getSkillCoolDowns() != null)
				getOwner().getSkillCoolDowns().clear(); // Make CD rank-dependent, not skill-dependent

			if (challengerRank >= CustomInstanceRankEnum.ANCIENT.getMinRank())
				getOwner().queueSkill(3539, 65, 0); // Ignite Aether

			if (challengerRank >= CustomInstanceRankEnum.CERANIUM.getMinRank())
				getOwner().queueSkill(618, 65, 0); // Ankle Snare
			else if (challengerRank >= CustomInstanceRankEnum.GOLD.getMinRank())
				getOwner().queueSkill(1328, 65, 0); // Restraint (^)

			if (challengerRank >= CustomInstanceRankEnum.ANCIENT_PLUS.getMinRank())
				getOwner().queueSkill(3775, 65, 0); // Fear
			else if (challengerRank >= CustomInstanceRankEnum.GOLD.getMinRank())
				getOwner().queueSkill(1417, 65, 0); // Curse Tree (^)

			// SILVER:
			getOwner().queueSkill(3581, 65, 0); // Withering Gloom

			if (challengerRank >= CustomInstanceRankEnum.PLATINUM.getMinRank())
				getOwner().queueSkill(3574, 65, 0); // Shackle of Vulnerability
			else // SILVER:
				getOwner().queueSkill(3780, 65, 0); // Root of Enervation (^)

			if (challengerRank >= CustomInstanceRankEnum.MITHRIL.getMinRank())
				if (getOwner().getTarget() != null && getOwner().getTarget() instanceof Player
					&& ((Player) (getOwner().getTarget())).getPlayerClass().isPhysicalClass())
					getOwner().queueSkill(3571, 65, 0); // Body Root
				else
					getOwner().queueSkill(3572, 65, 0); // Sigil of Silence

			if (challengerRank >= CustomInstanceRankEnum.CERANIUM.getMinRank())
				if (getOwner().getTarget() != null && getOwner().getTarget() instanceof Player
					&& ((Player) (getOwner().getTarget())).getPlayerClass().isPhysicalClass())
					getOwner().queueSkill(4135, 65, 0); // Blinding Light
				else
					getOwner().queueSkill(1336, 65, 0); // Curse of Weakness

			if (challengerRank >= CustomInstanceRankEnum.ANCIENT.getMinRank())
				getOwner().queueSkill(4490, 65, 0); // Paralysis Resonation

			debuffTask = ThreadPoolManager.getInstance().schedule(this::debuffTarget, 30000 - challengerRank * 1000 / 3); // 30s ... 20s
		}

	}

	private void cancelTasks() {
		if (debuffTask != null && !debuffTask.isCancelled())
			debuffTask.cancel(true);
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

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		if (challengerObjId != 0) {
			ThreadPoolManager.getInstance().schedule(() -> {
				Player challenger = getPosition().getWorldMapInstance().getPlayer(challengerObjId);
				if (challenger != null && !getOwner().getAggroList().isHating(challenger) && getOwner().canSee(challenger))
					getOwner().getAggroList().addHate(challenger, 1000);
			}, 1000);
		}
	}
}
