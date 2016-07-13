package com.aionemu.gameserver.dataholders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.templates.npc.NpcTemplate;

import gnu.trove.procedure.TObjectProcedure;

/**
 * @author MrPoke
 * @modified Neon
 */
public class NpcDropData {

	private static final Logger log = LoggerFactory.getLogger(DataManager.class);

	public static void load() {
		int npcCount = 0;
		for (int npcId : DataManager.CUSTOM_NPC_DROP.getNpcIds()) {
			NpcTemplate npcTemplate = DataManager.NPC_DATA.getNpcTemplate(npcId);
			if (npcTemplate == null) {
				log.warn("Invalid npc ID " + npcId + " for custom drop.");
				continue;
			}
			npcTemplate.setNpcDrop(DataManager.CUSTOM_NPC_DROP.getNpcDrop(npcId));
			npcCount++;
		}
		log.info("Drop loader: Loaded custom npc drops for " + npcCount + " npcs.");
	}

	public static void reload() {
		DataManager.NPC_DATA.getNpcData().forEachValue(new TObjectProcedure<NpcTemplate>() {

			@Override
			public boolean execute(NpcTemplate npcTemplate) {
				npcTemplate.setNpcDrop(null);
				return true;
			}
		});
		load();
	}
}
