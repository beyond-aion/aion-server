package ai;

import java.util.HashMap;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AI2Request;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.skill.PlayerSkillEntry;
import com.aionemu.gameserver.model.skill.PlayerSkillList;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SKILL_COOLDOWN;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.action.Action;
import com.aionemu.gameserver.skillengine.action.DpUseAction;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.skillengine.model.TransformType;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;


/**
 * @author Yeats
 *
 */
@AIName("customcdreset")
public class SkillCooltimeResetAI2 extends NpcAI2 {
	//npc_id: 205517
	int price = 50000; //=50.000 Kinah
	int maxCooldownTime = 3000; //=5min -> skills with a cd >5min are ignored
	
	@Override
	protected void handleDialogStart(Player player) {
		if (player.getController().isInCombat()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_CAN_NOT_ACT_WHILE_IN_ABNORMAL_STATE);
		} else if (player.getSkillCoolDowns().isEmpty()) {
			PacketSendUtility.sendPacket(player, new SM_MESSAGE(getOwner().getObjectId(), getOwner().getName(), "Daeva has no skill cooldowns to reset, yang." , ChatType.NORMAL));
		} else if (player.isTransformed() && player.getTransformModel().getType() == TransformType.AVATAR) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_CAN_NOT_ACT_WHILE_IN_ABNORMAL_STATE);
		} else {
			if (Rnd.get(101) <= 10) {
				PacketSendUtility.sendPacket(player, new SM_MESSAGE(getOwner().getObjectId(), getOwner().getName(), "I can reset your skill cooldowns for 50.000 Kinah, yang yang." , ChatType.NORMAL));
			}
			sendRequest(player);
		}
	}
	
	private void sendRequest(Player player) {
		AI2Actions.addRequest(this, player, 1300765, 0, new AI2Request() {

			@Override
			public void acceptRequest(Creature requester, Player responder, int requestId) {
				if (responder.getWorldId() == requester.getWorldId()) {
					if (responder.getInventory().getKinah() < price) {
						PacketSendUtility.sendPacket(responder, SM_SYSTEM_MESSAGE.STR_MSG_NOT_ENOUGH_KINA(price));
					} else if (MathUtil.getDistance(requester, responder) > 5) {
						PacketSendUtility.sendPacket(responder, SM_SYSTEM_MESSAGE.STR_WAREHOUSE_TOO_FAR_FROM_NPC);
					} else if (responder.isTransformed() && responder.getTransformModel().getType() == TransformType.AVATAR) {
						PacketSendUtility.sendPacket(responder, SM_SYSTEM_MESSAGE.STR_SKILL_CAN_NOT_ACT_WHILE_IN_ABNORMAL_STATE);
					} else {
				
						HashMap<Integer, Long> resetSkillCoolDowns = new HashMap<>();
						
						PlayerSkillList skillList = responder.getSkillList();
						for (PlayerSkillEntry skill : skillList.getAllSkills()) {
							skillId = skill.getSkillId();
							SkillTemplate st =  DataManager.SKILL_DATA.getSkillTemplate(skillId);
							
							if (st != null && st.getCooldown() <= maxCooldownTime) {
								if (!st.isDeityAvatar()) {
									boolean hasDpAction = false;
							
									if (st.getActions() != null) {
										for (Action ac : st.getActions().getActions()) {
											if (ac instanceof DpUseAction) {
												hasDpAction = true;
												break;
											}
										}	
									}
							
									if (!hasDpAction) {
										if ((responder.getSkillCoolDown(st.getCooldownId()) - System.currentTimeMillis()) > 0) {
											responder.setSkillCoolDown(st.getCooldownId(), System.currentTimeMillis());
											resetSkillCoolDowns.put(st.getCooldownId(), System.currentTimeMillis());
										}
									}
								}
							}
						}
				
						if (resetSkillCoolDowns.size() > 0 ) {
							if (responder.getInventory().tryDecreaseKinah(price)) {
								responder.getEffectController().removeAllEffects();
								if (responder.getSummon() != null) {
									responder.getSummon().getEffectController().removeAllEffects();
								}
								PacketSendUtility.sendPacket(responder, new SM_SKILL_COOLDOWN(resetSkillCoolDowns));
							} else {
								PacketSendUtility.sendPacket(responder, SM_SYSTEM_MESSAGE.STR_MSG_NOT_ENOUGH_KINA(price));
							}
						}
					}
				} else {
					PacketSendUtility.sendPacket(responder, SM_SYSTEM_MESSAGE.STR_WAREHOUSE_TOO_FAR_FROM_NPC);
				}	
			}
		}, price);
	}
}