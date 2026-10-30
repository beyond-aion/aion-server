package ai.instance.empyreanCrucible;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTargetAttribute;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author Luzien, w4terbomb
 */
@AIName("mage_preceptor")
public class MagePreceptorAI extends CrucibleBossAI {

	public MagePreceptorAI(Npc owner) {
		super(owner);
	}

	private boolean isLight() {
		return getNpcId() == 217580;
	}

	@Override
	protected void onFightStart() {
		say(isLight() ? 1500215 : 1500217);
		cast(19607, 10);
	}

	@Override
	protected void onFightTick(int seconds, int hp) {
		if (every(seconds, 15, 40))
			cast(19605, 10);
		if (every(seconds, 12, 12))
			cast(19581, 10);
		if (passed(60)) {
			cast(19606, 10);
			cast(19609, 10, NpcSkillTargetAttribute.ME);
			WorldPosition p = getPosition();
			spawn(282363, p.getX() + 5, p.getY() - 5, p.getZ(), p.getHeading()); // S7_Summon_MagmaElemental_55_Ah
			spawn(282364, p.getX() - 5, p.getY() + 5, p.getZ(), p.getHeading()); // S7_Summon_TempestElemental_55_Ah
			cast(19608, 10, NpcSkillTargetAttribute.ME);
		}
		if (passed(30)) {
			cast(19606, 10);
			cast(19605, 10, NpcSkillTargetAttribute.RANDOM);
			cast(19605, 10, NpcSkillTargetAttribute.RANDOM);
			cast(19582, 10);
		}
	}

	@Override
	protected void onFightEnd() {
		getPosition().getWorldMapInstance().getNpcs(282363, 282364).forEach(npc -> npc.getController().delete());
	}

	@Override
	protected void handleDied() {
		say(isLight() ? 1500216 : 1500218);
		super.handleDied();
	}
}
