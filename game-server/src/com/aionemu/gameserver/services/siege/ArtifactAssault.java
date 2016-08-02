package com.aionemu.gameserver.services.siege;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.siege.SiegeModType;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Luzien
 * @modified Whoop TODO: onAssaultFail(), if BalaurAssaulter fails to capture
 */
public class ArtifactAssault extends Assault<ArtifactSiege> {

	private boolean spawned = false;

	public ArtifactAssault(ArtifactSiege siege) {
		super(siege);
	}

	@Override
	public void scheduleAssault(int delay) {
		spawnTask = ThreadPoolManager.getInstance().schedule(() -> spawnAttacker(), delay * 60 * 1000); // Assault between 1h and 10h
	}

	@Override
	public void onAssaultFinish(boolean captured) {
		if (!spawned)
			return;

		if (captured)
			siegeLocation.doOnAllPlayers(p -> PacketSendUtility.sendPacket(p, SM_SYSTEM_MESSAGE.STR_ABYSS_DRAGON_BOSS_KILLED()));
	}

	private void spawnAttacker() {
		if (spawned)
			return;

		spawned = true;
		float x1 = (float) (boss.getX() + Math.cos(Math.PI * Rnd.get()));
		float y1 = (float) (boss.getY() + Math.sin(Math.PI * Rnd.get()));

		SpawnTemplate spawnTemplate = SpawnEngine.addNewSiegeSpawn(worldId, getAssaulterIdByBossLvl(), locationId, SiegeRace.BALAUR,
			SiegeModType.ASSAULT, x1, y1, boss.getZ(), (byte) 0);
		Npc assaulter = (Npc) SpawnEngine.spawnObject(spawnTemplate, 1);
		assaulter.getAggroList().addHate(boss, 1000);
	}

	private int getAssaulterIdByBossLvl() {
		switch (boss.getLevel()) {
			case 40:
				return 276719;
			case 50:
				return 277016;
			default:
				return 251160;
		}
	}
}
