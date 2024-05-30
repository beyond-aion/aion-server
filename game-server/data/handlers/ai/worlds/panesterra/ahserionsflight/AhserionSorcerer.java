package ai.worlds.panesterra.ahserionsflight;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.item.ItemAttackType;

/**
 * @author Estrayl
 */
@AIName("ahserion_sorcerer")
public class AhserionSorcerer extends AhserionAggressiveNpcAI {

	public AhserionSorcerer(Npc owner) {
		super(owner);
	}

	@Override
	public ItemAttackType modifyAttackType(ItemAttackType type) {
		return ItemAttackType.MAGICAL_FIRE;
	}
}
