package instance;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_MSG_IDCatacombs_BigOrb_Spawn;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.StaticDoor;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 * @author Gigi, nrg, oslo0322, xTz
 * @version TODO: Hard-/normal mode TODO: AI for each boss see
 *         http://raouooble.com/Beshmundir_Temple_Guide.html see
 *         http://gameguide.na.aiononline.com/aion/Beshmundir+Temple+Walkthrough%3A+Hard+Mode
 */
@InstanceID(300170000)
public class BeshmundirInstance extends GeneralInstanceHandler {

	private AtomicInteger macunbello = new AtomicInteger();
	private AtomicInteger kills = new AtomicInteger();
	Npc npcMacunbello = null;
	private Map<Integer, StaticDoor> doors;
	private Race instanceRace;

	@Override
	public void onEnterInstance(final Player player) {
		if (instanceRace == null) {
			instanceRace = player.getRace();
		}
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
					npcMacunbello = (Npc) spawn(216735, 981.015015f, 134.373001f, 241.755005f, (byte) 30); // strongest macunbello
					SkillEngine.getInstance().applyEffectDirectly(19046, npcMacunbello, npcMacunbello, 0);
				} else if (killedCount < 14) {
					npcMacunbello = (Npc) spawn(216734, 981.015015f, 134.373001f, 241.755005f, (byte) 30); // 2nd strongest macunbello
					SkillEngine.getInstance().applyEffectDirectly(19047, npcMacunbello, npcMacunbello, 0);
				} else if (killedCount < 21) {
					npcMacunbello = (Npc) spawn(216737, 981.015015f, 134.373001f, 241.755005f, (byte) 30); // 2nd weakest macunbello
					SkillEngine.getInstance().applyEffectDirectly(19048, npcMacunbello, npcMacunbello, 0);
				} else {
					spawn(216245, 981.015015f, 134.373001f, 241.755005f, (byte) 30); // weakest macunbello
				}
				sendPacket(new SM_QUEST_ACTION(0, 0));
				openDoor(467);
				break;
			case 799342:
				sendPacket(new SM_PLAY_MOVIE(0, 447));
				break;
			case 216157:
			case 216238:
				openDoor(470);
				spawn(216159, 1357.0598f, 388.6637f, 249.26372f, (byte) 90);
				break;
			case 216165:
			case 216246:
				openDoor(473);
				break;
			case 216739:
			case 216740:
				int killedTotal = kills.incrementAndGet();
				if (killedTotal < 10) {
					sendMsg(1400465);
				} else if (killedTotal == 10) {
					sendMsg(1400470);
					spawn(216158, 1356.5719f, 147.76418f, 246.27373f, (byte) 91);
				}
				break;
			case 216158:
			case 216239:
				openDoor(471);
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
				sendMsg(1400480);
				spawn(730275, 1611.1266f, 1604.6935f, 311.00503f, (byte) 17);
				break;
			case 216250: // Dorakiki the Bold
			case 216169:
				sendMsg(1400471);
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
				int killedMacunbello = macunbello.incrementAndGet();
				switch (killedMacunbello) {
					case 12:
						sendMsg(1400466);
						break;
					case 14:
						sendMsg(1400467);
						break;
					case 21:
						sendMsg(1400468);
						break;
				}
				break;
		}
	}

	private void sendPacket(final AionServerPacket packet) {
		instance.doOnAllPlayers(new Visitor<Player>() {

			@Override
			public void visit(Player player) {
				PacketSendUtility.sendPacket(player, packet);
			}

		});
	}

	@Override
	public void onPlayMovieEnd(Player player, int movieId) {
		switch (movieId) {
			case 443:
				PacketSendUtility.sendPacket(player, STR_MSG_IDCatacombs_BigOrb_Spawn);
				break;
		}
	}

	@Override
	public void onInstanceCreate(WorldMapInstance instance) {
		super.onInstanceCreate(instance);
		doors = instance.getDoors();
		doors.get(535).setOpen(true);
	}

	private void openDoor(int doorId) {
		StaticDoor door = doors.get(doorId);
		if (door != null)
			door.setOpen(true);
	}

	@Override
	public void onInstanceDestroy() {
		doors.clear();
	}

	@Override
	public boolean onDie(final Player player, Creature lastAttacker) {
		PacketSendUtility.broadcastPacket(player,
			new SM_EMOTION(player, EmotionType.DIE, 0, player.equals(lastAttacker) ? 0 : lastAttacker.getObjectId()), true);

		PacketSendUtility.sendPacket(player, new SM_DIE(player.haveSelfRezEffect(), player.haveSelfRezItem(), 0, 8));
		return true;
	}
}
