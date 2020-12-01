package ai.instance.abyss;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

import ai.AggressiveNpcAI;

/**
 * Created on June 24th, 2016
 *
 * @author Estrayl
 * @since AION 4.8
 */
@AIName("fortress_instance_duke")
public class FortressInstanceDukeAI extends AggressiveNpcAI {

	public FortressInstanceDukeAI(Npc owner) {
		super(owner);
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		if (skillTemplate.getSkillId() == 18003)
			spawn(284978, getOwner().getX(), getOwner().getY(), getOwner().getZ(), getOwner().getHeading());
	}

	private void deleteSummons() {
		getPosition().getWorldMapInstance().forEachNpc(npc -> {
			if (npc.getNpcId() >= 284978 && npc.getNpcId() <= 284981)
				npc.getController().delete();
		});
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
