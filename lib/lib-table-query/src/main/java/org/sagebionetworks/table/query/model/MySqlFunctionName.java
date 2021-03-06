package org.sagebionetworks.table.query.model;

/**
 * Information about the supported MySql functions.
 *
 */
public enum MySqlFunctionName {

	CURRENT_TIMESTAMP(FunctionReturnType.STRING),
	CURRENT_DATE(FunctionReturnType.STRING),
	CURRENT_TIME(FunctionReturnType.STRING),
	NOW(FunctionReturnType.STRING),
	UNIX_TIMESTAMP(FunctionReturnType.LONG),
	FROM_UNIXTIME(FunctionReturnType.STRING),
	CONCAT(FunctionReturnType.STRING),
	REPLACE(FunctionReturnType.STRING),
	UPPER(FunctionReturnType.STRING),
	LOWER(FunctionReturnType.STRING),
	TRIM(FunctionReturnType.STRING);
	
	FunctionReturnType returnType;
	
	MySqlFunctionName(FunctionReturnType returnType){
		this.returnType = returnType;
	}
	
	/**
	 * The return type of this function.
	 * @return
	 */
	public FunctionReturnType getFunctionReturnType(){
		return this.returnType;
	}
}
