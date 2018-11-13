package ai.events;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.ChestAI;

/**
 * @author Estrayl
 */
@AIName("hidden_cake")
public class HiddenBirthdayCakeAI extends ChestAI {

	private final static int JEST_SPAWN_CHANCE = 25;
	private final static int[] JEST_SPAWN_IDS = { 210341, 214732, 210595 };

	public HiddenBirthdayCakeAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		if (getOwner().isInState(CreatureState.DEAD))
			return;

		if (Rnd.chance() < JEST_SPAWN_CHANCE) {
			VisibleObject s = spawn(Rnd.get(JEST_SPAWN_IDS), getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0);
			ThreadPoolManager.getInstance().schedule(() -> s.getController().delete(), 120000);
			PacketSendUtility.sendPacket(player,
				SM_SYSTEM_MESSAGE.STR_MSG_TOYPET_FEED_FOOD_NOT_LOVEFLAVOR(s.getObjectTemplate().getL10n(), getObjectTemplate().getL10n()));
		}
		super.handleUseItemFinish(player);
	}

}
