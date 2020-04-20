package kandinsky.api;

public interface DataSourceConnector {
	
	public String getApps() throws Exception;
	
	public String getCompanies() throws Exception;
	
	public String getTablesDefinition(String appPackageId, String appId, String companyName) throws Exception;
	
	public void migrateTable(String sourceTable, String destTable) throws Exception;
	
	public void setConfigurations(Configuration sourceConf, Configuration destConf);
	
	public String executeAutoMapping(String sourceTable, String destTable) throws Exception;
	
}
