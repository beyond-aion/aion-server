package mysql5;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.dao.MotionDAO;
import com.aionemu.gameserver.dao.MySQL5DAOUtils;
import com.aionemu.gameserver.dao.PlayerEmotionListDAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.motion.Motion;
import com.aionemu.gameserver.model.gameobjects.player.motion.MotionList;

/**
 * @author MrPoke
 */
public class MySQL5MotionDAO extends MotionDAO {

	/** Logger */
	private static final Logger log = LoggerFactory.getLogger(PlayerEmotionListDAO.class);
	public static final String INSERT_QUERY = "INSERT INTO `player_motions` (`player_id`, `motion_id`, `active`,  `time`) VALUES (?,?,?,?)";
	public static final String SELECT_QUERY = "SELECT `motion_id`, `active`, `time` FROM `player_motions` WHERE `player_id`=?";
	public static final String DELETE_QUERY = "DELETE FROM `player_motions` WHERE `player_id`=? AND `motion_id`=?";
	public static final String UPDATE_QUERY = "UPDATE `player_motions` SET `active`=? WHERE `player_id`=? AND `motion_id`=?";

	@Override
	public boolean supports(String s, int i, int i1) {
		return MySQL5DAOUtils.supports(s, i, i1);
	}

	@Override
	public void loadMotionList(Player player) {
		MotionList motions = new MotionList(player);
		try {
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_QUERY)) {
				stmt.setInt(1, player.getObjectId());
				try (ResultSet rset = stmt.executeQuery()) {
					while (rset.next()) {
						int motionId = rset.getInt("motion_id");
						int time = rset.getInt("time");
						boolean isActive = rset.getBoolean("active");
						motions.add(new Motion(motionId, time, isActive), false);
					}
				}
			}
		}
		catch (Exception e) {
			log.error("Could not restore motions for playerObjId: " + player.getObjectId() + " from DB: " + e.getMessage(), e);
		}
		player.setMotions(motions);
	}

	@Override
	public boolean storeMotion(int objectId, Motion motion) {
		try {
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(INSERT_QUERY)) {
				stmt.setInt(1, objectId);
				stmt.setInt(2, motion.getId());
				stmt.setBoolean(3, motion.isActive());
				stmt.setInt(4, motion.getExpireTime());
				stmt.execute();
			}
		}
		catch (Exception e) {
			log.error("Could not store motion for player " + objectId + " from DB: " + e.getMessage(), e);
			return false;
		}
		return true;
	}

	@Override
	public boolean deleteMotion(int objectId, int motionId) {
		try {
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(DELETE_QUERY)) {
				stmt.setInt(1, objectId);
				stmt.setInt(2, motionId);
				stmt.execute();
			}
		}
		catch (Exception e) {
			log.error("Could not delete motion for player " + objectId + " from DB: " + e.getMessage(), e);
			return false;
		}
		return true;
	}

	@Override
	public boolean updateMotion(int objectId, Motion motion) {
		try {
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(UPDATE_QUERY)) {
				stmt.setBoolean(1, motion.isActive());
				stmt.setInt(2, objectId);
				stmt.setInt(3, motion.getId());
				stmt.execute();
			}
		}
		catch (Exception e) {
			log.error("Could not store motion for player " + objectId + " from DB: " + e.getMessage(), e);
			return false;
		}
		return true;
	}
}
