package com.aionemu.gameserver.custom.instance.neuralnetwork;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Persistable;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillSubType;
import com.aionemu.gameserver.utils.PositionUtil;

/**
 * @author Jo
 */
public class PlayerModelEntry implements Persistable {

	private PersistentState persistentState;

	private Timestamp timestamp;
	private float timeCDdone;
	private int skillID; // used for output (binary array)
	private int playerID; // used for selection
	private int playerClassID;
	private float playerHPpercentage, playerMPpercentage; // used for input

	private boolean playerIsRooted, playerIsSilenced, playerIsBound, playerIsStunned, playerIsAetherhold; // used for input
	private int playerBuffCount; // used for input
	private int playerDebuffCount; // used for input
	private boolean playerIsShielded; // used for input

	private float targetHPpercentage, targetMPpercentage; // used for input
	private boolean targetFocusesPlayer; // used for input
	private float distance; // used for input
	private boolean targetIsRooted, targetIsSilenced, targetIsBound, targetIsStunned, targetIsAetherhold; // used for input
	private int targetBuffCount, targetDebuffCount; // used for input
	private boolean targetIsShielded; // used for input

	// live constructor
	public PlayerModelEntry(Creature playerOrBoss, int skillID, Creature target) {
		timestamp = new Timestamp(System.currentTimeMillis());

		this.skillID = skillID;
		playerID = playerOrBoss.getObjectId();
		playerClassID = playerOrBoss instanceof Player ? ((Player) playerOrBoss).getPlayerClass().getClassId() : -1;
		playerHPpercentage = playerOrBoss.getLifeStats().getHpPercentage();
		playerMPpercentage = playerOrBoss.getLifeStats().getMpPercentage();

		playerIsRooted = playerOrBoss.getEffectController().isAbnormalSet(AbnormalState.ROOT);
		playerIsSilenced = playerOrBoss.getEffectController().isAbnormalSet(AbnormalState.SILENCE);
		playerIsBound = playerOrBoss.getEffectController().isAbnormalSet(AbnormalState.BIND);
		playerIsStunned = playerOrBoss.getEffectController().isAbnormalSet(AbnormalState.ANY_STUN);
		playerIsAetherhold = playerOrBoss.getEffectController().isAbnormalSet(AbnormalState.OPENAERIAL);
		playerIsShielded = playerOrBoss.getEffectController().isUnderNormalShield();

		playerBuffCount = 0;
		playerDebuffCount = 0;
		for (Effect e : playerOrBoss.getEffectController().getAbnormalEffects()) {
			if ((e.getSkillTemplate() != null && e.getSkillTemplate().getSubType() == SkillSubType.BUFF) || // buff skills
				(e.getSkill() != null && e.getSkill().getItemObjectId() != 0)) // buff items
				playerBuffCount++;
			else // debuffs
				playerDebuffCount++;
		}
		if (target != null) {
			if (target.getLifeStats().getMaxHp() > 0)
				targetHPpercentage = target.getLifeStats().getHpPercentage();
			else
				targetHPpercentage = 0;
			if (target.getLifeStats().getMaxMp() > 0)
				targetMPpercentage = target.getLifeStats().getMpPercentage();
			else
				targetMPpercentage = 0;
			targetFocusesPlayer = target.getTarget() == playerOrBoss;
			distance = (float) PositionUtil.getDistance(playerOrBoss, target);
			targetIsRooted = target.getEffectController().isAbnormalSet(AbnormalState.ROOT);
			targetIsSilenced = target.getEffectController().isAbnormalSet(AbnormalState.SILENCE);
			targetIsBound = target.getEffectController().isAbnormalSet(AbnormalState.BIND);
			targetIsStunned = target.getEffectController().isAbnormalSet(AbnormalState.ANY_STUN);
			targetIsAetherhold = target.getEffectController().isAbnormalSet(AbnormalState.OPENAERIAL);
			targetIsShielded = target.getEffectController().isUnderNormalShield();

			targetBuffCount = 0;
			targetDebuffCount = 0;
			for (Effect e : target.getEffectController().getAbnormalEffects())
				if ((e.getSkillTemplate() != null && e.getSkillTemplate().getSubType() == SkillSubType.BUFF) || // buff skills
					(e.getSkill() != null && e.getSkill().getItemObjectId() != 0)) // buff items
					targetBuffCount++;
				else // debuffs
					targetDebuffCount++;
		} else {
			targetBuffCount = targetDebuffCount = -1;
			targetHPpercentage = distance = targetMPpercentage = -1;
			targetFocusesPlayer = targetIsRooted = targetIsSilenced = targetIsBound = targetIsStunned = targetIsAetherhold = targetIsShielded = false;
		}
		setPersistentState(PersistentState.NEW);
	}

	// constructor from persistence
	public PlayerModelEntry(int playerID, Timestamp timestamp, int skillID, int playerClassID, float playerHPpercentage, float playerMPpercentage,
		boolean playerIsRooted, boolean playerIsSilenced, boolean playerIsBound, boolean playerIsStunned, boolean playerIsAetherhold, int playerBuffCount,
		int playerDebuffCount, boolean playerIsShielded, float targetHPpercentage, float targetMPpercentage, boolean targetFocusesPlayer, float distance,
		boolean targetIsRooted, boolean targetIsSilenced, boolean targetIsBound, boolean targetIsStunned, boolean targetIsAetherhold, int targetBuffCount,
		int targetDebuffCount, boolean targetIsShielded) {
		this.timestamp = timestamp;
		this.skillID = skillID;
		this.playerID = playerID;
		this.playerClassID = playerClassID;
		this.playerHPpercentage = playerHPpercentage;
		this.playerMPpercentage = playerMPpercentage;
		this.playerIsRooted = playerIsRooted;
		this.playerIsSilenced = playerIsSilenced;
		this.playerIsBound = playerIsBound;
		this.playerIsStunned = playerIsStunned;
		this.playerIsAetherhold = playerIsAetherhold;
		this.playerBuffCount = playerBuffCount;
		this.playerDebuffCount = playerDebuffCount;
		this.playerIsShielded = playerIsShielded;
		this.targetHPpercentage = targetHPpercentage;
		this.targetMPpercentage = targetMPpercentage;
		this.targetFocusesPlayer = targetFocusesPlayer;
		this.distance = distance;
		this.targetIsRooted = targetIsRooted;
		this.targetIsSilenced = targetIsSilenced;
		this.targetIsBound = targetIsBound;
		this.targetIsStunned = targetIsStunned;
		this.targetIsAetherhold = targetIsAetherhold;
		this.targetBuffCount = targetBuffCount;
		this.targetDebuffCount = targetDebuffCount;
		this.targetIsShielded = targetIsShielded;
		setPersistentState(PersistentState.UPDATED);
	}

	public double[] toStateInputArray(List<Integer> skillSet, int previousSkillID) {
		List<Double> input = new ArrayList<>();
		input.add((double) playerHPpercentage);
		input.add((double) playerMPpercentage);
		input.add((double) (playerIsRooted ? 1 : 0));
		input.add((double) (playerIsSilenced ? 1 : 0));
		input.add((double) (playerIsBound ? 1 : 0));
		input.add((double) (playerIsStunned ? 1 : 0));
		input.add((double) (playerIsAetherhold ? 1 : 0));
		input.add((double) playerBuffCount);
		input.add((double) playerDebuffCount);
		input.add((double) (playerIsShielded ? 1 : 0));

		input.add((double) targetHPpercentage);
		input.add((double) targetMPpercentage);
		input.add((double) (targetFocusesPlayer ? 1 : 0));
		input.add((double) distance);
		input.add((double) (targetIsRooted ? 1 : 0));
		input.add((double) (targetIsSilenced ? 1 : 0));
		input.add((double) (targetIsBound ? 1 : 0));
		input.add((double) (targetIsStunned ? 1 : 0));
		input.add((double) (targetIsAetherhold ? 1 : 0));
		input.add((double) targetBuffCount);
		input.add((double) targetDebuffCount);
		input.add((double) (targetIsShielded ? 1 : 0));

		for (int skillID : skillSet)
			input.add((double) (skillID == previousSkillID ? 1 : 0));

		return input.stream().mapToDouble(Double::doubleValue).toArray();
	}

	public double[] toActionOutputArray(List<Integer> skillSet) {
		return skillSet.stream().mapToDouble(skillId -> skillId == this.skillID ? 1 : 0).toArray();
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public float getTimeCDdone() {
		return timeCDdone;
	}

	public int getSkillID() {
		return skillID;
	}

	public int getPlayerID() {
		return playerID;
	}

	public int getPlayerClassID() {
		return playerClassID;
	}

	public float getPlayerHPpercentage() {
		return playerHPpercentage;
	}

	public float getPlayerMPpercentage() {
		return playerMPpercentage;
	}

	public boolean isPlayerRooted() {
		return playerIsRooted;
	}

	public boolean isPlayerSilenced() {
		return playerIsSilenced;
	}

	public boolean isPlayerBound() {
		return playerIsBound;
	}

	public boolean isPlayerStunned() {
		return playerIsStunned;
	}

	public boolean isPlayerAetherhold() {
		return playerIsAetherhold;
	}

	public int getPlayerBuffCount() {
		return playerBuffCount;
	}

	public int getPlayerDebuffCount() {
		return playerDebuffCount;
	}

	public boolean isPlayerIsShielded() {
		return playerIsShielded;
	}

	public float getTargetHPpercentage() {
		return targetHPpercentage;
	}

	public float getTargetMPpercentage() {
		return targetMPpercentage;
	}

	public boolean isTargetFocusesPlayer() {
		return targetFocusesPlayer;
	}

	public float getDistance() {
		return distance;
	}

	public boolean isTargetRooted() {
		return targetIsRooted;
	}

	public boolean isTargetSilenced() {
		return targetIsSilenced;
	}

	public boolean isTargetBound() {
		return targetIsBound;
	}

	public boolean isTargetStunned() {
		return targetIsStunned;
	}

	public boolean isTargetAetherhold() {
		return targetIsAetherhold;
	}

	public int getTargetBuffCount() {
		return targetBuffCount;
	}

	public int getTargetDebuffCount() {
		return targetDebuffCount;
	}

	public boolean isTargetIsShielded() {
		return targetIsShielded;
	}

	@Override
	public PersistentState getPersistentState() {
		return persistentState;
	}

	@Override
	public void setPersistentState(PersistentState state) {
		persistentState = state;
	}
}
