package com.aionemu.gameserver.utils.audit;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.CustomPlayerState;
import com.aionemu.gameserver.model.gameobjects.player.FriendList.Status;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.SkillLearnService;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.chathandlers.ChatProcessor;

/**
 * @author MrPoke, Neon
 */
public class GMService {

	public static GMService getInstance() {
		return SingletonHolder.instance;
	}

	private final Map<Integer, Player> staffMembers = new ConcurrentHashMap<>();
	private final List<SkillTemplate> gmSkills;

	private GMService() {
		gmSkills = DataManager.SKILL_DATA.getSkillTemplates().stream()
			.filter(t -> t.getGroup() != null && t.getGroup().startsWith("GM_") || t.getStack().startsWith("GM_")).collect(Collectors.toList());
		if (gmSkills.isEmpty())
			LoggerFactory.getLogger(GMService.class).warn("No GM skills found, possibly because of changed or missing skill templates.");
	}

	public Collection<Player> getOnlineStaffMembers() {
		return staffMembers.values();
	}

	public void onPlayerLogin(Player player) {
		if (player.isStaff()) {
			AdminConfig.LOGIN_EXECUTE_COMMANDS.forEach(cmd -> ChatProcessor.getInstance().handleChatCommand(player, cmd));
			staffMembers.put(player.getObjectId(), player);
			scheduleBroadcastLogin(player);
		}
	}

	public void onPlayerLogout(Player player) {
		if (staffMembers.remove(player.getObjectId()) != null && isAnnounceable(player))
			broadcastConnectionStatus(player, false);
	}

	public boolean isAnnounceable(Player player) {
		return player.isOnline() && player.isStaff() && !player.isInCustomState(CustomPlayerState.NO_WHISPERS_MODE)
			&& player.getFriendList().getStatus() != Status.OFFLINE
			&& (AdminConfig.ANNOUNCE_LEVELS.contains(String.valueOf(player.getAccount().getAccessLevel())) || AdminConfig.ANNOUNCE_LEVELS.contains("*"));
	}

	private void broadcastConnectionStatus(Player gm, boolean connected) {
		String name = ChatUtil.name(gm);
		SM_SYSTEM_MESSAGE sysMsg = connected ? SM_SYSTEM_MESSAGE.STR_NOTIFY_LOGIN_BUDDY(name) : SM_SYSTEM_MESSAGE.STR_NOTIFY_LOGOFF_BUDDY(name);

		if ((connected && AdminConfig.ANNOUNCE_LOGIN_TO_ALL_PLAYERS) || (!connected && AdminConfig.ANNOUNCE_LOGOUT_TO_ALL_PLAYERS)) {
			PacketSendUtility.broadcastToWorld(sysMsg, p -> !p.equals(gm));
		} else {
			PacketSendUtility.broadcastToWorld(sysMsg, p -> p.isStaff() && !p.equals(gm));
		}
	}

	private void scheduleBroadcastLogin(Player gm) {
		if (!isAnnounceable(gm))
			return;

		byte delay = 15;
		PacketSendUtility.sendMessage(gm,
			"Your login will be announced in " + delay + "s.\nYou can disable this by setting whisper off or changing your online status to invisible.");
		ThreadPoolManager.getInstance().schedule(() -> {
			if (isAnnounceable(gm)) {
				broadcastConnectionStatus(gm, true);
				PacketSendUtility.sendMessage(gm, "Your login has been announced.");
			} else {
				PacketSendUtility.sendMessage(gm, "Your login has not been announced.");
			}
		}, delay * 1000);
	}

	public void addGmSkills(Player player) {
		for (SkillTemplate t : gmSkills) {
			switch (t.getSkillId()) {
				case 322: // [Event] Manastone Preservation
				case 323: // Homerun Energy
				case 339: // Panesterra Dominant
					continue;
			}
			if (player.getRace() == Race.ASMODIANS && t.getStack().contains("_LIGHT"))
				continue;
			if (player.getRace() == Race.ELYOS && t.getStack().contains("_DARK"))
				continue;
			SkillLearnService.learnTemporarySkill(player, t.getSkillId(), t.getLvl());
		}
	}

	private static class SingletonHolder {

		protected static final GMService instance = new GMService();
	}
}
