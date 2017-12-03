package ai.instance.rakes;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.world.WorldMapInstance;

import ai.ShifterAI;

/**
 * @author xTz
 */
@AIName("feeding_mantutu")
public class FeedingMantutuAI extends ShifterAI {

	public FeedingMantutuAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDialogStart(Player player) {
		WorldMapInstance instance = getPosition().getWorldMapInstance();
		if (instance.getNpc(281128) == null && instance.getNpc(281129) == null) {
			super.handleDialogStart(player);
		}
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		super.handleUseItemFinish(player);
		Npc boss = getPosition().getWorldMapInstance().getNpc(219033);
		if (boss != null && boss.isSpawned() && !boss.isDead()) {
			Npc npc = null;
			switch (getNpcId()) {
				case 701387: // water supply
					npc = (Npc) spawn(281129, 712.042f, 490.5559f, 939.7027f, (byte) 0);
					break;
				case 701386: // feed supply
					npc = (Npc) spawn(281128, 714.62634f, 504.4552f, 939.60675f, (byte) 0);
					break;
			}
			boss.getAi().onCustomEvent(1, npc);
			AIActions.deleteOwner(this);
		}
	}

}
