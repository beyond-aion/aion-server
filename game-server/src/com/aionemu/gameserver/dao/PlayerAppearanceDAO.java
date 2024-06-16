package com.aionemu.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.commons.database.IUStH;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerAppearance;

/**
 * Class that is responsible for loading/storing player appearance
 * 
 * @author SoulKeeper, AEJTester, srx47
 */
public class PlayerAppearanceDAO {

	private static final Logger log = LoggerFactory.getLogger(PlayerAppearanceDAO.class);

	public static PlayerAppearance load(int playerId) {
		PlayerAppearance pa = new PlayerAppearance();
		try (Connection con = DatabaseFactory.getConnection();
				 PreparedStatement stmt = con.prepareStatement("SELECT * FROM player_appearance WHERE player_id = ?")) {
			stmt.setInt(1, playerId);
			try (ResultSet resultSet = stmt.executeQuery()) {
				if (resultSet.next()) {
					pa.setFace(resultSet.getInt("face"));
					pa.setHair(resultSet.getInt("hair"));
					pa.setDeco(resultSet.getInt("deco"));
					pa.setTattoo(resultSet.getInt("tattoo"));
					pa.setFaceContour(resultSet.getInt("face_contour"));
					pa.setExpression(resultSet.getInt("expression"));
					pa.setJawLine(resultSet.getInt("jaw_line"));
					pa.setSkinRGB(resultSet.getInt("skin_rgb"));
					pa.setHairRGB(resultSet.getInt("hair_rgb"));
					pa.setEyeRGB(resultSet.getInt("eye_rgb"));
					pa.setLipRGB(resultSet.getInt("lip_rgb"));
					pa.setFaceShape(resultSet.getInt("face_shape"));
					pa.setForehead(resultSet.getInt("forehead"));
					pa.setEyeHeight(resultSet.getInt("eye_height"));
					pa.setEyeSpace(resultSet.getInt("eye_space"));
					pa.setEyeWidth(resultSet.getInt("eye_width"));
					pa.setEyeSize(resultSet.getInt("eye_size"));
					pa.setEyeShape(resultSet.getInt("eye_shape"));
					pa.setEyeAngle(resultSet.getInt("eye_angle"));
					pa.setBrowHeight(resultSet.getInt("brow_height"));
					pa.setBrowAngle(resultSet.getInt("brow_angle"));
					pa.setBrowShape(resultSet.getInt("brow_shape"));
					pa.setNose(resultSet.getInt("nose"));
					pa.setNoseBridge(resultSet.getInt("nose_bridge"));
					pa.setNoseWidth(resultSet.getInt("nose_width"));
					pa.setNoseTip(resultSet.getInt("nose_tip"));
					pa.setCheek(resultSet.getInt("cheek"));
					pa.setLipHeight(resultSet.getInt("lip_height"));
					pa.setMouthSize(resultSet.getInt("mouth_size"));
					pa.setLipSize(resultSet.getInt("lip_size"));
					pa.setSmile(resultSet.getInt("smile"));
					pa.setLipShape(resultSet.getInt("lip_shape"));
					pa.setJawHeigh(resultSet.getInt("jaw_height"));
					pa.setChinJut(resultSet.getInt("chin_jut"));
					pa.setEarShape(resultSet.getInt("ear_shape"));
					pa.setHeadSize(resultSet.getInt("head_size"));
					pa.setNeck(resultSet.getInt("neck"));
					pa.setNeckLength(resultSet.getInt("neck_length"));
					pa.setShoulders(resultSet.getInt("shoulders"));
					pa.setShoulderSize(resultSet.getInt("shoulder_size"));
					pa.setTorso(resultSet.getInt("torso"));
					pa.setChest(resultSet.getInt("chest"));
					pa.setWaist(resultSet.getInt("waist"));
					pa.setHips(resultSet.getInt("hips"));
					pa.setArmThickness(resultSet.getInt("arm_thickness"));
					pa.setArmLength(resultSet.getInt("arm_length"));
					pa.setHandSize(resultSet.getInt("hand_size"));
					pa.setLegThickness(resultSet.getInt("leg_thickness"));
					pa.setLegLength(resultSet.getInt("leg_length"));
					pa.setFootSize(resultSet.getInt("foot_size"));
					pa.setFacialRate(resultSet.getInt("facial_rate"));
					pa.setVoice(resultSet.getInt("voice"));
					pa.setHeight(resultSet.getFloat("height"));
				}
			}
		} catch (Exception e) {
			log.error("Could not restore PlayerAppearance data for player " + playerId + " from DB: " + e.getMessage(), e);
			return null;
		}
		return pa;
	}

	/**
	 * Saves player appearance in database.<br>
	 * Actually calls {@link #store(int, com.aionemu.gameserver.model.gameobjects.player.PlayerAppearance)}
	 * 
	 * @param player
	 *          whos appearance to store
	 * @return true, if sql query was successful, false overwise
	 */
	public static boolean store(Player player) {
		return store(player.getObjectId(), player.getPlayerAppearance());
	}

	public static boolean store(int id, PlayerAppearance pa) {

		return DB.insertUpdate("REPLACE INTO player_appearance ("
			+ "player_id, face, hair, deco, tattoo, face_contour, expression, jaw_line, skin_rgb, hair_rgb, lip_rgb, eye_rgb, face_shape,"
			+ "forehead, eye_height, eye_space, eye_width, eye_size, eye_shape, eye_angle,"
			+ "brow_height, brow_angle, brow_shape, nose, nose_bridge, nose_width, nose_tip, "
			+ "cheek, lip_height, mouth_size, lip_size, smile, lip_shape, jaw_height, chin_jut, ear_shape,"
			+ "head_size, neck, neck_length, shoulders, shoulder_size , torso, chest, waist, hips, arm_thickness, arm_length, hand_size,"
			+ "leg_thickness, leg_length, foot_size, facial_rate, voice, height)" + " VALUES "
			+ "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,"
			+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?" + ")", new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement ps) throws SQLException {
				ps.setInt(1, id);
				ps.setInt(2, pa.getFace());
				ps.setInt(3, pa.getHair());
				ps.setInt(4, pa.getDeco());
				ps.setInt(5, pa.getTattoo());
				ps.setInt(6, pa.getFaceContour());
				ps.setInt(7, pa.getExpression());
				ps.setInt(8, pa.getJawLine());
				ps.setInt(9, pa.getSkinRGB());
				ps.setInt(10, pa.getHairRGB());
				ps.setInt(11, pa.getLipRGB());
				ps.setInt(12, pa.getEyeRGB());
				ps.setInt(13, pa.getFaceShape());
				ps.setInt(14, pa.getForehead());
				ps.setInt(15, pa.getEyeHeight());
				ps.setInt(16, pa.getEyeSpace());
				ps.setInt(17, pa.getEyeWidth());
				ps.setInt(18, pa.getEyeSize());
				ps.setInt(19, pa.getEyeShape());
				ps.setInt(20, pa.getEyeAngle());
				ps.setInt(21, pa.getBrowHeight());
				ps.setInt(22, pa.getBrowAngle());
				ps.setInt(23, pa.getBrowShape());
				ps.setInt(24, pa.getNose());
				ps.setInt(25, pa.getNoseBridge());
				ps.setInt(26, pa.getNoseWidth());
				ps.setInt(27, pa.getNoseTip());
				ps.setInt(28, pa.getCheek());
				ps.setInt(29, pa.getLipHeight());
				ps.setInt(30, pa.getMouthSize());
				ps.setInt(31, pa.getLipSize());
				ps.setInt(32, pa.getSmile());
				ps.setInt(33, pa.getLipShape());
				ps.setInt(34, pa.getJawHeigh());
				ps.setInt(35, pa.getChinJut());
				ps.setInt(36, pa.getEarShape());
				ps.setInt(37, pa.getHeadSize());
				ps.setInt(38, pa.getNeck());
				ps.setInt(39, pa.getNeckLength());
				ps.setInt(40, pa.getShoulders());
				ps.setInt(41, pa.getShoulderSize());
				ps.setInt(42, pa.getTorso());
				ps.setInt(43, pa.getChest());
				ps.setInt(44, pa.getWaist());
				ps.setInt(45, pa.getHips());
				ps.setInt(46, pa.getArmThickness());
				ps.setInt(47, pa.getArmLength());
				ps.setInt(48, pa.getHandSize());
				ps.setInt(49, pa.getLegThickness());
				ps.setInt(50, pa.getLegLength());
				ps.setInt(51, pa.getFootSize());
				ps.setInt(52, pa.getFacialRate());
				ps.setInt(53, pa.getVoice());
				ps.setFloat(54, pa.getHeight());
				ps.execute();
			}
		});
	}

}
