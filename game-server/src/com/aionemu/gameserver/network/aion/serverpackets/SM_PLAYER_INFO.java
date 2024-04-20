package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.actions.PlayerMode;
import com.aionemu.gameserver.model.gameobjects.player.CustomPlayerState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerAppearance;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.templates.cp.CPType;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.services.conquerorAndProtectorSystem.CPInfo;
import com.aionemu.gameserver.services.conquerorAndProtectorSystem.ConquerorAndProtectorService;

/**
 * This packet is displaying visible players.
 *
 * @author -Nemesiss-, Avol, srx47, cura, -Enomine-, -Artur-, Neon
 */
public class SM_PLAYER_INFO extends AbstractPlayerInfoPacket {

	private final Player player;
	private boolean enemy;

	public SM_PLAYER_INFO(Player player) {
		this(player, false);
	}

	public SM_PLAYER_INFO(Player player, boolean enemy) {
		this.player = player;
		this.enemy = enemy;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		Player activePlayer = con.getActivePlayer();
		if (activePlayer == null)
			return;

		PlayerCommonData pcd = player.getCommonData();
		PlayerAppearance playerAppearance = player.getPlayerAppearance();
		int raceId = activePlayer.isEnemy(player) ? activePlayer.getOppositeRace().getRaceId() : player.getRace().getRaceId();
		if (player.isInCustomState(CustomPlayerState.NEUTRAL_TO_ALL_PLAYERS) || activePlayer.isInCustomState(CustomPlayerState.NEUTRAL_TO_ALL_PLAYERS))
			raceId = activePlayer.getRace().getRaceId();

		writeF(player.getX());// x
		writeF(player.getY());// y
		writeF(player.getZ());// z
		writeD(player.getObjectId());
		writeD(pcd.getTemplateId()); // 0xA3 female asmodian, 0xA2 male asmodian, 0xA1 female elyos, 0xA0 male elyos
		writeD(player.getRobotId());// RobotId
		writeD(player.getTransformModel().getModelId()); // Transformed state: transformed model id, Regular state: player model id
		writeC(0x00); // new 2.0 Packet --- probably pet info?
		writeD(player.getTransformModel().getType().getId());
		writeC(enemy ? 0x00 : 0x26);

		writeC(raceId); // race
		writeC(pcd.getPlayerClass().getClassId());
		writeC(pcd.getGender().getGenderId()); // sex
		writeH(player.getState());

		writeD(0);
		writeD(0);

		writeC(player.getHeading());

		writeS(player.getName(true));

		writeH(pcd.getTitleId());
		writeH(player.getCommonData().isHaveMentorFlag() ? 1 : 0);

		writeH(player.getCastingSkillId());

		if (player.isLegionMember()) {
			writeD(player.getLegion().getLegionId());
			writeC(player.getLegion().getLegionEmblem().getEmblemId());
			writeC(player.getLegion().getLegionEmblem().getEmblemType().getValue());
			writeC(player.getLegion().getLegionEmblem().getColor_a());
			writeC(player.getLegion().getLegionEmblem().getColor_r());
			writeC(player.getLegion().getLegionEmblem().getColor_g());
			writeC(player.getLegion().getLegionEmblem().getColor_b());
			writeS(player.getLegion().getName());
		} else {
			writeB(new byte[12]);
		}
		int maxHp = player.getLifeStats().getMaxHp();
		int currHp = player.getLifeStats().getCurrentHp();
		writeC(100 * currHp / maxHp);// %hp
		writeH(pcd.getDp());// current dp
		writeC(0x00);// unk (0x00)

		writeEquippedItems(player.getEquipment().getEquippedForAppearance());

		writeD(playerAppearance.getSkinRGB());
		writeD(playerAppearance.getHairRGB());
		writeD(playerAppearance.getEyeRGB());
		writeD(playerAppearance.getLipRGB());
		writeC(playerAppearance.getFace());
		writeC(playerAppearance.getHair());
		writeC(playerAppearance.getDeco());
		writeC(playerAppearance.getTattoo());
		writeC(playerAppearance.getFaceContour());
		writeC(playerAppearance.getExpression());

		writeC(5); // unk 0x05 0x06

		writeC(playerAppearance.getJawLine());
		writeC(playerAppearance.getForehead());

		writeC(playerAppearance.getEyeHeight());
		writeC(playerAppearance.getEyeSpace());
		writeC(playerAppearance.getEyeWidth());
		writeC(playerAppearance.getEyeSize());
		writeC(playerAppearance.getEyeShape());
		writeC(playerAppearance.getEyeAngle());

		writeC(playerAppearance.getBrowHeight());
		writeC(playerAppearance.getBrowAngle());
		writeC(playerAppearance.getBrowShape());

		writeC(playerAppearance.getNose());
		writeC(playerAppearance.getNoseBridge());
		writeC(playerAppearance.getNoseWidth());
		writeC(playerAppearance.getNoseTip());

		writeC(playerAppearance.getCheek());
		writeC(playerAppearance.getLipHeight());
		writeC(playerAppearance.getMouthSize());
		writeC(playerAppearance.getLipSize());
		writeC(playerAppearance.getSmile());
		writeC(playerAppearance.getLipShape());
		writeC(playerAppearance.getJawHeigh());
		writeC(playerAppearance.getChinJut());
		writeC(playerAppearance.getEarShape());
		writeC(playerAppearance.getHeadSize());
		// 1.5.x 0x00, shoulderSize, armLength, legLength (BYTE) after HeadSize

		writeC(playerAppearance.getNeck());
		writeC(playerAppearance.getNeckLength());
		writeC(playerAppearance.getShoulderSize());

		writeC(playerAppearance.getTorso());
		writeC(playerAppearance.getChest()); // only woman
		writeC(playerAppearance.getWaist());

		writeC(playerAppearance.getHips());
		writeC(playerAppearance.getArmThickness());
		writeC(playerAppearance.getHandSize());
		writeC(playerAppearance.getLegThickness());

		writeC(playerAppearance.getFootSize());
		writeC(playerAppearance.getFacialRate());

		writeC(0x00); // always 0
		writeC(playerAppearance.getArmLength());
		writeC(playerAppearance.getLegLength());
		writeC(playerAppearance.getShoulders());
		writeC(playerAppearance.getFaceShape());
		writeC(0x00); // always 0

		writeC(playerAppearance.getVoice());

		writeF(playerAppearance.getHeight());
		writeF(0.25f); // scale
		writeF(2.0f); // gravity or slide surface o_O
		writeF(player.getGameStats().getMovementSpeedFloat()); // move speed

		Stat2 attackSpeed = player.getGameStats().getAttackSpeed();
		writeH(attackSpeed.getBase());
		writeH(attackSpeed.getCurrent());
		writeC(player.getPortAnimationId()); // not visible to other players (they always see a simple fade in animation)

		writeS(player.hasStore() ? player.getStore().getStoreMessage() : ""); // private store message

		// Movement
		writeF(0);
		writeF(0);
		writeF(0);

		writeF(player.getX());// x
		writeF(player.getY());// y
		writeF(player.getZ());// z
		writeC(0x00); // move type

		if (player.isUsingFlyTeleport()) {
			writeD(player.getFlightTeleportId());
			writeD(player.getFlightDistance());
		} else if (player.isInPlayerMode(PlayerMode.WINDSTREAM)) {
			writeD(player.windstreamPath.teleportId);
			writeD(player.windstreamPath.distance);
		}
		writeC(player.getVisualState()); // visualState
		writeS(player.getCommonData().getNote()); // note show in right down windows if your target on player

		writeH(player.getLevel()); // [level]
		writeH(player.getPlayerSettings().getDisplay()); // unk - 0x04
		writeH(player.getPlayerSettings().getDeny()); // unk - 0x00
		writeH(player.getAbyssRank().getRank().getId()); // abyss rank
		writeH(0x00); // unk - 0x01
		writeD(player.getTarget() == null ? 0 : player.getTarget().getObjectId());
		writeC(0); // suspect id
		writeD(player.getCurrentTeamId());
		writeC(player.isMentor() ? 1 : 0);
		writeD(player.getActiveHouse() == null ? 0 : player.getActiveHouse().getAddress().getId()); // 3.0

		if (player.getAccount().getMembership() > 0)
			writeD(0x03 + player.getAccount().getMembership());// 1 = normal, 2 = new player(ascension boost), 3 = returning player, 4 = vip 1
		else
			writeD(0x01);
		writeD(0x01); // unk 4.7
		writeC(3); // can be 3 or 5 on elyos side (3 is more common), not sure what it's for (TODO: check asmo side)

		CPInfo cpInfo = ConquerorAndProtectorService.getInstance().getCPInfoForCurrentMap(player);
		writeC(cpInfo != null && cpInfo.getType() == CPType.CONQUEROR ? cpInfo.getRank() : 0); // Conqueror rank
		writeC(cpInfo != null && cpInfo.getType() == CPType.PROTECTOR ? cpInfo.getRank() : 0); // Protector rank

		writeC(0); // Officer rank icon
	}

}
