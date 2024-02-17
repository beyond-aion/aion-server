package instance.pvparenas;

import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.flyring.FlyRing;
import com.aionemu.gameserver.model.gameobjects.Gatherable;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.geometry.Point3D;
import com.aionemu.gameserver.model.instance.InstanceScoreType;
import com.aionemu.gameserver.model.instance.playerreward.PvPArenaPlayerReward;
import com.aionemu.gameserver.model.templates.flyring.FlyRingTemplate;
import com.aionemu.gameserver.network.aion.instanceinfo.ArenaScoreWriter;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INSTANCE_SCORE;
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
		pointsPerKill = 1000;
		pointsPerDeath = -125;
		super.onInstanceCreate();
	}

	@Override
	protected int getBoostMoraleEffectDuration(int rank) {
		return switch (rank) {
			case 0, 1 -> 14000;
			case 4, 5 -> 16000;
			case 6, 7 -> 17000;
			default -> 15000;
		};
	}

	@Override
	protected float getRunnerUpScoreMod(int victimRank) {
		return switch (victimRank) {
			case 0 -> 3f;
			case 1 -> 2f;
			default -> 1f;
		};
	}

	@Override
	protected void sendPacket(Player player, InstanceScoreType scoreType) {
		instance.forEachPlayer(
			p -> PacketSendUtility.sendPacket(p, new SM_INSTANCE_SCORE(instance.getMapId(), new ArenaScoreWriter(instanceScore, p.getObjectId(), false))));
	}

	@Override
	public void onGather(Player player, Gatherable gatherable) {
		if (!instanceScore.isStartProgress())
			return;
		updatePoints(getPlayerSpecificReward(player), player, gatherable, 1250);
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
		PvPArenaPlayerReward playerReward = getPlayerSpecificReward(player);
		if (playerReward == null || !instanceScore.isStartProgress()) {
			return false;
		}
		Npc npc = switch (flyingRing) {
			case "PVP_ARENA_1" -> getNpc(674.841f, 1793.065f, 150.964f); // Flame Loop
			case "PVP_ARENA_2" -> getNpc(688.410f, 1769.611f, 150.964f); // Flame Loop
			case "PVP_ARENA_3" -> getNpc(664.160f, 1761.933f, 171.504f); // Flame Loop
			case "PVP_ARENA_VOID_1" -> getNpc(693.061f, 1752.479f, 186.750f); // Aether Vortex
			case "PVP_ARENA_VOID_2" -> getNpc(688.061f, 1798.229f, 198.500f); // Aether Vortex
			case "PVP_ARENA_VOID_3" -> getNpc(659.311f, 1768.979f, 201.500f); // Aether Vortex
			default -> null;
		};
		if (npc != null && npc.isSpawned()) {
			if (npc.getNpcId() == 701184 || npc.getNpcId() == 701198 || npc.getNpcId() == 701212) { // Flame Loop
				npc.getController().deleteAndScheduleRespawn();
				updatePoints(playerReward, player, npc, 250);
			} else if (npc.getNpcId() == 701183 || npc.getNpcId() == 701197 || npc.getNpcId() == 701211) { // Aether Vortex
				useSkill(npc, player, 20059, 1);
				npc.getController().deleteAndScheduleRespawn();
			}
		}
		return false;
	}

}
