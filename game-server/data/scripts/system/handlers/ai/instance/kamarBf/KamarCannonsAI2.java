package ai.instance.kamarBf;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.ActionItemNpcAI2;


/**
 * @author Cheatkiller
 *
 */
@AIName("kamarcannons")
public class KamarCannonsAI2 extends ActionItemNpcAI2 {
	
	private Npc flag;
	
	@Override
	protected void handleUseItemFinish(Player player) {
		switch(getOwner().getNpcId()) {
			case 701806:
			case 701902:
				SkillEngine.getInstance().applyEffectDirectly(21409, player, player, 0);
				AI2Actions.deleteOwner(this);
				break;
			case 701808:
			case 701912:
				Item siegePower = player.getInventory().getFirstItemByItemId(164000262);
				if (siegePower != null && siegePower.getItemCount() >= 1) {
					player.getInventory().decreaseByItemId(164000262, 1);
					SkillEngine.getInstance().applyEffectDirectly(player.getRace() == Race.ELYOS ? 21403 : 21404, player, player, 0);
					AI2Actions.deleteOwner(this);
				}
				else
					PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1401854));
				break;
		}
	}
	
	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		int npcId = getOwner().getNpcId();
		if (npcId != 701806 && npcId != 701902)
			flag = (Npc)spawn(npcId == 701808 ? 801961 : 801960, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0);
	}
	
	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		if (flag != null)
			flag.getController().onDelete();
	}
	
	@Override
	protected void handleDied() {
		super.handleDied();
		if (flag != null)
			flag.getController().onDelete();
	}

}
