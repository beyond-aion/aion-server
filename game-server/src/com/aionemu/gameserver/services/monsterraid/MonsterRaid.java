package com.aionemu.gameserver.services.monsterraid;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AbstractAI;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.monsterraid.MonsterRaidLocation;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.MonsterRaidService;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 * @author Whoop
 */
public class MonsterRaid {

	private final MonsterRaidDeathListener deathListener = new MonsterRaidDeathListener(this);
	private final AtomicBoolean isFinished = new AtomicBoolean(false);
	private final AtomicBoolean isStarted = new AtomicBoolean(false);
	private final MonsterRaidLocation mrl;
	private boolean isBossKilled;
	private Npc boss, flag, vortex;
	private Future<?> despawnTask;

	public MonsterRaid(MonsterRaidLocation mrl) {
		this.mrl = mrl;
	}

	public final void startMonsterRaid() throws RuntimeException {
		if (isStarted.compareAndSet(false, true))
			onMonsterRaidStart();
		else
			throw new RuntimeException("Attempt to start monster raid twice! ID:" + getLocationId());
	}

	public final void stopMonsterRaid() throws RuntimeException {
		if (isFinished.compareAndSet(false, true))
			onMonsterRaidFinish();
		else
			throw new RuntimeException("Attempt to stop monster raid twice! ID:" + getLocationId());
	}

	private final void onMonsterRaidStart() {
		spawnFlag();
		broadcastMessage(SM_SYSTEM_MESSAGE.STR_MSG_WORLDRAID_MESSAGE_01());
		// TODO: Better Implementation for this latency period
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				spawnVortex();
				broadcastMessage(SM_SYSTEM_MESSAGE.STR_MSG_WORLDRAID_MESSAGE_02());
				ThreadPoolManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						broadcastMessage(SM_SYSTEM_MESSAGE.STR_MSG_WORLDRAID_MESSAGE_03());
						ThreadPoolManager.getInstance().schedule(new Runnable() {

							@Override
							public void run() {
								spawnBoss();
								regDeathListener();
								broadcastMessage(SM_SYSTEM_MESSAGE.STR_MSG_WORLDRAID_MESSAGE_04());
								scheduleDespawn();
							}
						}, 300000);
						//}, Rnd.get(50, 100) * 6000); // 5 to 10 minutes after third announce
					}
				}, 900000);
				//}, Rnd.get(50, 1000) * 6000); // 5 to 100 minutes after second announce
			}
		}, 600000);
		//}, Rnd.get(100, 150) * 6000); // 10 to 15 minutes after initialization
	}

	private final void onMonsterRaidFinish() {
		rmvDeathListener();
		despawnVisiualNpcs();
		if (isBossKilled()) { // TODO: Switch for different NPCs
			broadcastMessage(SM_SYSTEM_MESSAGE.STR_MSG_WORLDRAID_MESSAGE_DIE_03());
			cancelDespawn();
		} else {
			boss.getController().onDelete();
		}
	}

	private final void scheduleDespawn() {
		despawnTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!boss.getLifeStats().isAlreadyDead())
					stopRaid();
			}
		}, 3600000); // two hours should be enough + some latency for troll
	}

	private final void cancelDespawn() {
		if (despawnTask != null && !despawnTask.isCancelled())
			despawnTask.cancel(true);
	}

	private void despawnVisiualNpcs() {
		if (flag != null)
			flag.getController().onDelete();
		if (vortex != null)
			vortex.getController().onDelete();
	}

	private void regDeathListener() {
		AbstractAI ai = (AbstractAI) getBoss().getAi2();
		ai.addEventListener(deathListener);
	}

	private void rmvDeathListener() {
		AbstractAI ai = (AbstractAI) getBoss().getAi2();
		ai.removeEventListener(deathListener);
	}

	private void spawnBoss() {
		SpawnTemplate temp = SpawnEngine.addNewSingleTimeSpawn(mrl.getWorldId(), mrl.getNpcIds().get(Rnd.get(0, mrl.getNpcIds().size() - 1)), mrl.getX(),
			mrl.getY(), mrl.getZ(), mrl.getH());
		initBoss((Npc) SpawnEngine.spawnObject(temp, 1));
	}

	private void spawnFlag() {
		SpawnTemplate temp = SpawnEngine.addNewSingleTimeSpawn(mrl.getWorldId(), 832819, mrl.getX(), mrl.getY(), mrl.getZ(), mrl.getH());
		initFlag((Npc) SpawnEngine.spawnObject(temp, 1));
	}

	private void spawnVortex() {
		SpawnTemplate temp = SpawnEngine.addNewSingleTimeSpawn(mrl.getWorldId(), 702550, mrl.getX(), mrl.getY(), mrl.getZ() + 40f, mrl.getH());
		initVortex((Npc) SpawnEngine.spawnObject(temp, 1));
	}

	private void initBoss(Npc npc) throws RuntimeException, NullPointerException {
		if (npc == null)
			throw new NullPointerException("No boss found for Monster Raid! ID:" + getLocationId());
		if (boss != null)
			throw new RuntimeException("Tried to initialize boss twice for Monster Raid! ID:" + getLocationId());

		boss = npc;
	}

	private void initFlag(Npc npc) throws RuntimeException, NullPointerException {
		if (npc == null)
			throw new NullPointerException("No flag found for Monster Raid! ID:" + getLocationId());
		if (flag != null)
			throw new RuntimeException("Tried to initialize flag twice for Monster Raid! ID:" + getLocationId());

		flag = npc;
	}

	private void initVortex(Npc npc) throws RuntimeException, NullPointerException {
		if (npc == null)
			throw new NullPointerException("No vortex found for Monster Raid! ID:" + getLocationId());
		if (vortex != null)
			throw new RuntimeException("Tried to initialize vortex twice for Monster Raid! ID:" + getLocationId());

		vortex = npc;
	}

	private void broadcastMessage(SM_SYSTEM_MESSAGE msg) {
		if (msg != null) {
			World.getInstance().getWorldMap(mrl.getWorldId()).getMainWorldMapInstance().forEachPlayer(new Visitor<Player>() {

				@Override
				public void visit(Player player) {
					PacketSendUtility.sendPacket(player, msg);
				}
			});
		}
	}

	public MonsterRaidLocation getLocation() {
		return mrl;
	}

	public int getLocationId() {
		return mrl.getLocationId();
	}

	public boolean isBossKilled() {
		return isBossKilled;
	}

	public void setBossKilled(boolean bossKilled) {
		this.isBossKilled = bossKilled;
	}

	public boolean isStarted() {
		return isStarted.get();
	}

	public boolean isFinished() {
		return isFinished.get();
	}

	public Npc getBoss() {
		return boss;
	}

	public Npc getFlag() {
		return flag;
	}

	public Npc getVortex() {
		return vortex;
	}

	/**
	 * Need to be stopped by service to remove active raid from list to secure multiple raids 
	 * for the same location if server is running longer then one day or multiple raids at one day
	 */
	private void stopRaid() {
		MonsterRaidService.getInstance().stopRaid(getLocationId());
	}
}
