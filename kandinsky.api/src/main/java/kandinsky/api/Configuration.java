package kandinsky.api;

public class Configuration {
	
	public String server;
	public int port;
	
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
	
}
