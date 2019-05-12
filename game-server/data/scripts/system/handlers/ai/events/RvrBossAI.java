package ai.events;

import java.util.List;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.LetterType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.services.mail.SystemMailService;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.time.ServerTime;
import com.aionemu.gameserver.world.WorldMapInstance;

import ai.AggressiveNpcAI;

/**
 * @author Bobobear
 */
@AIName("rvr_boss")
public class RvrBossAI extends AggressiveNpcAI {

	public RvrBossAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		// add player to event list for additional reward
		if (creature instanceof Player && getPosition().getMapId() == 600010000) {
			SiegeService.getInstance().checkRvrEventPlayer((Player) creature);
		}
		// TODO Spawn defensive guards (only for bosses in silentera Canyon)
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		despawnEnemyBoss();
		scheduleRespawn();
		sendParticipationRewards();
	}

	// despawn enemy boss (only for silentera)
	private void despawnEnemyBoss() {
		if (getPosition().getMapId() == 600010000) {
			WorldMapInstance instance = getPosition().getWorldMapInstance();
			deleteNpcs(instance.getNpcs(getNpcId() == 220948 ? 220949 : 220948));
		}
	}

	// schedule respawn of both silentera bosses (bosses in other maps must not respawn)
	private void scheduleRespawn() {
		if (getPosition().getMapId() == 600010000) {
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					spawn(220948, 658.7087f, 795.21857f, 293.14087f, (byte) 7);
					spawn(220949, 657.95105f, 737.5624f, 293.19818f, (byte) 0);
				}
			}, 43200000);
		}
	}

	private void deleteNpcs(List<Npc> npcs) {
		for (Npc npc : npcs) {
			if (npc != null)
				npc.getController().delete();
		}
	}

	private void sendParticipationRewards() {
		if (getPosition().getMapId() == 600010000) {
			int hour = ServerTime.now().getHour();
			if (hour >= 19 && hour <= 23) {
				for (Player rewardedPlayer : SiegeService.getInstance().getRvrEventPlayers()) {
					SystemMailService.sendMail("EventService", rewardedPlayer.getName(), "EventReward", "Medal", 186000147, 1, 0, LetterType.NORMAL);
				}
			}
			SiegeService.getInstance().clearRvrEventPlayers();
		}
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
	}
}
