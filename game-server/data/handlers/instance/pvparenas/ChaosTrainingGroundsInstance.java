package instance.pvparenas;

import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.actions.NpcActions;
import com.aionemu.gameserver.model.flyring.FlyRing;
import com.aionemu.gameserver.model.gameobjects.Gatherable;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.playerreward.PvPArenaPlayerReward;
import com.aionemu.gameserver.model.templates.flyring.FlyRingTemplate;
import com.aionemu.gameserver.model.utils3d.Point3D;
import com.aionemu.gameserver.network.aion.instanceinfo.ChaosScoreInfo;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INSTANCE_SCORE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz
 */
@InstanceID(300420000)
public class ChaosTrainingGroundsInstance extends PvPArenaInstance {

	public ChaosTrainingGroundsInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onInstanceCreate() {
		killBonus = 1000;
		deathFine = -125;
		super.onInstanceCreate();
	}

	@Override
	protected void sendPacket() {
		instance.forEachPlayer(player -> PacketSendUtility.sendPacket(player, new SM_INSTANCE_SCORE(instance.getMapId(), new ChaosScoreInfo(instanceReward, player.getObjectId()))));
	}

	@Override
	public void onGather(Player player, Gatherable gatherable) {
		if (!instanceReward.isStartProgress()) {
			return;
		}
		getPlayerReward(player).addPoints(1250);
		sendPacket();
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_GET_SCORE(gatherable.getObjectTemplate().getL10n(), 1250));
	}

	@Override
	protected void spawnRings() {
		spawnFlyRing("PVP_ARENA_1", new Point3D(674.66974, 1792.8499, 149.77501), new Point3D(674.66974, 1792.8499, 155.77501),
			new Point3D(678.83636, 1788.5325, 149.77501));
		spawnFlyRing("PVP_ARENA_2", new Point3D(688.30615, 1769.7937, 149.88556), new Point3D(688.30615, 1769.7937, 155.88556),
			new Point3D(689.42096, 1763.8982, 149.88556));
		spawnFlyRing("PVP_ARENA_3", new Point3D(664.2252, 1761.671, 170.95732), new Point3D(664.2252, 1761.671, 176.95732),
			new Point3D(669.2843, 1764.8967, 170.95732));
		spawnFlyRing("PVP_ARENA_VOID_1", new Point3D(690.28625, 1753.8561, 192.07726), new Point3D(690.28625, 1753.8561, 198.07726),
			new Point3D(689.4365, 1747.9165, 192.07726));
		spawnFlyRing("PVP_ARENA_VOID_2", new Point3D(690.1935, 1797.0029, 203.79236), new Point3D(690.1935, 1797.0029, 209.79236),
			new Point3D(692.8295, 1802.3928, 203.79236));
		spawnFlyRing("PVP_ARENA_VOID_3", new Point3D(659.2784, 1766.0273, 207.25465), new Point3D(659.2784, 1766.0273, 213.25465),
			new Point3D(665.2619, 1766.4718, 207.25465));
	}

	private void spawnFlyRing(String name, Point3D center, Point3D p1, Point3D p2) {
		new FlyRing(new FlyRingTemplate(name, mapId, center, p1, p2, 6), instance.getInstanceId()).spawn();
	}

	@Override
	public boolean onPassFlyingRing(Player player, String flyingRing) {
		PvPArenaPlayerReward playerReward = getPlayerReward(player);
		if (playerReward == null || !instanceReward.isStartProgress()) {
			return false;
		}
		Npc npc;
		if (flyingRing.equals("PVP_ARENA_1")) {
			npc = getNpc(674.841f, 1793.065f, 150.964f);
			if (npc != null && npc.isSpawned()) {
				NpcActions.delete(npc, true);
				sendSystemMsg(player, npc, 250);
				sendPacket();
			}
		} else if (flyingRing.equals("PVP_ARENA_2")) {
			npc = getNpc(688.410f, 1769.611f, 150.964f);
			if (npc != null && npc.isSpawned()) {
				NpcActions.delete(npc, true);
				playerReward.addPoints(250);
				sendSystemMsg(player, npc, 250);
				sendPacket();
			}
		} else if (flyingRing.equals("PVP_ARENA_3")) {
			npc = getNpc(664.160f, 1761.933f, 171.504f);
			if (npc != null && npc.isSpawned()) {
				NpcActions.delete(npc, true);
				playerReward.addPoints(250);
				sendSystemMsg(player, npc, 250);
				sendPacket();
			}
		} else if (flyingRing.equals("PVP_ARENA_VOID_1")) {
			npc = getNpc(693.061f, 1752.479f, 186.750f);
			if (npc != null && npc.isSpawned()) {
				useSkill(npc, player, 20059, 1);
				NpcActions.delete(npc, true);
			}
		} else if (flyingRing.equals("PVP_ARENA_VOID_2")) {
			npc = getNpc(688.061f, 1798.229f, 198.500f);
			if (npc != null && npc.isSpawned()) {
				useSkill(npc, player, 20059, 1);
				NpcActions.delete(npc, true);
			}
		} else if (flyingRing.equals("PVP_ARENA_VOID_3")) {
			npc = getNpc(659.311f, 1768.979f, 201.500f);
			if (npc != null && npc.isSpawned()) {
				useSkill(npc, player, 20059, 1);
				NpcActions.delete(npc, true);
			}
		}
		return false;
	}

}
