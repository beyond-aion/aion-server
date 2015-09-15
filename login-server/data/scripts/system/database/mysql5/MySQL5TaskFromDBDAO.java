package mysql5;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javolution.util.FastTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.loginserver.dao.TaskFromDBDAO;
import com.aionemu.loginserver.taskmanager.handler.TaskFromDBHandler;
import com.aionemu.loginserver.taskmanager.handler.TaskFromDBHandlerHolder;
import com.aionemu.loginserver.taskmanager.trigger.TaskFromDBTrigger;
import com.aionemu.loginserver.taskmanager.trigger.TaskFromDBTriggerHolder;

/**
 * @author Divinity, nrg
 */
public class MySQL5TaskFromDBDAO extends TaskFromDBDAO {

	/**
	 * Logger for this class.
	 */
	private static final Logger log = LoggerFactory.getLogger(MySQL5TaskFromDBDAO.class);
	private static final String SELECT_ALL_QUERY = "SELECT * FROM tasks ORDER BY id";

	@Override
	public List<TaskFromDBTrigger> getAllTasks() {
		final List<TaskFromDBTrigger> result = new FastTable<TaskFromDBTrigger>();

		Connection con = null;

		PreparedStatement stmt = null;
		try {
			con = DatabaseFactory.getConnection();
			stmt = con.prepareStatement(SELECT_ALL_QUERY);

			ResultSet rset = stmt.executeQuery();

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

			rset.close();
			stmt.close();
		} catch (SQLException e) {
			log.error("Loading tasks failed: ", e);
		} finally {
			DatabaseFactory.close(stmt, con);
		}

		return result;
	}

	@Override
	public boolean supports(String s, int i, int i1) {
		return MySQL5DAOUtils.supports(s, i, i1);
	}
}
