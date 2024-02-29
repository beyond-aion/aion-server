package ai.instance.custom.eternalChallenge;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AttackIntention;
import com.aionemu.gameserver.custom.instance.RoahCustomInstanceHandler;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FORCED_MOVE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.geo.GeoService;

import ai.AggressiveNoLootNpcAI;

/**
 * @author Sykra
 */
@AIName("custom_instance_bulky")
public class CustomInstanceBulkyAI extends AggressiveNoLootNpcAI {

	public CustomInstanceBulkyAI(Npc owner) {
		super(owner);
	}

	@Override
	public AttackIntention chooseAttackIntention() {
		WorldMapInstance wmi = getPosition().getWorldMapInstance();
		if (!(wmi.getInstanceHandler() instanceof RoahCustomInstanceHandler))
			return super.chooseAttackIntention();

		VisibleObject target = getTarget();
		if (!isDead() && target != null) {
			if (!GeoService.getInstance().canSee(getOwner(), target)) {
				World.getInstance().updatePosition(getOwner(), target.getX(), target.getY(), target.getZ(), (byte) 30);
				PacketSendUtility.broadcastPacketAndReceive(getOwner(), new SM_FORCED_MOVE(getOwner(), getOwner()));
			}
		}
		return super.chooseAttackIntention();
	}

}
