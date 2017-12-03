package ai.instance.eternalBastion;

import static com.aionemu.gameserver.model.DialogAction.SETPRO1;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.TribeClass;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.ActionItemNpcAI;

/**
 * @author Cheatkiller
 */
@AIName("eb_siegecannon")
public class SiegeCannonAI extends ActionItemNpcAI {

	public SiegeCannonAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogActionId, int questId, int extendedRewardIndex) {
		if (dialogActionId == SETPRO1) {
			checkItem(player);
		}
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
		return true;
	}

	private void checkItem(Player player) {
		Item gen = player.getInventory().getFirstItemByItemId(185000136);
		Item key = player.getInventory().getFirstItemByItemId(185000137);
		if ((getOwner().getTribe() == TribeClass.IDF5_TD_WEAPON_PC || getOwner().getTribe() == TribeClass.IDF5_TD_WEAPON_PC_DARK)) {
			if (gen != null && gen.getItemCount() >= 1) {
				int buffId = getOwner().getTribe() == TribeClass.IDF5_TD_WEAPON_PC ? 21138 : 21139;
				player.getInventory().decreaseByItemId(185000136, 1);
				SkillEngine.getInstance().applyEffectDirectly(buffId, player, player, 0);
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
				AIActions.deleteOwner(this);
			} else {
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1401679));
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
			}
		} else {
			if (key != null && key.getItemCount() >= 1) {
				player.getInventory().decreaseByItemId(185000137, 1);
				SkillEngine.getInstance().applyEffectDirectly(21141, player, player, 0);
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
				AIActions.deleteOwner(this);
			} else {
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1401680));
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
			}
		}
	}
}
