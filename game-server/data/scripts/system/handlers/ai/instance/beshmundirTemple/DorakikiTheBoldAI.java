package ai.instance.beshmundirTemple;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.handler.AttackEventHandler;

import ai.AggressiveNpcAI;

/**
 * @author Cheatkiller
 */
@AIName("dorakiki_the_bold")
public class DorakikiTheBoldAI extends AggressiveNpcAI {

	@Override
	protected void handleAttackComplete() {
		AttackEventHandler.onAttackComplete(this);
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
