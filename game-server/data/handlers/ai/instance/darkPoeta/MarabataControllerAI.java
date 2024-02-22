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

	public MarabataControllerAI(Npc owner) {
		super(owner);
	}

	private Npc getBoss() {
		Npc npc = switch (getNpcId()) {
			case 700443, 700444, 700442 -> getPosition().getWorldMapInstance().getNpc(214850);
			case 700446, 700447, 700445 -> getPosition().getWorldMapInstance().getNpc(214851);
			case 700440, 700441, 700439 -> getPosition().getWorldMapInstance().getNpc(214849);
			default -> null;
		};
		return npc;
	}

	private void applyEffect(boolean remove) {
		Npc boss = getBoss();
		if (boss != null && !boss.isDead()) {
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
		ThreadPoolManager.getInstance().schedule(this::useSkill, 3500);
		ThreadPoolManager.getInstance().schedule(() -> applyEffect(false), 10000);
	}

	private void useSkill() {
		if (isDead())
			return;

		AIActions.targetSelf(this);
		int skill = switch (getNpcId()) {
			case 700443, 700446, 700440 -> 18554;
			case 700444, 700447, 700441 -> 18555;
			case 700442, 700445, 700439 -> 18553;
			default -> 0;
		};
		AIActions.useSkill(this, skill);
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case ALLOW_DECAY, ALLOW_RESPAWN, REWARD_AP_XP_DP_LOOT -> false;
			default -> super.ask(question);
		};
	}
}
