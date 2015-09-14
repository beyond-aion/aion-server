package mysql5;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.dao.ChatLogDAO;
import com.aionemu.commons.database.DatabaseFactory;

public class MySQL5ChatLogDAO extends ChatLogDAO {

	private static final Logger log = LoggerFactory.getLogger(MySQL5ChatLogDAO.class);

	public static final String INSERT_QUERY = "INSERT INTO `chatlog` (`sender`,`message`, `receiver`, `type`) VALUES (?, ?, ?, ?)";

	@Override
	public void add_ChannelChat(String sender, String message, String receiver, String type) {
		Connection con = null;
		try {
			con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement(INSERT_QUERY);
			stmt.setString(1, sender);
			stmt.setString(2, message);
			stmt.setString(3, null);
			stmt.setString(4, type);
			stmt.execute();
			stmt.close();
		} catch (Exception e) {
			log.error("Cannot insert Channel Chat", e);
		} finally {
			DatabaseFactory.close(con);
		}

	}

	@Override
	public boolean supports(String database, int majorVersion, int minorVersion) {
		return MySQL5DAOUtils.supports(database, majorVersion, minorVersion);
	}
}
