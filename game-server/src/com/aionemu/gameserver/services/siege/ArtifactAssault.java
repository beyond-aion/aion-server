package com.aionemu.gameserver.services.siege;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.siege.SiegeModType;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * TODO: onAssaultFail(), if BalaurAssaulter fails to capture
 * 
 * @author Luzien, Whoop
 */
public class ArtifactAssault extends Assault<ArtifactSiege> {

	public ArtifactAssault(ArtifactSiege siege) {
		super(siege);
	}

	@Override
	public void handleAssault() {
		spawnAssaulter();
	}

	@Override
	public void onAssaultFinish(boolean captured) {
		if (captured)
			siegeLocation.forEachPlayer(p -> PacketSendUtility.sendPacket(p, SM_SYSTEM_MESSAGE.STR_ABYSS_DRAGON_BOSS_KILLED(getBossNpcL10n())));
	}

	private void spawnAssaulter() {
		double angleRadians = Math.toRadians(Rnd.nextFloat(180f));
		float x1 = (float) (boss.getX() + Math.cos(angleRadians));
		float y1 = (float) (boss.getY() + Math.sin(angleRadians));

		SpawnTemplate spawnTemplate = SpawnEngine.newSiegeSpawn(worldId, getAssaulterIdByBossLvl(), locationId, SiegeRace.BALAUR, SiegeModType.ASSAULT,
			x1, y1, boss.getZ(), (byte) 0);
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
				return 251463;
		}
	}
}
