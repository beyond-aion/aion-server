package ai.instance.darkPoeta;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author xTz
 */
@AIName("marabata_controller")
public class MarabataControllerAI extends NpcAI {

	private Npc getBoss() {
		Npc npc = null;
		switch (getNpcId()) {
			case 700443:
			case 700444:
			case 700442:
				npc = getPosition().getWorldMapInstance().getNpc(214850);
				break;
			case 700446:
			case 700447:
			case 700445:
				npc = getPosition().getWorldMapInstance().getNpc(214851);
				break;
			case 700440:
			case 700441:
			case 700439:
				npc = getPosition().getWorldMapInstance().getNpc(214849);
				break;
		}
		return npc;
	}

	private void applyEffect(boolean remove) {
		Npc boss = getBoss();
		if (boss != null && !boss.getLifeStats().isAlreadyDead()) {
			switch (getNpcId()) {
				case 700443:
				case 700446:
				case 700440:
					if (remove)
						boss.getEffectController().removeEffect(18556);
					else
						boss.getController().useSkill(18556);
					break;
				case 700444:
				case 700447:
				case 700441:
					// TODO unk
					break;
				case 700442:
				case 700445:
				case 700439:
					if (remove)
						boss.getEffectController().removeEffect(18110);
					else
						boss.getController().useSkill(18110);
					break;
			}
		}
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		applyEffect(true);
		AIActions.deleteOwner(this);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		ThreadPoolManager.getInstance().schedule(() -> useSkill(), 3500);
		ThreadPoolManager.getInstance().schedule(() -> applyEffect(false), 10000);
	}

	private void useSkill() {
		if (isAlreadyDead())
			return;

		AIActions.targetSelf(this);
		int skill = 0;
		switch (getNpcId()) {
			case 700443:
			case 700446:
			case 700440:
				skill = 18554;
				break;
			case 700444:
			case 700447:
			case 700441:
				skill = 18555;
				break;
			case 700442:
			case 700445:
			case 700439:
				skill = 18553;
				break;
		}
		AIActions.useSkill(this, skill);
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
