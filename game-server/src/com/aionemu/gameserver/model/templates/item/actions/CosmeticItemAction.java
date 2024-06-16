package com.aionemu.gameserver.model.templates.item.actions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dao.PlayerAppearanceDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.actions.PlayerMode;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerAppearance;
import com.aionemu.gameserver.model.templates.cosmeticitems.CosmeticItemTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CosmeticItemAction")
public class CosmeticItemAction extends AbstractItemAction {

	@XmlAttribute(name = "name")
	private String cosmeticName;

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem, Object... params) {
		CosmeticItemTemplate template = DataManager.COSMETIC_ITEMS_DATA.getCosmeticItemsTemplate(cosmeticName);
		if (template == null) {
			return false;
		}
		if (!template.getRace().equals(player.getRace())) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANNOT_USE_ITEM_INVALID_RACE());
			return false;
		}
		if (!template.getGenderPermitted().equals("ALL")) {
			if (!player.getGender().toString().equals(template.getGenderPermitted())) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANNOT_USE_ITEM_INVALID_GENDER());
				return false;
			}
		}
		if (player.isInPlayerMode(PlayerMode.RIDE)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ITEM_RESTRICTION_RIDE());
			return false;
		}
		return true;
	}

	@Override
	public void act(final Player player, Item parentItem, Item targetItem, Object... params) {
		CosmeticItemTemplate template = DataManager.COSMETIC_ITEMS_DATA.getCosmeticItemsTemplate(cosmeticName);
		PlayerAppearance playerAppearance = player.getPlayerAppearance();
		String type = template.getType();
		int id = template.getId();
		switch (type) {
			case "hair_color" -> playerAppearance.setHairRGB(id);
			case "face_color" -> playerAppearance.setSkinRGB(id);
			case "lip_color" -> playerAppearance.setLipRGB(id);
			case "eye_color" -> playerAppearance.setEyeRGB(id);
			case "hair_type" -> playerAppearance.setHair(id);
			case "face_type" -> playerAppearance.setFace(id);
			case "voice_type" -> playerAppearance.setVoice(id);
			case "makeup_type" -> playerAppearance.setTattoo(id);
			case "tattoo_type" -> playerAppearance.setDeco(id);
			case "preset_name" -> {
				CosmeticItemTemplate.Preset preset = template.getPreset();
				playerAppearance.setEyeRGB(preset.getEyeColor());
				playerAppearance.setLipRGB(preset.getLipColor());
				playerAppearance.setHairRGB(preset.getHairColor());
				playerAppearance.setSkinRGB(preset.getEyeColor());
				playerAppearance.setHair(preset.getHairType());
				playerAppearance.setFace(preset.getFaceType());
				playerAppearance.setHeight(preset.getScale());
				player.getAccountData().updateBoundingRadius();
			}
			default -> {
				LoggerFactory.getLogger(getClass()).warn("Unhandled cosmetic item type: " + type);
				return;
			}
		}
		PlayerAppearanceDAO.store(player);
		player.getInventory().delete(targetItem);
		player.getController().onChangedPlayerAttributes();
	}
}
