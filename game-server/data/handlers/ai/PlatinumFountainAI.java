package ai;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Luzien
 */
@AIName("fountain")
public class PlatinumFountainAI extends ActionItemNpcAI {

	public PlatinumFountainAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDialogStart(Player player) {
		if (player.getInventory().getItemCountByItemId(186000030) > 0) {
			super.handleDialogStart(player);
			PacketSendUtility.sendMessage(player, "Du forderst dein Gl�ck heraus und wirfst eine Goldmedaille in den Brunnen!");
		} else
			PacketSendUtility.sendMessage(player, "Du hast leider keine Goldmedaillen bei dir, die du in den Brunnen werfen k�nntest.");
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		if (!player.getInventory().decreaseByItemId(186000030, 1))
			return;

		if (Rnd.chance() < 10) {
			ItemService.addItem(player, 186000096, 1);
			PacketSendUtility.sendMessage(player, "Du hattest Gl�ck! Eine Medaille aus reinem Platin springt dir entgegen!");
		} else {
			ItemService.addItem(player, 182005205, 1);
			PacketSendUtility.sendMessage(player, "Du findest leider nur eine alte, verrostete Medaille. Vielleicht hast du beim n�chsten Mal mehr Gl�ck!");
		}
	}
}
