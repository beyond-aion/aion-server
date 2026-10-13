package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.Gender;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.PlayerAppearance;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.utils.Util;

/**
 * @author Neon
 */
public abstract class AbstractCharacterEditPacket extends AionClientPacket {

	/**
	 * Range of the height slider in character creation. The client renders any other value as is.
	 */
	private static final float MIN_HEIGHT_SCALE = 0.65f, MAX_HEIGHT_SCALE = 1.2f;

	protected String characterName;
	protected Gender gender;
	protected Race race;
	protected PlayerClass playerClass;
	protected PlayerAppearance playerAppearance;

	public AbstractCharacterEditPacket(int opcode, Set<AionConnection.State> validStates) {
		super(opcode, validStates);
	}

	protected void readBasicInfo(boolean ignoreInvalidPlayerClass) {
		characterName = Util.convertName(readS(25)); // client leaks random data here when entering char creation screen for the first time
		gender = readD() == 0 ? Gender.MALE : Gender.FEMALE;
		race = readD() == 0 ? Race.ELYOS : Race.ASMODIANS;
		playerClass = PlayerClass.getPlayerClassById((byte) readD(), ignoreInvalidPlayerClass);
	}

	protected void readAppearance() {
		playerAppearance = new PlayerAppearance();

		playerAppearance.setVoice(readD());
		playerAppearance.setSkinRGB(readD());
		playerAppearance.setHairRGB(readD());
		playerAppearance.setEyeRGB(readD());
		playerAppearance.setLipRGB(readD());
		playerAppearance.setFace(readUC());
		playerAppearance.setHair(readUC());
		playerAppearance.setDeco(readUC());
		playerAppearance.setTattoo(readUC());
		playerAppearance.setFaceContour(readUC());
		playerAppearance.setExpression(readUC());
		readC(); // always 6, unused by the client
		playerAppearance.setJawLine(readUC());
		playerAppearance.setForehead(readUC());

		playerAppearance.setEyeHeight(readUC());
		playerAppearance.setEyeSpace(readUC());
		playerAppearance.setEyeWidth(readUC());
		playerAppearance.setEyeSize(readUC());
		playerAppearance.setEyeShape(readUC());
		playerAppearance.setEyeAngle(readUC());

		playerAppearance.setBrowHeight(readUC());
		playerAppearance.setBrowAngle(readUC());
		playerAppearance.setBrowShape(readUC());

		playerAppearance.setNose(readUC());
		playerAppearance.setNoseBridge(readUC());
		playerAppearance.setNoseWidth(readUC());
		playerAppearance.setNoseTip(readUC());

		playerAppearance.setCheek(readUC());
		playerAppearance.setLipHeight(readUC());
		playerAppearance.setMouthSize(readUC());
		playerAppearance.setLipSize(readUC());
		playerAppearance.setSmile(readUC());
		playerAppearance.setLipShape(readUC());
		playerAppearance.setChinHeight(readUC());
		playerAppearance.setCheekBones(readUC());
		playerAppearance.setEarShape(readUC());
		playerAppearance.setHeadSize(readUC());

		playerAppearance.setNeck(readUC());
		playerAppearance.setNeckLength(readUC());

		playerAppearance.setShoulderSize(readUC());

		playerAppearance.setTorso(readUC());
		playerAppearance.setChest(readUC()); // only woman
		playerAppearance.setWaist(readUC());
		playerAppearance.setHips(readUC());

		playerAppearance.setArmThickness(readUC());

		playerAppearance.setHandSize(readUC());
		playerAppearance.setLegThickness(readUC());

		playerAppearance.setFootSize(readUC());
		playerAppearance.setFacialRate(readUC());

		readC(); // wing size, the slider is hidden so it's always 0
		playerAppearance.setArmLength(readUC());
		playerAppearance.setLegLength(readUC());
		playerAppearance.setShoulders(readUC()); // shoulder width, unlike shoulderSize
		playerAppearance.setFaceShape(readUC());
		readC(); // unused
		readH(); // padding
		float heightScale = readF();
		// Stricter than retail: retail keeps NaN, which the client can't render, we use the default height instead
		playerAppearance.setHeightScale(Float.isNaN(heightScale) ? 1 : Math.clamp(heightScale, MIN_HEIGHT_SCALE, MAX_HEIGHT_SCALE));
	}
}
