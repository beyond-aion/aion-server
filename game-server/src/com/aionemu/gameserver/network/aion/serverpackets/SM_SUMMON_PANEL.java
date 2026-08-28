package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.utils.stats.CalculationType;

/**
 * @author ATracer, xTz
 */
public class SM_SUMMON_PANEL extends AionServerPacket {

	private final Summon summon;

	public SM_SUMMON_PANEL(Summon summon) {
		this.summon = summon;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(summon.getObjectId());
		writeH(summon.getLevel());
		writeD(0);// unk
		writeD(0);// unk
		writeD(summon.getLifeStats().getCurrentHp());
		writeD(summon.getGameStats().getMaxHp().getCurrent());
		writeD(summon.getGameStats().getMainHandPAttack(CalculationType.DISPLAY).getCurrent());
		writeD(summon.getGameStats().getPDef().getCurrent());
		writeD(summon.getGameStats().getMDef().getCurrent());
		writeH(0);// unk
		writeD(summon.getLiveTime()); // life time
	}

}
