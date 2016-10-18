package ai.worlds.panesterra.ahserionsflight;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.controllers.attack.AggroInfo;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.TribeClass;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.spawns.SpawnGroup2;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.panesterra.AhserionsFlightSpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.panesterra.ahserion.AhserionRaid;
import com.aionemu.gameserver.services.panesterra.ahserion.PanesterraTeamId;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

/**
 * @author Yeats
 *
 */
@AIName("ahserion_construct")
public class AhserionConstructAI extends NpcAI2 {
	
	private Npc flag = null;
	private Future<?> attackSchedule;
	private AtomicBoolean canShout = new AtomicBoolean(true);
	
	@Override
	public void handleAttack(Creature creature) {
		super.handleAttack(creature);
		broadcastAttack();
	}
	
	private void broadcastAttack() {
		if (canShout.compareAndSet(true, false)) {
			if (getOwner() != null && !getOwner().getLifeStats().isAlreadyDead()
				&& getOwner().getWorldId() == 400030000) {
				switch (getOwner().getSpawn().getStaticId()) {
					case 180:
						sendPacket(1402260);
						break;
					case 181:
						sendPacket(1402259);
						break;
					case 182:
						sendPacket(1402261);
						break;
					case 183:
						sendPacket(1402258);
						break;
				}
				allowShout();
			}
		}
	}
	
	private void allowShout() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				if (getOwner() != null && !getOwner().getLifeStats().isAlreadyDead()) {
					canShout.set(true);
				}
			}
		}, 3000);
	}
	
	private void sendPacket(int msgId) {
		World.getInstance().getWorldMap(400030000).getMainWorldMapInstance().forEachPlayer(new Consumer<Player>() {
      @Override
      public void accept(Player player) {
      	PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(msgId));
      }
		});
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
		if (getOwner().getSpawn().getStaticId() == 0
			|| !(getOwner().getSpawn() instanceof AhserionsFlightSpawnTemplate)
			|| !AhserionRaid.getInstance().isStarted()
			|| getOwner().getTribe() == TribeClass.GAB1_SUB_NONAGGRESSIVE_DRAKAN) {
			return;
		}
		attackSchedule = ThreadPoolManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				switch (getOwner().getSpawn().getStaticId()) {
					case 180:
						AhserionRaid.getInstance().spawnStage(3, PanesterraTeamId.BALAUR);
						break;
					case 181:
						AhserionRaid.getInstance().spawnStage(4, PanesterraTeamId.BALAUR);
						break;
					case 182:
						AhserionRaid.getInstance().spawnStage(5, PanesterraTeamId.BALAUR);
						break;
					case 183:
						AhserionRaid.getInstance().spawnStage(6, PanesterraTeamId.BALAUR);
							break;
				}
			}
		}, 300000); //5min
	}
	
	private void spawnIcon() {
		switch (getOwner().getNpcId()) {
			//north
			case 297310: //balaur
				spawnNpc(804118);
				break;
			case 297314: //belus
				spawnNpc(804114);
				break;
			case 297318: //aspida
				spawnNpc(804115);
				break;
			case 297322: //atanatos
				spawnNpc(804116);
				break;
			case 297326: //disillon
				spawnNpc(804117);
				break;
				
			//south
			case 297311: //balaur
				spawnNpc(804123);
				break;
			case 297315: //belus
				spawnNpc(804119);
				break;
			case 297319: //aspida
				spawnNpc(804120);
				break;
			case 297323: //atanatos
				spawnNpc(804121);
				break;
			case 297327: //disillon
				spawnNpc(804122);
				break;
				
			//west
			case 297312: //balaur
				spawnNpc(804128);
				break;
			case 297316: //belus
				spawnNpc(804124);
				break;
			case 297320: //aspida
				spawnNpc(804125);
				break;
			case 297324: //atanatos
				spawnNpc(804126);
				break;
			case 297328: //disillon
				spawnNpc(804127);
				break;
				
			//east
			case 297313: //balaur
				spawnNpc(804133);
				break;
			case 297317: //belus
				spawnNpc(804129);
				break;
			case 297321: //aspida
				spawnNpc(804130);
				break;
			case 297325: //atanatos
				spawnNpc(804131);
				break;
			case 297329: //disillon
				spawnNpc(804132);
				break;
		}
	}

	@Override
	protected void handleDespawned() {
		if (attackSchedule != null && !attackSchedule.isCancelled()) {
			attackSchedule.cancel(false);
		}
		if (flag != null) {
			flag.getController().delete();
		}
		super.handleDespawned();
	}
	
	@Override
	public void handleDied() {
		if (attackSchedule != null && !attackSchedule.isCancelled()) {
			attackSchedule.cancel(false);
		}
		if (flag != null) {
			flag.getController().delete();
		}
		switch (getOwner().getSpawn().getStaticId()) {
			case 180:
				sendPacket(1402264);
				break;
			case 181:
				sendPacket(1402263);
				break;
			case 182:
				sendPacket(1402265);
				break;
			case 183:
				sendPacket(1402262);
					break;
		}
		Map<PanesterraTeamId, Integer> panesterraDamage = new HashMap<>();
		
		//Only players or balaur can attack the controller, there are no other npcs on this map
		for (AggroInfo ai : getOwner().getAggroList().getFinalDamageList(false)) {
			if (ai.getAttacker() instanceof Player) {
				Player attacker = (Player) ai.getAttacker();
				if (attacker.getPanesterraTeam() != null) {
					PanesterraTeamId teamId = attacker.getPanesterraTeam().getTeamId();
					
					if (panesterraDamage.containsKey(teamId)) {
						int curDamage = panesterraDamage.get(teamId);
						panesterraDamage.put(teamId, curDamage + ai.getDamage());
					} else {
						panesterraDamage.put(attacker.getPanesterraTeam().getTeamId(), ai.getDamage());
					}
				}
			} else {
				if (panesterraDamage.containsKey(PanesterraTeamId.BALAUR)) {
					int curDmg = panesterraDamage.get(PanesterraTeamId.BALAUR);
					panesterraDamage.put(PanesterraTeamId.BALAUR, curDmg + ai.getDamage());
				} else {
					panesterraDamage.put(PanesterraTeamId.BALAUR,ai.getDamage());
				}
			}
		}
		getWinnerTeam(panesterraDamage);
		super.handleDied();
	}

	/**
	 * @param panesterraDamage
	 */
	private void getWinnerTeam(Map<PanesterraTeamId, Integer> panesterraDamage) {
		//just in case: we'll spawn balaur construct again
		PanesterraTeamId winner = PanesterraTeamId.BALAUR;
		int maxDmg = 0;
		if (panesterraDamage.containsKey(PanesterraTeamId.GAB1_SUB_DEST_69)
			&& AhserionRaid.getInstance().isTeamNotEliminated(PanesterraTeamId.GAB1_SUB_DEST_69)) {
			if (panesterraDamage.get(PanesterraTeamId.GAB1_SUB_DEST_69) > maxDmg) {
				maxDmg = panesterraDamage.get(PanesterraTeamId.GAB1_SUB_DEST_69);
				winner = PanesterraTeamId.GAB1_SUB_DEST_69;
			}
		}
		if (panesterraDamage.containsKey(PanesterraTeamId.GAB1_SUB_DEST_70)
			&& AhserionRaid.getInstance().isTeamNotEliminated(PanesterraTeamId.GAB1_SUB_DEST_70)) {
			if (panesterraDamage.get(PanesterraTeamId.GAB1_SUB_DEST_70) > maxDmg) {
				maxDmg = panesterraDamage.get(PanesterraTeamId.GAB1_SUB_DEST_70);
				winner = PanesterraTeamId.GAB1_SUB_DEST_70;
			}
		}
		if (panesterraDamage.containsKey(PanesterraTeamId.GAB1_SUB_DEST_71)
			&& AhserionRaid.getInstance().isTeamNotEliminated(PanesterraTeamId.GAB1_SUB_DEST_71)) {
			if (panesterraDamage.get(PanesterraTeamId.GAB1_SUB_DEST_71) > maxDmg) {
				maxDmg = panesterraDamage.get(PanesterraTeamId.GAB1_SUB_DEST_71);
				winner = PanesterraTeamId.GAB1_SUB_DEST_71;
			}
		}
		if (panesterraDamage.containsKey(PanesterraTeamId.GAB1_SUB_DEST_72)
			&& AhserionRaid.getInstance().isTeamNotEliminated(PanesterraTeamId.GAB1_SUB_DEST_72)) {
			if (panesterraDamage.get(PanesterraTeamId.GAB1_SUB_DEST_72) > maxDmg) {
				maxDmg = panesterraDamage.get(PanesterraTeamId.GAB1_SUB_DEST_72);
				winner = PanesterraTeamId.GAB1_SUB_DEST_72;
			}
		}
		spawnTankFleetForWinner(getOwner().getSpawn().getStaticId(), winner);
	}
	
	private void spawnTankFleetForWinner(int staticId, PanesterraTeamId team) {

		ThreadPoolManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				List<SpawnGroup2> ahserionSpawns = DataManager.SPAWNS_DATA2.getAhserionSpawnByTeamId(team.getId());
				if (ahserionSpawns == null) {
					return;
				}
				
				for (SpawnGroup2 grp : ahserionSpawns) {
					for (SpawnTemplate template : grp.getSpawnTemplates()) {
						AhserionsFlightSpawnTemplate ahserionTemplate = (AhserionsFlightSpawnTemplate) template;
						if (ahserionTemplate.getStage() == staticId) {
							SpawnEngine.spawnObject(ahserionTemplate, 1);
							return;
						}
					}
				}
			}
		}, 15 * 1000);
	}
	
	private void spawnNpc(int npcId) {
		SpawnTemplate template = SpawnEngine.addNewSingleTimeSpawn(400030000, npcId,
			getOwner().getX(), getOwner().getY(), getOwner().getZ(), getOwner().getHeading());
		flag = (Npc) SpawnEngine.spawnObject(template, 1);
		flag.getPosition().getMapRegion().activate();
	}
	
	@Override
	public boolean ask(AIQuestion question) {
		switch (question) {
			case SHOULD_DECAY:
			case SHOULD_RESPAWN:
			case SHOULD_LOOT:
				return false;
			default:
				return super.ask(question);
		}
	}
}
