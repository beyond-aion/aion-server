package ai.quests;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.AIState;
import com.aionemu.gameserver.controllers.attack.AggroInfo;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * @author Yeats, Pad
 *
 */
@AIName("_2041_nidalber_balaur")
public class NidalberBalaurQ2041AI2 extends AggressiveNpcAI2{
	
	private Npc kargate = null;
	
	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		findKargate();
	}
	
	private void setNewSpawnPosition() {
		getSpawnTemplate().setX(271.153f);
		getSpawnTemplate().setY(178.903f);
		getSpawnTemplate().setZ(204.52f);
	}
	
	private void findKargate() {
		if (getOwner().getPosition().getMapId() != 320040000)
			return;
		kargate = getOwner().getPosition().getWorldMapInstance().getNpc(204432);
		if (kargate != null && !kargate.getLifeStats().isAlreadyDead()) {
			setNewSpawnPosition();
			moveToKargate();
		}
		else {
			getOwner().getController().onDelete();
		}
	}
	
	private void moveToKargate() {
		setStateIfNot(AIState.WALKING);
		getOwner().setState(1);
		getOwner().getMoveController().moveToPoint(getSpawnTemplate().getX(), getSpawnTemplate().getY(), getSpawnTemplate().getZ());
	}
	
	@Override
	public void handleNotAtHome() {
		if (getOwner().getPosition().getMapId() != 320040000)
			super.handleNotAtHome();
		else
			moveToKargate();
	}
	
	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		addHate();
	}
	
	@Override
	public void handleMoveArrived() {
		super.handleMoveArrived();
		if (getOwner().isAtSpawnLocation())
			addHate();
	}
	
	private void addHate() {
		if (getOwner().getPosition().getMapId() != 320040000)
			return;
		if (kargate == null || kargate.getLifeStats().isAlreadyDead()) {
			getOwner().getController().onDelete();
			return;
		}
		for (AggroInfo info : getOwner().getAggroList().getList()) {
			if (info == null)
				continue;
			if (info.getAttacker() == kargate && info.getHate() > 0)
				return;
		}
		getOwner().getAggroList().addHate(kargate, 100);
	}
}
