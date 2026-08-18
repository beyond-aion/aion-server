package com.aionemu.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.commons.database.IUStH;
import com.aionemu.commons.database.ParamReadStH;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.effect.EffectTemplate;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.Effect.ForceType;

/**
 * @author ATracer
 */
public class PlayerEffectsDAO {

	private static final Logger log = LoggerFactory.getLogger(PlayerEffectsDAO.class);

	public static final String INSERT_QUERY = "INSERT INTO `player_effects` (`player_id`, `skill_id`, `skill_lvl`, `remaining_time`, `end_time`, `force_type`, `magical_criticals`) VALUES (?,?,?,?,?,?,?)";
	public static final String DELETE_QUERY = "DELETE FROM `player_effects` WHERE `player_id`=?";
	public static final String SELECT_QUERY = "SELECT `skill_id`, `skill_lvl`, `remaining_time`, `end_time`, `force_type`, `magical_criticals` FROM `player_effects` WHERE `player_id`=?";

	private static final Predicate<Effect> insertableEffectsPredicate = effect -> effect.canSaveOnLogout() && effect.getRemainingTimeMillis() > 28000;

	public static void loadPlayerEffects(Player player) {
		DB.select(SELECT_QUERY, new ParamReadStH() {

			@Override
			public void setParams(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, player.getObjectId());
			}

			@Override
			public void handleRead(ResultSet rset) throws SQLException {
				while (rset.next()) {
					int skillId = rset.getInt("skill_id");
					int skillLvl = rset.getInt("skill_lvl");
					int remainingTime = rset.getInt("remaining_time");
					long endTime = rset.getLong("end_time");
					String forceTypeStr = rset.getString("force_type");
					ForceType forceType = forceTypeStr == null ? null : ForceType.getInstance(forceTypeStr);
					Set<Integer> magicalCriticalPositions = decodeMagicalCriticalPositions(rset.getInt("magical_criticals"));

					if (remainingTime > 0)
						player.getEffectController().addSavedEffect(skillId, skillLvl, remainingTime, endTime, forceType, magicalCriticalPositions);
				}
			}
		});
		player.getEffectController().broadCastEffects(null);
	}

	public static void storePlayerEffects(Player player) {
		deletePlayerEffects(player);

		List<Effect> effects = player.getEffectController().getAbnormalEffects();
		effects = effects.stream().filter(insertableEffectsPredicate).collect(Collectors.toList());

		if (effects.isEmpty())
			return;

		try (Connection con = DatabaseFactory.getConnection();
				 PreparedStatement ps = con.prepareStatement(INSERT_QUERY)) {
			con.setAutoCommit(false);

			for (Effect effect : effects) {
				ps.setInt(1, player.getObjectId());
				ps.setInt(2, effect.getSkillId());
				ps.setInt(3, effect.getSkillLevel());
				ps.setInt(4, (int) effect.getRemainingTimeMillis());
				ps.setLong(5, effect.getEndTime());
				ps.setString(6, effect.getForceType() == null ? null : effect.getForceType().getName());
				ps.setInt(7, encodeMagicalCriticalPositions(effect));
				ps.addBatch();
			}

			ps.executeBatch();
			con.commit();
		} catch (SQLException e) {
			log.error("Exception while saving effects of player " + player.getObjectId(), e);
		}
	}

	/**
	 * Magical criticals are stored as one bit per effect position, bit 0 being position 1.
	 */
	private static int encodeMagicalCriticalPositions(Effect effect) {
		int bits = 0;
		for (EffectTemplate template : effect.getEffectTemplates())
			if (effect.isMagicalCritical(template.getPosition()))
				bits |= 1 << template.getPosition() - 1;
		return bits;
	}

	private static Set<Integer> decodeMagicalCriticalPositions(int bits) {
		Set<Integer> positions = new HashSet<>();
		for (int position = 1; bits != 0; position++, bits >>= 1)
			if ((bits & 1) != 0)
				positions.add(position);
		return positions;
	}

	private static void deletePlayerEffects(Player player) {
		DB.insertUpdate(DELETE_QUERY, new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, player.getObjectId());
				stmt.execute();
			}
		});
	}

}
