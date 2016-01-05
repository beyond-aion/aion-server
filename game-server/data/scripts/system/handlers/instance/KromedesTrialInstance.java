package instance;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME;

import java.util.List;

import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.storage.Storage;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMap;
import com.aionemu.gameserver.world.zone.ZoneInstance;
import com.aionemu.gameserver.world.zone.ZoneName;

import javolution.util.FastTable;

/**
 * @author xTz, Gigi
 */
@InstanceID(300230000)
public class KromedesTrialInstance extends GeneralInstanceHandler {

	private int skillId;
	private List<Integer> movies = new FastTable<>();
	private boolean isSpawned = false;
	private boolean isInDungeon = false; //Workaround
	
	@Override
	public void onDie(Npc npc) {
		if (npc.getNpcId() == 216968)
			isInDungeon = true;
		
		switch (npc.getNpcId()) {
			case 282093: //mana relic
				Npc kaliga = instance.getNpc(217006);
				if (kaliga != null && !kaliga.getLifeStats().isAlreadyDead()) {
					kaliga.getEffectController().removeEffect(19248);
					respawn(282093, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), npc.getInstanceId(), npc.getWorldId(), npc.getSpawn().getStaticId());
				}
				npc.getController().onDelete();
				break;
			case 282095: //strength relic
				Npc kaliga2 = instance.getNpc(217006);
				if (kaliga2 != null && !kaliga2.getLifeStats().isAlreadyDead()) {
					kaliga2.getEffectController().removeEffect(19247);
					respawn(282095, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), npc.getInstanceId(), npc.getWorldId(), npc.getSpawn().getStaticId());
				}
				npc.getController().onDelete();
				break;
				default:
					break;
		}
	}

	private void respawn(int npcId, float x, float y, float z, byte heading, int instanceId, int worldId, int staticId) {
		ThreadPoolManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				Npc kaliga = instance.getNpc(217006);
				if (kaliga != null && !kaliga.getLifeStats().isAlreadyDead()) {
					SpawnTemplate temp = SpawnEngine.addNewSingleTimeSpawn(worldId, npcId, x, y, z, heading);
					temp.setStaticId(staticId);
					SpawnEngine.spawnObject(temp, instanceId);
				}
			}
		}, 10000);
	}
	@Override
	public void onEnterInstance(Player player) {
		if (movies.contains(453)) {
			return;
		}
		skillId = player.getRace() == Race.ASMODIANS ? 19270 : 19220;
		sendMovie(player, 453);
	}

	@Override
	public void onLeaveInstance(Player player) {
		player.getEffectController().removeEffect(skillId);
		removeItems(player);
	}

	@Override
	public void onPlayerLogOut(Player player) {
		player.setTransformed(false);
	}

	@Override
	public void onPlayMovieEnd(Player player, int movieId) {
		Storage storage = player.getInventory();
		switch (movieId) {
			case 453:
				SkillEngine.getInstance().applyEffectDirectly(skillId, player, player, 0);
				break;
			case 454:
				Npc npc1 = getNpc(730308);
				if (npc1 != null && MathUtil.isIn3dRange(player, npc1, 20)) {
					storage.decreaseByItemId(185000109, storage.getItemCountByItemId(185000109));
					TeleportService2.teleportTo(player, mapId, 687.56116f, 681.68225f, 200.28648f, (byte) 30);
				}
				break;
		}
	}

	@Override
	public void onEnterZone(Player player, ZoneInstance zone) {
		if (zone.getAreaTemplate().getZoneName() == ZoneName.get("MANOR_ENTRANCE_300230000")) {
			sendMovie(player, 462);
		} else if (zone.getAreaTemplate().getZoneName() == ZoneName.get("KALIGA_TREASURY_300230000")) {
			{
				if (!isSpawned) {
					isSpawned = true;
					Npc npc1 = getNpc(217002);
					Npc npc2 = getNpc(217000);
					Npc npc3 = getNpc(216982);
					if (isDead(npc1) && isDead(npc2) && isDead(npc3)) {
						spawn(217005, 669.214f, 774.387f, 216.88f, (byte) 60);
						spawn(217001, 663.8805f, 779.1967f, 216.26213f, (byte) 60);
						spawn(217003, 663.0468f, 774.6116f, 216.26215f, (byte) 60);
						spawn(217004, 663.0468f, 770.03815f, 216.26212f, (byte) 60);
					} else {
						spawn(217006, 669.214f, 774.387f, 216.88f, (byte) 60);
					}
				}
			}
		}
	}

	private boolean isDead(Npc npc) {
		return (npc == null || npc.getLifeStats().isAlreadyDead());
	}

	private void sendMovie(Player player, int movie) {
		if (!movies.contains(movie)) {
			movies.add(movie);
			PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(0, movie));
		}
	}

	@Override
	public void onInstanceDestroy() {
		movies.clear();
	}

	@Override
	public boolean onDie(final Player player, Creature lastAttacker) {
		PacketSendUtility.broadcastPacket(player,
			new SM_EMOTION(player, EmotionType.DIE, 0, player.equals(lastAttacker) ? 0 : lastAttacker.getObjectId()), true);

		PacketSendUtility.sendPacket(player, new SM_DIE(player.haveSelfRezEffect(), player.haveSelfRezItem(), 0, 8));
		return true;
	}

	@Override
	public boolean onReviveEvent(Player player) {
		WorldMap map = World.getInstance().getWorldMap(player.getWorldId());
		if (map == null) {
			PlayerReviveService.bindRevive(player);
			return true;
		}
		PlayerReviveService.revive(player, 25, 25, true, 0);
		PacketSendUtility.sendPacket(player, STR_REBIRTH_MASSAGE_ME);
		player.getGameStats().updateStatsAndSpeedVisually();
		PacketSendUtility.sendPacket(player, new SM_PLAYER_INFO(player, false));
		PacketSendUtility.sendPacket(player, new SM_MOTION(player.getObjectId(), player.getMotions().getActiveMotions()));
		if (!isInDungeon)
			TeleportService2.teleportTo(player, player.getWorldId(), 248, 244, 189);
		else
			TeleportService2.teleportTo(player, player.getWorldId(), 686, 676, 201);
		SkillEngine.getInstance().applyEffectDirectly(skillId, player, player, 0);
		player.unsetResPosState();
		return true;
	}
	
	//Removes all instance items
	private void removeItems(Player player) {
		// Temple Vault Door Key		185000098
		// Dungeon Grate Key				185000099
		// Dungeon Door Key					185000100
		// Secret Safe Key					185000101
		// Relic Key								185000109
		List<Integer> items = FastTable.of(185000098, 185000099, 185000100, 185000101, 185000109);
		Storage storage = player.getInventory();
		for (int item : items)
			storage.decreaseByItemId(item, storage.getItemCountByItemId(item));
	}
}
