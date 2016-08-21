package ai.instance.beshmundirTemple;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.handler.AttackEventHandler;

import ai.AggressiveNpcAI2;

/**
 * @author Cheatkiller
 */
@AIName("dorakiki_the_bold")
public class DorakikiTheBoldAI2 extends AggressiveNpcAI2 {

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
