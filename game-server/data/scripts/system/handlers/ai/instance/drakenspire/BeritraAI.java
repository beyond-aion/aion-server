package ai.instance.drakenspire;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;

import ai.AggressiveNpcAI;

/**
 * Possible NPC IDs 236245, 236246, 236247
 * Possible skill IDs 21610, 21611, 21612
 */
@AIName("beritra")
public class BeritraAI extends AggressiveNpcAI {

	public BeritraAI(Npc owner) {
		super(owner);
	}
}
