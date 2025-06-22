package ai;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.stats.calc.Stat2;

/**
 * @author Estrayl
 */
@AIName("rift_protector")
public class RiftProtectorAI extends AggressiveNpcAI {

	public RiftProtectorAI(Npc owner) {
		super(owner);
	}

	@Override
	public void modifyOwnerStat(Stat2 stat) {
		stat.setBaseRate(0.1f);
	}
}
