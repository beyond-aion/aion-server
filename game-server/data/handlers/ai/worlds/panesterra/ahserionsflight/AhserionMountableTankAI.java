package ai.worlds.panesterra.ahserionsflight;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.panesterra.PanesterraService;
import com.aionemu.gameserver.services.panesterra.ahserion.AhserionRaid;
import com.aionemu.gameserver.services.panesterra.ahserion.PanesterraFaction;
import com.aionemu.gameserver.services.panesterra.ahserion.PanesterraTeam;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.ActionItemNpcAI;

/**
 * @author Estrayl
 */
@AIName("ahserion_mountable_tank")
public class AhserionMountableTankAI extends ActionItemNpcAI {

	private final AtomicBoolean canUse = new AtomicBoolean(true);
	private PanesterraFaction ownerFaction;

	public AhserionMountableTankAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		ownerFaction = switch (getNpcId()) {
			case 277233, 277238 -> PanesterraFaction.BELUS;
			case 277234, 277239 -> PanesterraFaction.ASPIDA;
			case 277235, 277240 -> PanesterraFaction.ATANATOS;
			case 277236, 277241 -> PanesterraFaction.DISILLON;
			default -> PanesterraFaction.BALAUR;
		};
		getOwner().getController().addTask(TaskId.DESPAWN,
			ThreadPoolManager.getInstance().schedule(() -> getOwner().getController().deleteIfAliveOrCancelRespawn(), 12, TimeUnit.MINUTES));
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		if (canUseTank(player) && canUse.compareAndSet(true, false)) {
			int skillId = getSkillId();
			if (skillId != 0)
				SkillEngine.getInstance().getSkill(getOwner(), skillId, 65, player).useNoAnimationSkill();
			AIActions.deleteOwner(this);
		}
	}

	private boolean canUseTank(Player player) {
		if (AhserionRaid.getInstance().isStarted()) {
			PanesterraTeam team = PanesterraService.getInstance().getTeam(player);
			return team != null && team.getFaction() == ownerFaction;
		}
		return false;
	}

	private int getSkillId() {
		return switch (getNpcId()) {
			// Board the Chariot
			case 277233 -> 21582; // Belus
			case 277234 -> 21589; // Aspida
			case 277235 -> 21590; // Atanatos
			case 277236 -> 21591; // Disillon
			// Board the Ignus Engine
			case 277238 -> 21579; // Belus
			case 277239 -> 21586; // Aspida
			case 277240 -> 21587; // Atanatos
			case 277241 -> 21588; // Disillon
			default -> 0;
		};
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case REWARD_AP_XP_DP_LOOT -> false;
			default -> super.ask(question);
		};
	}
}
