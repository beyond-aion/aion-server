package ai.instance.drakenspire;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * Guide:
 * Lv 2 (Normal Mode) specifics:
 * - Features more HP and damage compared to Lv 1 (Easy Mode)
 * - When players remove a buff, Beritra will spawn three "Drakenspire Pustule"
 * - Helper NPCs will only spawn when the third phase begins (either by reaching the 11-minute mark or dropping below 36% HP)
 * - Helper NPCs will only remove "Everlasting Life"
 * <br/>
 * <br/>
 * Drakenspire Pustule (Poison minion):
 * - Follows a player for 20s
 * - Dies after 20s, leaving a poison skill area that deals damage and debuffs players within it
 *
 * @author Estrayl
 */
@AIName("drakenspire_lv2_human_beritra")
public class Lv2HumanBeritraAI extends Lv1HumanBeritraAI {

	private final AtomicInteger sealsBlasted = new AtomicInteger();

	public Lv2HumanBeritraAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleFightStarted() {
		// Don't do anything here.
	}

	protected void handleThirdPhaseStarted() {
		if (getEffectController().findBySkillId(21612) != null)
			spawnFactionHelpers(getAttackingPlayerRace() == Race.ELYOS ? List.of(209734, 209735, 209735) : List.of(209799, 209800, 209800));
	}

	protected void handleLastSealBlasted() {
		// Don't do anything here.
	}

	@Override
	public void onEffectApplied(Effect effect) {
		if (effect.getSkillId() == 21624) { // Dragon Lord Seal
			switch (sealsBlasted.incrementAndGet()) {
				case 1 -> {
					PacketSendUtility.broadcastMessage(getOwner(), 1501272); // You insects think you have a chance against me?
					spawnPustules();
				}
				case 2 -> {
					PacketSendUtility.broadcastMessage(getOwner(), 1501271); // Fregion! You left me to clean up your mess...
					spawnPustules();
				}
				case 3 -> handleLastSealBlasted();
			}
		}
		super.onEffectApplied(effect);
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		if (skillTemplate.getSkillId() == 21609 && skillLevel == 58)
			handleThirdPhaseStarted();
		super.onEndUseSkill(skillTemplate, skillLevel);
	}

	private void spawnPustules() {
		spawn(855446, 135.192f, 502.221f, 1749.448f, (byte) 0); // Drakenspire Pustule
		spawn(855446, 167.133f, 502.114f, 1749.448f, (byte) 30); // Drakenspire Pustule
		spawn(855446, 161.501f, 535.842f, 1749.367f, (byte) 90); // Drakenspire Pustule
	}

	protected void resetBlastedSeal() {
		sealsBlasted.set(0);
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		resetBlastedSeal();
	}
}
