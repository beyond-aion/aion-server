package ai.events;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.item.ItemService;

import ai.ActionItemNpcAI;

/**
 * Part of 'Blessing Of The Magic Fountain' event.
 * 
 * @author Estrayl March 17th, 2018
 */
@AIName("aether_blossom")
public class AetherBlossomAI extends ActionItemNpcAI {

	private AtomicBoolean hasRewarded = new AtomicBoolean();

	public AetherBlossomAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		if (hasRewarded.compareAndSet(false, true)) // just in case
			ItemService.addItem(player, 186000406, 1); // [Event] Aether Blossom
		super.handleUseItemFinish(player);
		AIActions.die(this);
	}

}
