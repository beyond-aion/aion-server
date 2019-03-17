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
import com.google.common.primitives.Doubles;

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
	private float playerX, playerY, playerZ;

	private boolean playerIsRooted, playerIsSilenced, playerIsBound, playerIsStunned, playerIsAetherhold; // used for input
	private int playerBuffCount; // used for input
	private int playerDebuffCount; // used for input
	private boolean playerIsShielded; // used for input

	private int targetID;
	private int targetClassID; // -1 for NPCs
	private float targetHPpercentage, targetMPpercentage; // used for input
	private boolean targetIsPvP, targetFocusesPlayer; // used for input
	private float distance; // used for input
	private float targetX, targetY, targetZ;
	private boolean targetIsRooted, targetIsSilenced, targetIsBound, targetIsStunned, targetIsAetherhold; // used for input
	private int targetBuffCount, targetDebuffCount; // used for input
	private boolean targetIsShielded; // used for input

	private boolean isBossPhase;

	// live constructor
	public PlayerModelEntry(Creature playerOrBoss, int skillID, Creature target) {
		timestamp = new Timestamp(System.currentTimeMillis());

		this.skillID = skillID;
		playerID = playerOrBoss.getObjectId();
		playerClassID = playerOrBoss instanceof Player ? ((Player) playerOrBoss).getPlayerClass().getClassId() : -1;
		playerHPpercentage = playerOrBoss.getLifeStats().getHpPercentage();
		playerMPpercentage = playerOrBoss.getLifeStats().getMpPercentage();
		playerX = playerOrBoss.getX();
		playerY = playerOrBoss.getY();
		playerZ = playerOrBoss.getZ();

		playerIsRooted = playerOrBoss.getEffectController().isAbnormalSet(AbnormalState.ROOT);
		playerIsSilenced = playerOrBoss.getEffectController().isAbnormalSet(AbnormalState.SILENCE);
		playerIsBound = playerOrBoss.getEffectController().isAbnormalSet(AbnormalState.BIND);
		playerIsStunned = playerOrBoss.getEffectController().isAbnormalSet(AbnormalState.ANY_STUN);
		playerIsAetherhold = playerOrBoss.getEffectController().isAbnormalSet(AbnormalState.OPENAERIAL);
		playerIsShielded = playerOrBoss.getEffectController().isUnderShield();

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
			targetID = target.getObjectId();
			targetClassID = target instanceof Player ? ((Player) target).getPlayerClass().getClassId() : -1;
			if (target.getLifeStats().getMaxHp() > 0)
				targetHPpercentage = target.getLifeStats().getHpPercentage();
			else
				targetHPpercentage = 0;
			if (target.getLifeStats().getMaxMp() > 0)
				targetMPpercentage = target.getLifeStats().getMpPercentage();
			else
				targetMPpercentage = 0;
			targetIsPvP = target instanceof Player;
			targetFocusesPlayer = target.getTarget() == playerOrBoss;
			distance = (float) PositionUtil.getDistance(playerOrBoss, target);
			targetX = target.getX();
			targetY = target.getY();
			targetZ = target.getZ();
			targetIsRooted = target.getEffectController().isAbnormalSet(AbnormalState.ROOT);
			targetIsSilenced = target.getEffectController().isAbnormalSet(AbnormalState.SILENCE);
			targetIsBound = target.getEffectController().isAbnormalSet(AbnormalState.BIND);
			targetIsStunned = target.getEffectController().isAbnormalSet(AbnormalState.ANY_STUN);
			targetIsAetherhold = target.getEffectController().isAbnormalSet(AbnormalState.OPENAERIAL);
			targetIsShielded = target.getEffectController().isUnderShield();

			targetBuffCount = 0;
			targetDebuffCount = 0;
			for (Effect e : target.getEffectController().getAbnormalEffects())
				if ((e.getSkillTemplate() != null && e.getSkillTemplate().getSubType() == SkillSubType.BUFF) || // buff skills
					(e.getSkill() != null && e.getSkill().getItemObjectId() != 0)) // buff items
					targetBuffCount++;
				else // debuffs
					targetDebuffCount++;
		} else {
			targetID = targetClassID = targetBuffCount = targetDebuffCount = -1;
			targetHPpercentage = distance = targetMPpercentage = targetX = targetY = targetZ = -1;
			targetIsPvP = targetFocusesPlayer = targetIsRooted = targetIsSilenced = targetIsBound = targetIsStunned = targetIsAetherhold = targetIsShielded = false;
		}
		if (playerOrBoss.getPosition().getY() > 550) // in throne room
			isBossPhase = true;

		setPersistentState(PersistentState.NEW);
	}

	// constructor from persistence
	public PlayerModelEntry(int playerID, Timestamp timestamp, int skillID, int playerClassID, float playerHPpercentage, float playerMPpercentage,
		float playerX, float playerY, float playerZ, boolean playerIsRooted, boolean playerIsSilenced, boolean playerIsBound, boolean playerIsStunned,
		boolean playerIsAetherhold, int playerBuffCount, int playerDebuffCount, boolean playerIsShielded, int targetID, float targetHPpercentage,
		float targetMPpercentage, boolean targetIsPvP, boolean targetFocusesPlayer, float distance, float targetX, float targetY, float targetZ,
		boolean targetIsRooted, boolean targetIsSilenced, boolean targetIsBound, boolean targetIsStunned, boolean targetIsAetherhold, int targetBuffCount,
		int targetDebuffCount, boolean targetIsShielded, boolean isBossPhase) {
		this.timestamp = timestamp;
		this.skillID = skillID;
		this.playerID = playerID;
		this.playerClassID = playerClassID;
		this.playerHPpercentage = playerHPpercentage;
		this.playerMPpercentage = playerMPpercentage;
		this.playerX = playerX;
		this.playerY = playerY;
		this.playerZ = playerZ;
		this.playerIsRooted = playerIsRooted;
		this.playerIsSilenced = playerIsSilenced;
		this.playerIsBound = playerIsBound;
		this.playerIsStunned = playerIsStunned;
		this.playerIsAetherhold = playerIsAetherhold;
		this.playerBuffCount = playerBuffCount;
		this.playerDebuffCount = playerDebuffCount;
		this.playerIsShielded = playerIsShielded;
		this.targetID = targetID;
		this.targetHPpercentage = targetHPpercentage;
		this.targetMPpercentage = targetMPpercentage;
		this.targetIsPvP = targetIsPvP;
		this.targetFocusesPlayer = targetFocusesPlayer;
		this.distance = distance;
		this.targetX = targetX;
		this.targetY = targetY;
		this.targetZ = targetZ;
		this.targetIsRooted = targetIsRooted;
		this.targetIsSilenced = targetIsSilenced;
		this.targetIsBound = targetIsBound;
		this.targetIsStunned = targetIsStunned;
		this.targetIsAetherhold = targetIsAetherhold;
		this.targetBuffCount = targetBuffCount;
		this.targetDebuffCount = targetDebuffCount;
		this.targetIsShielded = targetIsShielded;
		this.isBossPhase = isBossPhase;
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
		input.add((double) (targetIsPvP ? 1 : 0));
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

		return Doubles.toArray(input);
	}

	public double[] toActionOutputArray(List<Integer> skillSet) {
		List<Double> output = new ArrayList<>();
		for (int skillID : skillSet)
			output.add((double) (skillID == this.skillID ? 1 : 0));
		return Doubles.toArray(output);
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

	public float getPlayerX() {
		return playerX;
	}

	public float getPlayerY() {
		return playerY;
	}

	public float getPlayerZ() {
		return playerZ;
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

	public int getTargetID() {
		return targetID;
	}

	public int getTargetClassID() {
		return targetClassID;
	}

	public float getTargetHPpercentage() {
		return targetHPpercentage;
	}

	public float getTargetMPpercentage() {
		return targetMPpercentage;
	}

	public boolean isTargetIsPvP() {
		return targetIsPvP;
	}

	public boolean isTargetFocusesPlayer() {
		return targetFocusesPlayer;
	}

	public float getDistance() {
		return distance;
	}

	public float getTargetX() {
		return targetX;
	}

	public float getTargetY() {
		return targetY;
	}

	public float getTargetZ() {
		return targetZ;
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

	public boolean isBossPhase() {
		return isBossPhase;
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
