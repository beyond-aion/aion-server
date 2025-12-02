package com.aionemu.gameserver.services;

import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.LegionConfig;
import com.aionemu.gameserver.dao.InventoryDAO;
import com.aionemu.gameserver.dao.ItemStoneListDAO;
import com.aionemu.gameserver.dao.LegionDAO;
import com.aionemu.gameserver.dao.LegionMemberDAO;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.DialogPage;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.DeniedStatus;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.model.gameobjects.player.RequestResponseHandler;
import com.aionemu.gameserver.model.items.storage.IStorage;
import com.aionemu.gameserver.model.items.storage.StorageType;
import com.aionemu.gameserver.model.team.legion.*;
import com.aionemu.gameserver.network.aion.serverpackets.*;
import com.aionemu.gameserver.services.conquerorAndProtectorSystem.ConquerorAndProtectorService;
import com.aionemu.gameserver.services.player.PlayerService;
import com.aionemu.gameserver.services.trade.PricesService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.audit.AuditLogger;
import com.aionemu.gameserver.utils.collections.FixedElementCountSplitList;
import com.aionemu.gameserver.utils.collections.SplitList;
import com.aionemu.gameserver.utils.idfactory.IDFactory;
import com.aionemu.gameserver.world.World;

/**
 * This class is designed to do all the work related with loading/storing legions and their members.
 *
 * @author Simple, cura, Source, Neon
 */
public class LegionService {

	private static final Logger log = LoggerFactory.getLogger(LegionService.class);
	private final Map<Integer, Legion> legionsById = new ConcurrentHashMap<>();
	private final Map<Integer, LegionMember> legionMemberById = new ConcurrentHashMap<>();
	private static final int MAX_LEGION_LEVEL = 8;

	private LegionRestrictions legionRestrictions = new LegionRestrictions();

	public static LegionService getInstance() {
		return SingletonHolder.instance;
	}

	private LegionService() {
	}

	private void storeLegion(Legion legion, boolean newLegion) {
		if (newLegion) {
			addCachedLegion(legion);
			LegionDAO.saveNewLegion(legion);
		} else {
			LegionDAO.storeLegion(legion);
			LegionDAO.storeLegionEmblem(legion.getLegionId(), legion.getLegionEmblem());
		}
	}

	private void storeLegion(Legion legion) {
		storeLegion(legion, false);
	}

	public void storeLegionMember(LegionMember legionMember) {
		LegionMemberDAO.storeLegionMember(legionMember);
	}

	public Collection<Legion> getCachedLegions() {
		return legionsById.values();
	}

	private void addCachedLegion(Legion legion) {
		legionsById.put(legion.getLegionId(), legion);
	}

	public static void deleteLegionFromDB(int legionId) {
		LegionDAO.deleteLegion(legionId);
		InventoryDAO.deletePlayerOrLegionItems(legionId);
	}

	/**
	 * This method will remove the legion member from cache and the database
	 */
	private void deleteLegionMemberFromDB(LegionMember legionMember) {
		legionMemberById.remove(legionMember.getObjectId());
		LegionMemberDAO.deleteLegionMember(legionMember.getObjectId());
		Legion legion = legionMember.getLegion();
		legion.removeMember(legionMember.getObjectId());
		addHistory(legion, legionMember.getName(), LegionHistoryAction.KICK);
	}

	public Legion getLegion(String legionName) {
		Legion legion = legionsById.values().stream().filter(l -> l.getName().equalsIgnoreCase(legionName)).findAny().orElse(null);
		if (legion == null) {
			legion = LegionDAO.loadLegion(legionName);
			if (legion == null || checkDisband(legion))
				return null;
			loadLegionInfo(legion);
			addCachedLegion(legion);
		} else if (checkDisband(legion)) {
			return null;
		}
		return legion;
	}

	public Legion getLegion(int legionId) {
		Legion legion = legionsById.get(legionId);
		if (legion == null) {
			legion = LegionDAO.loadLegion(legionId);
			if (legion == null || checkDisband(legion))
				return null;
			loadLegionInfo(legion);
			addCachedLegion(legion);
		} else if (checkDisband(legion)) {
			return null;
		}
		return legion;
	}

	private void loadLegionInfo(Legion legion) {
		legion.setMemberIds(LegionMemberDAO.loadLegionMembers(legion.getLegionId()));
		legion.setAnnouncement(LegionDAO.loadAnnouncement(legion.getLegionId()));
		legion.setLegionEmblem(LegionDAO.loadLegionEmblem(legion.getLegionId()));
		InventoryDAO.loadStorage(legion.getLegionId(), legion.getLegionWarehouse());
		ItemStoneListDAO.load(legion.getLegionWarehouse().getItems());
		LegionDAO.loadHistory(legion);
	}

	private LegionMember getLegionMember(String name) {
		PlayerCommonData playerCommonData = PlayerService.getOrLoadPlayerCommonData(name);
		return playerCommonData == null ? null : getLegionMember(playerCommonData);
	}

	public LegionMember getLegionMember(int playerObjId) {
		return getLegionMember(playerObjId, null);
	}

	public LegionMember getLegionMember(PlayerCommonData playerCommonData) {
		return getLegionMember(playerCommonData.getPlayerObjId(), playerCommonData);
	}

	private LegionMember getLegionMember(int playerObjectId, PlayerCommonData playerCommonData) {
		LegionMember legionMember = legionMemberById.computeIfAbsent(playerObjectId, _ -> {
			LegionMember lm = LegionMemberDAO.loadLegionMember(playerObjectId);
			if (lm != null)
				lm.setPlayerData(playerCommonData == null ? PlayerService.getOrLoadPlayerCommonData(playerObjectId) : playerCommonData);
			return lm;
		});
		return legionMember == null || checkDisband(legionMember.getLegion()) ? null : legionMember;
	}

	/**
	 * Method that checks if a legion is disbanding
	 *
	 * @return true if it's time to be deleted
	 */
	private boolean checkDisband(Legion legion) {
		if (legion.isDisbanding()) {
			if ((System.currentTimeMillis() / 1000) > legion.getDisbandTime()) {
				disbandLegion(legion);
				return true;
			}
		}
		return false;
	}

	/**
	 * This method will disband a legion and update all members
	 */
	public void disbandLegion(Legion legion) {
		legionsById.remove(legion.getLegionId());
		legion.getMemberIds().forEach(legionMemberById::remove);
		SiegeService.getInstance().cleanLegionId(legion.getLegionId());
		deleteLegionFromDB(legion.getLegionId());
		updateAfterDisbandLegion(legion);
	}

	public void requestDisbandLegion(Npc npc, Player activePlayer) {
		if (legionRestrictions.canDisbandLegion(activePlayer)) {
			RequestResponseHandler<Npc> disbandResponseHandler = new RequestResponseHandler<>(npc) {

				@Override
				public void acceptRequest(Npc requester, Player responder) {
					Legion legion = responder.getLegion();
					int unixTime = (int) ((System.currentTimeMillis() / 1000) + LegionConfig.LEGION_DISBAND_TIME);
					legion.setDisbandTime(unixTime);
					updateMembersOfDisbandLegion(legion, unixTime);
				}
			};

			boolean disbandResult = activePlayer.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_GUILD_DISPERSE_STAYMODE, disbandResponseHandler);
			if (disbandResult) {
				PacketSendUtility.sendPacket(activePlayer, new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_GUILD_DISPERSE_STAYMODE, 0, 0));
			}
		}
	}

	public void createLegion(Player activePlayer, String legionName) {
		if (legionRestrictions.canCreateLegion(activePlayer, legionName)) {
			Legion legion = new Legion(IDFactory.getInstance().nextId(), legionName);
			legion.addLegionMember(activePlayer.getObjectId());

			activePlayer.getInventory().decreaseKinah(LegionConfig.LEGION_CREATE_REQUIRED_KINAH);

			storeLegion(legion, true);
			addLegionMember(legion, activePlayer, LegionRank.BRIGADE_GENERAL);
			addHistory(legion, "", LegionHistoryAction.CREATE);
			addHistory(legion, activePlayer.getName(), LegionHistoryAction.JOIN);

			PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_CREATED(legion.getName()));
		}
	}

	public boolean addToLegion(Legion legion, Player invited, Player inviter) {
		int playerObjId = invited.getObjectId();
		if (legion.addLegionMember(playerObjId)) {
			// Bind LegionMember to Player
			addLegionMember(legion, invited);

			// Display current announcement
			displayLegionAnnouncement(invited, legion.getAnnouncement());

			// Add to history of legion
			addHistory(legion, invited.getName(), LegionHistoryAction.JOIN);
			return true;
		}
		PacketSendUtility.sendPacket(inviter, SM_SYSTEM_MESSAGE.STR_GUILD_INVITE_CAN_NOT_ADD_MEMBER_ANY_MORE());
		return false;
	}

	public void invitePlayerToLegion(Player activePlayer, String targetName) {
		Player targetPlayer = World.getInstance().getPlayer(targetName);
		if (legionRestrictions.canInvitePlayer(activePlayer, targetPlayer)) {
			Legion legion = activePlayer.getLegion();
			RequestResponseHandler<Player> responseHandler = new RequestResponseHandler<>(activePlayer) {

				@Override
				public void acceptRequest(Player requester, Player responder) {
					addToLegion(legion, responder, requester);
				}

				@Override
				public void denyRequest(Player requester, Player responder) {
					PacketSendUtility.sendPacket(requester, SM_SYSTEM_MESSAGE.STR_GUILD_INVITE_HE_REJECTED_INVITATION(responder.getName()));
				}
			};

			boolean requested = targetPlayer.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_GUILD_INVITE_DO_YOU_ACCEPT_INVITATION,
				responseHandler);
			// If the player is busy and could not be asked
			if (!requested) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_INVITE_OTHER_IS_BUSY());
			} else {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_INVITE_SENT_INVITE_MSG_TO_HIM(targetPlayer.getName()));

				// Send question packet to buddy
				PacketSendUtility.sendPacket(targetPlayer, new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_GUILD_INVITE_DO_YOU_ACCEPT_INVITATION, 0, 0,
					legion.getName(), legion.getLegionLevel() + "", activePlayer.getName()));
			}
		}
	}

	/**
	 * Displays current legion announcement
	 */
	private void displayLegionAnnouncement(Player targetPlayer, Legion.Announcement announcement) {
		if (announcement != null)
			PacketSendUtility.sendPacket(targetPlayer, SM_SYSTEM_MESSAGE.STR_GUILD_NOTICE(announcement.message(), announcement.time().getTime() / 1000));
	}

	public void startBrigadeGeneralChangeProcess(Player legionLeader, String memberName) {
		Player newLegionLeader = World.getInstance().getPlayer(memberName);
		if (newLegionLeader == null) {
			PacketSendUtility.sendPacket(legionLeader, SM_SYSTEM_MESSAGE.STR_GUILD_CHANGE_MASTER_NO_SUCH_USER());
			return;
		} 
		RequestResponseHandler<Player> responseHandler = new RequestResponseHandler<>(newLegionLeader) {

			@Override
			public void acceptRequest(Player newBrigadeGeneral, Player responder) {
				appointBrigadeGeneral(responder, newBrigadeGeneral);
			}
		};
		boolean requested = legionLeader.getResponseRequester().putRequest(904979, responseHandler);
		if (requested) {
			PacketSendUtility.sendPacket(legionLeader, new SM_QUESTION_WINDOW(904979, 0, 0, newLegionLeader.getName()));
		}
	}

	private void appointBrigadeGeneral(final Player activePlayer, final Player targetPlayer) {
		if (legionRestrictions.canAppointBrigadeGeneral(activePlayer, targetPlayer)) {
			RequestResponseHandler<Player> responseHandler = new RequestResponseHandler<>(activePlayer) {

				@Override
				public void acceptRequest(Player requester, Player responder) {
					if (!responder.isOnline()) {
						PacketSendUtility.sendPacket(requester, SM_SYSTEM_MESSAGE.STR_GUILD_CHANGE_MASTER_NO_SUCH_USER());
					} else if (!legionRestrictions.canAppointBrigadeGeneral(requester, responder)) {
						AuditLogger.log(requester, "possibly tried to exploit legion leadership transfer");
					} else {
						appointBrigadeGeneral(responder.getLegionMember());
					}
				}

				@Override
				public void denyRequest(Player requester, Player responder) {
					PacketSendUtility.sendPacket(requester, SM_SYSTEM_MESSAGE.STR_GUILD_CHANGE_MASTER_HE_DECLINE_YOUR_OFFER(responder.getName()));
				}
			};

			boolean requested = targetPlayer.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_GUILD_CHANGE_MASTER_DO_YOU_ACCEPT_OFFER,
				responseHandler);
			// If the player is busy and could not be asked
			if (!requested) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_CHANGE_MASTER_SENT_CANT_OFFER_WHEN_HE_IS_QUESTION_ASKED());
			} else {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_CHANGE_MASTER_SENT_OFFER_MSG_TO_HIM(targetPlayer.getName()));

				// Send question packet to buddy
				// TODO: Add char name parameter? Doesn't work?
				PacketSendUtility.sendPacket(targetPlayer, new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_GUILD_CHANGE_MASTER_DO_YOU_ACCEPT_OFFER,
					activePlayer.getObjectId(), 0, activePlayer.getName()));
			}
		}
	}

	public void appointBrigadeGeneral(LegionMember member) {
		if (member.isBrigadeGeneral())
			return;
		Legion legion = member.getLegion();
		LegionMember prevBrigadeGeneral = legion.getBrigadeGeneral();
		prevBrigadeGeneral.setRank(LegionRank.CENTURION);
		if (!prevBrigadeGeneral.isOnline())
			LegionMemberDAO.storeLegionMember(prevBrigadeGeneral);
		PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_UPDATE_MEMBER(prevBrigadeGeneral));
		member.setRank(LegionRank.BRIGADE_GENERAL);
		PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_UPDATE_MEMBER(member, 1300273, member.getName()));
		PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_EDIT(0x08));
		addHistory(legion, member.getName(), LegionHistoryAction.APPOINTED);
	}

	/**
	 * This method will handle the process when a member is demoted or promoted.
	 */
	public void appointRank(Player player, String charName, int rankId) {
		LegionMember legionMember = getLegionMember(charName);
		if (legionRestrictions.canAppointRank(player, legionMember)) {
			LegionRank rank = LegionRank.values()[rankId];
			int msgId = switch (rank) {
				case DEPUTY -> 1400902;
				case LEGIONARY -> 1300268;
				case CENTURION -> 1300267;
				case VOLUNTEER -> 1400903;
				default -> 0;
			};
			legionMember.setRank(rank);
			if (!legionMember.isOnline())
				LegionMemberDAO.storeLegionMember(legionMember);
			PacketSendUtility.broadcastToLegion(legionMember.getLegion(), new SM_LEGION_UPDATE_MEMBER(legionMember, msgId, legionMember.getName()));
		}
	}

	public void changeSelfIntro(Player activePlayer, String newSelfIntro) {
		if (legionRestrictions.canChangeSelfIntro(activePlayer, newSelfIntro)) {
			LegionMember legionMember = activePlayer.getLegionMember();
			legionMember.setSelfIntro(newSelfIntro);
			PacketSendUtility.broadcastToLegion(legionMember.getLegion(), new SM_LEGION_UPDATE_SELF_INTRO(activePlayer.getObjectId(), newSelfIntro));
			PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_WRITE_INTRO_DONE());
		}
	}

	public void changePermissions(Player player, short deputyPermission, short centurionPermission, short legionarPermission,
		short volunteerPermission) {
		LegionMember legionMember = player.getLegionMember();
		if (legionMember == null || !legionMember.isBrigadeGeneral()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GUILD_CHANGE_RIGHT_DONT_HAVE_RIGHT());
			return;
		}
		legionMember.getLegion().setLegionPermissions(deputyPermission, centurionPermission, legionarPermission, volunteerPermission);
		PacketSendUtility.broadcastToLegion(legionMember.getLegion(), new SM_LEGION_EDIT(0x02, legionMember.getLegion()));
	}

	/**
	 * This method will handle the leveling up of a legion
	 */
	public void requestChangeLevel(Player activePlayer) {
		if (legionRestrictions.canChangeLevel(activePlayer)) {
			Legion legion = activePlayer.getLegion();
			activePlayer.getInventory().decreaseKinah(legion.getKinahPrice());
			changeLevel(legion, legion.getLegionLevel() + 1, false);
			addHistory(legion, legion.getLegionLevel() + "", LegionHistoryAction.LEVEL_UP);
		}
	}

	/**
	 * This method will change the legion level and send update to online members
	 */
	public void changeLevel(Legion legion, int newLevel, boolean save) {
		legion.setLegionLevel(newLevel);
		PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_EDIT(0x00, legion));
		PacketSendUtility.broadcastToLegion(legion, SM_SYSTEM_MESSAGE.STR_GUILD_EVENT_LEVELUP(newLevel));
		if (save)
			storeLegion(legion);
	}

	public void changeNickname(Player activePlayer, String memberName, String newNickname) {
		LegionMember legionMember = getLegionMember(memberName);
		if (legionRestrictions.canChangeNickname(activePlayer, legionMember, memberName, newNickname)) {
			legionMember.setNickname(newNickname);
			PacketSendUtility.broadcastToLegion(legionMember.getLegion(), new SM_LEGION_UPDATE_NICKNAME(legionMember.getObjectId(), newNickname));
			if (!legionMember.isOnline())
				LegionMemberDAO.storeLegionMember(legionMember);
		}
	}

	/**
	 * This method will remove legion from all legion members online after a legion has been disbanded
	 */
	private void updateAfterDisbandLegion(Legion legion) {
		for (Player onlineLegionMember : legion.getOnlinePlayers()) {
			PacketSendUtility.broadcastPacket(onlineLegionMember,
				new SM_LEGION_UPDATE_TITLE(onlineLegionMember.getObjectId(), 0, "", onlineLegionMember.getLegionMember().getRank()), true);
			PacketSendUtility.sendPacket(onlineLegionMember, new SM_LEGION_LEAVE_MEMBER(1300302, 0, legion.getName()));
			onlineLegionMember.resetLegionMember();
			ConquerorAndProtectorService.getInstance().onLeaveLegion(onlineLegionMember);
		}
	}

	private void updateMembersEmblem(Legion legion) {
		LegionEmblem legionEmblem = legion.getLegionEmblem();
		for (Player onlineLegionMember : legion.getOnlinePlayers()) {
			PacketSendUtility.broadcastPacket(onlineLegionMember, new SM_LEGION_UPDATE_EMBLEM(legion.getLegionId(), legionEmblem), true);
			if (legionEmblem.getEmblemType() == LegionEmblemType.CUSTOM)
				sendEmblemData(onlineLegionMember, legionEmblem, legion.getLegionId(), legion.getName());
		}
	}

	/**
	 * This method will send a packet to every legion member and update them about the disbanding
	 */
	private void updateMembersOfDisbandLegion(Legion legion, int unixTime) {
		for (Player onlineLegionMember : legion.getOnlinePlayers()) {
			PacketSendUtility.sendPacket(onlineLegionMember, new SM_LEGION_UPDATE_MEMBER(onlineLegionMember, 1300303, unixTime + ""));
			PacketSendUtility.sendPacket(onlineLegionMember, new SM_LEGION_EDIT(0x06, unixTime));
		}
	}

	/**
	 * This method will send a packet to every legion member and update them about the recreation
	 */
	private void updateMembersOfRecreateLegion(Legion legion) {
		for (Player onlineLegionMember : legion.getOnlinePlayers()) {
			PacketSendUtility.sendPacket(onlineLegionMember, new SM_LEGION_UPDATE_MEMBER(onlineLegionMember, 1300307, ""));
			PacketSendUtility.sendPacket(onlineLegionMember, new SM_LEGION_EDIT(0x07));
		}
	}

	public void storeLegionEmblem(Player activePlayer, int emblemId, int color_a, int color_r, int color_g, int color_b, LegionEmblemType emblemType) {
		if (legionRestrictions.canStoreLegionEmblem(activePlayer, emblemId)) {
			Legion legion = activePlayer.getLegion();
			addHistory(legion, "", LegionHistoryAction.EMBLEM_MODIFIED);
			activePlayer.getInventory().decreaseKinah(PricesService.getPriceForService(LegionConfig.LEGION_EMBLEM_REQUIRED_KINAH, activePlayer.getRace()));
			legion.getLegionEmblem().setEmblem(emblemId, color_a, color_r, color_g, color_b, emblemType, null);
			updateMembersEmblem(legion);
			PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_CHANGE_EMBLEM());
		}
	}

	public void openLegionWarehouse(Player player, Npc npc) {
		if (legionRestrictions.canOpenWarehouse(player, npc)) {
			LegionWhUpdate(player);
			PacketSendUtility.sendPacket(player, new SM_LEGION_EDIT(0x04, player.getLegion()));// kinah
			int whLvl = player.getLegion().getWarehouseExpansions();
			List<Item> items = player.getLegion().getLegionWarehouse().getItems();
			int storageId = StorageType.LEGION_WAREHOUSE.getId();

			SplitList<Item> legionMemberSplitList = new FixedElementCountSplitList<>(items, false, 10);
			legionMemberSplitList
				.forEach(part -> PacketSendUtility.sendPacket(player, new SM_WAREHOUSE_INFO(part, storageId, whLvl, part.isFirst(), player)));
			PacketSendUtility.sendPacket(player, new SM_WAREHOUSE_INFO(null, storageId, whLvl, items.isEmpty(), player));
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(npc.getObjectId(), DialogPage.LEGION_WAREHOUSE.id()));
		}
	}

	public void recreateLegion(Npc npc, Player activePlayer) {
		if (legionRestrictions.canRecreateLegion(activePlayer)) {
			RequestResponseHandler<Npc> disbandResponseHandler = new RequestResponseHandler<Npc>(npc) {

				@Override
				public void acceptRequest(Npc requester, Player responder) {
					Legion legion = responder.getLegion();
					legion.setDisbandTime(0);
					PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_EDIT(0x07));
					updateMembersOfRecreateLegion(legion);
				}

			};

			boolean disbandResult = activePlayer.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_GUILD_DISPERSE_STAYMODE_CANCEL,
				disbandResponseHandler);
			if (disbandResult) {
				PacketSendUtility.sendPacket(activePlayer, new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_GUILD_DISPERSE_STAYMODE_CANCEL, 0, 0));
			}
		}
	}

	public void LegionWhUpdate(Player player) {
		Legion legion = player.getLegion();

		if (legion == null)
			return;

		List<Item> allItems = legion.getLegionWarehouse().getItemsWithKinah();
		allItems.addAll(legion.getLegionWarehouse().getDeletedItems());
		try {
			InventoryDAO.store(allItems, player.getObjectId(), player.getAccount().getId(), legion.getLegionId());
			ItemStoneListDAO.save(allItems);
		} catch (Exception ex) {
			log.error("Exception during periodic saving of legion WH", ex);
		}
	}

	/**
	 * This method will update all players about the level/class/map/online change
	 */
	public void updateMemberInfo(Player player) {
		LegionMember legionMember = player.getLegionMember();
		legionMember.setPlayerData(player);
		PacketSendUtility.broadcastToLegion(player.getLegion(), new SM_LEGION_UPDATE_MEMBER(legionMember));
	}

	/**
	 * This method will set the contribution points, specially for legion command
	 */
	public void setContributionPoints(Legion legion, long newPoints, boolean save) {
		legion.setContributionPoints(newPoints);
		PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_EDIT(0x03, legion));
		if (save)
			storeLegion(legion);
	}

	public void uploadEmblemInfo(Player activePlayer, int totalSize, int color_a, int color_r, int color_g, int color_b, LegionEmblemType emblemType) {
		LegionEmblem legionEmblem = activePlayer.getLegion().getLegionEmblem();
		if (legionRestrictions.canUploadEmblem(activePlayer, true)) {
			legionEmblem.resetUploadSettings();
			legionEmblem.setEmblem(legionEmblem.getEmblemId(), color_a, color_r, color_g, color_b, emblemType, null);
			legionEmblem.setUploadSize(totalSize);
			legionEmblem.setUploading(true);
		} else {
			legionEmblem.resetUploadSettings();
		}
	}

	public void uploadEmblemData(Player activePlayer, int size, byte[] data) {
		LegionEmblem legionEmblem = activePlayer.getLegion().getLegionEmblem();
		if (legionRestrictions.canUploadEmblem(activePlayer, false)) {
			legionEmblem.addUploadedSize(size);
			legionEmblem.addUploadData(data);

			if (legionEmblem.getUploadedSize() >= legionEmblem.getUploadSize()) {
				if (legionEmblem.getUploadedSize() == 0 || legionEmblem.getUploadedSize() > legionEmblem.getUploadSize()) {
					PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_WARN_CORRUPT_EMBLEM_FILE());
					return;
				}
				activePlayer.getInventory()
					.decreaseKinah(PricesService.getPriceForService(LegionConfig.LEGION_EMBLEM_REQUIRED_KINAH, activePlayer.getRace()));
				// Finished
				legionEmblem.setCustomEmblemData(legionEmblem.getUploadData());
				LegionDAO.storeLegionEmblem(activePlayer.getLegion().getLegionId(), legionEmblem);
				addHistory(activePlayer.getLegion(), "", LegionHistoryAction.EMBLEM_REGISTER);
				updateMembersEmblem(activePlayer.getLegion());
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_WARN_SUCCESS_UPLOAD_EMBLEM());
				legionEmblem.resetUploadSettings();
			}
		} else {
			PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_WARN_FAILURE_UPLOAD_EMBLEM());
			legionEmblem.resetUploadSettings();
		}
	}

	public void sendEmblemData(Player player, LegionEmblem legionEmblem, int legionId, String legionName) {
		int dataLength = legionEmblem.getCustomEmblemData() == null ? 0 : legionEmblem.getCustomEmblemData().length;
		PacketSendUtility.sendPacket(player, new SM_LEGION_SEND_EMBLEM(legionId, legionEmblem, dataLength, legionName));
		if (dataLength > 0) {
			ByteBuffer buf = ByteBuffer.allocate(dataLength);
			buf.put(legionEmblem.getCustomEmblemData()).position(0);
			log.debug("legionEmblem size: " + buf.capacity() + " bytes");
			int maxSize = 7993;
			int currentSize;
			byte[] bytes;
			do {
				log.debug("legionEmblem data position: " + buf.position());
				currentSize = buf.capacity() - buf.position();
				log.debug("legionEmblem data remaining capacity: " + currentSize + " bytes");

				if (currentSize >= maxSize) {
					bytes = new byte[maxSize];
					for (int i = 0; i < maxSize; i++) {
						bytes[i] = buf.get();
					}
					log.debug("legionEmblem data send size: " + (bytes.length) + " bytes");
					PacketSendUtility.sendPacket(player, new SM_LEGION_SEND_EMBLEM_DATA(maxSize, bytes));
				} else {
					bytes = new byte[currentSize];
					for (int i = 0; i < currentSize; i++) {
						bytes[i] = buf.get();
					}
					log.debug("legionEmblem data send size: " + (bytes.length) + " bytes");
					PacketSendUtility.sendPacket(player, new SM_LEGION_SEND_EMBLEM_DATA(currentSize, bytes));
				}
			} while (buf.capacity() != buf.position());
		}
	}

	/**
	 * This will add a new announcement to the DB and change the current announcement
	 */
	public void changeAnnouncement(Player activePlayer, String message) {
		if (!activePlayer.getLegionMember().hasRights(LegionPermissionsMask.EDIT)) {
			PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_WRITE_NOTICE_DONT_HAVE_RIGHT());
			return;
		}
		Legion legion = activePlayer.getLegion();
		Legion.Announcement announcement = null;
		if (!message.isEmpty()) {
			if (message.length() > 256) {
				log.warn("Truncated legion announcement sent by " + activePlayer + " (old length: " + message.length() + ")");
				message = message.substring(0, 256);
			}
			announcement = new Legion.Announcement(message, new Timestamp(System.currentTimeMillis()));
		}
		legion.setAnnouncement(announcement);
		LegionDAO.saveAnnouncement(legion.getLegionId(), announcement);
		if (announcement == null) {
			PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_MSG_CLEAR_GUILD_NOTICE());
			PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_INFO(legion), activePlayer.getObjectId());
		} else {
			PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_WRITE_NOTICE_DONE());
			PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_EDIT(announcement));
		}
	}

	private void addHistory(Legion legion, String text, LegionHistoryAction action) {
		addHistory(legion, text, action, "");
	}

	public void addRewardHistory(Legion legion, long kinahAmount, LegionHistoryAction action, int fortressId) {
		addHistory(legion, String.valueOf(kinahAmount), action, String.valueOf(fortressId));
	}

	/**
	 * This method will add a new history for a legion
	 *
	 * @param name        in case of reward: kinah amount
	 * @param description in case of reward: fortress id
	 */
	public void addHistory(Legion legion, String name, LegionHistoryAction action, String description) {
		LegionHistoryEntry historyEntry = LegionDAO.insertHistory(legion.getLegionId(), action, name, description);
		List<LegionHistoryEntry> removedEntries = legion.addHistory(historyEntry);
		LegionDAO.deleteHistory(legion.getLegionId(), removedEntries);
		PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_HISTORY(legion.getHistory(action.getType()), action.getType()));
	}

	/**
	 * This method will add a new legion member to a legion with VOLUNTEER rank
	 */
	private void addLegionMember(Legion legion, Player player) {
		addLegionMember(legion, player, LegionRank.VOLUNTEER);
	}

	private void addLegionMember(Legion legion, Player player, LegionRank rank) {
		// Set legion member of player and save in the database
		player.setLegionMember(new LegionMember(player.getObjectId(), legion));
		player.getLegionMember().setPlayerData(player);
		player.getLegionMember().setRank(rank);
		LegionMemberDAO.saveNewLegionMember(player.getLegionMember());
		legionMemberById.put(player.getObjectId(), player.getLegionMember());

		// Send the new legion member the required legion packets
		PacketSendUtility.sendPacket(player, new SM_LEGION_INFO(legion));
		// do not include invited player in member list since he will be added via SM_LEGION_ADD_MEMBER
		updateLegionMemberList(player, false, player.getObjectId());

		// Send legion member info to the members
		PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_ADD_MEMBER(player, false, 1300260, player.getName()));
		// Send legion emblem information
		LegionEmblem legionEmblem = legion.getLegionEmblem();
		PacketSendUtility.broadcastPacket(player, new SM_LEGION_UPDATE_EMBLEM(legion.getLegionId(), legionEmblem), true);

		// Send legion edit
		PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_EDIT(0x08));

		// Update legion member's appearance in game
		PacketSendUtility.broadcastPacket(player,
			new SM_LEGION_UPDATE_TITLE(player.getObjectId(), legion.getLegionId(), legion.getName(), player.getLegionMember().getRank()), true);
		legion.addBonus();
	}

	private boolean removeLegionMember(Player player) {
		return removeLegionMember(player.getLegionMember(), null);
	}

	private boolean removeLegionMember(LegionMember legionMember, String kickerName) {
		if (legionMember == null)
			return false;
		// Delete legion member from database and cache
		deleteLegionMemberFromDB(legionMember);

		Legion legion = legionMember.getLegion();
		legion.getLegionWarehouse().unsetInUse(legionMember.getObjectId());

		if (kickerName != null) {
			PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_LEAVE_MEMBER(1300247, legionMember.getObjectId(), kickerName, legionMember.getName()),
				legionMember.getObjectId());
		} else {
			PacketSendUtility.broadcastToLegion(legion,
				new SM_LEGION_LEAVE_MEMBER(1300240, legionMember.getObjectId(), legionMember.getName(), legion.getName()), legionMember.getObjectId());
		}
		Player player = World.getInstance().getPlayer(legionMember.getObjectId());
		if (player != null) {
			PacketSendUtility.sendPacket(player, new SM_LEGION_LEAVE_MEMBER(kickerName != null ? 1300246 : 1300241, 0, legion.getName()));
			PacketSendUtility.broadcastPacket(player, new SM_LEGION_UPDATE_TITLE(player.getObjectId(), 0, "", legionMember.getRank()), true);
			if (legion.hasBonus())
				PacketSendUtility.sendPacket(player, new SM_ICON_INFO(1, false));
			player.resetLegionMember();
			ConquerorAndProtectorService.getInstance().onLeaveLegion(player);
		}
		legion.removeBonus();
		return true;
	}

	public void kickMember(Player player, String memberName) {
		LegionMember legionMember = getLegionMember(memberName);
		if (legionRestrictions.canKickPlayer(player, memberName, legionMember))
			removeLegionMember(legionMember, player.getName());
	}

	public boolean leaveLegion(Player player, boolean skipChecks) {
		if (skipChecks || legionRestrictions.canLeave(player))
			return removeLegionMember(player);
		return false;
	}

	public void onLogin(Player activePlayer) {
		Legion legion = activePlayer.getLegion();

		// Tell all legion members player has come online
		LegionService.getInstance().updateMemberInfo(activePlayer);

		// Notify legion members player has logged in
		PacketSendUtility.broadcastToLegion(legion, SM_SYSTEM_MESSAGE.STR_MSG_NOTIFY_LOGIN_GUILD(activePlayer.getName()), activePlayer.getObjectId());

		// Send member add to player
		PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_ADD_MEMBER(activePlayer, true, 0, ""));

		// Send legion info packets
		PacketSendUtility.sendPacket(activePlayer, new SM_LEGION_INFO(legion));
		updateLegionMemberList(activePlayer, false);

		// Send current announcement to player
		displayLegionAnnouncement(activePlayer, legion.getAnnouncement());

		if (legion.isDisbanding())
			PacketSendUtility.sendPacket(activePlayer, new SM_LEGION_EDIT(0x06, legion.getDisbandTime()));

		if (legion.hasBonus()) {
			PacketSendUtility.sendPacket(activePlayer, new SM_ICON_INFO(1, true));
		} else {
			legion.addBonus();
		}
	}

	public void onLogout(Player player) {
		LegionMember legionMember = player.getLegionMember();
		Legion legion = legionMember.getLegion();
		legion.getLegionWarehouse().unsetInUse(player.getObjectId());
		updateMemberInfo(player);
		storeLegion(legion);
		storeLegionMember(player.getLegionMember());
		legion.removeBonus();
	}

	/**
	 * This class contains all restrictions for legion features
	 *
	 * @author Simple
	 */
	private class LegionRestrictions {

		private static final int MIN_EMBLEM_ID = 0;
		private static final int MAX_EMBLEM_ID = 49;

		private boolean canCreateLegion(Player activePlayer, String legionName) {
			/* Some reasons why legions can' be created */
			if (!NameRestrictionService.isValidLegionName(legionName) || NameRestrictionService.isForbidden(legionName)) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_CREATE_INVALID_GUILD_NAME());
				return false;
			} // STR_GUILD_CREATE_TOO_FAR_FROM_CREATOR_NPC TODO
			else if (!isFreeName(legionName)) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_CREATE_SAME_GUILD_EXIST());
				return false;
			} else if (activePlayer.isLegionMember()) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_CREATE_ALREADY_BELONGS_TO_GUILD());
				return false;
			} else if (activePlayer.getInventory().getKinah() < LegionConfig.LEGION_CREATE_REQUIRED_KINAH) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_CREATE_NOT_ENOUGH_MONEY());
				return false;
			}
			return true;
		}

		private boolean canInvitePlayer(Player activePlayer, Player targetPlayer) {
			Legion legion = activePlayer.getLegion();
			if (targetPlayer == null) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_INVITE_NO_USER_TO_INVITE());
				return false;
			} else if (targetPlayer.getPlayerSettings().isInDeniedStatus(DeniedStatus.GUILD)) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_MSG_REJECTED_INVITE_GUILD(targetPlayer.getName()));
				return false;
			} else if (activePlayer.isDead()) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_INVITE_CANT_INVITE_WHEN_DEAD());
				return false;
			} else if (activePlayer.equals(targetPlayer)) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_INVITE_CAN_NOT_INVITE_SELF());
				return false;
			} else if (targetPlayer.isLegionMember()) {
				if (legion.isMember(targetPlayer.getObjectId())) {
					PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_INVITE_HE_IS_MY_GUILD_MEMBER(targetPlayer.getName()));
				} else {
					PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_INVITE_HE_IS_OTHER_GUILD_MEMBER(targetPlayer.getName()));
				}
				return false;
			} else if (!activePlayer.getLegionMember().hasRights(LegionPermissionsMask.INVITE)) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_INVITE_DONT_HAVE_RIGHT_TO_INVITE());
				return false;
			} else if (activePlayer.getRace() != targetPlayer.getRace() && !LegionConfig.LEGION_INVITEOTHERFACTION) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_INVITE_CAN_NOT_INVITE_OTHER_RACE());
				return false;
			}
			return true;
		}

		private boolean canKickPlayer(Player player, String charName, LegionMember legionMember) {
			Legion legion = player.getLegion();
			if (legion == null) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GUILD_BANISH_I_AM_NOT_BELONG_TO_GUILD());
				return false;
			} else if (legionMember == null || !legion.isMember(legionMember.getObjectId())) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GUILD_BANISH_HE_IS_NOT_MY_GUILD_MEMBER(charName));
				return false;
			} else if (player.getObjectId() == legionMember.getObjectId()) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GUILD_BANISH_CANT_BANISH_SELF());
				return false;
			} else if (legionMember.isBrigadeGeneral()) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GUILD_BANISH_CAN_BANISH_MASTER());
				return false;
			} else if (legionMember.getRank().getRankId() <= player.getLegionMember().getRank().getRankId()) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GUILD_BANISH_CAN_NOT_BANISH_SAME_MEMBER_RANK());
				return false;
			} else if (!player.getLegionMember().hasRights(LegionPermissionsMask.KICK)) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GUILD_BANISH_DONT_HAVE_RIGHT_TO_BANISH());
				return false;
			}
			return true;
		}

		private boolean canAppointBrigadeGeneral(Player activePlayer, Player targetPlayer) {
			Legion legion = activePlayer.getLegion();
			if (!isBrigadeGeneral(activePlayer)) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_CHANGE_MASTER_DONT_HAVE_RIGHT());
				return false;
			} else if (activePlayer.equals(targetPlayer)) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_CHANGE_MASTER_ERROR_SELF());
				return false;
			} else if (!legion.isMember(targetPlayer.getObjectId())) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_CHANGE_MASTER_NOT_MY_GUILD_MEMBER(targetPlayer.getName()));
				return false;
			}
			return true;
		}

		private boolean canAppointRank(Player activePlayer, LegionMember targetMember) {
			Legion legion = activePlayer.getLegion();
			if (legion == null) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_CHANGE_MEMBER_RANK_I_AM_NOT_BELONG_TO_GUILD());
				return false;
			} else if (!isBrigadeGeneral(activePlayer)) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_CHANGE_MEMBER_RANK_DONT_HAVE_RIGHT());
				return false;
			} else if (targetMember == null) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_CHANGE_MEMBER_RANK_NO_USER());
				return false;
			} else if (!legion.isMember(targetMember.getObjectId())) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_CHANGE_MEMBER_RANK_HE_IS_NOT_MY_GUILD_MEMBER(targetMember.getName()));
				return false;
			} else if (activePlayer.getObjectId() == targetMember.getObjectId()) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_CHANGE_MEMBER_RANK_ERROR_SELF());
				return false;
			}
			return true;
		}

		private boolean canChangeSelfIntro(Player activePlayer, String newSelfIntro) {
			return isValidSelfIntro(newSelfIntro);
		}

		private boolean canChangeLevel(Player activePlayer) {
			Legion legion = activePlayer.getLegion();
			int levelContributionPrice = legion.getContributionPrice();
			if (!activePlayer.getLegionMember().isBrigadeGeneral()) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_CHANGE_LEVEL_DONT_HAVE_RIGHT());
				return false;
			}
			if (legion.getLegionLevel() == MAX_LEGION_LEVEL) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_CHANGE_LEVEL_CANT_LEVEL_UP());
				return false;
			}
			if (LegionConfig.ENABLE_GUILD_TASK_REQ && legion.getLegionLevel() >= 5) {
				if (!ChallengeTaskService.getInstance().canRaiseLegionLevel(legion, activePlayer)) {
					PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_LEVEL_UP_CHALLENGE_TASK(legion.getLegionLevel()));
					return false;
				}
			}
			if (activePlayer.getInventory().getKinah() < legion.getKinahPrice()) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_CHANGE_LEVEL_NOT_ENOUGH_MONEY());
				return false;
			}
			if (!legion.hasRequiredMembers()) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_CHANGE_LEVEL_NOT_ENOUGH_MEMBER());
				return false;
			}
			if (legion.getContributionPoints() < levelContributionPrice) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_CHANGE_LEVEL_NOT_ENOUGH_POINT());
				return false;
			}
			return true;
		}

		private boolean canChangeNickname(Player player, LegionMember member, String memberName, String newNickname) {
			Legion legion = player.getLegion();
			if (legion == null) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GUILD_CHANGE_MEMBER_NICKNAME_I_AM_NOT_BELONG_TO_GUILD());
				return false;
			} else if (member == null || !legion.isMember(member.getObjectId())) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GUILD_CHANGE_MEMBER_NICKNAME_HE_IS_NOT_MY_GUILD_MEMBER(memberName));
				return false;
			} else if (!player.getLegionMember().isBrigadeGeneral()) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GUILD_CHANGE_MEMBER_NICKNAME_DONT_HAVE_RIGHT_TO_CHANGE_NICKNAME());
				return false;
			}
			return isValidNickname(newNickname);
		}

		private boolean canDisbandLegion(Player activePlayer) {
			Legion legion = activePlayer.getLegion();
			if (legion == null) {
				return false;
			}
			if (legion.isDisbanding()) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_DISPERSE_ALREADY_REQUESTED());
				return false;
			} else if (!isBrigadeGeneral(activePlayer)) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_DISPERSE_ONLY_MASTER_CAN_DISPERSE());
				return false;
			} else if (legion.getLegionWarehouse().getCurrentUser() != 0) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_DISPERSE_CANT_DISPERSE_GUILD_WHILE_USING_WAREHOUSE());
				return false;
			} else if (legion.getLegionWarehouse().size() > 0 || legion.getLegionWarehouse().getKinah() > 0) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_DISPERSE_CANT_DISPERSE_GUILD_STORE_ITEM_IN_WAREHOUSE());
				return false;
			}
			return true;
		}

		private boolean canLeave(Player activePlayer) {
			if (isBrigadeGeneral(activePlayer)) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_LEAVE_MASTER_CANT_LEAVE_BEFORE_CHANGE_MASTER());
				return false;
			} else if (activePlayer.getLegion().getLegionWarehouse().getCurrentUser() == activePlayer.getObjectId()) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_LEAVE_CANT_LEAVE_GUILD_WHILE_USING_WAREHOUSE());
				return false;
			}
			return true;
		}

		private boolean canRecreateLegion(Player activePlayer) {
			if (!isBrigadeGeneral(activePlayer)) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_DISPERSE_ONLY_MASTER_CAN_DISPERSE());
				return false;
			} else if (!activePlayer.getLegion().isDisbanding()) {
				// Legion is not disbanding
				return false;
			}
			return true;
		}

		private boolean canUploadEmblem(Player activePlayer, boolean initUpload) {
			if (!canStoreLegionEmblem(activePlayer, MIN_EMBLEM_ID)) {
				return false;
			} else if (activePlayer.getLegion().getLegionLevel() < 3) {
				// Legion level isn't high enough
				return false;
			} else if (initUpload && activePlayer.getLegion().getLegionEmblem().isUploading()) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_WARN_FAILURE_UPLOAD_EMBLEM());
				return false;
			} else if (!initUpload && !activePlayer.getLegion().getLegionEmblem().isUploading()) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_WARN_FAILURE_UPLOAD_EMBLEM());
				return false;
			}
			return true;
		}

		public boolean canOpenWarehouse(Player player, Npc npc) {
			if (!player.isLegionMember()) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_NO_GUILD_TO_DEPOSIT());
				return false;
			}
			LegionMember lm = player.getLegionMember();
			LegionWarehouse legWh = lm.getLegion().getLegionWarehouse();
			if (!LegionConfig.LEGION_WAREHOUSE || !npc.getObjectTemplate().supportsAction(DialogAction.OPEN_LEGION_WAREHOUSE)) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANT_USE_GUILD_STORAGE());
				return false;
			} else if (lm.getLegion().isDisbanding()) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GUILD_WAREHOUSE_CANT_USE_WHILE_DISPERSE());
				return false;
			} else if (!lm.hasRights(LegionPermissionsMask.WH_DEPOSIT) && !lm.hasRights(LegionPermissionsMask.WH_WITHDRAWAL)) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GUILD_WAREHOUSE_NO_RIGHT());
				return false;
			} else if (!legWh.setInUse(player.getObjectId()) && legWh.getCurrentUser() != player.getObjectId()) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GUILD_WAREHOUSE_IN_USE());
				return false;
			}
			return true;
		}

		public boolean canStoreLegionEmblem(Player activePlayer, int emblemId) {
			if (emblemId < MIN_EMBLEM_ID || emblemId > MAX_EMBLEM_ID) {
				// Not a valid emblemId
				return false;
			} else if (!isBrigadeGeneral(activePlayer)) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_CHANGE_EMBLEM_DONT_HAVE_RIGHT());
				return false;
			} else if (activePlayer.getLegion().getLegionLevel() < 2) {
				// legion level not high enough
				return false;
			} else if (activePlayer.getInventory().getKinah() < PricesService.getPriceForService(LegionConfig.LEGION_EMBLEM_REQUIRED_KINAH,
				activePlayer.getRace())) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_MSG_NOT_ENOUGH_MONEY());
				return false;
			}
			return true;
		}

		private boolean isBrigadeGeneral(Player player) {
			return player.getLegionMember().isBrigadeGeneral();
		}

		private boolean isFreeName(String name) {
			return !LegionDAO.isNameUsed(name);
		}

		private boolean isValidSelfIntro(String name) {
			return LegionConfig.SELF_INTRO_PATTERN.matcher(name).matches();
		}

		private boolean isValidNickname(String name) {
			return LegionConfig.NICKNAME_PATTERN.matcher(name).matches();
		}
	}

	public void addWHItemHistory(Player player, int itemId, long count, IStorage sourceStorage, IStorage destStorage) {
		Legion legion = player.getLegion();
		if (legion != null) {
			String description = itemId + ":" + count;
			if (sourceStorage.getStorageType() == StorageType.LEGION_WAREHOUSE) {
				addHistory(legion, player.getName(), LegionHistoryAction.ITEM_WITHDRAW, description);
			} else if (destStorage.getStorageType() == StorageType.LEGION_WAREHOUSE) {
				addHistory(legion, player.getName(), LegionHistoryAction.ITEM_DEPOSIT, description);
			}
		}
	}

	private static class SingletonHolder {

		protected static final LegionService instance = new LegionService();
	}

	public void updateLegionMemberList(Player player, boolean broadcastToLegion) {
		updateLegionMemberList(player, broadcastToLegion, null);
	}

	public void updateLegionMemberList(Player player, boolean broadcastToLegion, Integer excludedPlayerId) {
		if (player != null && player.getLegion() != null) {
			Legion legion = player.getLegion();
			List<LegionMember> allMembers = legion.getMembers();
			if (excludedPlayerId != null)
				allMembers.removeIf(member -> member.getObjectId() == excludedPlayerId);
			SplitList<LegionMember> legionMemberSplitList = new FixedElementCountSplitList<>(allMembers, true, 80);
			legionMemberSplitList.forEach(part -> {
				if (broadcastToLegion)
					PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_MEMBERLIST(part, part.isFirst(), part.isLast()));
				else
					PacketSendUtility.sendPacket(player, new SM_LEGION_MEMBERLIST(part, part.isFirst(), part.isLast()));
			});
		}
	}

	public boolean tryRename(Legion legion, String name, Player player, Integer legionNameChangeTicketItemObjId) {
		if (legion.getName().equals(name)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_EDIT_GUILD_NAME_ERROR_SAME_YOUR_NAME());
			return false;
		} else if (!NameRestrictionService.isValidLegionName(name) || NameRestrictionService.isForbidden(name)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_EDIT_GUILD_NAME_ERROR_WRONG_INPUT());
			return false;
		} else if (LegionDAO.isNameUsed(name)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_EDIT_GUILD_NAME_ALREADY_EXIST());
			return false;
		} else if (legion.isDisbanding()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_EDIT_GUILD_NAME_CANT_FOR_DISPERSING_GUILD());
			return false;
		} else if (legionNameChangeTicketItemObjId != null) {
			Item item = player.getInventory().getItemByObjId(legionNameChangeTicketItemObjId);
			if (item == null || item.getItemId() != 169680000 && item.getItemId() != 169680001 || !player.getInventory().decreaseByObjectId(
				legionNameChangeTicketItemObjId, 1)) {
				AuditLogger.log(player, "tried to rename legion without coupon.");
				return false;
			}
		}
		String oldName = legion.getName();
		legion.setName(name);
		LegionDAO.storeLegion(legion);
		addHistory(legion, oldName, LegionHistoryAction.LEGION_RENAME, name);
		PacketSendUtility.broadcastToWorld(new SM_RENAME(legion, oldName)); // broadcast to world to update all keeps, member's tags, etc.
		return true;
	}

	public void joinLegionDominion(Player player, int locId) {
		LegionMember legionMember = player.getLegionMember();
		if (!legionMember.isBrigadeGeneral() && legionMember.getRank() != LegionRank.DEPUTY)
			return;
		Legion legion = legionMember.getLegion();
		if (legion.getCurrentLegionDominion() > 0) // already selected
			return;
		if (LegionDominionService.getInstance().join(legion.getLegionId(), locId)) {
			legion.setCurrentLegionDominion(locId);
			storeLegion(legion);
			String locL10n = LegionDominionService.getInstance().getLegionDominionLoc(locId).getL10n();
			PacketSendUtility.broadcastToLegion(legion, SM_SYSTEM_MESSAGE.STR_MSG_GUILD_APPLY_DOMINION(locL10n));
			PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_INFO(legion));
		}
	}

}
