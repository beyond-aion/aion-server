package ai.instance.argentManor;

import ai.GeneralNpcAI2;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.poll.AIAnswer;
import com.aionemu.gameserver.ai2.poll.AIAnswers;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldPosition;

/**
 *
 * @author xTz
 */
@AIName("pletus_orb")
public class PletusOrbAI2 extends GeneralNpcAI2 {

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	protected void handleMoveArrived() {
		super.handleMoveArrived();
		getOwner().setState(Rnd.get(1, 2) == 1 ? 1 : 64);
		PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.START_EMOTE2, 0, getObjectId()));
		if (!isInRangeMagicalSap()) {
			if (Rnd.get(1, 100) >= 70) {
				WorldPosition p =  getPosition();
				spawn(282148, p.getX(), p.getY(), p.getZ(), (byte) 0);
			}
		}
	}

	private boolean isInRangeMagicalSap() {
		for (VisibleObject obj :getKnownList().getKnownObjects().values()) {
			if (obj instanceof Npc) {
				Npc npc = (Npc) obj;
				if (isInRange(obj, 1) && npc.getNpcId() == 282148) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public int modifyDamage(Creature creature, int damage) {
		return 1;
	}

	@Override
	public AIAnswer ask(AIQuestion question) {
		switch (question) {
			case CAN_RESIST_ABNORMAL:
				return AIAnswers.POSITIVE;
			default:
				return AIAnswers.NEGATIVE;
		}
	}
}