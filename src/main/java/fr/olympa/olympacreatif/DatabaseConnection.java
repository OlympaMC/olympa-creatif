package fr.olympa.olympacreatif;

import java.sql.*;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

public class DatabaseConnection {

    Connection con = null;
    Statement statement;
    ResultSet result;
    ResultSetMetaData metaBase;
    
	public DatabaseConnection(FileConfiguration config) {
		 
	    
        String url = "jdbc:mysql://" +config.getString("database.address")  + ":" + config.getString("database.port") + "/" + config.getString("database.database");
        try {
    		Class.forName("org.mariadb.jdbc.Driver");
			con = DriverManager.getConnection(url, config.getString("database.username") , config.getString("database.password"));
	        System.out.println("Overture de la connection");
	        statement = con.createStatement();
		} catch (SQLException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Impossible to connect to MariaDB database. Check availability of the database and the config validity.");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Impossible to connect to MariaDB database. Check availability of the database and the config validity.");
			e.printStackTrace();
		}
	}
	
	public ResultSet executeQuery(String s) {
		try {
			if (s.split(" ")[0].equals("SELECT")) {
				return statement.executeQuery(s);
			}else {
				statement.executeUpdate(s);
				return null;	
			}
		} catch (SQLException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Impossible to connect to MariaDB database. Check availability of the database and the config validity.");
			e.printStackTrace();
		}
		
		return null;
	}
	
	
}
