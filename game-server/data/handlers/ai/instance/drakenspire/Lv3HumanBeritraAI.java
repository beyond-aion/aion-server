package ai.instance.drakenspire;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * Guide:
 * Lv 3 (Hard Mode) specifics:
 * - Features even more HP and damage compared to Lv 2 (Normal Mode)
 * - When players remove a buff, Beritra will spawn three "Drakenspire Pustule"
 * - There will be no helper NPC spawn
 * - When all three buffs are removed, he will change into his dragon form.
 * - As a secondary option, he can be brought down to 40% HP and will also transform into his dragon form
 * - Players will only have 11 minutes before he basically resets the fight (re-buffing & healing)
 * <br/>
 * <br/>
 * The most effective way to deal with this is to remove the three buffs ASAP and not dealing major damage,
 * avoiding starting a new phase
 * 
 * @author Estrayl
 */
@AIName("drakenspire_lv3_human_beritra")
public class Lv3HumanBeritraAI extends Lv2HumanBeritraAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(40);

	public Lv3HumanBeritraAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleLastSealBlasted() {
		getOwner().queueSkill(20842, 56);
	}

	/**
	 * Can only happen if the fight takes longer than 11 minutes or HP reaches below 36%.
	 */
	@Override
	protected void handleThirdPhaseStarted() {
		if (getEffectController().findBySkillId(21611) != null)
			SkillEngine.getInstance().applyEffectDirectly(21611, getOwner(), getOwner());
		if (getEffectController().findBySkillId(21610) != null)
			SkillEngine.getInstance().applyEffectDirectly(21610, getOwner(), getOwner());
		PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_VRITRA_HUMAN_03());
		getLifeStats().setCurrentHp(getLifeStats().getMaxHp());
		getOwner().getGameStats().setFightStartingTime();
		resetBlastedSeal();
	}

	@Override
	protected void handleAttack(Creature creature) {
		hpPhases.tryEnterNextPhase(this);
		super.handleAttack(creature);
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		if (skillTemplate.getSkillId() == 20842) {
			// Retail would apply this through activate_skillarea
			getPosition().getWorldMapInstance().forEachPlayer(p -> SkillEngine.getInstance().applyEffectDirectly(20842, getOwner(), p));
			handleTransformation();
		}
	}

	private void handleTransformation() {
		getPosition().getWorldMapInstance().getNpcs(702695, 702696).forEach(npc -> npc.getController().die()); // Collapse ceiling and ambient light
		spawn(236247, 123.055f, 519.190f, 1750.3414f, (byte) 0); // Dragon Form
		AIActions.deleteOwner(this);
	}

	@Override
	public void handleHpPhase(int phaseHpPercent) {
		handleLastSealBlasted(); // Using the same logic here
	}
}
