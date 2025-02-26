package instance.abyss;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.zone.ZoneInstance;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author xTz, Gigi
 */
@InstanceID(300250000)
public class EsoterraceInstance extends GeneralInstanceHandler {

	public EsoterraceInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onInstanceCreate() {
		instance.setDoorState(367, true);
		if (Rnd.chance() < 21) {
			spawn(799580, 1034.11f, 985.01f, 327.35095f, (byte) 105);
			spawn(217649, 1033.67f, 978.08f, 327.35095f, (byte) 35);
		}
	}

	@Override
	public void onDie(Npc npc) {
		switch (npc.getObjectTemplate().getTemplateId()) {
			case 282295:
				instance.setDoorState(39, true);
				break;
			case 282291: // Surkana Feeder enables "hardmode"
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDF4Re_Drana_08());
				deleteAliveNpcs(217204);
				spawn(217205, 1315.43f, 1171.04f, 51.8054f, (byte) 66);
				break;
			case 217289:
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDF4Re_Drana_07());
				instance.setDoorState(122, true);
				break;
			case 217281:
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDF4Re_Drana_04());
				instance.setDoorState(70, true);
				break;
			case 217195:
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDF4Re_Drana_05());
				instance.setDoorState(45, true);
				instance.setDoorState(52, true);
				instance.setDoorState(67, true);
				spawn(701027, 751.513489f, 1136.021851f, 365.031158f, (byte) 60, 41);
				spawn(701027, 829.620789f, 1134.330078f, 365.031281f, (byte) 60, 77);
				break;
			case 217185:
				spawn(701023, 1264.862061f, 644.995178f, 296.831818f, (byte) 60, 112);
				instance.setDoorState(367, false);
				break;
			case 217204:
				instance.setDoorState(78, true); // collapse walls of Kexkra's chamber
				spawn(205437, 1309.390259f, 1163.644287f, 51.493992f, (byte) 13);
				spawn(701027, 1318.669800f, 1180.467651f, 52.879887f, (byte) 75, 727);
				break;
			case 217206:
				spawn(205437, 1309.390259f, 1163.644287f, 51.493992f, (byte) 13);
				spawn(701027, 1318.669800f, 1180.467651f, 52.879887f, (byte) 75, 727);
				spawn(701027, 1325.484497f, 1173.198486f, 52.879887f, (byte) 75, 726);
				break;
			case 217649:
				// keening sirokin treasure chest
				Npc keeningSirokin = getNpc(799580);
				spawn(701025, 1038.63f, 987.74f, 328.356f, (byte) 0, 725);
				PacketSendUtility.broadcastMessage(keeningSirokin, 342359);
				keeningSirokin.getController().delete();
				break;
			case 217282:
			case 217283:
			case 217284:
				if (instance.getNpcs(217282, 217283, 217284).stream().allMatch(Creature::isDead)) {
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDF4Re_Drana_03());
					instance.setDoorState(111, true);
				}
				break;
		}
	}

	@Override
	public boolean onReviveEvent(Player player) {
		PlayerReviveService.revive(player, 25, 25, false, 0);
		player.getGameStats().updateStatsAndSpeedVisually();
		PacketSendUtility.sendPacket(player, STR_REBIRTH_MASSAGE_ME());
		TeleportService.teleportTo(player, instance, 384.57535f, 535.4073f, 321.6642f, (byte) 17);
		return true;
	}

	@Override
	public void onEnterZone(Player player, ZoneInstance zone) {
		if (zone.getAreaTemplate().getZoneName() == ZoneName.get("DRANA_PRODUCTION_LAB_300250000")) {
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1400919));
		}
	}

}
