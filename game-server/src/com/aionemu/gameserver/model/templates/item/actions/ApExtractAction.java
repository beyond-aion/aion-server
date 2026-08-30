package com.aionemu.gameserver.model.templates.item.actions;

import javax.xml.bind.annotation.XmlAttribute;

import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.storage.Storage;
import com.aionemu.gameserver.model.templates.item.Acquisition;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author Rolandas, Luzien
 */
public class ApExtractAction extends AbstractItemAction {

	private static final int CASTING_DELAY = 3000;

	@XmlAttribute
	protected UseTarget target;
	@XmlAttribute
	protected float rate;

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem, Object... params) {
		if (targetItem == null || !targetItem.canApExtract()) {
			if (targetItem != null)
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_AP_DECOMPOSE_CANNOT(targetItem.getL10n()));
			return false;
		}
		if (targetItem.isEquipped()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_AP_DECOMPOSE_WRONG_EQUIPED());
			return false;
		}
		if (parentItem.getItemTemplate().getLevel() < targetItem.getItemTemplate().getLevel()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_AP_DECOMPOSE_WRONG_LEVEL(parentItem.getL10n(), targetItem.getL10n()));
			return false;
		}
		if (parentItem.getItemTemplate().getItemQuality().getQualityId() < targetItem.getItemTemplate().getItemQuality().getQualityId()) {
			PacketSendUtility.sendPacket(player,
				SM_SYSTEM_MESSAGE.STR_MSG_AP_DECOMPOSE_WRONG_QUALITY(parentItem.getL10n(), targetItem.getL10n()));
			return false;
		}

		UseTarget type;
		switch (targetItem.getItemTemplate().getItemGroup()) {
			case SWORD:
			case DAGGER:
			case MACE:
			case ORB:
			case SPELLBOOK:
			case BOW:
			case GREATSWORD:
			case POLEARM:
			case STAFF:
			case HARP:
			case GUN:
			case KEYBLADE:
			case CANNON:
				type = UseTarget.WEAPON;
				break;
			case TORSO:
			case PANTS:
			case SHOULDER:
			case GLOVE:
			case SHOES:
			case RB_TORSO:
			case RB_PANTS:
			case RB_SHOULDER:
			case RB_GLOVE:
			case RB_SHOES:
			case CL_TORSO:
			case CL_PANTS:
			case CL_SHOULDER:
			case CL_GLOVE:
			case CL_SHOES:
			case CH_TORSO:
			case CH_PANTS:
			case CH_SHOULDER:
			case CH_GLOVE:
			case CH_SHOES:
			case LT_TORSO:
			case LT_PANTS:
			case LT_SHOULDER:
			case LT_GLOVE:
			case LT_SHOES:
			case PL_TORSO:
			case PL_PANTS:
			case PL_SHOULDER:
			case PL_GLOVE:
			case PL_SHOES:
			case SHIELD:
				type = UseTarget.ARMOR;
				break;
			case NECKLACE:
			case EARRING:
			case RING:
			case BELT:
			case HEAD:
				type = UseTarget.ACCESSORY;
				break;
			case WING:
				type = UseTarget.WING;
				break;
			case NONE:
				// e.g. non-equipment "junk" items retail still allows AP extraction on (confirmed only matched by the OTHER/ALL target types)
				type = UseTarget.OTHER;
				break;
			default:
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_AP_DECOMPOSE_CANNOT(targetItem.getL10n()));
				return false;
		}
		// EQUIPMENT is a shorthand for "any of WEAPON/ARMOR/ACCESSORY/WING", confirmed on retail it does NOT also match OTHER
		if (target != UseTarget.ALL && target != type && !(target == UseTarget.EQUIPMENT && type != UseTarget.OTHER)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_AP_DECOMPOSE_CANNOT(targetItem.getL10n()));
			return false;
		}
		return true;
	}

	@Override
	public void act(Player player, Item parentItem, Item targetItem, Object... params) {
		PacketSendUtility.broadcastPacket(player,
			new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), CASTING_DELAY, 0, 0), true);

		ItemUseObserver observer = new ItemUseObserver() {

			@Override
			public void abort() {
				player.getController().cancelTask(TaskId.ITEM_USE);
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_AP_DECOMPOSE_ITEM_CANCELED(targetItem.getL10n()));
				PacketSendUtility.broadcastPacket(player,
					new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), 0, 2, 0), true);
				player.getObserveController().removeObserver(this);
			}

		};
		player.getObserveController().attach(observer);
		player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(() -> {
			player.getObserveController().removeObserver(observer);
			finishUse(player, parentItem, targetItem);
		}, CASTING_DELAY));
	}

	private void finishUse(Player player, Item parentItem, Item targetItem) {
		boolean success = extractAp(player, parentItem, targetItem);
		if (success)
			player.startCooldown(parentItem);
		PacketSendUtility.broadcastPacket(player,
			new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), 0, success ? 1 : 2, 0), true);
	}

	private boolean extractAp(Player player, Item parentItem, Item targetItem) {
		if (!canAct(player, parentItem, targetItem))
			return false;
		Acquisition acquisition = targetItem.getItemTemplate().getAcquisition();
		if (acquisition == null || acquisition.getRequiredAp() == 0)
			return false;
		Storage inventory = player.getInventory();
		if (!inventory.decreaseByObjectId(parentItem.getObjectId(), 1) || inventory.delete(targetItem) == null) {
			AuditLogger.log(player, "possibly using item AP extraction hack");
			return false;
		}
		int ap = (int) (acquisition.getRequiredAp() * rate);
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_AP_DECOMPOSE_ITEM_SUCCEED(targetItem.getL10n()));
		AbyssPointsService.addAp(player, ap, SM_SYSTEM_MESSAGE::STR_MSG_AP_DECOMPOSE_ITEM_SUCCEED_AP);
		return true;
	}

	public UseTarget getTarget() {
		return target;
	}

	public float getRate() {
		return rate;
	}
}
