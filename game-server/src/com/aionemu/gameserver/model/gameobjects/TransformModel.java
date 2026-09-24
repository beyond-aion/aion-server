package com.aionemu.gameserver.model.gameobjects;

import com.aionemu.gameserver.model.TribeClass;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CUSTOM_SETTINGS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_TRANSFORM;
import com.aionemu.gameserver.services.RecallService;
import com.aionemu.gameserver.services.RecallService.CancelReason;
import com.aionemu.gameserver.skillengine.model.TransformType;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Rolandas
 */
public class TransformModel {

	private Creature owner;

	private int modelId;
	private int eventModelId;
	private TransformType originalType;
	private TransformType transformType;
	private int panelId;
	private TribeClass transformTribe;

	// restrictions
	protected boolean cantUseSkills;
	protected boolean cantMove;
	protected boolean cantRecall;
	protected boolean cantJump;
	protected boolean cantAttack;
	protected boolean cantUseItems;
	protected boolean cantFly;

	public TransformModel(Creature creature) {
		this.originalType = creature instanceof Player ? TransformType.PC : TransformType.NONE;
		this.transformType = TransformType.NONE;
		this.owner = creature;
	}

	public void apply(int modelId) {
		apply(modelId, originalType, 0, false, false, false, false, false, false, false);
	}

	/**
	 * Function that activates transform
	 */
	public void apply(int modelId, TransformType type, int panelId, boolean cantUseSkills, boolean cantMove, boolean cantRecall, boolean cantJump, boolean cantAttack, boolean cantUseItems, boolean cantFly) {
		int originalModelId = owner.getObjectTemplate().getTemplateId();
		if (modelId == 0 || modelId == originalModelId) { // reset
			this.modelId = originalModelId;
			this.transformType = originalType;
			this.panelId = 0;
			this.cantUseSkills = false;
			this.cantMove = false;
			this.cantRecall = false;
			this.cantJump = false;
			this.cantAttack = false;
			this.cantUseItems = false;
			this.cantFly = false;
		} else { // set new
			this.modelId = modelId;
			this.transformType = type;
			this.panelId = panelId;
			this.cantUseSkills = cantUseSkills;
			this.cantMove = cantMove;
			this.cantRecall = cantRecall;
			this.cantJump = cantJump;
			this.cantAttack = cantAttack;
			this.cantUseItems = cantUseItems;
			this.cantFly = cantFly;
		}

		this.updateVisually();
	}

	public void updateVisually() {
		PacketSendUtility.broadcastPacketAndReceive(owner, new SM_TRANSFORM(owner));
	}

	private void updateTribeVisually() {
		if (owner instanceof Npc) {
			Npc npc = (Npc) owner;
			npc.getKnownList().forEachPlayer(player -> {
				PacketSendUtility.sendPacket(player, new SM_CUSTOM_SETTINGS(npc.getObjectId(), 0, npc.getType(player).getId(), 0));
			});
		} else if (owner instanceof Player) {
			Player player = (Player) owner;
			player.getKnownList().forEachNpc(npc -> {
				PacketSendUtility.sendPacket(player, new SM_CUSTOM_SETTINGS(npc.getObjectId(), 0, npc.getType(player).getId(), 0));
			});
		}
	}

	/**
	 * @return the modelId
	 */
	public int getModelId() {
		if (eventModelId == owner.getObjectTemplate().getTemplateId() && transformType == TransformType.PC && isUnrestricted()) { // Player removed visual appearance via Nomorph command
			return eventModelId;
		}
		if (isActive())
			return modelId;
		if (eventModelId > 0)
			return eventModelId;
		else
			return owner.getObjectTemplate().getTemplateId();
	}

	public boolean isUnrestricted() {
		return !cantUseSkills && !cantMove && !cantRecall && !cantJump && !cantAttack && !cantUseItems && !cantFly;
	}

	/**
	 * use this functions for events, when you want players/npcs to have such model(skin) for the whole duration of the event, even after getting for
	 * example Feared, or after using candys etc. You need to set it on the start of the event and then unset with setEventModelId(0)
	 * 
	 * @param eventModelId
	 *          the eventModelId to set
	 */
	public void setEventModelId(int eventModelId) {
		this.eventModelId = eventModelId;
	}

	public int getEventModelId() {
		return this.eventModelId;
	}

	/**
	 * @return the type
	 */
	public TransformType getType() {
		return transformType;
	}

	/**
	 * @return the panelId
	 */
	public int getPanelId() {
		return panelId;
	}

	public boolean isActive() {
		return modelId > 0 && modelId != owner.getObjectTemplate().getTemplateId();
	}

	/**
	 * @return the transformTribe
	 */
	public TribeClass getTribe() {
		return transformTribe;
	}

	/**
	 * @param transformTribe
	 *          the transformTribe to set
	 */
	public void setTribe(TribeClass transformTribe) {
		boolean tribeChanged = this.transformTribe != transformTribe;
		this.transformTribe = transformTribe;
		if (tribeChanged && owner instanceof Player player)
			RecallService.getInstance().cancel(player, CancelReason.CANCELLED);
		this.updateTribeVisually();
	}

	public boolean cantUseSkills() {
		return cantUseSkills;
	}

	public boolean cantMove() {
		return cantMove;
	}

	public boolean cantRecall() {
		return cantRecall;
	}

	public boolean cantJump() {
		return cantJump;
	}

	public boolean cantAttack() {
		return cantAttack;
	}

	public boolean cantUseItems() {
		return cantUseItems;
	}

	public boolean cantFly() {
		return cantFly;
	}
}
