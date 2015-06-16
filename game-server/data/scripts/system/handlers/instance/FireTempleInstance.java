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
 * @author Gigi
 */
@InstanceID(320100000)
public class FireTempleInstance extends GeneralInstanceHandler {
	
	@Override
	public void onInstanceCreate(WorldMapInstance instance) {
		super.onInstanceCreate(instance);
		// Random spawns of bosses
		if (Rnd.get(1, 100) > 25) { // Blue Crystal Molgat
			spawn(212839, 127.1218f, 176.1912f, 99.67548f, (byte) 15);
		}
		else { // elite mob spawns
			spawn(212790, 127.1218f, 176.1912f, 99.67548f, (byte) 15);
		}

		if (Rnd.get(1, 100) > 25) { // Black Smoke Asparn
			spawn(212842, 322.3193f, 431.2696f, 134.5296f, (byte) 80);
		}
		else { // elite mob spawns
			spawn(212799, 322.3193f, 431.2696f, 134.5296f, (byte) 80);
		}

		if (Rnd.get(1, 100) > 25) { // Lava Gatneri
			spawn(212840, 153.0038f, 299.7786f, 123.0186f, (byte) 30);
		}
		else { // elite mob spawns
			spawn(212794, 153.0038f, 299.7786f, 123.0186f, (byte) 30);
		}

		if (Rnd.get(1, 100) > 25) { // Tough Sipus
			spawn(212843, 296.6911f, 201.9092f, 119.3652f, (byte) 30);
		}
		else { // elite mob spawns
			spawn(212803, 296.6911f, 201.9092f, 119.3652f, (byte) 15);
		}

		if (Rnd.get(1, 100) > 25) { // Flame Branch Flavi
			spawn(212841, 350.9276f, 351.7389f, 146.8498f, (byte) 45);
		}
		else { // elite mob spawns
			spawn(212799, 350.9276f, 351.7389f, 146.8498f, (byte) 45);
		}

		if (Rnd.get(1, 100) > 25) { // Broken Wing Kutisen
			spawn(212845, 298.7095f, 89.42245f, 128.7143f, (byte) 15);
		}
		else { // elite mob spawns
			spawn(214094, 298.7095f, 89.42245f, 128.7143f, (byte) 15);
		}

		if (Rnd.get(1, 100) > 90) {// stronger kromede
			spawn(214621, 421.9935f, 93.18915f, 117.3053f, (byte) 46);
		}
		else { // normal kromede
			spawn(212846, 421.9935f, 93.18915f, 117.3053f, (byte) 46);
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
