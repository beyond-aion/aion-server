package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Map.Entry;

import com.aionemu.gameserver.controllers.movement.CreatureMoveController;
import com.aionemu.gameserver.model.CreatureType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.ItemSlot;
import com.aionemu.gameserver.model.items.NpcEquippedGear;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.TownService;

/**
 * This packet is displaying visible npc/monsters.
 *
 * @author -Nemesiss-
 */
public class SM_NPC_INFO extends AionServerPacket {

	private Creature npc;
	private int creatorId;
	private String masterName;
	private CreatureType creatureType;

	public SM_NPC_INFO(Npc npc, Player player) {
		this.npc = npc;
		creatorId = npc.getCreatorId();
		masterName = npc.getMasterName();
		creatureType = npc.getType(player);
	}

	public SM_NPC_INFO(Summon summon, Player player) {
		this.npc = summon;
		Player owner = summon.getMaster();
		creatorId = owner.getObjectId();
		masterName = owner.getName();
		creatureType = summon.getType(player);
	}

	@Override
	protected void writeImpl(AionConnection con) {
		NpcTemplate npcTemplate = (NpcTemplate) npc.getObjectTemplate();
		CreatureMoveController<?> mc = npc.getMoveController();

		writeF(npc.getX());
		writeF(npc.getY());
		writeF(npc.getZ());
		writeD(npc.getObjectId());
		writeD(npcTemplate.getTemplateId()); // npc id reference for hp gauge + talk properties
		writeD(npcTemplate.getTemplateId()); // npc id reference for visual appearance
		writeC(creatureType.getId());

		/*
		 * 3,19 - wings spread
		 * 5,6,11,21 - sitting
		 * 7,23,71 - dead, no drop
		 * 8,24 - dead, looks like some orb of light (no normal mesh)
		 * 32,33 - fight mode
		 * 65 - normal
		 */
		writeH(npc.getState());

		writeC(npc.getHeading());
		writeD(npcTemplate.getL10nId());
		writeD(npcTemplate.getTitleId());// TODO: implement fortress titles

		writeH(0x00);// unk
		writeC(0x00);// unk
		writeD(0x00);// unk

		/*
		 * Creator/Master Info (Summon, Kisk, Etc)
		 */
		writeD(creatorId);// creatorId - playerObjectId or House address
		writeS(masterName);// masterName

		writeC((byte) (100f * npc.getLifeStats().getCurrentHp() / npc.getLifeStats().getMaxHp()));// %hp
		writeD(npc.getGameStats().getMaxHp().getCurrent());
		writeC(npc.getLevel());

		NpcEquippedGear gear = npc.getOverrideEquipment(); // dynamically overriden Equipment (only for NPCs, not summons)
		if (gear == null) {
			writeD(0x00);
		} else {
			writeD(gear.getItemsMask());
			boolean hasWeapon = false;
			// getting it from template (later if we make sure that npcs actually use items, we'll make Item from it)
			for (Entry<ItemSlot, ItemTemplate> item : gear) {
				if (!hasWeapon)
					hasWeapon = item.getValue().isWeapon();
				writeD(item.getValue().getTemplateId());
				writeD(0x00);
				writeD(0x00);
				writeH(0x00);
				writeH(0x00); // 4.7
			}
		}
		writeF(npcTemplate.getBoundRadius().getMaxOfFrontAndSide());
		writeF(npcTemplate.getHeight());
		writeF(npc.getGameStats().getMovementSpeedFloat());// speed
		writeH(npcTemplate.getAttackSpeed());
		writeH(npcTemplate.getAttackSpeed());
		writeC(npc.isFlag() ? 0x13 : npc.isNewSpawn() ? 0x01 : 0x00);
		writeF(mc.getTargetX2());
		writeF(mc.getTargetY2());
		writeF(mc.getTargetZ2());
		writeC(mc.getMovementMask()); // move type
		writeH(npc.getSpawn() == null ? 0 : npc.getSpawn().getStaticId());
		writeC(0);
		writeC(0); // all unknown
		writeC(0);
		writeC(0);
		writeC(0);
		writeC(0);
		writeC(0);
		writeC(0);
		writeC(npc.getVisualState()); // visualState
		writeH(npc.getNpcObjectType().getId());
		writeC(0x00); // unk
		writeD(npc.getTarget() == null ? 0 : npc.getTarget().getObjectId());
		writeD(TownService.getInstance().getTownIdByPosition(npc));
		writeD(0);// unk 4.7.5
	}
}
