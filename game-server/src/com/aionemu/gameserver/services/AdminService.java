package com.aionemu.gameserver.services;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author KID
 */
public class AdminService {

	private static final Logger itemLog = LoggerFactory.getLogger("GMITEMRESTRICTION");
	private List<Integer> list;
	private static AdminService instance = new AdminService();

	public static AdminService getInstance() {
		return instance;
	}

	public AdminService() {
		list = new ArrayList<>();
		reload();
	}

	public void reload() {
		list.clear();

		try (BufferedReader br = new BufferedReader(new FileReader("./config/administration/item.restriction.txt"))) {
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("#") || line.trim().length() == 0)
					continue;

				String pt = line.split("#")[0].replaceAll(" ", "");
				list.add(Integer.parseInt(pt));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		LoggerFactory.getLogger(AdminService.class).info("AdminService loaded " + list.size() + " operational items.");
	}

	public boolean canOperate(Player player, Player target, Item item, String type) {
		return canOperate(player, target, item.getItemId(), type);
	}

	public boolean canOperate(Player player, Player target, int itemId, String type) {
		if (!player.isStaff())
			return true;

		if (player.hasAccess(AdminConfig.UNRESTRICTED_ITEMTRADE)) // staff member is allowed to trade with who ever he wants to
			return true;

		if (target != null && target.isStaff()) // allow between server staff
			return true;

		if (list.contains(itemId)) { // item goes from staff member to normal player, so log it
			itemLog.info(player + " traded item " + itemId + " via " + type + (target != null ? " to player " + target : ""));
			return true;
		}

		PacketSendUtility.sendMessage(player, "You cannot use " + type + " with this item.");
		return false;
	}
}
