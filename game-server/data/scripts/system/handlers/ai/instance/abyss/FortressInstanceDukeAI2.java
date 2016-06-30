package ai.instance.abyss;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;
import com.aionemu.gameserver.utils.MathUtil;

/**
 * Created on June 24th, 2016
 *
 * @author Estrayl
 * @since AION 4.8
 */
@AIName("fortress_instance_duke")
public class FortressInstanceDukeAI2 extends AggressiveNpcAI2 {

	@Override
	public void fireOnEndCastEvents(NpcSkillEntry usedSkill) {
		if (usedSkill.getSkillId() == 18003)
			spawn(284978, getOwner().getX(), getOwner().getY(), getOwner().getZ(), getOwner().getHeading());
	}
	
	private void deleteSummons() {
		getPosition().getWorldMapInstance().getNpcs().stream().filter(n -> MathUtil.isBetween(284978, 284981, n.getNpcId()))
		.forEach(n -> n.getController().onDelete());
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		deleteSummons();
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		deleteSummons();
	}
}
