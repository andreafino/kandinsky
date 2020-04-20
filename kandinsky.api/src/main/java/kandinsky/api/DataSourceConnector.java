package kandinsky.api;

public interface DataSourceConnector {
	
	public String getApps();
	
	public String getTablesDefinition();
	
	public void migrateTable(String sourceTable, String destTable);
	
	public void setConfigurations(Configuration sourceConf, Configuration destConf);
	
	public String executeAutoMapping(String sourceTable, String destTable);
	
}
