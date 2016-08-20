package com.aionemu.gameserver.utils.audit;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javolution.util.FastSet;

import com.aionemu.commons.objects.filter.ObjectFilter;
import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.model.gameobjects.player.FriendList.Status;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.gameobjects.player.CustomPlayerState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureSeeState;
import com.aionemu.gameserver.model.gameobjects.state.CreatureVisualState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_STATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author MrPoke
 * @modified Neon
 */
public class GMService {

	public static final GMService getInstance() {
		return SingletonHolder.instance;
	}

	private Map<Integer, Player> gms = new ConcurrentHashMap<>();
	private Set<Byte> announceList;

	private GMService() {
		if (AdminConfig.ANNOUNCE_LEVEL_LIST.contains("*")) {
			announceList = null;
		} else {
			announceList = new FastSet<>();
			for (String level : AdminConfig.ANNOUNCE_LEVEL_LIST.split(","))
				announceList.add(Byte.parseByte(level));
		}
	}

	public Collection<Player> getGms() {
		return gms.values();
	}

	public void onPlayerLogin(Player player) {
		if (player.isGM()) {
			String delimiter = "=============================";
			StringBuilder sb = new StringBuilder(delimiter);
			if (AdminConfig.INVULNERABLE_GM_CONNECTION) {
				player.setCustomState(CustomPlayerState.INVULNERABLE);
				sb.append("\n>> Connection in Invulnerable mode <<");
			}
			if (AdminConfig.INVISIBLE_GM_CONNECTION) {
				player.getEffectController().setAbnormal(AbnormalState.HIDE.getId());
				player.setVisualState(CreatureVisualState.HIDE20);
				PacketSendUtility.broadcastPacket(player, new SM_PLAYER_STATE(player), true);
				sb.append("\n>> Connection in Invisible mode <<");
			}
			if (AdminConfig.ENEMITY_MODE_GM_CONNECTION.equalsIgnoreCase("Neutral")) {
				player.setAdminNeutral(3);
				player.setAdminEnmity(0);
				sb.append("\n>> Connection in Neutral mode <<");
			}
			if (AdminConfig.ENEMITY_MODE_GM_CONNECTION.equalsIgnoreCase("Enemy")) {
				player.setAdminNeutral(0);
				player.setAdminEnmity(3);
				sb.append("\n>> Connection in Enemy mode <<");
			}
			if (AdminConfig.VISION_GM_CONNECTION) {
				player.setSeeState(CreatureSeeState.SEARCH10);
				PacketSendUtility.broadcastPacket(player, new SM_PLAYER_STATE(player), true);
				sb.append("\n>> Connection in Vision mode <<");
			}
			if (AdminConfig.WHISPER_GM_CONNECTION) {
				player.setCustomState(CustomPlayerState.NO_WHISPERS_MODE);
				sb.append("\n>> Accepting Whisper: OFF <<");
			}
			if (sb.length() > delimiter.length())
				PacketSendUtility.sendMessage(player, sb.append("\n" + delimiter).toString());

			gms.put(player.getObjectId(), player);
			scheduleBroadcastLogin(player);
		}
	}

	public void onPlayerLogout(Player player) {
		if (gms.remove(player.getObjectId()) != null && isAnnounceable(player))
			broadcastConnectionStatus(player, false);
	}

	public boolean isAnnounceable(Player gm) {
		return gm.isGM() && !gm.isInCustomState(CustomPlayerState.NO_WHISPERS_MODE) && gm.getFriendList().getStatus() != Status.OFFLINE && (announceList == null || announceList.contains(gm.getAccessLevel()));
	}

	public void broadcastMessageToGMs(String message) {
		for (Player gm : getGms()) {
			PacketSendUtility.sendMessage(gm, message, ChatType.YELLOW);
		}
	}

	private void broadcastConnectionStatus(Player gm, boolean connected) {
		String name = (AdminConfig.CUSTOMTAG_ENABLE ? "" : "GM: ") + ChatUtil.name(gm);
		SM_SYSTEM_MESSAGE sysMsg = connected ? SM_SYSTEM_MESSAGE.STR_NOTIFY_LOGIN_BUDDY(name) : SM_SYSTEM_MESSAGE.STR_NOTIFY_LOGOFF_BUDDY(name);

		if ((connected && AdminConfig.ANNOUNCE_LOGIN_TO_ALL_PLAYERS) || (!connected && AdminConfig.ANNOUNCE_LOGOUT_TO_ALL_PLAYERS)) {
			PacketSendUtility.broadcastFilteredPacket(sysMsg, new ObjectFilter<Player>() {

				@Override
				public boolean acceptObject(Player player) {
					return !player.equals(gm);
				}
			});
		} else {
			PacketSendUtility.broadcastFilteredPacket(sysMsg, new ObjectFilter<Player>() {

				@Override
				public boolean acceptObject(Player player) {
					return player.isGM() && !player.equals(gm);
				}
			});
		}
	}

	private void scheduleBroadcastLogin(Player gm) {
		if (!isAnnounceable(gm))
			return;

		byte delay = 15;
		PacketSendUtility.sendMessage(gm, "Your login will be announced in " + delay + "s.\nYou can disable this by setting whisper off or changing your online status to invisible.");
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (isAnnounceable(gm)) {
					broadcastConnectionStatus(gm, true);
					PacketSendUtility.sendMessage(gm, "Your login has been announced.");
				} else {
					PacketSendUtility.sendMessage(gm, "Your login has not been announced.");
				}
			}
		}, delay * 1000);
	}

	private static class SingletonHolder {

		protected static final GMService instance = new GMService();
	}
}
