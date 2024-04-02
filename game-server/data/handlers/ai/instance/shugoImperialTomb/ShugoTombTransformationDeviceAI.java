package ai.instance.shugoImperialTomb;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.ActionItemNpcAI;

/**
 * @author Estrayl
 */
@AIName("shugo_tomb_transformation_device")
public class ShugoTombTransformationDeviceAI extends ActionItemNpcAI {

	public ShugoTombTransformationDeviceAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		applyTransformationEffect(player);
		super.handleUseItemFinish(player);
	}

	private void applyTransformationEffect(Player player) {
		int skillId = switch (getNpcId()) {
			case 831097 -> player.getRace() == Race.ASMODIANS ? 21105 : 21096;
			case 831096 -> player.getRace() == Race.ASMODIANS ? 21104 : 21095;
			default -> 0;
		};
		if (skillId != 0)
			ThreadPoolManager.getInstance().schedule(() -> SkillEngine.getInstance().applyEffectDirectly(skillId, player, player), 1000);
	}
}
