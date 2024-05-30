package ai.worlds.panesterra.ahserionsflight;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.controllers.attack.AggroInfo;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.npc.NpcTemplateType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.panesterra.ahserion.AhserionRaid;
import com.aionemu.gameserver.services.panesterra.ahserion.PanesterraFaction;
import com.aionemu.gameserver.services.panesterra.ahserion.PanesterraTeam;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Yeats, Estrayl
 */
@AIName("ahserion_tank_controller")
public class AhserionTankControllerAI extends AhserionConstructAI {

	private final AtomicBoolean canShout = new AtomicBoolean(true);

	public AhserionTankControllerAI(Npc owner) {
		super(owner);
	}

	@Override
	public void handleAttack(Creature creature) {
		super.handleAttack(creature);
		broadcastAttack();
	}

	private void broadcastAttack() {
		if (canShout.compareAndSet(true, false)) {
			if (!getOwner().isDead() && getOwner().getWorldId() == 400030000) {
				switch (getOwner().getSpawn().getStaticId()) {
					case 180:
						PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_GAB1_SUB_TANK_C_ATTACKED());
						break;
					case 181:
						PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_GAB1_SUB_TANK_B_ATTACKED());
						break;
					case 182:
						PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_GAB1_SUB_TANK_D_ATTACKED());
						break;
					case 183:
						PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_GAB1_SUB_TANK_A_ATTACKED());
						break;
				}
				ThreadPoolManager.getInstance().schedule(this::allowShout, 3000);
			}
		}
	}

	private void allowShout() {
		if (!isDead())
			canShout.set(true);
	}

	private void deleteRelatedNpcs() {
		getKnownList().forEachNpc(npc -> {
			if (PositionUtil.isInRange(getOwner(), npc, 40)) {
				// Tanks, Flag, Attacker
				if (npc.getRace() == Race.CONSTRUCT || npc.getNpcTemplateType() == NpcTemplateType.FLAG || npc.getNpcId() == 297185)
					npc.getController().deleteIfAliveOrCancelRespawn();
			}
		});
	}

	@Override
	protected void handleDespawned() {
		deleteRelatedNpcs();
		super.handleDespawned();
	}

	@Override
	public void handleDied() {
		deleteRelatedNpcs();
		switch (getOwner().getSpawn().getStaticId()) {
			case 180:
				PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_GAB1_SUB_TANK_C_BROKEN());
				break;
			case 181:
				PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_GAB1_SUB_TANK_B_BROKEN());
				break;
			case 182:
				PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_GAB1_SUB_TANK_D_BROKEN());
				break;
			case 183:
				PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_GAB1_SUB_TANK_A_BROKEN());
				break;
		}
		Map<PanesterraFaction, Integer> panesterraDamage = new HashMap<>();

		// Only players or balaur can attack the controller, there are no other npcs on this map
		for (AggroInfo ai : getOwner().getAggroList().getFinalDamageList(false)) {
			PanesterraFaction faction = null;
			if (ai.getAttacker() instanceof Player) {
				PanesterraTeam team = AhserionRaid.getInstance().getPanesterraFactionTeam((Player) ai.getAttacker());
				if (team != null && !team.isEliminated())
					faction = team.getFaction();
			} else
				faction = PanesterraFaction.BALAUR;

			panesterraDamage.merge(faction, ai.getDamage(), Integer::sum);
		}
		int staticId = getSpawnTemplate().getStaticId();

		ThreadPoolManager.getInstance().schedule(() -> spawnNewTankBase(staticId, findWinnerTeam(panesterraDamage)), 3000);
		super.handleDied();

	}

	private PanesterraFaction findWinnerTeam(Map<PanesterraFaction, Integer> panesterraDamage) {
		// just in case: we'll spawn balaur construct again
		PanesterraFaction winner = PanesterraFaction.BALAUR;
		int maxDmg = panesterraDamage.getOrDefault(PanesterraFaction.BALAUR, 0);
		for (PanesterraFaction faction : PanesterraFaction.values()) {
			Integer dmg = panesterraDamage.get(faction);
			PanesterraTeam team = AhserionRaid.getInstance().getFactionTeam(faction);
			if (dmg != null && team != null && !team.isEliminated()) {
				if (dmg > maxDmg) {
					maxDmg = dmg;
					winner = faction;
				}
			}
		}
		return winner;
	}

	private void spawnNewTankBase(int staticId, PanesterraFaction faction) {
		if (AhserionRaid.getInstance().isStarted())
			AhserionRaid.getInstance().spawnStage(staticId, faction);
	}
}
