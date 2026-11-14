package ai.instance.empyreanCrucible;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTargetAttribute;

/**
 * Lehpar boss of stage 5: calls a pair of lehpars for help every 90 seconds while above 70 % HP. Below that, the call comes once and then no
 * more, its set depending on the HP.
 */
@AIName("lehpar_captain")
public class LehparCaptainAI extends CrucibleBossAI {

	private static final int FIGHTER = 217531, ASSASSIN = 217533, WIZARD = 217534, PRIEST = 217536;
	private final List<Npc> helpers = new ArrayList<>();
	private int helpIn;

	public LehparCaptainAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void onFightStart() {
		cast(16519, 34);
		helpIn = 60;
	}

	@Override
	protected void onFightTick(int seconds, int hp) {
		if (every(seconds, 20, 20)) {
			if (hp > 70) {
				cast(16519, 34);
				cast(16628, 33, NpcSkillTargetAttribute.RANDOM);
			} else if (hp > 30) {
				cast(16997, 34);
			} else {
				cast(16519, 34);
				cast(16684, 33);
			}
		}
		if (helpIn > 0 && --helpIn == 0) {
			if (hp > 70) {
				callHelp(FIGHTER, ASSASSIN);
				helpIn = 90;
			} else if (hp > 30) {
				callHelp(ASSASSIN, WIZARD);
			} else {
				callHelp(ASSASSIN, PRIEST, WIZARD);
			}
		}
	}

	private void callHelp(int... npcIds) {
		for (int npcId : npcIds) {
			VisibleObject helper = rndSpawnInRange(npcId, 1, 5);
			if (helper instanceof Npc npc)
				synchronized (helpers) {
					helpers.add(npc);
				}
		}
	}

	@Override
	protected void onFightEnd() {
		synchronized (helpers) {
			helpers.stream().filter(npc -> !npc.isDead()).forEach(npc -> npc.getController().delete());
			helpers.clear();
		}
	}
}
