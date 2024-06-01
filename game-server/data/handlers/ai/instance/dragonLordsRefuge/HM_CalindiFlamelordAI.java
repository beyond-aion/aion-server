package ai.instance.dragonLordsRefuge;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

/**
 * @author Cheatkiller, Yeats, Estrayl
 */
@AIName("IDTiamat_HM_calindi_flamelord")
public class HM_CalindiFlamelordAI extends CalindiFlamelordAI {

	public HM_CalindiFlamelordAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void startHallucinatoryVictoryEvent() {
		if (getPosition().getWorldMapInstance().getNpc(731629) == null && getPosition().getWorldMapInstance().getNpc(731630) == null)
			getOwner().queueSkill(21887, 1);
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		switch (skillTemplate.getSkillId()) {
			case 21887:
				spawn(731629, 482.21f, 458.06f, 427.42f, (byte) 98);
				spawn(731630, 482.21f, 571.16f, 427.42f, (byte) 22);
				rndSpawn(856298);
				break;
			case 21888:
				Player target = getRandomTarget();
				if (target != null)
					spawn(856299, target.getX(), target.getY(), target.getZ(), (byte) 0);
		}
	}

	@Override
	protected void blazeEngraving() {
		if (Rnd.chance() < 3 && getPosition().getWorldMapInstance().getNpc(856299) == null)
			getOwner().queueSkill(21888, 60);
	}
}
