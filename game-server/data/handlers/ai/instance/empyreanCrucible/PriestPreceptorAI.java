package ai.instance.empyreanCrucible;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTargetAttribute;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author Luzien, w4terbomb
 */
@AIName("priest_preceptor")
public class PriestPreceptorAI extends CrucibleBossAI {

	private final int[] helpers; // warrior, ranger, mage

	public PriestPreceptorAI(Npc owner) {
		super(owner);
		if (owner.getNpcId() == 217581) // Thrasymedes
			helpers = new int[] { 282366, 282367, 282368 }; // Boreas, Jumentis, Charna
		else // Freyr
			helpers = new int[] { 282369, 282370, 282371 }; // Traufnir, Sigyn, Sif
	}

	private boolean isLight() {
		return getNpcId() == 217581;
	}

	@Override
	protected void onFightStart() {
		say(isLight() ? 1500219 : 1500221);
		cast(19612, 10, NpcSkillTargetAttribute.ME);
	}

	@Override
	protected void onFightTick(int seconds, int hp) {
		if (every(seconds, 10, 15)) {
			cast(19587, 10);
			cast(19588, 15);
		}
		if (every(seconds, 50, 60))
			cast(19613, 10, NpcSkillTargetAttribute.RANDOM);
		if (passed(80))
			cast(19611, 10);
		if (passed(30)) {
			cast(19610, 10, NpcSkillTargetAttribute.ME);
			cast(19614, 10, NpcSkillTargetAttribute.ME);
			WorldPosition p = getPosition();
			for (int i = 0; i < helpers.length; i++) {
				if (spawn(helpers[i], p.getX(), p.getY(), p.getZ(), p.getHeading()) instanceof Npc helper) {
					int sicknessLevel = i == 0 ? 1 : i == 1 ? 2 : 4;
					ThreadPoolManager.getInstance().schedule(() -> helper.queueSkill(19594, sicknessLevel, -1, NpcSkillTargetAttribute.ME), 1000);
				}
			}
		}
	}

	@Override
	protected void handleDied() {
		say(isLight() ? 1500220 : 1500222);
		// the warrior and the mage go with the priest, the ranger only runs for 10 seconds and then fights on
		getPosition().getWorldMapInstance().getNpcs(helpers[0], helpers[2]).forEach(npc -> npc.getController().deleteIfAliveOrCancelRespawn());
		Npc ranger = getPosition().getWorldMapInstance().getNpc(helpers[1]);
		if (ranger != null && !ranger.isDead() && ranger.getAi() instanceof NpcAI rangerAi) {
			SkillEngine.getInstance().getSkill(ranger, 19604, 1, ranger).useNoAnimationSkill();
			CrucibleRun.start(rangerAi, null, 10000, null);
		}
		super.handleDied();
	}

	@Override
	protected void onFightEnd() {
		if (!isDead())
			getPosition().getWorldMapInstance().getNpcs(helpers).forEach(npc -> npc.getController().deleteIfAliveOrCancelRespawn());
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case REWARD_LOOT, ALLOW_DECAY -> false;
			default -> super.ask(question);
		};
	}
}
