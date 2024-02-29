package ai.instance.dragonLordsRefuge;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Estrayl
 */
@AIName("tiamats_incarnation_spawn")
public class TiamatsIncarnationSpawnsAI extends NpcAI {

	private Future<?> skillTask;

	public TiamatsIncarnationSpawnsAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		skillTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> AIActions.useSkill(this, getSkillId()), 2500, 3000);
	}

	@Override
	protected void handleDespawned() {
		skillTask.cancel(true);
		super.handleDespawned();
	}

	private int getSkillId() {
		switch (getNpcId()) {
			case 282727: // Gravity Whirlpool
			case 856074:
				return 20155;
			case 282729: // Thunderbolt Whirlpool
			case 856076:
				return 20156;
			case 282731: // Petrification Crystal
			case 856072:
				return 20159;
			case 282735: // Cavity of Earth
			case 856068:
				return 20172;
			case 282737: // Collapsing Earth
			case 856070:
				return 20173;
			default:
				return 0;
		}
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case REWARD_AP_XP_DP_LOOT, ALLOW_DECAY -> false;
			default -> super.ask(question);
		};
	}
}
