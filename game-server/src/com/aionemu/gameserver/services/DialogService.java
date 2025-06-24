package com.aionemu.gameserver.services;

import static com.aionemu.gameserver.model.DialogAction.*;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.configs.main.AutoGroupConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.DialogPage;
import com.aionemu.gameserver.model.autogroup.AutoGroupType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.PetAction;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Mailbox;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.RequestResponseHandler;
import com.aionemu.gameserver.model.siege.FortressLocation;
import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.templates.goods.GoodsList;
import com.aionemu.gameserver.model.templates.npc.TalkInfo;
import com.aionemu.gameserver.model.templates.tradelist.TradeListTemplate;
import com.aionemu.gameserver.model.templates.tradelist.TradeListTemplate.TradeTab;
import com.aionemu.gameserver.model.templates.zone.ZoneClassName;
import com.aionemu.gameserver.model.templates.zone.ZoneTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.*;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.services.craft.CraftSkillUpdateService;
import com.aionemu.gameserver.services.craft.RelinquishCraftStatus;
import com.aionemu.gameserver.services.instance.PeriodicInstanceManager;
import com.aionemu.gameserver.services.item.ItemChargeService;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.services.player.PlayerMailboxState;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.services.trade.PricesService;
import com.aionemu.gameserver.skillengine.model.DispelSlotType;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.zone.ZoneInstance;

/**
 * @author VladimirZ, Neon
 */
public class DialogService {

	private static final Logger log = LoggerFactory.getLogger(DialogService.class);

	public static void onCloseDialog(Player player, VisibleObject target) {
		if (target == null)
			return;

		if (target instanceof Npc npc) {
			npc.getAi().onCreatureEvent(AIEventType.DIALOG_FINISH, player);

			if (npc.getObjectTemplate().supportsAction(DialogAction.OPEN_LEGION_WAREHOUSE) && player.isLegionMember())
				player.getLegion().getLegionWarehouse().unsetInUse(player.getObjectId());
		}

		Mailbox mailbox = player.getMailbox();
		if (mailbox != null && mailbox.mailBoxState != PlayerMailboxState.CLOSED)
			mailbox.mailBoxState = PlayerMailboxState.CLOSED;
	}

	public static void onDialogSelect(int dialogActionId, Player player, Npc npc, int questId, int extendedRewardIndex) {
		int targetObjectId = npc.getObjectId();

		if (questId == 0) {
			switch (dialogActionId) {
				case BUY: {
					TradeListTemplate tradeListTemplate = DataManager.TRADE_LIST_DATA.getTradeListTemplate(npc.getNpcId());
					if (tradeListTemplate == null) {
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_BUY_SELL_HE_DOES_NOT_SELL_ITEM(npc.getObjectTemplate().getL10n()));
						break;
					}
					int tradeModifier = tradeListTemplate.getSellPriceRate();
					int legionLevel = player.getLegion() == null ? 0 : player.getLegion().getLegionLevel();
					boolean hasAnythingToSell = false;
					for (TradeTab tab : tradeListTemplate.getTradeTablist()) {
						GoodsList goodsList = DataManager.GOODSLIST_DATA.getGoodsListById(tab.getId());
						if (goodsList == null || goodsList.getLegionLevel() > legionLevel)
							continue;
						hasAnythingToSell = true;
						break;
					}
					if (hasAnythingToSell)
						PacketSendUtility.sendPacket(player,
							new SM_TRADELIST(player, npc, tradeListTemplate, PricesService.getVendorBuyModifier() * tradeModifier / 100));
					else
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_BUY_SELL_HE_DOES_NOT_SELL_ITEM(npc.getObjectTemplate().getL10n()));
					break;
				}
				case DEPOSIT_CHAR_WAREHOUSE: // warehouse (2.5)
				case OPEN_VENDOR: // Consign trade?? npc karinerk, koorunerk (2.5)
				case OPEN_STIGMA_WINDOW: // stigma
				case CREATE_LEGION: // create legion
				case OPEN_STIGMA_ENCHANT:
				case GIVE_ITEM_PROC: // Godstone socketing (2.5)
				case REMOVE_ITEM_OPTION: // remove manastone (2.5)
				case CHANGE_ITEM_SKIN: // modify appearance (2.5)
				case ITEM_UPGRADE: // item upgrade (4.7)
				case CLOSE_LEGION_WAREHOUSE: // WTF??? Quest dialog packet (2.5)
				case COMBINE_TASK: // crafting (2.5)
				case OPEN_INSTANCE_RECRUIT: // handled by AI
				case INSTANCE_ENTRY: // (2.5)
				case COMPOUND_WEAPON: // armsfusion (2.5)
				case DECOMPOUND_WEAPON: // armsbreaking (2.5)
				case HOUSING_BUILD: // housing build
				case HOUSING_DESTRUCT: // housing destruct
				case HOUSING_PERSONAL_AUCTION: // housing auction
				case CHARGE_ITEM_SINGLE: // condition an individual item
				case CHARGE_ITEM_SINGLE2: // augmenting an individual item
				case TOWN_CHALLENGE: // town improvement
					// Custom Feature: Quests can be done in any village
					sendDialogWindow(dialogActionId, player, npc);
					break;
				case DISPERSE_LEGION: // disband legion
					LegionService.getInstance().requestDisbandLegion(npc, player);
					break;
				case RECREATE_LEGION: // recreate legion
					LegionService.getInstance().recreateLegion(npc, player);
					break;
				case RECOVERY:// soul healing (2.5)
					final long expLost = player.getCommonData().getExpRecoverable();
					if (expLost == 0) {
						player.getEffectController().removeByDispelSlotType(DispelSlotType.SPECIAL2);
						player.getCommonData().setDeathCount(0);
					}
					final double factor = (expLost < 1000000 ? 0.25 - (0.00000015 * expLost) : 0.1);
					final int price = (int) (expLost * factor);

					RequestResponseHandler<Npc> responseHandler = new RequestResponseHandler<>(npc) {

						@Override
						public void acceptRequest(Npc requester, Player responder) {
							if (responder.getInventory().getKinah() >= price) {
								PacketSendUtility.sendPacket(responder, SM_SYSTEM_MESSAGE.STR_GET_EXP2(expLost));
								PacketSendUtility.sendPacket(responder, SM_SYSTEM_MESSAGE.STR_SUCCESS_RECOVER_EXPERIENCE());
								responder.getCommonData().resetRecoverableExp();
								responder.getInventory().decreaseKinah(price, ItemUpdateType.STATS_CHANGE);
								responder.getEffectController().removeByDispelSlotType(DispelSlotType.SPECIAL2);
								responder.getCommonData().setDeathCount(0);
							} else {
								PacketSendUtility.sendPacket(responder, SM_SYSTEM_MESSAGE.STR_MSG_NOT_ENOUGH_KINA(price));
							}
						}
					};
					if (player.getCommonData().getExpRecoverable() > 0) {
						boolean result = player.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_ASK_RECOVER_EXPERIENCE, responseHandler);
						if (result) {
							PacketSendUtility.sendPacket(player,
								new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_ASK_RECOVER_EXPERIENCE, 0, 0, String.valueOf(price)));
						}
					} else {
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DONOT_HAVE_RECOVER_EXPERIENCE());
					}
					break;
				case ENTER_PVP: // (2.5)
					switch (npc.getNpcId()) {
						case 204089: // pvp arena in pandaemonium.
							TeleportService.teleportTo(player, 120010000, 1, 984f, 1543f, 222.1f);
							break;
						case 203764: // pvp arena in sanctum.
							TeleportService.teleportTo(player, 110010000, 1, 1462.5f, 1326.1f, 564.1f);
							break;
						case 203981:
							TeleportService.teleportTo(player, 210020000, 1, 439.3f, 422.2f, 274.3f);
							break;
					}
					break;
				case LEAVE_PVP: // (2.5)
					switch (npc.getNpcId()) {
						case 204087:
							TeleportService.teleportTo(player, 120010000, 1, 1005.1f, 1528.9f, 222.1f);
							break;
						case 203875:
							TeleportService.teleportTo(player, 110010000, 1, 1470.3f, 1343.5f, 563.7f);
							break;
						case 203982:
							TeleportService.teleportTo(player, 210020000, 1, 446.2f, 431.1f, 274.5f);
							break;
					}
					break;
				case AIRLINE_SERVICE: { // flight and teleport (2.5)
					switch (npc.getNpcId()) {
						case 203679: // ishalgen teleporter
						case 203194: // poeta teleporter
							if (!player.getCommonData().isDaeva()) {
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(npc.getObjectId(), DialogPage.NO_RIGHT.id()));
								return;
							}
					}
					TeleportService.showMap(player, npc);
					break;
				}
				case GATHER_SKILL_LEVELUP: // improve extraction (2.5)
				case COMBINE_SKILL_LEVELUP: // learn tailoring armor smithing etc. (2.5)
					CraftSkillUpdateService.getInstance().learnSkill(player, npc);
					break;
				case EXTEND_INVENTORY: // expand cube (2.5)
					CubeExpandService.expandCube(player, npc);
					break;
				case EXTEND_CHAR_WAREHOUSE: // (2.5)
					WarehouseService.expandWarehouse(player, npc);
					break;
				case OPEN_LEGION_WAREHOUSE: // legion warehouse (2.5)
					LegionService.getInstance().openLegionWarehouse(player, npc);
					break;
				case EDIT_CHARACTER_ALL:
				case EDIT_CHARACTER_GENDER: // (2.5) and (4.3)
					PacketSendUtility.sendPacket(player, new SM_PLASTIC_SURGERY(player, dialogActionId == EDIT_CHARACTER_GENDER));
					player.getCommonData().setInEditMode(true);
					break;
				case MATCH_MAKER: // dredgion
					if (AutoGroupConfig.AUTO_GROUP_ENABLE) {
						AutoGroupType agt = AutoGroupType.getAutoGroup(npc.getNpcId());
						if (agt != null && PeriodicInstanceManager.getInstance().isRegistrationOpen(agt.getTemplate().getMaskId()))
							PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(agt.getTemplate().getMaskId()));
					} else {
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(targetObjectId, 1011));
					}
					break;
				case FACTION_JOIN: // join npcFaction (2.5)
					player.getNpcFactions().enterGuild(npc);
					break;
				case FACTION_SEPARATE: // leave npcFaction (2.5)
					player.getNpcFactions().leaveNpcFaction(npc);
					break;
				case BUY_AGAIN: // repurchase (2.5)
					PacketSendUtility.sendPacket(player, new SM_REPURCHASE(player, npc.getObjectId()));
					break;
				case FUNC_PET_ADOPT: // adopt pet (2.5)
					PacketSendUtility.sendPacket(player, new SM_PET(PetAction.TALK_WITH_MERCHANT));
					break;
				case FUNC_PET_ABANDON: // surrender pet (2.5)
					PacketSendUtility.sendPacket(player, new SM_PET(PetAction.TALK_WITH_MINDER));
					break;
				case CHARGE_ITEM_MULTI: // condition all equiped items
					ItemChargeService.startChargingEquippedItems(player, targetObjectId, 1);
					break;
				case TRADE_IN:
					TradeListTemplate tradeListTemplate = DataManager.TRADE_LIST_DATA.getTradeInListTemplate(npc.getNpcId());
					if (tradeListTemplate == null)
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_BUY_SELL_HE_DOES_NOT_SELL_ITEM(npc.getObjectTemplate().getL10n()));
					else
						PacketSendUtility.sendPacket(player, new SM_TRADE_IN_LIST(npc, tradeListTemplate, 100));
					break;
				case SELL:
				case TRADE_SELL_LIST:
					PacketSendUtility.sendPacket(player, new SM_SELL_ITEM(npc));
					break;
				case GIVEUP_CRAFT_EXPERT: // relinquish Expert Status
					RelinquishCraftStatus.relinquishExpertStatus(player, CraftSkillUpdateService.getInstance().getProfessionByNpc(npc));
					break;
				case GIVEUP_CRAFT_MASTER: // relinquish Master Status
					RelinquishCraftStatus.relinquishMasterStatus(player, CraftSkillUpdateService.getInstance().getProfessionByNpc(npc));
					break;
				case FUNC_PET_H_ADOPT:
					PacketSendUtility.sendPacket(player, new SM_PET(PetAction.H_ADOPT));
					break;
				case FUNC_PET_H_ABANDON:
					PacketSendUtility.sendPacket(player, new SM_PET(PetAction.H_ABANDON));
					break;
				case CHARGE_ITEM_MULTI2: // augmenting all equipped items
					ItemChargeService.startChargingEquippedItems(player, targetObjectId, 2);
					break;
				case HOUSING_RECREATE_PERSONAL_INS: // recreate personal house instance (studio)
					HousingService.getInstance().recreatePlayerStudio(player);
					break;
				default:
					// action id = next page id
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(targetObjectId, dialogActionId));
					break;
			}
		} else {
			QuestEnv env = new QuestEnv(npc, player, questId, dialogActionId);
			env.setExtendedRewardIndex(extendedRewardIndex);
			if (QuestEngine.getInstance().onDialog(env))
				return;
			// action id = next page id
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(targetObjectId, dialogActionId, questId));
		}
	}

	private static void sendDialogWindow(int dialogActionId, final Player player, Npc npc) {
		if (npc.getObjectTemplate().supportsAction(dialogActionId))
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(npc.getObjectId(), DialogPage.getByActionId(dialogActionId).id()));
	}

	public static boolean isInteractionAllowed(Player player, Npc npc) {
		if (npc.getSummonOwner() != null && !isSummonOwner(player, npc))
			return false;
		return !isSubDialogRestricted(player, npc);
	}

	private static boolean isSummonOwner(Player player, Npc npc) {
		boolean playerIsCreator = player.getObjectId() == npc.getCreatorId();
		return switch (npc.getSummonOwner()) {
			case PRIVATE -> playerIsCreator;
			case GROUP -> playerIsCreator || player.getCurrentGroup() != null && player.getCurrentGroup().hasMember(npc.getCreatorId());
			case ALLIANCE -> playerIsCreator || player.getCurrentTeam()instanceof PlayerAlliance alliance && alliance.hasMember(npc.getCreatorId());
			case LEGION -> playerIsCreator || player.isLegionMember() && player.getLegion().isMember(npc.getCreatorId());
		};
	}

	private static boolean isSubDialogRestricted(Player player, Npc npc) {
		TalkInfo talkInfo = npc.getObjectTemplate().getTalkInfo();
		if (talkInfo == null)
			return false;
		switch (talkInfo.getSubDialogType()) {
			case ALL_ALLOWED:
				return false;
			case FORT_CAPTURE:
				if (player.getLegion() == null)
					return true;
				ZoneTemplate fortZone = null;
				for (ZoneInstance zone : npc.findZones()) {
					if (zone.getZoneTemplate().getZoneType() != ZoneClassName.FORT)
						continue;
					fortZone = zone.getZoneTemplate();
					break;
				}
				if (fortZone == null) {
					log.warn("Could not find FORT zone for npc: " + npc.getNpcId());
					return true;
				}
				List<Integer> siegeIds = fortZone.getSiegeId();
				for (Integer siegeId : siegeIds) {
					FortressLocation loc = SiegeService.getInstance().getFortress(siegeId);
					if (loc == null)
						continue;
					if (loc.getRace().getRaceId() == player.getRace().getRaceId() && loc.getLegionId() == player.getLegion().getLegionId()) {
						return false;
					}
				}
				return true;
			case SKILL_ID:
				return player.getSkillList().getSkillEntry(talkInfo.getSubDialogValue()) == null;
			case ITEM_ID:
				return player.getInventory().getItemCountByItemId(talkInfo.getSubDialogValue()) == 0;
			case ABYSSRANK:
				if (player.isStaff())
					return false;
				return player.getAbyssRank().getRank().getId() < talkInfo.getSubDialogValue();
			case TARGET_LEGION_DOMINION:
				if (LegionDominionService.getInstance().isInCalculationTime())
					return true;
				if (player.getLegion() != null) {
					return player.getLegion().getCurrentLegionDominion() != talkInfo.getSubDialogValue();
				}
				return true;
			case LEGION_DOMINION_NPC:
				if (player.getLegion() != null) {
					return player.getLegion().getOccupiedLegionDominion() != talkInfo.getSubDialogValue();
				}
				return true;
			default:
				return false;
		}
	}
}
