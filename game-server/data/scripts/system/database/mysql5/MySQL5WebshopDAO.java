package mysql5;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.dao.MySQL5DAOUtils;
import com.aionemu.gameserver.dao.WebshopDAO;
import com.aionemu.gameserver.services.webshop.WebshopRequest;


/**
 * @author ViAl
 *
 */
public class MySQL5WebshopDAO extends WebshopDAO {

	private static final Logger log = LoggerFactory.getLogger(WebshopDAO.class);
	private static final String SELECT_QUERY = "SELECT * FROM ingameshop_requests WHERE delivered = 0";
	private static final String UPDATE_QUERY = "UPDATE ingameshop_requests SET delivered = 1, delivered_at = ? WHERE request_id = ?";
	
	@Override
	public boolean supports(String databaseName, int majorVersion, int minorVersion) {
		return MySQL5DAOUtils.supports(databaseName, majorVersion, minorVersion);
	}

	@Override
	public List<WebshopRequest> loadRequests() {
		List<WebshopRequest> requests = new ArrayList<WebshopRequest>();
		try {
			try(Connection conn = DatabaseFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(SELECT_QUERY); ResultSet rset = stmt.executeQuery()) {
				while(rset.next()) {
					int requestId = rset.getInt("request_id");
					String buyerName = rset.getString("buyer_character_name");
					String receiverName = rset.getString("receiver_character_name");
					int itemId = rset.getInt("item_id");
					long itemCount = rset.getLong("item_count");
					WebshopRequest request = new WebshopRequest(requestId, buyerName, receiverName, itemId, itemCount);
					requests.add(request);
				}
			}
		}
		catch(Exception e) {
			log.error("Error while loading webshop requests.", e);
		}
		return requests;
	}

	@Override
	public void updateRequest(int requestId) {
		try {
			try(Connection conn = DatabaseFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(UPDATE_QUERY)) {
				stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
				stmt.setInt(2, requestId);
				stmt.executeUpdate();
			}
		}
		catch(Exception e) {
			log.error("Error while changing webshop request status, request id "+requestId, e);
		}
	}

}
