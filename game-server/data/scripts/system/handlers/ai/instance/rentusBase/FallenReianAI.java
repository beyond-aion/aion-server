package ai.instance.rentusBase;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PositionUtil;

/**
 * @author xTz
 */
@AIName("fallen_reian")
public class FallenReianAI extends NpcAI {

	private AtomicBoolean isCollapsed = new AtomicBoolean(false);

	public FallenReianAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleCreatureMoved(Creature creature) {
		if (isCollapsed.get())
			return;
		if (!(creature instanceof Player))
			return;
		Player player = (Player) creature;
		if (PositionUtil.isInRange(getOwner(), player, 20) && isCollapsed.compareAndSet(false, true))
			getPosition().getWorldMapInstance().setDoorState(getNpcId() == 799661 ? 16 : 54, true);
	}

}
