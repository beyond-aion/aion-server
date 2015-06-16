package instance;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz, Luzien
 */
@InstanceID(300160000)
public class LowerUdasTempleInstance extends GeneralInstanceHandler {
	@Override
	public void onInstanceCreate(WorldMapInstance instance) {
		super.onInstanceCreate(instance);
		int rnd = Rnd.get(1, 100);
		if (rnd > 80) { //spawn named drop chests, 20% both, 30% epic, 50% fabled chest
			spawn(216150, 455.984f, 1192.506f, 190.221f, (byte) 116);
			spawn(216645, 435.664f, 1182.577f, 190.221f, (byte) 116);
		}
		else if (rnd > 50) {
			spawn(216150, 455.984f, 1192.506f, 190.221f, (byte) 116);
		}
		else {
			spawn(216645, 435.664f, 1182.577f, 190.221f, (byte) 116);
		}
	}
	
	@Override
	public boolean onDie(final Player player, Creature lastAttacker) {
		PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.DIE, 0, player.equals(lastAttacker) ? 0
				: lastAttacker.getObjectId()), true);

		PacketSendUtility.sendPacket(player, new SM_DIE(player.haveSelfRezEffect(), player.haveSelfRezItem(), 0, 8));
		return true;
	}
}
