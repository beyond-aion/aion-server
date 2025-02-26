package instance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.flyring.FlyRing;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.geometry.Point3D;
import com.aionemu.gameserver.model.summons.SummonMode;
import com.aionemu.gameserver.model.summons.UnsummonType;
import com.aionemu.gameserver.model.templates.flyring.FlyRingTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.services.summons.SummonsService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz, Skyra
 */
@InstanceID(300190000)
public class TalocsHollowInstance extends GeneralInstanceHandler {

	private final List<Integer> movies = new ArrayList<>();
	private final AtomicBoolean isQueenMosquaHome = new AtomicBoolean(true);

	public TalocsHollowInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onEnterInstance(Player player) {
		addItems(player);
	}

	private void addItems(Player player) {
		QuestState qs1 = player.getQuestStateList().getQuestState(10032);
		QuestState qs2 = player.getQuestStateList().getQuestState(20032);
		if ((qs1 != null && qs1.getStatus() == QuestStatus.START) || (qs2 != null && qs2.getStatus() == QuestStatus.START))
			return;
		addMissingItems(player, 164000099); // Taloc's Tears
		addMissingItems(player, player.getRace() == Race.ELYOS ? 160001286 : 160001287); // Taloc Fruit
	}

	private void addMissingItems(Player player, int itemId) {
		if (player.getInventory().getFirstItemByItemId(itemId) == null)
			ItemService.addItem(player, itemId, 1);
	}

	@Override
	protected boolean isRestrictedToInstance(Item item) {
		switch (item.getItemId()) {
			case 164000099: // Taloc's Tears
			case 164000137: // Shishir's Powerstone
			case 164000138: // Gellmar's Wardstone
			case 164000139: // Neith's Sleepstone
			case 185000088: // Shishir's Corrosive Fluid
			case 185000108: // Dorkin's Pocket Knife
				return true;
		}
		return super.isRestrictedToInstance(item);
	}

	@Override
	public void onAggro(Npc npc) {
		if (npc.getNpcId() == 215480 && isQueenMosquaHome.compareAndSet(true, false))
			instance.setDoorState(7, false);
	}

	@Override
	public void onBackHome(Npc npc) {
		if (npc.getNpcId() == 215480) { // queen mosqua
			isQueenMosquaHome.set(true);
			instance.setDoorState(7, true);
		}
	}

	@Override
	public void onDie(Npc npc) {
		switch (npc.getNpcId()) {
			case 215467: // kinquid
				instance.setDoorState(48, true);
				instance.setDoorState(49, true);
				break;
			case 215457: // ancient octanus
				deleteAliveNpcs(700633);
				break;
			case 215480: // queen mosqua
				instance.setDoorState(7, true);
				Npc insectEgg = getNpc(700738);
				if (insectEgg != null) {
					insectEgg.getController().delete();
					SpawnTemplate eggTemplate = insectEgg.getSpawn();
					spawn(700739, eggTemplate.getX(), eggTemplate.getY(), eggTemplate.getZ(), eggTemplate.getHeading(), 11);
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDELIM_EGG_BREAK());
					instance.forEachPlayer(player -> {
						Summon summon = player.getSummon();
						if (summon != null) {
							if (summon.getNpcId() == 799500 || summon.getNpcId() == 799501) {
								SummonsService.doMode(SummonMode.RELEASE, summon, UnsummonType.UNSPECIFIED);
								PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(false, 0, 0, 435, true));
							}
						}
					});
				}
				break;
			case 700739: // cracked huge insect egg
				SpawnTemplate crackedEggTemplate = npc.getSpawn();
				spawn(281817, crackedEggTemplate.getX(), crackedEggTemplate.getY(), crackedEggTemplate.getZ(), crackedEggTemplate.getHeading(), 9);
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDELIM_WIND_INFO());
				break;
			case 215488: // celestius
				Player player = npc.getAggroList().getMostPlayerDamage();
				if (player != null)
					PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(false, 0, 10021, 437, true));
				Npc contaminatedFragment = getNpc(700740);
				if (contaminatedFragment != null) {
					SpawnTemplate fragmentTemplate = contaminatedFragment.getSpawn();
					spawn(700741, fragmentTemplate.getX(), fragmentTemplate.getY(), fragmentTemplate.getZ(), fragmentTemplate.getHeading(), 92);
					contaminatedFragment.getController().delete();
				}
				spawn(799503, 548f, 811f, 1375f, (byte) 0);
				break;
		}
	}

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		switch (npc.getNpcId()) {
			case 700940:
				player.getLifeStats().increaseHp(SM_ATTACK_STATUS.TYPE.HP, 20000, npc);
				npc.getController().delete();
				break;
			case 700941:
				player.getLifeStats().increaseHp(SM_ATTACK_STATUS.TYPE.HP, 30000, npc);
				npc.getController().delete();
				break;
		}
	}

	private void sendMovie(Player player, int movie) {
		if (!movies.contains(movie)) {
			movies.add(movie);
			PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(false, 0, 0, movie, true));
		}
	}

	@Override
	public void onInstanceCreate() {
		instance.setDoorState(48, true);
		instance.setDoorState(7, true);
		spawnRings();
	}

	private void spawnRings() {
		FlyRing f1 = new FlyRing(new FlyRingTemplate("TALOCS_1", mapId, new Point3D(253.85039, 649.23535, 1171.8772),
			new Point3D(253.85039, 649.23535, 1177.8772), new Point3D(262.84872, 649.4091, 1171.8772), 8), instance.getInstanceId());
		f1.spawn();
		FlyRing f2 = new FlyRing(new FlyRingTemplate("TALOCS_2", mapId, new Point3D(592.32275, 844.056, 1295.0966),
			new Point3D(592.32275, 844.056, 1301.0966), new Point3D(595.2305, 835.5387, 1295.0966), 8), instance.getInstanceId());
		f2.spawn();
	}

	@Override
	public boolean onPassFlyingRing(Player player, String flyingRing) {
		if (flyingRing.equals("TALOCS_1")) {
			sendMovie(player, 463);
		} else if (flyingRing.equals("TALOCS_2")) {
			sendMovie(player, 464);
		}
		return false;
	}

	@Override
	public boolean onReviveEvent(Player player) {
		PlayerReviveService.revive(player, 25, 25, false, 0);
		player.getGameStats().updateStatsAndSpeedVisually();
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME());
		TeleportService.teleportTo(player, instance, 202.26694f, 226.0532f, 1098.236f, (byte) 30);
		return true;
	}

}
