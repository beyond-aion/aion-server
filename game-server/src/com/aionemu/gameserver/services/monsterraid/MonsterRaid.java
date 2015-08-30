package com.aionemu.gameserver.services.monsterraid;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AbstractAI;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.monsterraid.MonsterRaidLocation;
import com.aionemu.gameserver.services.MonsterRaidService;


/**
 * @author Whoop
 *
 */
public class MonsterRaid extends MonsterRaidLocation {
	
	private static final Logger log = LoggerFactory.getLogger("[MONSTER_RAID]");
	private final MonsterRaidBossDeathListener monsterRaidBossDeathListener = new MonsterRaidBossDeathListener(this);
	private final AtomicBoolean finished = new AtomicBoolean();
	private final MonsterRaidLocation mrl;
	private boolean bossKilled;
	private boolean started;
	private Npc boss;
	private Npc vortex;
	private Npc flag;
	
	public MonsterRaid(MonsterRaidLocation mrl) {
		this.mrl = mrl;
	}
	
	public final void startMonsterRaid() {
		
		boolean doubleStart = false;
		
		synchronized (this) {
			if (started) {
				doubleStart = true;
			}
			else {
				started = true;
			}
		}
		
		if (doubleStart) {
			log.error("Attempt to start monster raid of raid location" + mrl.getLocationId() + " for 2 times!");
			return;
		}	
		onMonsterRaidStart();
	}
	
	public final void startMonsterRaid(int locationId) {
		MonsterRaidService.getInstance().startRaid(locationId);
	}

	private void onMonsterRaidStart() {
		log.info("Monster Raid Location with ID#" + mrl.getLocationId() + " was activated!");
		//Mark Location on Map and spawn vortex
		spawnLocation();
		
		ThreadPoolManager.getInstance().schedule(new Runnable() { //TODO: Better Implementation for this latency period
			
			@Override
			public void run() {
				spawnBoss();
				broadcastUpdate(1402386);
				MonsterRaidService.getInstance().scheduleStop(mrl.getLocationId());
			}
		}, Rnd.get(10, 240) * 60000); //latency period for boss spawn
	}
	
	public final void stopMonsterRaid() {
		if (finished.compareAndSet(false, true))
			onMonsterRaidFinish();
		else
			log.error("Attempt to stop monster raid of raid location#" + mrl.getLocationId() + " for 2 times!");
	}
	
	private void onMonsterRaidFinish() {
		if (isBossKilled())
			log.info("Monster Raid finished with ID#" + mrl.getLocationId());
		else
			log.info("Monster Raid finished without killing boss ID#" + mrl.getLocationId());
		
		unregisterMonsterRaidBossListeners();
		despawnLocation();
		
		if (isBossKilled())
			broadcastUpdate(1402387);
	}	
	
	public MonsterRaidLocation getMonsterRaidLocation() {
		return mrl;
	}
	
	public int getMonsterRaidLocationId() {
		return mrl.getLocationId();
	}

	public boolean isBossKilled() {
		return bossKilled;
	}

	public void setBossKilled(boolean bossKilled) {
		this.bossKilled = bossKilled;
	}

	public Npc getBoss() {
		return boss;
	}

	public void setBoss(Npc boss) {
		this.boss = boss;
	}
	
	public Npc getFlag() {
		return flag;
	}
	
	public void setFlag(Npc flag) {
		this.flag = flag;
	}
	
	public Npc getVortex() {
		return vortex;
	}
	
	public void setVortex(Npc vortex) {
		this.vortex = vortex;
	}
	
	public boolean isStarted() {
		return started;
	}

	public boolean isFinished() {
		return finished.get();
	}
	
	public void initVisiualNpcs(Npc flagNpc, Npc vortexNpc) {
		if (flagNpc == null || vortexNpc == null) {
			log.error("No flag or vortex available for ID#" + mrl.getLocationId());
			return;
		}
		setVortex(vortexNpc);
		setFlag(flagNpc);
	}
	
	public void initBoss(Npc raidBoss) {
		if (raidBoss == null) {
			log.error("No Raid Boss available for ID#" + mrl.getLocationId());
			return;
		}
		setBoss(raidBoss);
		registerMonsterRaidBossListeners();
	}
	
	protected void registerMonsterRaidBossListeners() {
		AbstractAI ai = (AbstractAI) getBoss().getAi2();
		ai.addEventListener(monsterRaidBossDeathListener);
	}

	protected void unregisterMonsterRaidBossListeners() {
		AbstractAI ai = (AbstractAI) getBoss().getAi2();
		ai.removeEventListener(monsterRaidBossDeathListener);
	}
	
	protected void spawnLocation() {
		List<Npc> location = MonsterRaidService.getInstance().spawnLocation(mrl);
		initVisiualNpcs(location.get(0), location.get(1));
	}
	
	protected void spawnBoss() {
		Npc raidBoss = MonsterRaidService.getInstance().spawnBoss(mrl);
		initBoss(raidBoss);
	}

	protected void despawnLocation() {
		MonsterRaidService.getInstance().despawnLocation(this);
	}

	protected void broadcastUpdate(int msg) {
		MonsterRaidService.getInstance().broadcastUpdate(mrl.getLocationId(), msg);
	}
}
