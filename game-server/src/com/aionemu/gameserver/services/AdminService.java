package com.aionemu.gameserver.services;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javolution.util.FastTable;

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

	private final Logger log = LoggerFactory.getLogger(AdminService.class);
	private static final Logger itemLog = LoggerFactory.getLogger("GMITEMRESTRICTION");
	private FastTable<Integer> list;
	private static AdminService instance = new AdminService();

	public static AdminService getInstance() {
		return instance;
	}

	public AdminService() {
		list = new FastTable<>();
		if (AdminConfig.ENABLE_TRADEITEM_RESTRICTION)
			reload();
	}

	public void reload() {
		if (list.size() > 0)
			list.clear();

		try (BufferedReader br = new BufferedReader(new FileReader("./config/administration/item.restriction.txt"))) {
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("#") || line.trim().length() == 0)
					continue;

				String pt = line.split("#")[0].replaceAll(" ", "");
				list.add(Integer.parseInt(pt));
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		log.info("AdminService loaded " + list.size() + " operational items.");
	}

	public boolean canOperate(Player player, Player target, Item item, String type) {
		return canOperate(player, target, item.getItemId(), type);
	}

	public boolean canOperate(Player player, Player target, int itemId, String type) {
		if (!AdminConfig.ENABLE_TRADEITEM_RESTRICTION)
			return true;

		if (target != null && target.getAccessLevel() > 0) // allow between gms
			return true;

		if (player.getAccessLevel() > 0 && player.getAccessLevel() < 4) { // run check only for 1-3 level gms
			boolean value = list.contains(itemId);
			String str = "GM " + player.getName() + "|" + player.getObjectId() + " (" + type + "): " + itemId + "|result=" + value;
			if (target != null)
				str += "|target=" + target.getName() + "|" + target.getObjectId();
			itemLog.info(str);
			if (!value)
				PacketSendUtility.sendMessage(player, "You cannot use " + type + " with this item.");

			return value;
		}
		else
			return true;
	}
}
