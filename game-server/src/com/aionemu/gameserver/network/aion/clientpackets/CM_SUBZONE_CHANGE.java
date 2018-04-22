package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.zone.ZoneClassName;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.zone.ZoneInstance;

/**
 * @author Rolandas
 */
public class CM_SUBZONE_CHANGE extends AionClientPacket {

	private byte unk;

	public CM_SUBZONE_CHANGE(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		// Always 1, maybe for neutral zones 0 ?
		unk = readC();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		player.revalidateZones();
		if (player.hasAccess(AdminConfig.ZONE_INFO)) {
			int foundZones = 0;
			for (ZoneInstance zone : player.findZones()) {
				if (zone.getZoneTemplate().getZoneType() == ZoneClassName.DUMMY || zone.getZoneTemplate().getZoneType() == ZoneClassName.WEATHER)
					continue;
				foundZones++;
				PacketSendUtility.sendMessage(player, "Passed zone: unk=" + unk + "; " + zone.getZoneTemplate().getZoneType() + " "
					+ zone.getAreaTemplate().getZoneName().name());
			}
			if (foundZones == 0) {
				PacketSendUtility.sendMessage(player, "Passed unknown zone, unk=" + unk);
				return;
			}
		}
	}

}
