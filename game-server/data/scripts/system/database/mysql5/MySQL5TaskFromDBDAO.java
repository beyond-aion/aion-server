package mysql5;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.dao.MySQL5DAOUtils;
import com.aionemu.gameserver.dao.TaskFromDBDAO;
import com.aionemu.gameserver.taskmanager.fromdb.handler.TaskFromDBHandler;
import com.aionemu.gameserver.taskmanager.fromdb.handler.TaskFromDBHandlerHolder;
import com.aionemu.gameserver.taskmanager.fromdb.trigger.TaskFromDBTrigger;
import com.aionemu.gameserver.taskmanager.fromdb.trigger.TaskFromDBTriggerHolder;

/**
 * @author nrg
 */
public class MySQL5TaskFromDBDAO extends TaskFromDBDAO {

	/**
	 * Logger for this class.
	 */
	private static final Logger log = LoggerFactory.getLogger(MySQL5TaskFromDBDAO.class);
	private static final String SELECT_ALL_QUERY = "SELECT * FROM tasks ORDER BY id";

	@Override
	public List<TaskFromDBTrigger> getAllTasks() {
		final List<TaskFromDBTrigger> result = new ArrayList<>();
		try {
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement stmt = con.prepareStatement(SELECT_ALL_QUERY);
				ResultSet rset = stmt.executeQuery()) {
				while (rset.next()) {
					try {
						TaskFromDBTrigger trigger = TaskFromDBTriggerHolder.valueOf(rset.getString("trigger_type")).getTriggerClass().newInstance();
						TaskFromDBHandler handler = TaskFromDBHandlerHolder.valueOf(rset.getString("task_type")).getTaskClass().newInstance();

						handler.setTaskId(rset.getInt("id"));

						String execParamsResult = rset.getString("exec_param");
						if (execParamsResult != null) {
							handler.setParams(rset.getString("exec_param").split(" "));
						}

						trigger.setHandlerToTrigger(handler);

						String triggerParamsResult = rset.getString("trigger_param");
						if (triggerParamsResult != null) {
							trigger.setParams(rset.getString("trigger_param").split(" "));
						}
						result.add(trigger);
					} catch (InstantiationException ex) {
						log.error(ex.getMessage(), ex);
					} catch (IllegalAccessException ex) {
						log.error(ex.getMessage(), ex);
					}
				}
			}
		} catch (SQLException e) {
			log.error("Loading tasks failed: ", e);
		}
		return result;
	}

	@Override
	public boolean supports(String s, int i, int i1) {
		return MySQL5DAOUtils.supports(s, i, i1);
	}
}
