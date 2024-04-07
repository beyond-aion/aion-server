package ai.instance.esoterrace;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author xTz
 */
@AIName("wardensurama")
public class WardenSuramaAI extends AggressiveNpcAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(50, 25, 5);

	public WardenSuramaAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		hpPhases.tryEnterNextPhase(this);
	}

	@Override
	public void handleHpPhase(int phaseHpPercent) {
		spawnGeysers();
	}

	private void spawnGeysers() {
		SkillEngine.getInstance().getSkill(getOwner(), 19332, 50, getOwner()).useNoAnimationSkill();
		spawn(282171, 1317.097656f, 1145.419556f, 53.203529f, (byte) 0, 595);
		spawn(282172, 1343.426147f, 1170.675293f, 53.203529f, (byte) 0, 596);
		spawn(282173, 1316.953979f, 1196.861328f, 53.203529f, (byte) 0, 598);
		spawn(282174, 1290.778442f, 1170.730957f, 53.203529f, (byte) 0, 597);
		spawn(282425, 1305.310059f, 1159.337769f, 53.203529f, (byte) 0, 721);
		spawn(282426, 1328.446289f, 1159.062500f, 53.203529f, (byte) 0, 718);
		spawn(282427, 1328.613770f, 1182.369873f, 53.203529f, (byte) 0, 722);
		spawn(282428, 1305.083130f, 1182.424927f, 53.203529f, (byte) 0, 719);

		PacketSendUtility.broadcastPacket(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_IDF4Re_Drana_10());
		PacketSendUtility.broadcastPacket(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_IDF4Re_Drana_09());
		ThreadPoolManager.getInstance().schedule(this::despawnGeysers, 13000);
	}

	private void despawnGeysers() {
		getPosition().getWorldMapInstance().getNpcs(282171, 282172, 282173, 282174, 282425, 282426, 282427, 282428).forEach(npc -> npc.getController().delete());
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		hpPhases.reset();
	}
}
