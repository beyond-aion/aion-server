package instance.pvparenas;

import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.network.aion.instanceinfo.DisciplineScoreWriter;
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

	@Override
	public void onInstanceCreate() {
		killBonus = 200;
		deathFine = -100;
		super.onInstanceCreate();
	}

	@Override
	protected void sendPacket() {
		instance.forEachPlayer(player -> PacketSendUtility.sendPacket(player, new SM_INSTANCE_SCORE(instance.getMapId(), new DisciplineScoreWriter(instanceReward, player.getObjectId()))));
	}
}
