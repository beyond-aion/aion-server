package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.skillengine.model.TransformType;

/**
 * @author Sweetkr, xTz, kecimis
 */
public class SM_TRANSFORM extends AionServerPacket {

	private Creature creature;

	// testing stuff
	private boolean custom = false;
	private int modelId;
	private int panelId;
	private TransformType type;
	private int unk1, unk2, unk3, unk4, unk5, unk6, unk7;

	public SM_TRANSFORM(Creature creature) {
		this.creature = creature;
	}

	// for testing
	public SM_TRANSFORM(Creature creature, int modelId, int unk7, TransformType type, int unk1, int unk2, int unk3, int unk4, int unk5, int unk6,
		int panelId) {
		this.creature = creature;
		this.modelId = modelId;
		this.unk7 = unk7;
		this.type = type;
		this.unk1 = unk1;
		this.unk2 = unk2;
		this.unk3 = unk3;
		this.unk4 = unk4;
		this.unk5 = unk5;
		this.unk6 = unk6;
		this.panelId = panelId;
		this.custom = true;
	}

	/**
	 * structure SM_TRANSFORM D - objectId D - modelId (res9) 100% H - state F - 0,25f F - 2.0f C - cannotuseskill(res7) 100%- not used for FORM1 D -
	 * transformTypeId (res8) 100% C - (res6) cannot fly/glide? C - cannot useitem (res5) C - attack disabled (res3) C - jump disabled (res2?) C -
	 * summon disabled? (res1?) C - move disabled(res13) 100% D - panelId(res4) 100% cant mount - client recognises it by modelId? TODO server side
	 * checks for mounts
	 */
	@Override
	protected void writeImpl(AionConnection con) {
		if (custom) {
			writeD(creature.getObjectId());
			writeD(modelId);
			writeH(creature.getState());
			writeF(0.25f);
			writeF(2.0f);
			writeC(unk7);
			writeD(type.getId());
			writeC(unk1);
			writeC(unk2);
			writeC(unk3);
			writeC(unk4);
			writeC(unk5);
			writeC(unk6);
			writeD(panelId); // display panel
		} else {
			writeD(creature.getObjectId());
			writeD(creature.getTransformModel().getModelId());
			writeH(creature.getState());
			writeF(0.25f);
			writeF(2.0f);
			writeC(creature.getTransformModel().getBanUseSkills());
			writeD(creature.getTransformModel().getType().getId());
			writeC(creature.getTransformModel().getRes6());
			writeC(creature.getTransformModel().getRes5());
			writeC(creature.getTransformModel().getRes3());
			writeC(creature.getTransformModel().getRes2());
			writeC(creature.getTransformModel().getRes1());
			writeC(creature.getTransformModel().getBanMovement());
			writeD(creature.getTransformModel().getPanelId()); // display panel
		}
	}
}
