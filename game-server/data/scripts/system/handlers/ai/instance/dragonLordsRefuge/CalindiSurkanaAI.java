package ai.instance.dragonLordsRefuge;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Cheatkiller
 * @modified Estrayl March 8th, 2018
 */
@AIName("calindi_surkana")
public class CalindiSurkanaAI extends NpcAI {

	Npc calindi;

	public CalindiSurkanaAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		calindi = getPosition().getWorldMapInstance().getNpc(219359);
		reflect();
	}

	private void reflect() {
		ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			if (!isDead()) {
				int buffId = 0;
				switch (getNpcId()) {
					case 730695:
						buffId = 20590;
						break;
					case 730696:
						buffId = 20591;
						break;
				}
				SkillEngine.getInstance().applyEffectDirectly(buffId, getOwner(), calindi);
			}
		}, 2500, 5000);
	}

	@Override
	public boolean ask(AIQuestion question) {
		switch (question) {
			case SHOULD_DECAY:
			case SHOULD_RESPAWN:
			case SHOULD_REWARD:
				return false;
			default:
				return super.ask(question);
		}
	}

}
