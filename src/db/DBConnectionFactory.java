package db;

import db.mongodb.MongoDBCollection;
import db.mySQL.MySQLConnection;

public class DBConnectionFactory {
	private static final String DEFAULT_DB = "mysql";
	
	public static DBConnection getConnection(String db) {
		switch (db) {
		case "mysql":
			return new MySQLConnection();
		case "mongodb":
			 return new MongoDBCollection();
		default:
			throw new IllegalArgumentException("Invalid db:" + db);
		}

	}

	public static DBConnection getConnection() {
		return getConnection(DEFAULT_DB);
	}

}
