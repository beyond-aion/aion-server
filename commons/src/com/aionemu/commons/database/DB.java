package com.aionemu.commons.database;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <b>DB Documentation</b>
 * <p>
 * This class is used for making SQL query's utilizing the database connection defined in database.properties<br>
 * <br>
 * Here are the functions that one may use to utilize this class in creating an ease of access to database information.
 * </p>
 * <hr>
 * <b>SELECT (select method)</b>
 * <p>
 * Parameters:
 * <ul>
 * <li><b>Required: String query</b> - Query that will be utilized in select statement.</li>
 * <li><b>Required: ReadStH reader</b> - Interface implementation used to read output ResultSet from select statement.</li>
 * <li><i>Optional: String errMsg</i> - Custom error message that will be logged if query fails.</li>
 * </ul>
 * Returns:(<b>boolean </b>) Returns true if the query ran successfully.<br>
 * <br>
 * Purpose:<br>
 * The select function one to grab data from the database with ease. Utilizing the ReadStT, one may set up query parameters (then use ParamReadStH and
 * set params in <code>setParams()</code>) and read the replied data from the query easily.<br>
 * <br>
 * Best practices is to create custom classes that implement ReadStT in order to throw the data around.<br>
 * <br>
 * After the function is called, it automatically closes and recycles the SQL Connection.<br>
 * <br>
 * Example:
 * 
 * <pre>
 * DB.select(&quot;SELECT name FROM test_table WHERE id=?&quot;, new ParamReadStH() {
 * 
 * 	public void setParams(PreparedStatement stmt) throws SQLException {
 * 		stmt.setInt(1, 50);
 * 	}
 * 
 * 	public void handleRead(ResultSet rset) throws SQLException {
 * 		while (rset.next()) {
 * 			// Usually here in the custom class you would set it to your needed var.
 * 			var = rset.getString(&quot;name&quot;);
 * 		}
 * 	}
 * });
 * </pre>
 * </p>
 * <hr>
 * <b>INSERT / UPDATE (insertUpdate method)</b>
 * <p>
 * Parameters:
 * <ul>
 * <li><b>Required: String query</b> - Query that will be executed in insert/update statement.</li>
 * <li><b>Required: IUStT batch</b> - Util used to modify query parameters OR add add batches.</li>
 * <li><i>Optional: String errMsg</i> - Custom error message that will be logged if query fails.</li>
 * </ul>
 * Returns:(<b>boolean</b>) Returns true if the query ran successfully.<br>
 * <br>
 * Purpose:<br>
 * The insertUpdate function allows one to insert and update database entries. One may utilize it without needing to modify the query at all or
 * provide a IUStH interface implementation to add parameters to statement and/or gather them in batch.<br>
 * <br>
 * <b> If the IUStH util IS provided in the function's parameters - The coder MUST call the functions stmt.executeBatch() OR stmt.executeUpdate() in
 * order to successfully run the query.<br>
 * If IUSth util is NOT provided, the query will execute as it is. </b><br>
 * <br>
 * Best practices is to create custom classes that implement IUStT in order to modify the query in proficient manners.<br>
 * <br>
 * After the function is called, it automatically closes and recycles the SQL Connection.<br>
 * <br>
 * Example:<br>
 * 
 * <pre>
 * DB.insertUpdate(&quot;UPDATE test_table SET some_column=1&quot;);
 * </pre>
 * 
 * <br>
 * 
 * <pre>
 * DB.insertUpdate(&quot;INSERT INTO test_table VALUES (?)&quot;, new IUStH() {
 * 
 * 	public void handleInsertUpdate(PreparedStatement stmt) {
 * 		// Usually this would be data from the custom class that implements IUSth
 * 		String[] batchTestVars = { &quot;bob&quot;, &quot;mike&quot;, &quot;joe&quot; };
 * 
 * 		for (String n : batchTestVars) {
 * 			stmt.setString(1, n);
 * 			stmt.addBatch();
 * 		}
 * 
 * 		// REQUIRED
 * 		stmt.executeBatch();
 * 	}
 * });
 * 
 * </pre>
 * 
 * <br>
 * 
 * <pre>
 * DB.insertUpdate(&quot;UPDATE test_table SET some_column=? WHERE other_column=?&quot;, new IUStH() {
 * 
 * 	public void handleInsertUpdate(PreparedStatement stmt) {
 * 		stmt.setString(1, &quot;xxx&quot;);
 * 		stmt.setInt(2, 10);
 * 		stmt.executeUpdate();
 * 	}
 * });
 * 
 * </pre>
 * </p>
 * 
 * @author Disturbing
 */
public final class DB {

	protected static final Logger log = LoggerFactory.getLogger(DB.class);

	/**
	 * Prevent instantiation
	 */
	private DB() {
	}

	/**
	 * Executes Select Query. Uses ReadSth to utilize params and return data. Recycles connection after completion.
	 * 
	 * @param query
	 * @param reader
	 * @return boolean Success
	 */
	public static boolean select(String query, ReadStH reader) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(query)) {
			if (reader instanceof ParamReadStH)
				((ParamReadStH) reader).setParams(stmt);
			ResultSet rset = stmt.executeQuery();
			reader.handleRead(rset);
		} catch (Exception e) {
			log.error("Error executing select query " + query, e);
			return false;
		}
		return true;
	}

	/**
	 * Call stored procedure
	 * 
	 * @param query
	 * @param reader
	 * @return
	 */
	public static boolean call(String query, ReadStH reader) {
		try (Connection con = DatabaseFactory.getConnection(); CallableStatement stmt = con.prepareCall(query)) {
			if (reader instanceof CallReadStH)
				((CallReadStH) reader).setParams(stmt);
			ResultSet rset = stmt.executeQuery();
			reader.handleRead(rset);
		} catch (Exception e) {
			log.error("Error calling stored procedure " + query, e);
			return false;
		}
		return true;
	}

	/**
	 * Executes Insert or Update Query not needing any further modification or batching. Recycles connection after completion.
	 * 
	 * @param query
	 * @return boolean Success
	 */
	public static boolean insertUpdate(String query) {
		return insertUpdate(query, null);
	}

	/**
	 * Executes Insert / Update Query. Utilizes IUSth for Batching and Query Editing. MUST MANUALLY EXECUTE QUERY / BATCH IN IUSth (No need to close
	 * Statement after execution). Recycles connection after completion
	 * 
	 * @param query
	 * @param batch
	 * @return boolean Success
	 */
	public static boolean insertUpdate(String query, IUStH batch) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(query)) {
			if (batch != null)
				batch.handleInsertUpdate(stmt);
			else
				stmt.executeUpdate();
		} catch (Exception e) {
			log.error("Failed to execute IU query " + query, e);
			return false;
		}
		return true;
	}

	/**
	 * Begins new transaction
	 * 
	 * @return new Transaction object
	 * @throws java.sql.SQLException
	 *           if was unable to create transaction
	 */
	public static Transaction beginTransaction() throws SQLException {
		Connection con = DatabaseFactory.getConnection();
		return new Transaction(con);
	}

	/**
	 * Creates PreparedStatement with given sql string.<br>
	 * Statements are created with {@link java.sql.ResultSet#TYPE_FORWARD_ONLY} and {@link java.sql.ResultSet#CONCUR_READ_ONLY}
	 * 
	 * @param sql
	 *          SQL query
	 * @return Prepared statement if ok or null if error happened while creating
	 */
	public static PreparedStatement prepareStatement(String sql) {
		return prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	}

	/**
	 * Creates {@link java.sql.PreparedStatement} with given sql<br>
	 * 
	 * @param sql
	 *          SQL query
	 * @param resultSetType
	 *          a result set type; one of <br>
	 *          <code>ResultSet.TYPE_FORWARD_ONLY</code>,<br>
	 *          <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or <br>
	 *          <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
	 * @param resultSetConcurrency
	 *          a concurrency type; one of <br>
	 *          <code>ResultSet.CONCUR_READ_ONLY</code> or <br>
	 *          <code>ResultSet.CONCUR_UPDATABLE</code>
	 * @return Prepared Statement if ok or null if error happened while creating
	 */
	public static PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) {
		Connection c = null;
		PreparedStatement ps = null;
		try {
			c = DatabaseFactory.getConnection();
			ps = c.prepareStatement(sql, resultSetType, resultSetConcurrency);
		} catch (Exception e) {
			log.error("Can't create PreparedStatement for query: " + sql, e);
			if (c != null) {
				try {
					c.close();
				} catch (SQLException e1) {
					log.error("Can't close connection after exception", e1);
				}
			}
		}

		return ps;
	}

	/**
	 * Executes PreparedStatement
	 * 
	 * @param statement
	 *          PreparedStatement to execute
	 * @return returns result of {@link java.sql.PreparedStatement#executeQuery()} or -1 in case of error
	 */
	public static int executeUpdate(PreparedStatement statement) {
		try {
			return statement.executeUpdate();
		} catch (Exception e) {
			log.error("Can't execute update for PreparedStatement", e);
		}

		return -1;
	}

	/**
	 * Executes PreparedStatement and closes it and it's connection
	 * 
	 * @param statement
	 *          PreparedStatement to close
	 */
	public static void executeUpdateAndClose(PreparedStatement statement) {
		executeUpdate(statement);
		close(statement);
	}

	/**
	 * Executes query and returns ResultSet
	 * 
	 * @param statement
	 *          preparedStement to execute
	 * @return ResultSet or null if error
	 */
	public static ResultSet executeQuerry(PreparedStatement statement) {
		ResultSet rs = null;
		try {
			rs = statement.executeQuery();
		} catch (Exception e) {
			log.error("Error while executing query", e);
		}
		return rs;
	}

	/**
	 * Closes PreparedStatemet, it's connection and last ResultSet
	 * 
	 * @param statement
	 *          statement to close
	 */
	public static void close(PreparedStatement statement) {
		if (statement == null) // e.g. null from failed DB.prepareStatement call
			return;
		try {
			if (statement.isClosed()) {
				// noinspection ThrowableInstanceNeverThrown
				log.warn("Attempt to close PreparedStatement that is closed already", new Exception());
				return;
			}

			Connection c = statement.getConnection();
			statement.close();
			c.close();
		} catch (Exception e) {
			log.error("Error while closing PreparedStatement", e);
		}
	}
}
