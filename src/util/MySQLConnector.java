package util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.mysql.cj.jdbc.MysqlDataSource;

public class MySQLConnector {

	private Connection connection;

	public MySQLConnector
				(String serverName,
				 String databaseName,
				 String user,
				 String password) throws SQLException {
		MysqlDataSource ds = new MysqlDataSource();
		ds.setServerName(serverName);
		ds.setDatabaseName(databaseName);
		ds.setUser(user);
		ds.setPassword(password);
		connection = ds.getConnection();
	}

	public void executeUpdate(String mysqlQuery) throws SQLException {
		connection.createStatement().executeUpdate(mysqlQuery);
	}

	public ResultSet executeQuery(String mysqlQuery) throws SQLException {
		return connection.createStatement().executeQuery(mysqlQuery);
	}

}
