package com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.operations;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Mr. Poke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NpcDialogOperation")
public class NpcDialogOperation extends QuestOperation {

	@XmlAttribute(required = true)
	protected int id;
	@XmlAttribute(name = "quest_id")
	protected Integer questId;

	@Override
	public void doOperate(QuestEnv env) {
		Player player = env.getPlayer();
		VisibleObject obj = env.getVisibleObject();
		int qId = env.getQuestId();
		if (questId != null)
			qId = questId;
		if (qId == 0)
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(obj.getObjectId(), id));
		else
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(obj.getObjectId(), id, qId));
	}

}
