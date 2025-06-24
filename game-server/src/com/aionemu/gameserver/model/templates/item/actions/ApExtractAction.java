package com.aionemu.gameserver.model.templates.item.actions;

import javax.xml.bind.annotation.XmlAttribute;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.storage.Storage;
import com.aionemu.gameserver.model.templates.item.Acquisition;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author Rolandas, Luzien
 */
public class ApExtractAction extends AbstractItemAction {

	@XmlAttribute
	protected UseTarget target;
	@XmlAttribute
	protected float rate;

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem, Object... params) {
		if (targetItem == null || !targetItem.canApExtract())
			return false;
		if (parentItem.getItemTemplate().getLevel() < targetItem.getItemTemplate().getLevel())
			return false;
		if (parentItem.getItemTemplate().getItemQuality() != targetItem.getItemTemplate().getItemQuality())
			return false;

		// TODO: ApExtractTarget.OTHER, ApExtractTarget.ALL. Find out what should go there

		UseTarget type = null;
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
			case NONE:
				if (targetItem.getItemTemplate().getItemGroup() == ItemGroup.WING) {
					type = UseTarget.WING;
					break;
				}
				return false;
			default:
				return false;
		}
		return (target == UseTarget.EQUIPMENT || target == type);
	}

	@Override
	public void act(Player player, Item parentItem, Item targetItem, Object... params) {
		Acquisition acquisition = targetItem.getItemTemplate().getAcquisition();
		if (acquisition == null || acquisition.getRequiredAp() == 0)
			return;
		int ap = (int) (acquisition.getRequiredAp() * rate);
		Storage inventory = player.getInventory();

		if (inventory.delete(targetItem) != null) {
			if (inventory.decreaseByObjectId(parentItem.getObjectId(), 1))
				AbyssPointsService.addAp(player, ap);
		} else
			AuditLogger.log(player, "possibly using item AP extraction hack");
	}

	public UseTarget getTarget() {
		return target;
	}

	public float getRate() {
		return rate;
	}
}
