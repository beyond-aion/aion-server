package ai.instance.kamarBf;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.AggressiveNpcAI;

/**
 * @author Cheatkiller
 */
@AIName("kamarbosses")
public class KamarBossesAI extends AggressiveNpcAI {

	private Npc flag;

	public KamarBossesAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		switch (getOwner().getNpcId()) {
			case 232853:
				PacketSendUtility.broadcastToMap(getOwner(), 1401845);
				break;
			case 232857:
				PacketSendUtility.broadcastToMap(getOwner(), 1401848);
				break;
			case 232858:
				PacketSendUtility.broadcastToMap(getOwner(), 1401850);
				break;

		}
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		switch (getOwner().getNpcId()) {
			case 232854:
			case 232853:
			case 232852:
				flag = (Npc) spawn(801956, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0);
				break;
			case 232857:
				flag = (Npc) spawn(801957, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0);
				break;
			case 232858:
				flag = (Npc) spawn(801958, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0);
				break;

		}
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		if (flag != null)
			flag.getController().delete();
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		switch (getOwner().getNpcId()) {
			case 232853:
				PacketSendUtility.broadcastToMap(getOwner(), 1401846);
				break;
			case 232857:
				PacketSendUtility.broadcastToMap(getOwner(), 1401849);
				break;
			case 232858:
				PacketSendUtility.broadcastToMap(getOwner(), 1401851);
				break;

		}
		if (flag != null)
			flag.getController().delete();
	}

}
