package mysql5;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javolution.util.FastTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.commons.database.IUStH;
import com.aionemu.gameserver.dao.MySQL5DAOUtils;
import com.aionemu.gameserver.dao.WebDAO;
import com.aionemu.gameserver.services.web.WebRequest;

/**
 * Created on 24.05.2016
 * 
 * @author Estrayl
 */
public class MySQL5WebDAO extends WebDAO {

	private static final Logger log = LoggerFactory.getLogger(WebDAO.class);
	private static final String SELECT_QUERY = "SELECT * FROM `web_rewards`";
	private static final String DELETE_QUERY = "DELETE FROM `web_rewards` WHERE `request_id` = ?";

	@Override
	public List<WebRequest> loadRequests() {
		List<WebRequest> requests = new FastTable<WebRequest>();
		try {
			try (Connection conn = DatabaseFactory.getConnection();
				PreparedStatement stmt = conn.prepareStatement(SELECT_QUERY);
				ResultSet rset = stmt.executeQuery()) {
				while (rset.next()) {
					int requestId = rset.getInt("request_id");
					String receiverName = rset.getString("receiver_name");
					int itemId = rset.getInt("item_id");
					long itemCount = rset.getLong("item_count");
					WebRequest request = new WebRequest(requestId, receiverName, itemId, itemCount);
					requests.add(request);
				}
			}
		} catch (Exception e) {
			log.error("Error while loading web requests.", e);
		}
		return requests;
	}

	@Override
	public void deleteRequest(int requestId) {
		DB.insertUpdate(DELETE_QUERY, new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, requestId);
				stmt.execute();
			}
		});
	}

	@Override
	public boolean supports(String databaseName, int majorVersion, int minorVersion) {
		return MySQL5DAOUtils.supports(databaseName, majorVersion, minorVersion);
	}
}
