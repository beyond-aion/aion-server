package ai.instance.beshmundirTemple;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;

import ai.AggressiveNpcAI;

/**
 * @author Cheatkiller
 */
@AIName("dorakiki_the_bold")
public class DorakikiTheBoldAI extends AggressiveNpcAI {

	public DorakikiTheBoldAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttackComplete() {
		super.handleAttackComplete();
		if (getEffectController().hasAbnormalEffect(18901)) {
			getEffectController().removeEffect(18901);
		}
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		if (getEffectController().hasAbnormalEffect(18901)) {
			getEffectController().removeEffect(18901);
		}
	}
}
