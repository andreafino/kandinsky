package kandinsky.sql.connector;

public enum ObjectsType {
	TableExtension (15),
	Table (1);
	
	private final int objType;
	
	private ObjectsType(int objType)
	{
		this.objType = objType;
	}
	
	public int getValue() { 
		return objType; 
	}
	
	
}
