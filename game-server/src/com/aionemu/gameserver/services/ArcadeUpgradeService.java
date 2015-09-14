package com.aionemu.gameserver.services;


import java.util.ArrayList;
import java.util.List;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.EventsConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.storage.Storage;
import com.aionemu.gameserver.model.templates.arcadeupgrade.ArcadeTab;
import com.aionemu.gameserver.model.templates.arcadeupgrade.ArcadeTabItemList;
import com.aionemu.gameserver.network.aion.serverpackets.SM_UPGRADE_ARCADE;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ginho1
 */
public class ArcadeUpgradeService {

	public static final ArcadeUpgradeService getInstance() {
		return SingletonHolder.instance;
	}
	
	private int[] tabReward = new int[4];

	public ArcadeUpgradeService() {
		tabReward[0] = 0;
		tabReward[1] = 4;
		tabReward[2] = 6;
		tabReward[3] = 8;
	}

	public int getRewardTabForLevel(int level) {
		int tab = 1;

		for (int i = tabReward.length; i > 0; i--) {
			if (level >= tabReward[(i - 1)]) {
				tab = i;
				break;
			}
		}
		if (tab > tabReward.length)
			return tabReward.length;
		return tab;
	}

	public void startArcadeUpgrade(Player player) {
		PacketSendUtility.sendPacket(player, new SM_UPGRADE_ARCADE());
	}
	
	public void openArcadeUpgrade(Player player) {
		PacketSendUtility.sendPacket(player, new SM_UPGRADE_ARCADE(2));
	}

	public void showRewardList(Player player) {
		PacketSendUtility.sendPacket(player, new SM_UPGRADE_ARCADE(10));
	}

	public List<ArcadeTab> getTabs() {
		return DataManager.ARCADE_UPGRADE_DATA.getArcadeTabs();
	}
	
	public void tryArcadeUpgrade(Player player) {
		if(!EventsConfig.ENABLE_EVENT_ARCADE)
			return;
		
		if(player.getArcadeUpgradeLevel() >= 8)
			return;

		if(player.getArcadeUpgradeLevel() == 1){
			Storage inventory = player.getInventory();

			if(!inventory.decreaseByItemId(186000389, 1))
				return;

			if(!player.getArcadeUpgradeIsFrenzy()){
				player.setArcadeUpgradeFrenzy(player.getArcadeUpgradeFrenzy() + 8);

				if(player.getArcadeUpgradeFrenzy() >= 100){
					PacketSendUtility.sendPacket(player, new SM_UPGRADE_ARCADE(7, 90));
					player.setArcadeUpgradeIsFrenzy(true);
					player.setArcadeUpgradeFrenzy(8);

					ThreadPoolManager.getInstance().schedule(new Runnable() {
						@Override
						public void run() {
							PacketSendUtility.sendPacket(player, new SM_UPGRADE_ARCADE(7, 0));
							player.setArcadeUpgradeIsFrenzy(false);
						}
					}, 90000);
				}
			}
		}

		if (Rnd.get(1, 100) <= EventsConfig.EVENT_ARCADE_CHANCE) {
			PacketSendUtility.sendPacket(player, new SM_UPGRADE_ARCADE(3, true, player.getArcadeUpgradeFrenzy()));
			player.setArcadeUpgradeLevel(player.getArcadeUpgradeLevel() + 1);
			PacketSendUtility.sendPacket(player, new SM_UPGRADE_ARCADE(4, player.getArcadeUpgradeLevel()));
		}else{
			PacketSendUtility.sendPacket(player, new SM_UPGRADE_ARCADE(3, false, player.getArcadeUpgradeFrenzy()));
			PacketSendUtility.sendPacket(player, new SM_UPGRADE_ARCADE(5));
			player.setArcadeUpgradeLevel(1);
		}
	}

	public void getReward(Player player) {
		if(!EventsConfig.ENABLE_EVENT_ARCADE)
			return;

		int rewardTab = getRewardTabForLevel(player.getArcadeUpgradeLevel());

		List<ArcadeTabItemList> rewardList = new ArrayList<ArcadeTabItemList>();

		for (ArcadeTab arcadetab : getTabs()){
			if(rewardTab == arcadetab.getId()){
				for (ArcadeTabItemList arcadetabitem : arcadetab.getArcadeTabItems()){
					if(player.getArcadeUpgradeIsFrenzy()){
						if(arcadetabitem.getFrenzyCount() > 0){
							rewardList.add(arcadetabitem);
						}
					}else{
						if(arcadetabitem.getNormalCount() > 0){
							rewardList.add(arcadetabitem);
						}
					}
				}
			}
		}

		if(rewardList.size() > 0){
			int index = Rnd.get(0, rewardList.size() - 1);
			ArcadeTabItemList item = rewardList.get(index);
			ItemService.addItem(player, item.getItemId(), player.getArcadeUpgradeIsFrenzy() ? item.getFrenzyCount(): item.getNormalCount());
			PacketSendUtility.sendPacket(player, new SM_UPGRADE_ARCADE(6, item));
			player.setArcadeUpgradeLevel(1);
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final ArcadeUpgradeService instance = new ArcadeUpgradeService();
	}
}