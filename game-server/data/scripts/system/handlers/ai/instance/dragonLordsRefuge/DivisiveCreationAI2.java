package ai.instance.dragonLordsRefuge;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.AIState;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.templates.item.ItemAttackType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Cheatkiller
 */
@AIName("divisivecreation")
public class DivisiveCreationAI2 extends AggressiveNpcAI2 {

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		final WorldMapInstance inastance = this.getPosition().getWorldMapInstance();
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				AI2Actions.targetCreature(DivisiveCreationAI2.this, inastance.getPlayersInside().get(Rnd.get(inastance.getPlayersInside().size() - 1)));
				setStateIfNot(AIState.WALKING);
				getOwner().setState(1);
				getMoveController().moveToTargetObject();
				PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.START_EMOTE2, 0, getOwner().getObjectId()));
			}
		}, 5000);
	}

	@Override
	public int modifyMaccuracy(int value) {
		return 1200;
	}

	@Override
	public ItemAttackType modifyAttackType(ItemAttackType type) {
		return ItemAttackType.MAGICAL_EARTH;
	}

	@Override
	public boolean ask(AIQuestion question) {
		switch (question) {
			case SHOULD_DECAY:
			case SHOULD_RESPAWN:
			case SHOULD_REWARD:
				return false;
			default:
				return super.ask(question);
		}
	}
}
