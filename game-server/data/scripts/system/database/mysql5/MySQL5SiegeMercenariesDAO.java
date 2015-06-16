package mysql5;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.dao.MySQL5DAOUtils;
import com.aionemu.gameserver.dao.SiegeMercenariesDAO;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.services.MercenariesService;


/**
 * @author ViAl
 *
 */
public class MySQL5SiegeMercenariesDAO extends SiegeMercenariesDAO {

	private static final Logger log = LoggerFactory.getLogger(MySQL5SiegeMercenariesDAO.class);
	
	private static final String SELECT_QUERY = "SELECT * FROM siege_mercenaries";
	private static final String DELETE_QUERY = "DELETE FROM siege_mercenaries WHERE location_id=? AND zone_id=?";
	private static final String INSERT_QUERY = "INSERT INTO siege_mercenaries(location_id, zone_id, race) VALUES (?,?,?)";
	
	@Override
	public void loadActiveMercenaries() {
		try(Connection conn = DatabaseFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(SELECT_QUERY); ResultSet rset = stmt.executeQuery();) {
			while(rset.next()) {
				int locationId = rset.getInt("location_id");
				int zoneId = rset.getInt("zone_id");
				Race race = Race.valueOf(rset.getString("race"));
				MercenariesService.loadMercenaries(locationId, zoneId, race);
			}
		}
		catch(Exception e) {
			log.error("Error while loading mercenaries", e);
		}
	}

	@Override
	public void deleteMercenaries(int locationId, int zoneId) {
		try(Connection conn = DatabaseFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(DELETE_QUERY);) {
			stmt.setInt(1, locationId);
			stmt.setInt(2, zoneId);
			stmt.executeUpdate();
		}
		catch(Exception e) {
			log.error("Error while deleting mercenaries, location "+locationId+", zone "+zoneId, e);
		}
	}

	@Override
	public void insertMercenaries(int locationId, int zoneId, Race race) {
		try(Connection conn = DatabaseFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(INSERT_QUERY)) {
			stmt.setInt(1, locationId);
			stmt.setInt(2, zoneId);
			stmt.setString(3, race.toString());
			stmt.execute();
		}
		catch(Exception e) {
			log.error("Error while inserting mercenaries, location "+locationId+", zone "+zoneId+", race "+race, e);
		}
	}
	
	@Override
	public boolean supports(String databaseName, int majorVersion, int minorVersion) {
		return MySQL5DAOUtils.supports(databaseName, majorVersion, minorVersion);
	}

}
