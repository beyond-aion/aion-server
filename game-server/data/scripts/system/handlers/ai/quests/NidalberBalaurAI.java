package ai.quests;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.controllers.attack.AggroInfo;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;

import ai.AggressiveNpcAI;

/**
 * @author Yeats, Pad
 */
@AIName("nidalber_balaur")
public class NidalberBalaurAI extends AggressiveNpcAI {

	private Npc questNpc = null;

	public NidalberBalaurAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		findQuestNpc();
	}

	private void setNewSpawnPosition() {
		getSpawnTemplate().setX(271.153f);
		getSpawnTemplate().setY(178.903f);
		getSpawnTemplate().setZ(204.52f);
	}

	private void findQuestNpc() {
		int mapId = getOwner().getPosition().getMapId();
		if (mapId != 310040000 && mapId != 320040000)
			return;
		questNpc = getOwner().getPosition().getWorldMapInstance().getNpc(mapId == 310040000 ? 204044 : 204432);
		if (questNpc != null && !questNpc.isDead()) {
			setNewSpawnPosition();
			moveToQuestNpc();
		} else {
			getOwner().getController().delete();
		}
	}

	private void moveToQuestNpc() {
		setStateIfNot(AIState.WALKING);
		getOwner().setState(CreatureState.ACTIVE, true);
		getOwner().getMoveController().moveToPoint(getSpawnTemplate().getX(), getSpawnTemplate().getY(), getSpawnTemplate().getZ());
	}

	@Override
	public void handleNotAtHome() {
		if (getOwner().getPosition().getMapId() != 310040000 && getOwner().getPosition().getMapId() != 320040000)
			super.handleNotAtHome();
		else
			moveToQuestNpc();
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
		if (getOwner().getPosition().getMapId() != 310040000 && getOwner().getPosition().getMapId() != 320040000)
			return;
		if (questNpc == null || questNpc.isDead()) {
			getOwner().getController().delete();
			return;
		}
		for (AggroInfo info : getOwner().getAggroList().getList()) {
			if (info == null)
				continue;
			if (info.getAttacker() == questNpc && info.getHate() > 0)
				return;
		}
		getOwner().getAggroList().addHate(questNpc, 100);
	}
}
