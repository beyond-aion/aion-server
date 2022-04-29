package ai.instance.rentusBase;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.manager.WalkManager;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.templates.walker.RouteStep;
import com.aionemu.gameserver.model.templates.walker.WalkerTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;

import ai.GeneralNpcAI;

/**
 * @author xTz
 */
@AIName("imprisoned_reian")
public class ImprisonedReianAI extends GeneralNpcAI {

	private AtomicBoolean isSaved = new AtomicBoolean(false);
	private AtomicBoolean isAsked = new AtomicBoolean(false);
	private String walkerId;
	private WalkerTemplate template;

	public ImprisonedReianAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		walkerId = getSpawnTemplate().getWalkerId();
		getSpawnTemplate().setWalkerId(null);
		if (walkerId != null) {
			template = DataManager.WALKER_DATA.getWalkerTemplate(walkerId);
		}
		super.handleSpawned();
	}

	@Override
	protected void handleMoveArrived() {
		RouteStep step = getOwner().getMoveController().getCurrentStep();
		super.handleMoveArrived();
		if (template.getRouteSteps().size() - 4 == step.getStepIndex()) {
			getSpawnTemplate().setWalkerId(null);
			WalkManager.stopWalking(this);
			AIActions.deleteOwner(this);
		}
	}

	@Override
	protected void handleCreatureMoved(Creature creature) {
		if (walkerId != null) {
			if (creature instanceof Player) {
				final Player player = (Player) creature;
				if (PositionUtil.getDistance(getOwner(), player) <= 21) {
					if (isAsked.compareAndSet(false, true)) {
						switch (Rnd.get(1, 10)) {
							case 1:
								PacketSendUtility.broadcastMessage(getOwner(), 390563);
								break;
							case 2:
								PacketSendUtility.broadcastMessage(getOwner(), 390567);
								break;
						}
					}
				}
				if (PositionUtil.getDistance(getOwner(), player) <= 6) {
					if (isSaved.compareAndSet(false, true)) {
						getSpawnTemplate().setWalkerId(walkerId);
						WalkManager.startWalking(this);
						getOwner().setState(CreatureState.ACTIVE, true);
						PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.CHANGE_SPEED, 0, getObjectId()));
						switch (Rnd.get(1, 10)) {
							case 1:
								PacketSendUtility.broadcastMessage(getOwner(), 342410);
								break;
							case 2:
								PacketSendUtility.broadcastMessage(getOwner(), 342411);
								break;
						}
					}
				}
			}
		}
	}
}
