package kandinsky.api;

import java.text.MessageFormat;

public class Configuration {
	
	public String server;
	public int port;
	
	public String schema;
	public String database;
	public String username;
	public String password;
	
	public Configuration()
	{
		this.server = "";
		this.port = 0;
		
		this.database = "";
		this.username = "";
		this.password = "";
	}
	
	public String getConnectionString()
	{
		String connStr = "";
		connStr = "jdbc:sqlserver://{0}:{1};databaseName={2};user={3};password={4}";
		connStr = MessageFormat.format(connStr, this.server, String.valueOf(this.port), this.database, this.username, this.password);
		System.out.println(connStr);
		return connStr;
	}
	
}
