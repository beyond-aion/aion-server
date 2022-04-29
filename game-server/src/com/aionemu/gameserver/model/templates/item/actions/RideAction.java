package com.aionemu.gameserver.model.templates.item.actions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.actions.PlayerMode;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.ride.RideInfo;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.zone.ZoneInstance;

/**
 * @author Rolandas, ginho1
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RideAction")
public class RideAction extends AbstractItemAction {

	@XmlAttribute(name = "npc_id")
	protected int npcId;

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem, Object... params) {
		if (!player.isInPlayerMode(PlayerMode.RIDE)) { // RideAction is for mounting and dismounting, canAct should never forbid dismounting
			if (parentItem == null)
				return false;

			if (CustomConfig.ENABLE_RIDE_RESTRICTION) {
				for (ZoneInstance zone : player.findZones()) {
					if (!zone.canRide()) {
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANNOT_RIDE_INVALID_LOCATION());
						return false;
					}
				}
			}
			if (player.isInState(CreatureState.RESTING)) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_RIDE(ChatUtil.l10n(1400057)));
				return false;
			}
			if (player.getEffectController().isInAnyAbnormalState(AbnormalState.DISMOUNT_RIDE)) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANNOT_RIDE_ABNORMAL_STATE());
				return false;
			}
		}
		return true;
	}

	@Override
	public void act(final Player player, final Item parentItem, Item targetItem, Object... params) {
		player.getController().cancelUseItem();
		if (player.isInPlayerMode(PlayerMode.RIDE)) {
			player.unsetPlayerMode(PlayerMode.RIDE);
			return;
		}

		PacketSendUtility.broadcastPacket(player,
			new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), 3000, 0, 0), true);
		final ItemUseObserver observer = new ItemUseObserver() {

			@Override
			public void abort() {
				player.getController().cancelTask(TaskId.ITEM_USE);
				player.removeItemCoolDown(parentItem.getItemTemplate().getUseLimits().getDelayId());
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ITEM_CANCELED());
				PacketSendUtility.broadcastPacket(player,
					new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), 0, 3, 0), true);
				player.getObserveController().removeObserver(this);
			}

		};

		player.getObserveController().attach(observer);
		player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				player.unsetState(CreatureState.ACTIVE);
				player.setState(CreatureState.RESTING);
				if (player.isInFlyingState())
					player.setState(CreatureState.FLOATING_CORPSE);
				player.getObserveController().removeObserver(observer);
				ItemTemplate itemTemplate = parentItem.getItemTemplate();
				player.setPlayerMode(PlayerMode.RIDE, getRideInfo());
				PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.CHANGE_SPEED, 0, 0), true);
				PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.RIDE, 0, getRideInfo().getNpcId()), true);
				PacketSendUtility.broadcastPacket(player,
					new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), 0, 1, 1), true);
				player.getController().cancelTask(TaskId.ITEM_USE);
				QuestEngine.getInstance().rideAction(new QuestEnv(null, player, 0), itemTemplate.getTemplateId());
			}

		}, 3000));

		ActionObserver rideObserver = new ActionObserver(ObserverType.ABNORMALSETTED) {

			@Override
			public void abnormalsetted(AbnormalState state) {
				if ((state.getId() & AbnormalState.DISMOUNT_RIDE.getId()) != 0) {
					player.unsetPlayerMode(PlayerMode.RIDE);
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_UNRIDE_ABNORMAL_STATE());
				}
			}
		};
		player.getObserveController().addObserver(rideObserver);
		player.setRideObservers(rideObserver);

		// TODO some mounts have lower chance of dismounting
		ActionObserver attackedObserver = new ActionObserver(ObserverType.ATTACKED) {

			@Override
			public void attacked(Creature creature, int skillId) {
				if (Rnd.chance() < 20)// 20% from client action file
					player.unsetPlayerMode(PlayerMode.RIDE);
			}
		};
		player.getObserveController().addObserver(attackedObserver);
		player.setRideObservers(attackedObserver);

		ActionObserver dotAttackedObserver = new ActionObserver(ObserverType.DOT_ATTACKED) {

			@Override
			public void dotattacked(Creature creature, Effect dotEffect) {
				if (Rnd.chance() < 20)// 20% from client action file
					player.unsetPlayerMode(PlayerMode.RIDE);
			}
		};
		player.getObserveController().addObserver(dotAttackedObserver);
		player.setRideObservers(dotAttackedObserver);
	}

	public RideInfo getRideInfo() {
		return DataManager.RIDE_DATA.getRideInfo(npcId);
	}

}
