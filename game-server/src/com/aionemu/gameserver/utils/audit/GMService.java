package com.aionemu.gameserver.utils.audit;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javolution.util.FastMap;
import javolution.util.FastTable;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureSeeState;
import com.aionemu.gameserver.model.gameobjects.state.CreatureVisualState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_STATE;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * @author MrPoke
 * @modified Neon
 */
public class GMService {

	public static final GMService getInstance() {
		return SingletonHolder.instance;
	}

	private Map<Integer, Player> gms = new FastMap<Integer, Player>();
	private boolean announceAny = false;
	private List<Byte> announceList;

	private GMService() {
		announceList = new FastTable<Byte>();
		announceAny = AdminConfig.ANNOUNCE_LEVEL_LIST.equals("*");
		if (!announceAny) {
			try {
				for (String level : AdminConfig.ANNOUNCE_LEVEL_LIST.split(","))
					if (!level.equals("0"))
						announceList.add(Byte.parseByte(level));
			} catch (Exception e) {
			}
		}
	}

	public Collection<Player> getAnnounceGMs() {
		return gms.values();
	}

	public void onPlayerLogin(Player player) {
		if (player.isGM()) {
			gms.put(player.getObjectId(), player);
			broadcastGMConnectionMessage(player, "login", "%s logged in.");

			String delimiter = "=============================";
			StringBuilder sb = new StringBuilder(delimiter);
			if (AdminConfig.INVULNERABLE_GM_CONNECTION) {
				player.setInvul(true);
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
				player.setUnWispable();
				sb.append("\n>> Accepting Whisper: OFF <<");
			}
			if (sb.length() > delimiter.length())
				PacketSendUtility.sendMessage(player, sb.append("\n" + delimiter).toString());
		}
	}

	public void onPlayerLogout(Player player) {
		if (gms.remove(player.getObjectId()) != null)
			broadcastGMConnectionMessage(player, "logout", "%s went offline.");
	}

	public void broadcastMessageToGMs(String message) {
		for (Player gm : getAnnounceGMs()) {
			PacketSendUtility.sendYellowMessage(gm, message);
		}
	}

	private void broadcastMessageToAllPlayers(String message) {
		for (Player player : World.getInstance().getAllPlayers()) {
			PacketSendUtility.sendYellowMessage(player, message);
		}
	}

	private void broadcastGMConnectionMessage(Player gm, String event, String format) {
		if (announceAny || announceList.contains(gm.getAccessLevel())) {
			String message = String.format(format, AdminConfig.CUSTOMTAG_ENABLE ? gm.getName(true) : "GM: " + gm.getName());

			if (("logout".equals(event) && AdminConfig.ANNOUNCE_LOGOUT_TO_ALL_PLAYERS)
				|| ("login".equals(event) && AdminConfig.ANNOUNCE_LOGIN_TO_ALL_PLAYERS))
				broadcastMessageToAllPlayers(message);
			else
				broadcastMessageToGMs(message);
		}
	}

	private static class SingletonHolder {

		protected static final GMService instance = new GMService();
	}
}
