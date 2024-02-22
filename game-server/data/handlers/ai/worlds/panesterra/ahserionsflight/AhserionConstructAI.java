package ai.worlds.panesterra.ahserionsflight;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.controllers.attack.AggroInfo;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.TribeClass;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.spawns.SpawnGroup;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.panesterra.AhserionsFlightSpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.RespawnService;
import com.aionemu.gameserver.services.panesterra.ahserion.AhserionRaid;
import com.aionemu.gameserver.services.panesterra.ahserion.PanesterraFaction;
import com.aionemu.gameserver.services.panesterra.ahserion.PanesterraTeam;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Yeats, Estrayl
 */
@AIName("ahserion_construct")
public class AhserionConstructAI extends NpcAI {

	private Npc flag = null;
	private Future<?> attackSchedule;
	private AtomicBoolean canShout = new AtomicBoolean(true);

	public AhserionConstructAI(Npc owner) {
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
				allowShout();
			}
		}
	}

	private void allowShout() {
		ThreadPoolManager.getInstance().schedule(() -> {
			if (!isDead())
				canShout.set(true);
		}, 3000);
	}

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		if (getOwner().getWorldId() == 400030000) {
			spawnIcon();
			scheduleAttack();
		}
	}

	private void scheduleAttack() {
		if (getOwner().getSpawn().getStaticId() == 0 || !(getOwner().getSpawn() instanceof AhserionsFlightSpawnTemplate)
			|| !AhserionRaid.getInstance().isStarted() || getOwner().getTribe() == TribeClass.GAB1_SUB_NONAGGRESSIVE_DRAKAN) {
			return;
		}
		attackSchedule = ThreadPoolManager.getInstance().schedule(() -> {
			switch (getOwner().getSpawn().getStaticId()) {
				case 180 -> AhserionRaid.getInstance().spawnStage(3, PanesterraFaction.BALAUR);
				case 181 -> AhserionRaid.getInstance().spawnStage(4, PanesterraFaction.BALAUR);
				case 182 -> AhserionRaid.getInstance().spawnStage(5, PanesterraFaction.BALAUR);
				case 183 -> AhserionRaid.getInstance().spawnStage(6, PanesterraFaction.BALAUR);
			}
		}, 300000); // 5min
	}

	private void spawnIcon() {
		switch (getOwner().getNpcId()) {
			// north
			case 297310: // balaur
				spawnNpc(804118);
				break;
			case 297314: // belus
				spawnNpc(804114);
				break;
			case 297318: // aspida
				spawnNpc(804115);
				break;
			case 297322: // atanatos
				spawnNpc(804116);
				break;
			case 297326: // disillon
				spawnNpc(804117);
				break;

			// south
			case 297311: // balaur
				spawnNpc(804123);
				break;
			case 297315: // belus
				spawnNpc(804119);
				break;
			case 297319: // aspida
				spawnNpc(804120);
				break;
			case 297323: // atanatos
				spawnNpc(804121);
				break;
			case 297327: // disillon
				spawnNpc(804122);
				break;

			// west
			case 297312: // balaur
				spawnNpc(804128);
				break;
			case 297316: // belus
				spawnNpc(804124);
				break;
			case 297320: // aspida
				spawnNpc(804125);
				break;
			case 297324: // atanatos
				spawnNpc(804126);
				break;
			case 297328: // disillon
				spawnNpc(804127);
				break;

			// east
			case 297313: // balaur
				spawnNpc(804133);
				break;
			case 297317: // belus
				spawnNpc(804129);
				break;
			case 297321: // aspida
				spawnNpc(804130);
				break;
			case 297325: // atanatos
				spawnNpc(804131);
				break;
			case 297329: // disillon
				spawnNpc(804132);
				break;
		}
	}

	private void cancelAttackScheduleAndDeleteFlag() {
		if (attackSchedule != null && !attackSchedule.isCancelled())
			attackSchedule.cancel(false);
		if (flag != null)
			flag.getController().delete();
	}

	@Override
	protected void handleDespawned() {
		cancelAttackScheduleAndDeleteFlag();
		super.handleDespawned();
	}

	@Override
	public void handleDied() {
		cancelAttackScheduleAndDeleteFlag();
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
		PanesterraFaction winnerTeam = findWinnerTeam(panesterraDamage);
		spawnTankFleetForWinner(getOwner().getSpawn().getStaticId(), winnerTeam);
		RespawnService.scheduleDecayTask(getOwner(), 3000);
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

	private void spawnTankFleetForWinner(int staticId, PanesterraFaction faction) {
		ThreadPoolManager.getInstance().schedule(() -> {
			List<SpawnGroup> ahserionSpawns = DataManager.SPAWNS_DATA.getAhserionSpawnByTeamId(faction.getId());
			if (ahserionSpawns == null)
				return;

			for (SpawnGroup grp : ahserionSpawns) {
				for (SpawnTemplate template : grp.getSpawnTemplates()) {
					AhserionsFlightSpawnTemplate ahserionTemplate = (AhserionsFlightSpawnTemplate) template;
					if (ahserionTemplate.getStage() == staticId) {
						SpawnEngine.spawnObject(ahserionTemplate, 1);
						return;
					}
				}
			}
		}, 15 * 1000);
	}

	private void spawnNpc(int npcId) {
		flag = (Npc) spawn(npcId, getOwner().getX(), getOwner().getY(), getOwner().getZ(), getOwner().getHeading());
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case ALLOW_RESPAWN, REWARD_LOOT -> false;
			default -> super.ask(question);
		};
	}
}
