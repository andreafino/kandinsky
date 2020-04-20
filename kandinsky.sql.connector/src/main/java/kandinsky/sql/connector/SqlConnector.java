package kandinsky.sql.connector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import kandinsky.api.Configuration;
import kandinsky.api.DataSourceConnector;

@Component(name = "sql.connector", immediate = true)
public class SqlConnector implements DataSourceConnector {

	private Configuration sourceConf;
	private Configuration destConf;
	private Connection conn;

	
	@Activate
	protected void activated() throws Exception
	{
		Configuration a = new Configuration();
		a.server = "EOS-AFINO";
		a.port = 1433;
		a.database = "BC_ARAP";
		a.schema = "dbo";
		a.username = "bcuser";
		a.password = "bcuser";
		
		Configuration b = new Configuration();
		b.server = "EOS-AFINO";
		b.port = 1433;
		b.database = "Txl_Db_TestDatabase";
		b.schema = "dbo";
		b.username = "bcuser";
		b.password = "bcuser";
		
		setConfigurations(a, b);
		System.out.println(getApps());
	}
	
	@Override
	public String getApps() throws Exception {
		String sql = "SELECT * FROM " + formatTableName(destConf, SqlConstants.APPS);
		
		// Open connecton and create sql command
		openConnection(destConf.getConnectionString());
		
		PreparedStatement stmt = createCommand(sql);
		
		ResultSet rs = stmt.executeQuery();
		String result = parseRsToJson(rs);
		
		// Close all connection to database
		rs.close();
		stmt.close();
		closeConnection();
		
		return result;
	}

	@Override
	public String getTablesDefinition(String appPackageId, String appId, String companyName) throws SQLException, ClassNotFoundException {
		openConnection(destConf.getConnectionString());
		
		PreparedStatement objectsStmt = null;
		PreparedStatement columnsStmt = null;
		
		String sql = "SELECT * FROM " + SqlConstants.APP_OBJECTS + " WHERE [App Package ID] = ?";
		objectsStmt = createCommand(sql);
		objectsStmt.setString(1, appPackageId);
		
		ResultSet rs = objectsStmt.executeQuery();
		JSONArray resArray = new JSONArray();
		
		while(rs.next())
		{
			int objectType = rs.getInt("Object Type");
			String objectName = rs.getString("Object Name");
			
			if (objectType == ObjectsType.TableExtension.getValue()) {
				int objectSubtype = rs.getInt("Object Type");
				objectName = getTableExtensionName(objectSubtype) + "$" + appId;
			}
			
			// Get correct table name
			String tableName = companyName.replace('.', '_') + "$";
			tableName += objectName.replace('.', '_');
			
			// Get metadata of current table
			sql = "SELECT * FROM " + formatTableName(destConf, SqlConstants.SYSCOLUMNS);
			sql += " WHERE TABLE_NAME = ?";
			
			columnsStmt = createCommand(sql);
			columnsStmt.setString(1, tableName);
			
			// Execute query and create Json Object
			ResultSet rs2 = columnsStmt.executeQuery();
			
			JSONArray metadataTable = new JSONArray();
			addResultSetToArray(rs2, metadataTable);
			
			JSONObject jObj = new JSONObject();
			jObj.put("name", tableName);
			jObj.put("metadata", metadataTable);
			resArray.add(jObj);
			
			rs2.close();
			columnsStmt.close();
		}	
		
		rs.close();
		objectsStmt.close();
		closeConnection();
		
		return resArray.toJSONString();
	}

	@Override
	public void migrateTable(String sourceTable, String destTable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setConfigurations(Configuration sourceConf, Configuration destConf) {
		this.sourceConf = sourceConf;
		this.destConf = destConf;
	}

	@Override
	public String executeAutoMapping(String sourceTable, String destTable) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getCompanies() throws Exception {
		String sql = "SELECT * FROM " + formatTableName(destConf, SqlConstants.COMPANIES);
		
		openConnection(destConf.getConnectionString());
		PreparedStatement companyStmt = createCommand(sql);
		
		ResultSet rs = companyStmt.executeQuery();
		String result = parseRsToJson(rs);
		
		rs.close();
		companyStmt.close();
		closeConnection();
		
		return result;
	}
	
	private void addResultSetToArray(ResultSet rs, JSONArray targetArray) throws SQLException
	{
		while(rs.next())
		{
			JSONObject jObj = new JSONObject();
			
			ResultSetMetaData rsMetadata = rs.getMetaData();
			int count = rsMetadata.getColumnCount();
			
			for (int i = 0; i < count; i++) {
				String colName = rsMetadata.getColumnName(i);
				
				jObj.put(colName, rs.getObject(i));
			}
			
			targetArray.add(jObj);
		}
	}
	
	
	private String getTableExtensionName(int objectSubtype) throws SQLException, ClassNotFoundException
	{
		String sql = "SELECT * FROM " + formatTableName(destConf, SqlConstants.OBJECTS) + " WHERE ID = ?";
		PreparedStatement stmt = createCommand(sql);
		stmt.setInt(1, objectSubtype);
		
		ResultSet rs = stmt.executeQuery();
		rs.first();
		String name = rs.getString("Name");
		
		rs.close();
		stmt.close();
		closeConnection();
		
		return name;
	}
	
	
	/*
	 * Open new connection
	 * 
	 * */
	private void openConnection(String connectionUrl) throws SQLException, ClassNotFoundException
	{
		Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		this.conn = DriverManager.getConnection(connectionUrl);
	}
	
	/*
	 * Create new command from sql
	 * 
	 * */
	private PreparedStatement createCommand(String sql) throws SQLException
	{
		PreparedStatement stmt = null;
		stmt = conn.prepareStatement(sql);
		return stmt;
	}
	
	/*
	 * Close connection
	 * 
	 * */
	private void closeConnection() throws SQLException
	{
		conn.close();
	}
	
	
	
	private String formatTableName(Configuration conf, String tableName)
	{
		return "[" + conf.database + "].[" + conf.schema + "].[" + tableName + "]";  
	}
	
	private String parseRsToJson(ResultSet rs) throws SQLException
	{
		JSONArray resArray = new JSONArray();
		while(rs.next())
		{
			JSONObject jObj = new JSONObject();
			
			ResultSetMetaData rsMetadata = rs.getMetaData();
			int count = rsMetadata.getColumnCount();
			
			for (int i = 1; i <= count; i++) {
				String colName = rsMetadata.getColumnName(i);			
				jObj.put(colName, rs.getObject(i));
			}
			
			resArray.add(jObj);
		}
				
		return resArray.toJSONString();
	}

	
	
}
