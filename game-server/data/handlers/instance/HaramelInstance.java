package instance;

import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Undertrey
 */
@InstanceID(300200000)
public class HaramelInstance extends GeneralInstanceHandler {

	public HaramelInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onDie(Npc npc) {
		Player player = npc.getAggroList().getMostPlayerDamage();
		if (player == null)
			return;
		switch (npc.getNpcId()) {
			case 216922:
				npc.getController().delete();
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDNOVICE_HAMEROON_TREASUREBOX_SPAWN());
				PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(false, 0, 0, 457, true));
				switch (player.getPlayerClass()) {
					case GLADIATOR:
					case TEMPLAR:
						spawn(700829, 224.137f, 268.608f, 144.898f, (byte) 90); // chest warrior
						break;
					case ASSASSIN:
					case RANGER:
					case GUNNER:
						spawn(700830, 224.137f, 268.608f, 144.898f, (byte) 90); // chest scout
						break;
					case BARD:
					case SORCERER:
					case SPIRIT_MASTER:
						spawn(700831, 224.137f, 268.608f, 144.898f, (byte) 90); // chest mage
						break;
					case CLERIC:
					case CHANTER:
					case RIDER:
						spawn(700832, 224.137f, 268.608f, 144.898f, (byte) 90); // chest cleric
						break;
				}
				spawn(700852, 224.5984f, 331.1431f, 141.8925f, (byte) 90); // spawn opened dimensional gate
				break;
			case 216920: // Brainwashed Dukaki Weakarm
			case 216921: // Brainwashed Dukaki Peon
			case 217067: // Brainwashed MuMu Worker
			case 700950: // Aether Cart
				npc.getController().delete();
				break;
		}
	}
}
