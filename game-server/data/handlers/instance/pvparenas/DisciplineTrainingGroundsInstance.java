package instance.pvparenas;

import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.InstanceScoreType;
import com.aionemu.gameserver.network.aion.instanceinfo.ArenaScoreWriter;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INSTANCE_SCORE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz
 */
@InstanceID(300430000)
public class DisciplineTrainingGroundsInstance extends PvPArenaInstance {

	public DisciplineTrainingGroundsInstance(WorldMapInstance instance) {
		super(instance);
	}

	protected void setScoreCaps() {
		instanceScore.setLowerScoreCap(10000);
		instanceScore.setUpperScoreCap(50000);
		instanceScore.setMaxScoreGap(1500);
	}

	@Override
	public void onInstanceCreate() {
		pointsPerKill = 200;
		pointsPerDeath = -100;
		super.onInstanceCreate();
	}

	@Override
	protected int getBoostMoraleEffectDuration(int rank) {
		return switch (rank) {
			case 0 -> 14000;
			case 1 -> 16000;
			default -> 15000;
		};
	}

	@Override
	protected void sendPacket(Player player, InstanceScoreType scoreType) {
		instance.forEachPlayer(
			p -> PacketSendUtility.sendPacket(p, new SM_INSTANCE_SCORE(instance.getMapId(), new ArenaScoreWriter(instanceScore, p.getObjectId(), true))));
	}
}
