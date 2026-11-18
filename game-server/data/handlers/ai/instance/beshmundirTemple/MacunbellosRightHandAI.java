package ai.instance.beshmundirTemple;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

import ai.AggressiveNpcAI;

/**
 * Macunbello's Right Hand, retail AI IDCT_SumLich.
 * <br>
 * Curses a random attacker with Curse of Souls every 12s, switches to that target and
 * tells Macunbello to devour the cursed soul. Despawns when leaving combat.
 *
 * @author Estrayl
 */
@AIName("macunbellos_right_hand")
public class MacunbellosRightHandAI extends AggressiveNpcAI {

	private static final int CURSE_OF_SOULS = 19050;

	public MacunbellosRightHandAI(Npc owner) {
		super(owner);
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		super.onEndUseSkill(skillTemplate, skillLevel);
		if (skillTemplate.getSkillId() != CURSE_OF_SOULS || !(getOwner().getTarget() instanceof Creature cursed) || cursed.isDead())
			return;
		getAggroList().addHate(cursed, 10000);
		if (getOwner().getCreator() instanceof Npc macunbello && !macunbello.isDead())
			macunbello.getAi().onCustomEvent(MacunbelloAI.EVENT_SOUL_CURSED, cursed);
	}

	@Override
	protected void handleBackHome() {
		getOwner().getController().delete();
	}
}
