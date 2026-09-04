package com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.operations;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.services.teleport.TeleportService;

/**
 * Teleports the player, staying in his current world when no world id is given.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TeleportOperation")
public class TeleportOperation extends QuestOperation {

	@XmlAttribute(name = "world_id")
	protected int worldId;
	@XmlAttribute(required = true)
	protected float x;
	@XmlAttribute(required = true)
	protected float y;
	@XmlAttribute(required = true)
	protected float z;
	@XmlAttribute
	protected byte h;

	@Override
	public void doOperate(QuestEnv env) {
		Player player = env.getPlayer();
		TeleportService.teleportTo(player, worldId == 0 ? player.getWorldId() : worldId, x, y, z, h);
	}
}
