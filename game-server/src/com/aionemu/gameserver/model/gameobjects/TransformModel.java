package com.aionemu.gameserver.model.gameobjects;

import com.aionemu.gameserver.model.TribeClass;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CUSTOM_SETTINGS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_TRANSFORM;
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
	protected int banUseSkills;
	protected int banMovement;
	protected int res1;
	protected int res2;
	protected int res3;
	protected int res5;
	protected int res6;

	public TransformModel(Creature creature) {
		this.originalType = creature instanceof Player ? TransformType.PC : TransformType.NONE;
		this.transformType = TransformType.NONE;
		this.owner = creature;
	}

	public void apply(int modelId) {
		apply(modelId, originalType, 0, 0, 0, 0, 0, 0, 0, 0);
	}

	/**
	 * Function that activates transform
	 * 
	 * @param modelId
	 * @param type
	 * @param panelId
	 * @param banUseSkills
	 * @param banMovement
	 * @param res1
	 * @param res2
	 * @param res3
	 * @param res5
	 * @param res6
	 */
	public void apply(int modelId, TransformType type, int panelId, int banUseSkills, int banMovement, int res1, int res2, int res3, int res5, int res6) {
		int originalModelId = owner.getObjectTemplate().getTemplateId();
		if (modelId == 0 || modelId == originalModelId) { // reset
			this.modelId = originalModelId;
			this.transformType = originalType;
			this.panelId = 0;
			this.banUseSkills = 0;
			this.banMovement = 0;
			this.res1 = 0;
			this.res2 = 0;
			this.res3 = 0;
			this.res5 = 0;
			this.res6 = 0;
		} else { // set new
			this.modelId = modelId;
			this.transformType = type;
			this.panelId = panelId;
			this.banUseSkills = banUseSkills;
			this.banMovement = banMovement;
			this.res1 = res1;
			this.res2 = res2;
			this.res3 = res3;
			this.res5 = res5;
			this.res6 = res6;
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
		return banUseSkills == 0 && banMovement == 0 && res1 == 0 && res2 == 0 && res3 == 0 && res5 == 0 && res6 == 0;
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
		this.transformTribe = transformTribe;
		this.updateTribeVisually();
	}

	/**
	 * @return the banUseSkills
	 */
	public int getBanUseSkills() {
		return banUseSkills;
	}

	/**
	 * @return the banMovement
	 */
	public int getBanMovement() {
		return banMovement;
	}

	/**
	 * @return the res1
	 */
	public int getRes1() {
		return res1;
	}

	/**
	 * @return the res2
	 */
	public int getRes2() {
		return res2;
	}

	/**
	 * @return the res3
	 */
	public int getRes3() {
		return res3;
	}

	/**
	 * @return the res5
	 */
	public int getRes5() {
		return res5;
	}

	/**
	 * @return the res6
	 */
	public int getRes6() {
		return res6;
	}
}
