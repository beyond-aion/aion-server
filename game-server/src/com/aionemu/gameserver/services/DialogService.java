package com.aionemu.gameserver.services;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.AutoGroupConfig;
import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.DialogPage;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.autogroup.AutoGroupType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.PetAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerHouseOwnerFlags;
import com.aionemu.gameserver.model.gameobjects.player.RequestResponseHandler;
import com.aionemu.gameserver.model.siege.FortressLocation;
import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.model.team.legion.LegionWarehouse;
import com.aionemu.gameserver.model.templates.goods.GoodsList;
import com.aionemu.gameserver.model.templates.npc.SubDialogType;
import com.aionemu.gameserver.model.templates.npc.TalkInfo;
import com.aionemu.gameserver.model.templates.portal.PortalPath;
import com.aionemu.gameserver.model.templates.teleport.TeleportLocation;
import com.aionemu.gameserver.model.templates.teleport.TeleporterTemplate;
import com.aionemu.gameserver.model.templates.tradelist.TradeListTemplate;
import com.aionemu.gameserver.model.templates.tradelist.TradeListTemplate.TradeTab;
import com.aionemu.gameserver.model.templates.zone.ZoneClassName;
import com.aionemu.gameserver.model.templates.zone.ZoneTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_AUTO_GROUP;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PET;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLASTIC_SURGERY;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_REPURCHASE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SELL_ITEM;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_TRADELIST;
import com.aionemu.gameserver.network.aion.serverpackets.SM_TRADE_IN_LIST;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.restrictions.RestrictionsManager;
import com.aionemu.gameserver.services.craft.CraftSkillUpdateService;
import com.aionemu.gameserver.services.craft.RelinquishCraftStatus;
import com.aionemu.gameserver.services.instance.periodic.DredgionService2;
import com.aionemu.gameserver.services.item.ItemChargeService;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.services.teleport.PortalService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.services.trade.PricesService;
import com.aionemu.gameserver.skillengine.model.SkillTargetSlot;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.MapRegion;
import com.aionemu.gameserver.world.zone.ZoneInstance;

/**
 * @author VladimirZ
 */
public class DialogService {

	private static final Logger log = LoggerFactory.getLogger(DialogService.class);

	public static void onCloseDialog(Npc npc, Player player) {
		if (checkFuncDialog(DialogAction.OPEN_LEGION_WAREHOUSE.id(), npc)) {
			sendDialogWindow(DialogAction.NULL.id(), player, npc);
			Legion legion = player.getLegion();
			if (legion != null) {
				LegionWarehouse lwh = player.getLegion().getLegionWarehouse();
				if (lwh.getWhUser() == player.getObjectId()) {
					lwh.setWhUser(0);
				}
			}
		}
	}

	public static void onDialogSelect(int dialogId, final Player player, Npc npc, int questId, int extendedRewardIndex) {
		QuestEnv env = new QuestEnv(npc, player, questId, dialogId);
		env.setExtendedRewardIndex(extendedRewardIndex);
		int targetObjectId = npc.getObjectId();

		if (player.getAccessLevel() >= 3 && CustomConfig.ENABLE_SHOW_DIALOGID) {
			PacketSendUtility.sendMessage(player, "dialogId: " + dialogId);
			PacketSendUtility.sendMessage(player, "questId: " + questId);
		}

		if (questId == 0) {
			DialogAction action = DialogAction.getActionByDialogId(dialogId);
			if (action == null) {
				log.warn("Dialog Service: unknown dialog action, dialog id:" + dialogId);
				return;
			}
			switch (DialogAction.getActionByDialogId(dialogId)) {
				case BUY: {
					TradeListTemplate tradeListTemplate = DataManager.TRADE_LIST_DATA.getTradeListTemplate(npc.getNpcId());
					if (tradeListTemplate == null) {
						log.error("Buy list missing for npc " + npc.getNpcId());
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_BUY_SELL_HE_DOES_NOT_SELL_ITEM(npc.getObjectTemplate().getNameId()));
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
						PacketSendUtility.sendPacket(player, new SM_TRADELIST(player, npc.getObjectId(), tradeListTemplate, PricesService.getVendorBuyModifier()
							* tradeModifier / 100));
					else
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_BUY_SELL_HE_DOES_NOT_SELL_ITEM(npc.getObjectTemplate().getNameId()));
					break;
				}
				case DEPOSIT_CHAR_WAREHOUSE: // warehouse (2.5)
					if (!RestrictionsManager.canUseWarehouse(player))
						return;
					sendDialogWindow(dialogId, player, npc);
					break;
				case OPEN_VENDOR: // Consign trade?? npc karinerk, koorunerk (2.5)
				case OPEN_STIGMA_WINDOW: // stigma
				case CREATE_LEGION: // create legion
				case OPEN_STIGMA_ENCHANT:
				case GIVE_ITEM_PROC: // Godstone socketing (2.5)
				case REMOVE_MANASTONE: // remove mana stone (2.5)
				case CHANGE_ITEM_SKIN: // modify appearance (2.5)
				case ITEM_UPGRADE: // item upgrade (4.7)
				case CLOSE_LEGION_WAREHOUSE: // WTF??? Quest dialog packet (2.5)
				case CRAFT: // (2.5)
				case OPEN_INSTANCE_RECRUIT: // TODO NEW INSTANCE FIND GROUP SYSTEM
				case INSTANCE_ENTRY: // (2.5)
				case COMPOUND_WEAPON: // armsfusion (2.5)
				case DECOMPOUND_WEAPON: // armsbreaking (2.5)
				case HOUSING_BUILD: // housing build
				case HOUSING_DESTRUCT: // housing destruct
				case CHARGE_ITEM_SINGLE: // condition an individual item
				case CHARGE_ITEM_SINGLE2: // augmenting an individual item
				case TOWN_CHALLENGE: // town improvement
					// Custom Feature: Quests can be done in any village
					sendDialogWindow(dialogId, player, npc);
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
						player.getEffectController().removeAbnormalEffectsByTargetSlot(SkillTargetSlot.SPEC2);
						player.getCommonData().setDeathCount(0);
					}
					final double factor = (expLost < 1000000 ? 0.25 - (0.00000015 * expLost) : 0.1);
					final int price = (int) (expLost * factor);

					RequestResponseHandler responseHandler = new RequestResponseHandler(npc) {

						@Override
						public void acceptRequest(Creature requester, Player responder) {
							if (player.getInventory().getKinah() >= price) {
								PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GET_EXP2(expLost));
								PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SUCCESS_RECOVER_EXPERIENCE);
								player.getCommonData().resetRecoverableExp();
								player.getInventory().decreaseKinah(price, ItemUpdateType.STATS_CHANGE);
								player.getEffectController().removeAbnormalEffectsByTargetSlot(SkillTargetSlot.SPEC2);
								player.getCommonData().setDeathCount(0);
							} else {
								PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_NOT_ENOUGH_KINA(price));
							}
						}

						@Override
						public void denyRequest(Creature requester, Player responder) {
							// no message
						}
					};
					if (player.getCommonData().getExpRecoverable() > 0) {
						boolean result = player.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_ASK_RECOVER_EXPERIENCE, responseHandler);
						if (result) {
							PacketSendUtility
								.sendPacket(player, new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_ASK_RECOVER_EXPERIENCE, 0, 0, String.valueOf(price)));
						}
					} else {
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DONOT_HAVE_RECOVER_EXPERIENCE);
					}
					break;
				case ENTER_PVP: // (2.5)
					switch (npc.getNpcId()) {
						case 204089: // pvp arena in pandaemonium.
							TeleportService2.teleportTo(player, 120010000, 1, 984f, 1543f, 222.1f);
							break;
						case 203764: // pvp arena in sanctum.
							TeleportService2.teleportTo(player, 110010000, 1, 1462.5f, 1326.1f, 564.1f);
							break;
						case 203981:
							TeleportService2.teleportTo(player, 210020000, 1, 439.3f, 422.2f, 274.3f);
							break;
					}
					break;
				case LEAVE_PVP: // (2.5)
					switch (npc.getNpcId()) {
						case 204087:
							TeleportService2.teleportTo(player, 120010000, 1, 1005.1f, 1528.9f, 222.1f);
							break;
						case 203875:
							TeleportService2.teleportTo(player, 110010000, 1, 1470.3f, 1343.5f, 563.7f);
							break;
						case 203982:
							TeleportService2.teleportTo(player, 210020000, 1, 446.2f, 431.1f, 274.5f);
							break;
					}
					break;
				case AIRLINE_SERVICE: { // flight and teleport (2.5)
					switch (npc.getNpcId()) {
						case 203679: // ishalgen teleporter
						case 203194: // poeta teleporter
							if (!player.getCommonData().isDaeva()) {
								sendDialogWindow(DialogPage.NO_RIGHT.id(), player, npc);
								break;
							}
						default:
							TeleportService2.showMap(player, npc);
					}
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
				case EDIT_CHARACTER:
				case EDIT_GENDER: // (2.5) and (4.3)
					byte changesex = 0; // 0 plastic surgery, 1 gender switch
					byte check_ticket = 2; // 2 no ticket, 1 have ticket
					if (dialogId == DialogAction.EDIT_GENDER.id()) {
						// Gender Switch
						changesex = 1;
						if (player.getInventory().getItemCountByItemId(169660000) > 0 || player.getInventory().getItemCountByItemId(169660001) > 0
							|| player.getInventory().getItemCountByItemId(169660002) > 0 || player.getInventory().getItemCountByItemId(169660003) > 0) {
							check_ticket = 1;
						}
					} else {
						// Plastic Surgery
						if (player.getInventory().getItemCountByItemId(169650000) > 0 || player.getInventory().getItemCountByItemId(169650001) > 0
							|| player.getInventory().getItemCountByItemId(169650002) > 0 || player.getInventory().getItemCountByItemId(169650003) > 0
							|| player.getInventory().getItemCountByItemId(169650004) > 0 || player.getInventory().getItemCountByItemId(169650005) > 0
							|| player.getInventory().getItemCountByItemId(169650006) > 0 || player.getInventory().getItemCountByItemId(169650007) > 0) {
							check_ticket = 1;
						}
					}
					PacketSendUtility.sendPacket(player, new SM_PLASTIC_SURGERY(player, check_ticket, changesex));
					player.getCommonData().setInEditMode(true);
					break;
				case MATCH_MAKER: // dredgion
					if (AutoGroupConfig.AUTO_GROUP_ENABLE && DredgionService2.getInstance().isRegisterAvailable()) {
						AutoGroupType agt = AutoGroupType.getAutoGroup(npc.getNpcId());
						if (agt != null && agt.isDredgion())
							PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(agt.getInstanceMaskId()));
						else
							sendDialogWindow(DialogAction.NULL.id(), player, npc);
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
				case PET_ADOPT: // adopt pet (2.5)
					PacketSendUtility.sendPacket(player, new SM_PET(PetAction.TALK_WITH_MERCHANT));
					break;
				case PET_ABANDON: // surrender pet (2.5)
					PacketSendUtility.sendPacket(player, new SM_PET(PetAction.TALK_WITH_MINDER));
					break;
				case CHARGE_ITEM_MULTI: // condition all equiped items
					ItemChargeService.startChargingEquippedItems(player, targetObjectId, 1);
					break;
				case TRADE_IN: {
					TradeListTemplate tradeListTemplate = DataManager.TRADE_LIST_DATA.getTradeInListTemplate(npc.getNpcId());
					if (tradeListTemplate == null) {
						PacketSendUtility.sendMessage(player, "Buy list is missing!!");
						break;
					}
					PacketSendUtility.sendPacket(player, new SM_TRADE_IN_LIST(npc, tradeListTemplate, 100));
					break;
				}
				case SELL:
				case TRADE_SELL_LIST:
					PacketSendUtility.sendPacket(player, new SM_SELL_ITEM(npc));
					break;
				case GIVEUP_CRAFT_EXPERT: // relinquish Expert Status
					RelinquishCraftStatus.relinquishExpertStatus(player, npc);
					break;
				case GIVEUP_CRAFT_MASTER: // relinquish Master Status
					RelinquishCraftStatus.relinquishMasterStatus(player, npc);
					break;
				case HOUSING_PERSONAL_AUCTION: // housing auction
					if ((player.getBuildingOwnerStates() & PlayerHouseOwnerFlags.BIDDING_ALLOWED.getId()) == 0) {
						if (player.getRace() == Race.ELYOS)
							PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_CANT_OWN_NOT_COMPLETE_QUEST(18802));
						else
							PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_CANT_OWN_NOT_COMPLETE_QUEST(28802));
						return;
					}
					sendDialogWindow(dialogId, player, npc);
					break;
				case FUNC_PET_H_ADOPT:
					PacketSendUtility.sendPacket(player, new SM_PET(PetAction.H_ADOPT));
					break;
				case FUNC_PET_H_ABANDON:
					PacketSendUtility.sendPacket(player, new SM_PET(PetAction.H_ABANDON));
					break;
				case CHARGE_ITEM_MULTI2: // augmenting all equiped items
					ItemChargeService.startChargingEquippedItems(player, targetObjectId, 2);
					break;
				case HOUSING_RECREATE_PERSONAL_INS: // recreate personal house instance (studio)
					HousingService.getInstance().recreatePlayerStudio(player);
					break;
				case SETPRO1:
				case SETPRO2:
				case SETPRO3:
				case TELEPORT_SIMPLE:
					if (QuestEngine.getInstance().onDialog(env)) { // remove this shit after assigning AI portal_dialog
						return;
					}
					TeleporterTemplate template = DataManager.TELEPORTER_DATA.getTeleporterTemplateByNpcId(npc.getNpcId());
					PortalPath portalPath = DataManager.PORTAL2_DATA.getPortalDialog(npc.getNpcId(), dialogId, player.getRace());
					if (portalPath != null) {
						PortalService.port(portalPath, player, targetObjectId);
					} else if (template != null) {
						TeleportLocation loc = template.getTeleLocIdData().getTelelocations().get(0);
						if (loc != null)
							TeleportService2.teleport(template, loc.getLocId(), player, npc, npc.getAi2().getName().equals("general") ? TeleportAnimation.JUMP_IN
								: TeleportAnimation.FADE_OUT_BEAM);
					}
					break;
				default:
					if (QuestEngine.getInstance().onDialog(env)) {
						return;
					}
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(targetObjectId, dialogId));
					break;
			}
		} else {
			if (QuestEngine.getInstance().onDialog(env))
				return;
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(targetObjectId, dialogId, questId));
		}
	}

	private static boolean checkFuncDialog(int dialogId, Npc npc) {
		TalkInfo talkInfo = npc.getObjectTemplate().getTalkInfo();
		if (talkInfo == null || talkInfo.getFuncDialogIds() == null)
			return false;
		if (!talkInfo.getFuncDialogIds().contains(dialogId))
			return false;
		return true;
	}

	private static void sendDialogWindow(int dialogId, final Player player, Npc npc) {
		if (dialogId == DialogPage.NO_RIGHT.id())
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(npc.getObjectId(), DialogPage.NO_RIGHT.id()));
		else if (checkFuncDialog(dialogId, npc)) {
			DialogPage showPage = DialogPage.getPageByAction(dialogId);
			if (showPage != null)
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(npc.getObjectId(), showPage.id()));
		}
	}

	public static boolean isSubDialogRestricted(int dialogId, final Player player, Npc npc) {
		if (npc.getObjectTemplate().getTalkInfo() == null)
			return false;
		SubDialogType subdialog = npc.getObjectTemplate().getTalkInfo().getSubDialogType();
		if (subdialog == SubDialogType.ALL_ALLOWED)
			return false;
		Integer value = npc.getObjectTemplate().getTalkInfo().getSubDialogValue();
		switch (subdialog) {
			case FORT_CAPTURE:
				if (player.getLegion() == null)
					return true;
				MapRegion region = npc.getPosition().getMapRegion();
				List<ZoneInstance> zones = region.getZones(npc);
				ZoneTemplate fortZone = null;
				for (ZoneInstance zone : zones) {
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
				return player.getSkillList().getSkillEntry(value) == null;
			case ITEM_ID:
				return player.getInventory().getItemCountByItemId(value) == 0;
			case ABYSSRANK:
				return player.getAbyssRank().getRank().getId() < value;
			case TARGET_LEGION_DOMINION:
				if (LocalDateTime.now().getDayOfWeek() == DayOfWeek.WEDNESDAY
						&& LocalDateTime.now().getHour() >= 8 && LocalDateTime.now().getHour() <= 10) {
					return true;
				}
				if (player.getLegion() != null) {
					if (player.getLegion().getCurrentLegionDominion() == value) {
						return false;
					}
				}
				return true;
			case LEGION_DOMINION_NPC:
				if (player.getLegion() != null) {
					if (player.getLegion().getOccupiedLegionDominion() == value) {
						return false;
					}
				}
				return true;
			default:
				return false;
		}
	}
}
