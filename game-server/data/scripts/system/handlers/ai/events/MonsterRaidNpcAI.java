package ai.events;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;

import ai.AggressiveNpcAI;

/**
 * @author Whoop
 */
@AIName("monsterraid")
public class MonsterRaidNpcAI extends AggressiveNpcAI {

	public MonsterRaidNpcAI(Npc owner) {
		super(owner);
	}
	// TODO: Interesting Fight
}
