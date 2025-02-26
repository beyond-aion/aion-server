package instance;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_MSG_IDCatacombs_BigOrb_Spawn;

import java.util.concurrent.atomic.AtomicInteger;

import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Gigi, nrg, oslo0322, xTz
 * @version TODO: Hard-/normal mode TODO: AI for each boss see http://raouooble.com/Beshmundir_Temple_Guide.html see
 *          http://gameguide.na.aiononline.com/aion/Beshmundir+Temple+Walkthrough%3A+Hard+Mode
 */
@InstanceID(300170000)
public class BeshmundirInstance extends GeneralInstanceHandler {

	private final AtomicInteger macunbello = new AtomicInteger();
	private final AtomicInteger kills = new AtomicInteger();
	private Race instanceRace;

	public BeshmundirInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onEnterInstance(final Player player) {
		if (instanceRace == null)
			instanceRace = player.getRace();
	}

	@Override
	public float getExpMultiplier() {
		return 3f;
	}

	@Override
	public void onDie(Npc npc) {
		switch (npc.getNpcId()) {
			case 216583: // Brutal Soulwatcher (Difficult)
			case 216587: // Brutal Soulwatcher (Normal)
				spawn(799518, 936.0029f, 441.51712f, 220.5029f, (byte) 28);
				break;
			case 216584: // Brutal Soulwatcher (Difficult)
			case 216588: // Brutal Soulwatcher (Normal)
				spawn(799519, 791.0439f, 439.79608f, 220.3506f, (byte) 28);
				break;
			case 216585: // Brutal Soulwatcher (Difficult)
			case 216589: // Brutal Soulwatcher (Normal)
				spawn(799520, 820.70624f, 278.828f, 220.19385f, (byte) 55);
				break;
			case 216586: // Temadaro (Difficult)
			case 216590: // Temadaro (Normal)
				int killedCount = macunbello.getAndSet(0);
				if (killedCount < 12) {
					Npc npcMacunbello = (Npc) spawn(216735, 981.015015f, 134.373001f, 241.755005f, (byte) 30); // strongest macunbello
					SkillEngine.getInstance().applyEffectDirectly(19046, npcMacunbello, npcMacunbello);
				} else if (killedCount < 14) {
					Npc npcMacunbello = (Npc) spawn(216734, 981.015015f, 134.373001f, 241.755005f, (byte) 30); // 2nd strongest macunbello
					SkillEngine.getInstance().applyEffectDirectly(19047, npcMacunbello, npcMacunbello);
				} else if (killedCount < 21) {
					Npc npcMacunbello = (Npc) spawn(216737, 981.015015f, 134.373001f, 241.755005f, (byte) 30); // 2nd weakest macunbello
					SkillEngine.getInstance().applyEffectDirectly(19048, npcMacunbello, npcMacunbello);
				} else {
					spawn(216245, 981.015015f, 134.373001f, 241.755005f, (byte) 30); // weakest macunbello
				}
				sendPacket(new SM_QUEST_ACTION(0, 0));
				instance.setDoorState(467, true);
				break;
			case 799342:
				sendPacket(new SM_PLAY_MOVIE(false, 0, 0, 447, true));
				break;
			case 216157:
			case 216238:
				instance.setDoorState(470, true);
				spawn(216159, 1357.0598f, 388.6637f, 249.26372f, (byte) 90);
				break;
			case 216165:
			case 216246:
				instance.setDoorState(473, true);
				break;
			case 216739:
			case 216740:
				int killedTotal = kills.incrementAndGet();
				if (killedTotal < 10) {
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDCatacombs_NmdSpecter_Spawn());
				} else if (killedTotal == 10) {
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDCatacombs_NmdSpecter_Start());
					spawn(216158, 1356.5719f, 147.76418f, 246.27373f, (byte) 91);
				}
				break;
			case 216158:
			case 216239:
				instance.setDoorState(471, true);
				break;
			case 700608:
				if (instanceRace == Race.ASMODIANS) {
					spawn(799342, 1357.1f, 76.044f, 248.595f, (byte) 0);
				}
				break;
			case 216263:
			case 216182:
				// this is a safety Mechanism
				// super boss
				spawn(216183, 558.306f, 1369.02f, 224.795f, (byte) 70);
				// gate
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDCatacombs_BigOrb_Spawn());
				spawn(730275, 1611.1266f, 1604.6935f, 310.39972f, (byte) 17, 426);
				break;
			case 216250: // Dorakiki the Bold
			case 216169:
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDCatacombs_NmdShulack_Rufukin());
				spawn(216527, 1161.859985f, 1213.859985f, 284.057007f, (byte) 110); // Lupukin: cat trader
				break;
			case 216206:
			case 216207:
			case 216208:
			case 216209:
			case 216210:
			case 216211:
			case 216212:
			case 216213:
				switch (macunbello.incrementAndGet()) {
					case 12 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDCatacombs_NmdLich_weakness1());
					case 14 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDCatacombs_NmdLich_weakness2());
					case 21 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDCatacombs_NmdLich_weakness3());
				}
				break;
		}
	}

	private void sendPacket(AionServerPacket packet) {
		PacketSendUtility.broadcastToMap(instance, packet);
	}

	@Override
	public void onPlayMovieEnd(Player player, int movieId) {
		if (movieId == 443)
			PacketSendUtility.sendPacket(player, STR_MSG_IDCatacombs_BigOrb_Spawn());
	}

	@Override
	public void onInstanceCreate() {
		instance.setDoorState(535, true);
	}
}
