package com.aionemu.gameserver.services;

import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
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
import com.aionemu.gameserver.model.gameobjects.player.RequestResponseHandler;
import com.aionemu.gameserver.model.items.storage.IStorage;
import com.aionemu.gameserver.model.items.storage.StorageType;
import com.aionemu.gameserver.model.team.legion.*;
import com.aionemu.gameserver.network.aion.serverpackets.*;
import com.aionemu.gameserver.services.conquerorAndProtectorSystem.ConquerorAndProtectorService;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.trade.PricesService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.audit.AuditLogger;
import com.aionemu.gameserver.utils.collections.FixedElementCountSplitList;
import com.aionemu.gameserver.utils.collections.SplitList;
import com.aionemu.gameserver.utils.idfactory.IDFactory;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.container.LegionContainer;
import com.aionemu.gameserver.world.container.LegionMemberContainer;

/**
 * This class is designed to do all the work related with loading/storing legions and their members.
 *
 * @author Simple, cura, Source, Neon
 */
public class LegionService {

	private static final Logger log = LoggerFactory.getLogger(LegionService.class);
	private final LegionContainer allCachedLegions = new LegionContainer();
	private final LegionMemberContainer allCachedLegionMembers = new LegionMemberContainer();
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

	private void storeLegionMember(LegionMember legionMember, boolean newMember) {
		if (newMember) {
			addCachedLegionMember(legionMember);
			LegionMemberDAO.saveNewLegionMember(legionMember);
		} else
			LegionMemberDAO.storeLegionMember(legionMember.getObjectId(), legionMember);
	}

	public void storeLegionMember(LegionMember legionMember) {
		storeLegionMember(legionMember, false);
	}

	private void storeLegionMemberExInCache(Player player) {
		if (this.allCachedLegionMembers.containsEx(player.getObjectId())) {
			LegionMemberEx legionMemberEx = allCachedLegionMembers.getMemberEx(player.getObjectId());
			legionMemberEx.setNickname(player.getLegionMember().getNickname());
			legionMemberEx.setSelfIntro(player.getLegionMember().getSelfIntro());
			legionMemberEx.setPlayerClass(player.getPlayerClass());
			legionMemberEx.setLevelByExp(player.getCommonData().getExp());
			legionMemberEx.setLastOnline(player.getCommonData().getLastOnline());
			legionMemberEx.setWorldId(player.getPosition().getMapId());
			legionMemberEx.setOnline(false);
		} else {
			LegionMemberEx legionMemberEx = new LegionMemberEx(player, player.getLegionMember(), false);
			addCachedLegionMemberEx(legionMemberEx);
		}
	}

	private Legion getCachedLegion(int legionId) {
		return allCachedLegions.get(legionId);
	}

	private Legion getCachedLegion(String legionName) {
		return allCachedLegions.get(legionName);
	}

	public LegionContainer getCachedLegions() {
		return allCachedLegions;
	}

	private void addCachedLegion(Legion legion) {
		allCachedLegions.add(legion);
	}

	private void addCachedLegionMember(LegionMember legionMember) {
		allCachedLegionMembers.addMember(legionMember);
	}

	private void addCachedLegionMemberEx(LegionMemberEx legionMemberEx) {
		allCachedLegionMembers.addMemberEx(legionMemberEx);
	}

	/**
	 * Completely removes legion from database and cache
	 */
	private void deleteLegionFromDB(Legion legion) {
		this.allCachedLegions.remove(legion);
		LegionDAO.deleteLegion(legion.getLegionId());
	}

	/**
	 * This method will remove the legion member from cache and the database
	 */
	private void deleteLegionMemberFromDB(LegionMemberEx legionMember) {
		allCachedLegionMembers.remove(legionMember.getObjectId());
		LegionMemberDAO.deleteLegionMember(legionMember.getObjectId());
		Legion legion = legionMember.getLegion();
		legion.deleteLegionMember(legionMember.getObjectId());
		addHistory(legion, legionMember.getName(), LegionHistoryType.KICK);
	}

	public Legion getLegion(String legionName) {
		Legion legion = getCachedLegion(legionName);
		if (legion == null) {
			legion = LegionDAO.loadLegion(legionName);
			if (legion == null)
				return null;
			loadLegionInfo(legion);
			addCachedLegion(legion);
		}
		return checkDisband(legion) ? null : legion;
	}

	public Legion getLegion(int legionId) {
		Legion legion = getCachedLegion(legionId);
		if (legion == null) {
			legion = LegionDAO.loadLegion(legionId);
			if (legion == null)
				return null;
			loadLegionInfo(legion);
			addCachedLegion(legion);
		}
		return checkDisband(legion) ? null : legion;
	}

	private void loadLegionInfo(Legion legion) {
		// Load and add the legion members to legion
		legion.setLegionMembers(LegionMemberDAO.loadLegionMembers(legion.getLegionId()));

		// Load and set the announcement list
		legion.setAnnouncementList(LegionDAO.loadAnnouncementList(legion.getLegionId()));

		// Load legion emblem
		legion.setLegionEmblem(LegionDAO.loadLegionEmblem(legion.getLegionId()));

		// Load Legion Warehouse
		legion.setLegionWarehouse(LegionDAO.loadLegionStorage(legion));
		ItemService.loadItemStones(legion.getLegionWarehouse().getItems());

		// Load Legion History
		LegionDAO.loadLegionHistory(legion);
	}

	public int getBrigadeGeneralOfLegion(int legionId) {
		Legion legion = getLegion(legionId);
		return legion == null ? 0 : legion.getBrigadeGeneral();
	}

	public List<Integer> getMembersByRank(int legionId, LegionRank rank) {
		Legion legion = getLegion(legionId);
		List<Integer> members = new ArrayList<>();
		for (int memberObjId : legion.getLegionMembers()) {
			LegionMember legionMember = getLegionMember(memberObjId);
			if (legionMember.getRank() == rank)
				members.add(memberObjId);
		}
		return members;
	}

	public LegionMember getLegionMember(int playerObjId) {
		LegionMember legionMember = allCachedLegionMembers.getMember(playerObjId);
		if (legionMember == null) {
			legionMember = LegionMemberDAO.loadLegionMember(playerObjId);
			if (legionMember != null)
				addCachedLegionMember(legionMember);
		}
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
		for (Integer memberObjId : legion.getLegionMembers()) {
			allCachedLegionMembers.remove(memberObjId);
		}
		SiegeService.getInstance().cleanLegionId(legion.getLegionId());
		updateAfterDisbandLegion(legion);
		deleteLegionFromDB(legion);
	}

	/**
	 * Returns the offline legion member with given playerId (if such member exists)
	 */
	public LegionMemberEx getLegionMemberEx(int playerObjId) {
		if (this.allCachedLegionMembers.containsEx(playerObjId))
			return this.allCachedLegionMembers.getMemberEx(playerObjId);
		else {
			LegionMemberEx legionMember = LegionMemberDAO.loadLegionMemberEx(playerObjId);
			addCachedLegionMemberEx(legionMember);
			return legionMember;
		}
	}

	/**
	 * Returns the offline legion member with given playerId (if such member exists)
	 */
	private LegionMemberEx getLegionMemberEx(String playerName) {
		if (this.allCachedLegionMembers.containsEx(playerName))
			return this.allCachedLegionMembers.getMemberEx(playerName);
		else {
			LegionMemberEx legionMember = LegionMemberDAO.loadLegionMemberEx(playerName);
			if (legionMember != null)
				addCachedLegionMemberEx(legionMember);
			return legionMember;
		}
	}

	public void requestDisbandLegion(Npc npc, Player activePlayer) {
		if (legionRestrictions.canDisbandLegion(activePlayer)) {
			RequestResponseHandler<Npc> disbandResponseHandler = new RequestResponseHandler<Npc>(npc) {

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
			/**
			 * Create new legion and put originator as first member
			 */
			Legion legion = new Legion(IDFactory.getInstance().nextId(), legionName);
			legion.addLegionMember(activePlayer.getObjectId());

			activePlayer.getInventory().decreaseKinah(LegionConfig.LEGION_CREATE_REQUIRED_KINAH);

			/**
			 * Create a LegionMember, add it to the legion and bind it to a Player
			 */
			storeLegion(legion, true);
			Timestamp currentTime = new Timestamp(System.currentTimeMillis());
			storeNewAnnouncement(legion.getLegionId(), currentTime, "");
			legion.addAnnouncementToList(currentTime, "");
			addLegionMember(legion, activePlayer, LegionRank.BRIGADE_GENERAL);
			PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_EDIT(0x05, (int) (System.currentTimeMillis() / 1000), ""));
			/**
			 * Add create and joined legion history and save it
			 */
			addHistory(legion, "", LegionHistoryType.CREATE);
			addHistory(legion, activePlayer.getName(), LegionHistoryType.JOIN);

			PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_CREATED(legion.getName()));
		}
	}

	public boolean addToLegion(Legion legion, Player invited, Player inviter) {
		int playerObjId = invited.getObjectId();
		if (legion.addLegionMember(playerObjId)) {
			// Bind LegionMember to Player
			addLegionMember(legion, invited);

			// Display current announcement
			displayLegionMessage(invited, legion.getCurrentAnnouncement());

			// Add to history of legion
			addHistory(legion, invited.getName(), LegionHistoryType.JOIN);
			return true;
		}
		PacketSendUtility.sendPacket(inviter, SM_SYSTEM_MESSAGE.STR_GUILD_INVITE_CAN_NOT_ADD_MEMBER_ANY_MORE());
		return false;
	}

	private void invitePlayerToLegion(Player activePlayer, Player targetPlayer) {
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
	private void displayLegionMessage(Player targetPlayer, Entry<Timestamp, String> currentAnnouncement) {
		if (currentAnnouncement != null) {
			PacketSendUtility.sendPacket(targetPlayer,
				SM_SYSTEM_MESSAGE.STR_GUILD_NOTICE(currentAnnouncement.getValue(), (int) (currentAnnouncement.getKey().getTime() / 1000)));
		}
	}

	private void startBrigadeGeneralChangeProcess(Player legionLeader, Player newLegionLeader) {
		RequestResponseHandler<Player> responseHandler = new RequestResponseHandler<Player>(newLegionLeader) {

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
			final Legion legion = activePlayer.getLegion();
			RequestResponseHandler<Player> responseHandler = new RequestResponseHandler<Player>(activePlayer) {

				@Override
				public void acceptRequest(Player requester, Player responder) {
					if (!responder.isOnline()) {
						PacketSendUtility.sendPacket(requester, SM_SYSTEM_MESSAGE.STR_GUILD_CHANGE_MASTER_NO_SUCH_USER());
					} else if (!legionRestrictions.canAppointBrigadeGeneral(requester, responder)) {
						AuditLogger.log(requester, "possibly tried to exploit legion leadership transfer");
					} else {
						LegionMember legionMember = responder.getLegionMember();
						if (legionMember.getRank().getRankId() > LegionRank.BRIGADE_GENERAL.getRankId()) { // Demote Brigade General to Centurion
							requester.getLegionMember().setRank(LegionRank.CENTURION);
							PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_UPDATE_MEMBER(requester, 0, ""));

							// Promote member to Brigade General
							legionMember.setRank(LegionRank.BRIGADE_GENERAL);
							PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_UPDATE_MEMBER(responder, 1300273, responder.getName()));
							PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_EDIT(0x08));
							addHistory(legion, responder.getName(), LegionHistoryType.APPOINTED);
						}
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

	/**
	 * This method will handle the process when a member is demoted or promoted while offline.
	 */
	private void appointRank(Player activePlayer, String charName, int rankId) {
		final LegionMemberEx LM = getLegionMemberEx(charName);
		if (LM == null) {
			log.error("Char name does not exist in legion member table: " + charName);
			return;
		}
		if (legionRestrictions.canAppointRank(activePlayer, LM.getObjectId())) {
			Legion legion = activePlayer.getLegion();
			LegionRank rank = LegionRank.values()[rankId];
			int msgId = 0;
			switch (rank) {
				case DEPUTY:
					msgId = 1400902;
					break;
				case LEGIONARY:
					msgId = 1300268;
					break;
				case CENTURION:
					msgId = 1300267;
					break;
				case VOLUNTEER:
					msgId = 1400903;
			}
			LegionMember legionMember = getLegionMember(LM.getObjectId());
			legionMember.setRank(rank);
			LegionMemberDAO.storeLegionMember(legionMember.getObjectId(), legionMember);
			LM.setRank(rank);
			PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_UPDATE_MEMBER(LM, msgId, LM.getName()));
		}
	}

	/**
	 * This method will handle the process when a member is demoted or promoted.
	 */
	private void appointRank(Player activePlayer, Player targetPlayer, int rankId) {
		if (legionRestrictions.canAppointRank(activePlayer, targetPlayer.getObjectId())) {
			Legion legion = activePlayer.getLegion();
			int msgId = 0;
			LegionRank rank = LegionRank.values()[rankId];
			LegionMember legionMember = targetPlayer.getLegionMember();
			switch (rank) {
				case DEPUTY:
					msgId = 1400902;
					break;
				case LEGIONARY:
					msgId = 1300268;
					break;
				case CENTURION:
					msgId = 1300267;
					break;
				case VOLUNTEER:
					msgId = 1400903;
			}
			legionMember.setRank(rank);
			PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_UPDATE_MEMBER(targetPlayer, msgId, targetPlayer.getName()));
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

	public void changePermissions(Player actingPlayer, Legion legion, short deputyPermission, short centurionPermission, short legionarPermission,
		short volunteerPermission) {
		if (actingPlayer.getObjectId() != legion.getBrigadeGeneral())
			return;
		if (legion.setLegionPermissions(deputyPermission, centurionPermission, legionarPermission, volunteerPermission)) {
			PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_EDIT(0x02, legion));
		}
	}

	/**
	 * This method will handle the leveling up of a legion
	 */
	public void requestChangeLevel(Player activePlayer) {
		if (legionRestrictions.canChangeLevel(activePlayer)) {
			Legion legion = activePlayer.getLegion();
			activePlayer.getInventory().decreaseKinah(legion.getKinahPrice());
			changeLevel(legion, legion.getLegionLevel() + 1, false);
			addHistory(legion, legion.getLegionLevel() + "", LegionHistoryType.LEVEL_UP);
		}
	}

	/**
	 * This method will change the legion level and send update to online members
	 */
	public void changeLevel(Legion legion, int newLevel, boolean save) {
		legion.setLegionLevel(newLevel);
		legion.getLegionWarehouse().setLimit(legion.getWarehouseSlots());
		PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_EDIT(0x00, legion));
		PacketSendUtility.broadcastToLegion(legion, SM_SYSTEM_MESSAGE.STR_GUILD_EVENT_LEVELUP(newLevel));
		if (save)
			storeLegion(legion);
	}

	private void changeNickname(Player activePlayer, String charName, String newNickname) {
		Legion legion = activePlayer.getLegion();
		LegionMember legionMember;
		Player targetPlayer;
		if ((targetPlayer = World.getInstance().getPlayer(charName)) != null) {
			legionMember = targetPlayer.getLegionMember();
			if (targetPlayer.getLegion() != legion)
				return;
		} else {
			LegionMemberEx LM = getLegionMemberEx(charName);
			if (LM == null || LM.getLegion() != legion) {
				return;
			}
			legionMember = getLegionMember(LM.getObjectId());
		}
		if (legionRestrictions.canChangeNickname(legion, legionMember.getObjectId(), newNickname)) {
			legionMember.setNickname(newNickname);
			PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_UPDATE_NICKNAME(legionMember.getObjectId(), newNickname));
			if (targetPlayer == null)
				LegionMemberDAO.storeLegionMember(legionMember.getObjectId(), legionMember);
		}
	}

	/**
	 * This method will remove legion from all legion members online after a legion has been disbanded
	 */
	private void updateAfterDisbandLegion(Legion legion) {
		for (Player onlineLegionMember : legion.getOnlineLegionMembers()) {
			PacketSendUtility.broadcastPacket(onlineLegionMember,
				new SM_LEGION_UPDATE_TITLE(onlineLegionMember.getObjectId(), 0, "", onlineLegionMember.getLegionMember().getRank()), true);
			PacketSendUtility.sendPacket(onlineLegionMember, new SM_LEGION_LEAVE_MEMBER(1300302, 0, legion.getName()));
			onlineLegionMember.resetLegionMember();
			ConquerorAndProtectorService.getInstance().onLeaveLegion(onlineLegionMember);
		}
	}

	private void updateMembersEmblem(Legion legion) {
		LegionEmblem legionEmblem = legion.getLegionEmblem();
		for (Player onlineLegionMember : legion.getOnlineLegionMembers()) {
			PacketSendUtility.broadcastPacket(onlineLegionMember, new SM_LEGION_UPDATE_EMBLEM(legion.getLegionId(), legionEmblem), true);
			if (legionEmblem.getEmblemType() == LegionEmblemType.CUSTOM)
				sendEmblemData(onlineLegionMember, legionEmblem, legion.getLegionId(), legion.getName());
		}
	}

	/**
	 * This method will send a packet to every legion member and update them about the disbanding
	 */
	private void updateMembersOfDisbandLegion(Legion legion, int unixTime) {
		for (Player onlineLegionMember : legion.getOnlineLegionMembers()) {
			PacketSendUtility.sendPacket(onlineLegionMember, new SM_LEGION_UPDATE_MEMBER(onlineLegionMember, 1300303, unixTime + ""));
			PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_EDIT(0x06, unixTime));
		}
	}

	/**
	 * This method will send a packet to every legion member and update them about the recreation
	 */
	private void updateMembersOfRecreateLegion(Legion legion) {
		for (Player onlineLegionMember : legion.getOnlineLegionMembers()) {
			PacketSendUtility.sendPacket(onlineLegionMember, new SM_LEGION_UPDATE_MEMBER(onlineLegionMember, 1300307, ""));
			PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_EDIT(0x07));
		}
	}

	public void storeLegionEmblem(Player activePlayer, int emblemId, int color_a, int color_r, int color_g, int color_b, LegionEmblemType emblemType) {
		if (legionRestrictions.canStoreLegionEmblem(activePlayer, emblemId)) {
			Legion legion = activePlayer.getLegion();
			addHistory(legion, "", LegionHistoryType.EMBLEM_MODIFIED);
			activePlayer.getInventory().decreaseKinah(PricesService.getPriceForService(LegionConfig.LEGION_EMBLEM_REQUIRED_KINAH, activePlayer.getRace()));
			legion.getLegionEmblem().setEmblem(emblemId, color_a, color_r, color_g, color_b, emblemType, null);
			updateMembersEmblem(legion);
			PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_CHANGE_EMBLEM());
		}
	}

	public List<LegionMemberEx> loadLegionMemberExList(Legion legion, Integer objExcluded) {
		List<LegionMemberEx> legionMembers = new ArrayList<>();
		for (Integer memberObjId : legion.getLegionMembers()) {
			LegionMemberEx legionMemberEx;
			if (objExcluded != null && objExcluded.equals(memberObjId)) {
				continue;
			}
			Player memberPlayer = World.getInstance().getPlayer(memberObjId);
			if (memberPlayer != null) {
				legionMemberEx = new LegionMemberEx(memberPlayer, memberPlayer.getLegionMember(), true);
			} else {
				legionMemberEx = getLegionMemberEx(memberObjId);
			}
			legionMembers.add(legionMemberEx);
		}
		return legionMembers;
	}

	public void openLegionWarehouse(Player player, Npc npc) {
		if (legionRestrictions.canOpenWarehouse(player, npc)) {
			LegionWhUpdate(player);
			PacketSendUtility.sendPacket(player, new SM_LEGION_EDIT(0x04, player.getLegion()));// kinah
			int whLvl = player.getLegion().getWarehouseLevel();
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
	 * This method will update all players about the level/class change
	 */
	public void updateMemberInfo(Player player) {
		PacketSendUtility.broadcastToLegion(player.getLegion(), new SM_LEGION_UPDATE_MEMBER(player, 0, ""));
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
				addHistory(activePlayer.getLegion(), "", LegionHistoryType.EMBLEM_REGISTER);
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
	public void changeAnnouncement(Player activePlayer, String announcement) {
		if (legionRestrictions.canChangeAnnouncement(activePlayer.getLegionMember(), announcement)) {
			Legion legion = activePlayer.getLegion();

			Timestamp currentTime = new Timestamp(System.currentTimeMillis());
			storeNewAnnouncement(legion.getLegionId(), currentTime, announcement);
			legion.addAnnouncementToList(currentTime, announcement);
			PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_WRITE_NOTICE_DONE());
			PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_EDIT(0x05, (int) (System.currentTimeMillis() / 1000), announcement));
		}
	}

	private void storeLegionAnnouncements(Legion legion) {
		for (int i = 0; i < (legion.getAnnouncementList().size() - 7); i++) {
			removeAnnouncement(legion.getLegionId(), legion.getAnnouncementList().firstEntry().getKey());
			legion.removeFirstEntry();
		}
	}

	private boolean storeNewAnnouncement(int legionId, Timestamp currentTime, String message) {
		return LegionDAO.saveNewAnnouncement(legionId, currentTime, message);
	}

	private void removeAnnouncement(int legionId, Timestamp key) {
		LegionDAO.removeAnnouncement(legionId, key);
	}

	private void addHistory(Legion legion, String text, LegionHistoryType legionHistoryType) {
		addHistory(legion, text, legionHistoryType, 0, "");
	}

	public void addRewardHistory(Legion legion, long kinahAmount, LegionHistoryType lht, int fortressId) {
		addHistory(legion, String.valueOf(kinahAmount), lht, 1, String.valueOf(fortressId));
	}

	/**
	 * This method will add a new history for a legion
	 *
	 * @param text        in case of reward: kinah amount
	 * @param description in case of reward: fortress id
	 */
	public void addHistory(Legion legion, String text, LegionHistoryType legionHistoryType, int tabId, String description) {
		LegionHistory legionHistory = new LegionHistory(legionHistoryType, text, new Timestamp(System.currentTimeMillis()), tabId, description);

		legion.addHistory(legionHistory);
		LegionDAO.saveNewLegionHistory(legion.getLegionId(), legionHistory);

		PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_TABS(legion.getLegionHistoryByTabId(tabId), tabId));
	}

	/**
	 * This method will add a new legion member to a legion with VOLUNTEER rank
	 */
	private void addLegionMember(Legion legion, Player player) {
		addLegionMember(legion, player, LegionRank.VOLUNTEER);
	}

	private void addLegionMember(Legion legion, Player player, LegionRank rank) {
		// Set legion member of player and save in the database
		player.setLegionMember(new LegionMember(player.getObjectId(), legion, rank));
		storeLegionMember(player.getLegionMember(), true);

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

	private boolean removeLegionMember(String charName, String kickerName) {
		// Get LegionMemberEx from cache or database if offline
		LegionMemberEx legionMember = getLegionMemberEx(charName);
		if (legionMember == null) {
			log.error("Char name does not exist in legion member table: {}", charName);
			return false;
		}
		Legion legion = legionMember.getLegion();

		// Delete legion member from database and cache
		deleteLegionMemberFromDB(legionMember);

		legion.getLegionWarehouse().unsetInUse(legionMember.getObjectId());

		Player player = World.getInstance().getPlayer(charName);
		int playerObjectId = player != null ? player.getObjectId() : 0;
		if (kickerName != null) {
			PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_LEAVE_MEMBER(1300247, legionMember.getObjectId(), kickerName, legionMember.getName()),
				playerObjectId);
		} else {
			PacketSendUtility.broadcastToLegion(legion,
				new SM_LEGION_LEAVE_MEMBER(1300240, legionMember.getObjectId(), legionMember.getName(), legion.getName()), playerObjectId);
		}
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

	public void handleCharNameRequest(int exOpcode, Player activePlayer, String charName, String newNickname, int rank) {
		charName = Util.convertName(charName);
		Player targetPlayer = World.getInstance().getPlayer(charName);

		switch (exOpcode) {
			// invite to legion
			case 0x01:
				if (targetPlayer != null) {
					if (targetPlayer.getPlayerSettings().isInDeniedStatus(DeniedStatus.GUILD)) {
						PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_MSG_REJECTED_INVITE_GUILD(charName));
						return;
					}
					invitePlayerToLegion(activePlayer, targetPlayer);
				} else {
					PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_INVITE_NO_USER_TO_INVITE());
				}
				break;
			// kick member
			case 0x04:
				if (legionRestrictions.canKickPlayer(activePlayer, charName))
					removeLegionMember(charName, activePlayer.getName());
				break;
			// appoint a new Brigade General
			case 0x05:
				if (targetPlayer != null) {
					startBrigadeGeneralChangeProcess(activePlayer, targetPlayer);
				} else {
					PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_INVITE_NO_USER_TO_INVITE());
				}
				break;
			// change rank
			case 0x06:
				if (targetPlayer != null)
					appointRank(activePlayer, targetPlayer, rank);
				else
					appointRank(activePlayer, charName, rank);
				break;
			// change nickname
			case 0x0F:
				changeNickname(activePlayer, charName, newNickname);
				break;
		}
	}

	public boolean leaveLegion(Player player, boolean skipChecks) {
		if (skipChecks || legionRestrictions.canLeave(player))
			return removeLegionMember(player.getName(), null);
		return false;
	}

	public void onLogin(Player activePlayer) {
		Legion legion = activePlayer.getLegion();

		// Tell all legion members player has come online
		PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_UPDATE_MEMBER(activePlayer, 0, ""), activePlayer.getObjectId());

		// Notify legion members player has logged in
		PacketSendUtility.broadcastToLegion(legion, SM_SYSTEM_MESSAGE.STR_MSG_NOTIFY_LOGIN_GUILD(activePlayer.getName()), activePlayer.getObjectId());

		// Send member add to player
		PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_ADD_MEMBER(activePlayer, true, 0, ""));

		// Send legion info packets
		PacketSendUtility.sendPacket(activePlayer, new SM_LEGION_INFO(legion));
		updateLegionMemberList(activePlayer, false);

		// Send current announcement to player
		displayLegionMessage(activePlayer, legion.getCurrentAnnouncement());

		if (legion.isDisbanding())
			PacketSendUtility.sendPacket(activePlayer, new SM_LEGION_EDIT(0x06, legion.getDisbandTime()));

		legion.increaseOnlineMembersCount();
		if (legion.hasBonus()) {
			PacketSendUtility.sendPacket(activePlayer, new SM_ICON_INFO(1, true));
		} else {
			legion.addBonus();
		}
	}

	public void onLogout(Player player) {
		Legion legion = player.getLegion();
		legion.getLegionWarehouse().unsetInUse(player.getObjectId());
		PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_UPDATE_MEMBER(player));
		storeLegion(legion);
		storeLegionMember(player.getLegionMember());
		storeLegionMemberExInCache(player);
		storeLegionAnnouncements(legion);
		legion.decreaseOnlineMembersCount();
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
			if (activePlayer.isDead()) {
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

		private boolean canKickPlayer(Player activePlayer, String charName) {
			Legion legion = activePlayer.getLegion();
			if (legion == null) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_BANISH_I_AM_NOT_BELONG_TO_GUILD());
				return false;
			}
			// Get LegionMemberEx from cache or database if offline
			LegionMemberEx legionMember = getLegionMemberEx(charName);

			if (legionMember == null || !legion.isMember(legionMember.getObjectId())) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_BANISH_HE_IS_NOT_MY_GUILD_MEMBER(charName));
				return false;
			} else if (activePlayer.getObjectId() == legionMember.getObjectId()) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_BANISH_CANT_BANISH_SELF());
				return false;
			} else if (legionMember.isBrigadeGeneral()) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_BANISH_CAN_BANISH_MASTER());
				return false;
			} else if (legionMember.getRank().getRankId() <= activePlayer.getLegionMember().getRank().getRankId()) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_BANISH_CAN_NOT_BANISH_SAME_MEMBER_RANK());
				return false;
			} else if (!activePlayer.getLegionMember().hasRights(LegionPermissionsMask.KICK)) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_BANISH_DONT_HAVE_RIGHT_TO_BANISH());
				return false;
			}
			return true;
		}

		private boolean canAppointBrigadeGeneral(Player activePlayer, Player targetPlayer) {
			Legion legion = activePlayer.getLegion();
			if (!isBrigadeGeneral(activePlayer)) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_CHANGE_MEMBER_RANK_DONT_HAVE_RIGHT());
				return false;
			}
			if (activePlayer.equals(targetPlayer)) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_CHANGE_MASTER_ERROR_SELF());
				return false;
			} else if (!legion.isMember(targetPlayer.getObjectId()))
				// not in same legion
				return false;
			return true;
		}

		private boolean canAppointRank(Player activePlayer, int targetObjId) {
			Legion legion = activePlayer.getLegion();
			if (!isBrigadeGeneral(activePlayer)) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_CHANGE_MEMBER_RANK_DONT_HAVE_RIGHT());
				return false;
			}
			if (activePlayer.getObjectId() == targetObjId) {
				PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_CHANGE_MASTER_ERROR_SELF());
				return false;
			} else if (!legion.isMember(targetObjId)) {
				// not in same legion
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
			if (legion.getBrigadeGeneral() != activePlayer.getObjectId()) {
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

		private boolean canChangeNickname(Legion legion, int targetObjectId, String newNickname) {
			return isValidNickname(newNickname) && legion.isMember(targetObjectId);
		}

		private boolean canChangeAnnouncement(LegionMember legionMember, String announcement) {
			return legionMember.hasRights(LegionPermissionsMask.EDIT) && (announcement.isEmpty() || isValidAnnouncement(announcement));
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

		private boolean isValidAnnouncement(String name) {
			return LegionConfig.ANNOUNCEMENT_PATTERN.matcher(name.replaceAll("\\r\\n", "")).matches();
		}
	}

	public void addWHItemHistory(Player player, int itemId, long count, IStorage sourceStorage, IStorage destStorage) {
		Legion legion = player.getLegion();
		if (legion != null) {
			String description = Integer.toString(itemId) + ":" + Long.toString(count);
			if (sourceStorage.getStorageType() == StorageType.LEGION_WAREHOUSE) {
				LegionService.getInstance().addHistory(legion, player.getName(), LegionHistoryType.ITEM_WITHDRAW, 2, description);
			} else if (destStorage.getStorageType() == StorageType.LEGION_WAREHOUSE) {
				LegionService.getInstance().addHistory(legion, player.getName(), LegionHistoryType.ITEM_DEPOSIT, 2, description);
			}
		}
	}

	private static class SingletonHolder {

		protected static final LegionService instance = new LegionService();
	}

	public boolean hasCenturionPermission(Legion legion, Player player) {
		for (int memberObjId : legion.getLegionMembers()) {
			LegionMember legionMember = LegionService.getInstance().getLegionMember(memberObjId);
			if (legionMember.getRank() == LegionRank.CENTURION && legionMember.getObjectId() == player.getObjectId())
				return true;
		}
		return false;
	}

	public void updateLegionMemberList(Player player, boolean broadcastToLegion) {
		updateLegionMemberList(player, broadcastToLegion, 0);
	}

	public void updateLegionMemberList(Player player, boolean broadcastToLegion, int excludedPlayerId) {
		if (player != null && player.getLegion() != null) {
			Legion legion = player.getLegion();
			List<LegionMemberEx> allMembers = loadLegionMemberExList(legion, excludedPlayerId);
			SplitList<LegionMemberEx> legionMemberSplitList = new FixedElementCountSplitList<>(allMembers, true, 80);
			legionMemberSplitList.forEach(part -> {
				if (broadcastToLegion)
					PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_MEMBERLIST(part, part.isFirst(), part.isLast()));
				else
					PacketSendUtility.sendPacket(player, new SM_LEGION_MEMBERLIST(part, part.isFirst(), part.isLast()));
			});
		}
	}

	public void updateCachedPlayerName(String oldName, Player player) {
		if (player.getLegion() != null && allCachedLegionMembers.containsEx(player.getObjectId()))
			allCachedLegionMembers.updateCachedPlayerName(oldName, player);
	}

	public void updateCachedLegionName(String oldName, Legion legion) {
		allCachedLegions.updateCachedLegionName(oldName, legion);
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
