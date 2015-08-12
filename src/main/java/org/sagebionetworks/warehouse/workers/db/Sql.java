package org.sagebionetworks.warehouse.workers.db;

/**
 * SQL constants.
 *
 */
public class Sql {

	// FILE_STATE
	public static final String TABLE_FILE_STATE = 				"FILE_STATE";
	public static final String COL_FILE_STATE_BUCKET = 			"S3_BUCKET";
	public static final String COL_FILE_STATE_KEY = 			"S3_KEY";
	public static final String COL_FILE_STATE_STATE = 			"STATE";
	public static final String COL_FILE_STATE_UPDATED_ON = 		"UPDATED_ON";
	public static final String COL_FILE_STATE_ERROR = 			"ERROR_MESSAGE";
	public static final String COL_FILE_STATE_ERROR_DETAILS = 	"ERROR_DETAILS";

	// FOLDER_STATE
	public static final String TABLE_FOLDER_STATE = 			"FOLDER_STATE";
	public static final String COL_FOLDER_STATE_BUCKET = 		"S3_BUCKET";
	public static final String COL_FOLDER_STATE_PATH = 			"S3_PATH";
	public static final String COL_FOLDER_STATE_STATE = 		"STATE";
	public static final String COL_FOLDER_STATE_UPDATED_ON = 	"UPDATED_ON";

	// PROCESSED_ACCESS_RECORD
	public static final String TABLE_PROCESSED_ACCESS_RECORD = 								"PROCESSED_ACCESS_RECORD";
	public static final String COL_PROCESSED_ACCESS_RECORD_SESSION_ID = 					"SESSION_ID";
	public static final String COL_PROCESSED_ACCESS_RECORD_ENTITY_ID = 						"ENTITY_ID";
	public static final String COL_PROCESSED_ACCESS_RECORD_CLIENT = 						"CLIENT";
	public static final String COL_PROCESSED_ACCESS_RECORD_NORMALIZED_METHOD_SIGNATURE = 	"NORMALIZED_METHOD_SIGNATURE";

	// ACCESS_RECORD
	public static final String TABLE_ACCESS_RECORD = 					"ACCESS_RECORD";
	public static final String COL_ACCESS_RECORD_SESSION_ID = 			"SESSION_ID";
	public static final String COL_ACCESS_RECORD_RETURN_OBJECT_ID = 	"RETURN_OBJECT_ID";
	public static final String COL_ACCESS_RECORD_ELAPSE_MS = 			"ELAPSE_MS";
	public static final String COL_ACCESS_RECORD_TIMESTAMP = 			"TIMESTAMP";
	public static final String COL_ACCESS_RECORD_VIA = 					"VIA";
	public static final String COL_ACCESS_RECORD_HOST = 				"HOST";
	public static final String COL_ACCESS_RECORD_THREAD_ID = 			"THREAD_ID";
	public static final String COL_ACCESS_RECORD_USER_AGENT = 			"USER_AGENT";
	public static final String COL_ACCESS_RECORD_QUERY_STRING = 		"QUERY_STRING";
	public static final String COL_ACCESS_RECORD_X_FORWARDED_FOR = 		"X_FORWARDED_FOR";
	public static final String COL_ACCESS_RECORD_REQUEST_URL = 			"REQUEST_URL";
	public static final String COL_ACCESS_RECORD_USER_ID = 				"USER_ID";
	public static final String COL_ACCESS_RECORD_ORIGIN = 				"ORIGIN";
	public static final String COL_ACCESS_RECORD_DATE = 				"DATE";
	public static final String COL_ACCESS_RECORD_METHOD = 				"METHOD";
	public static final String COL_ACCESS_RECORD_VM_ID = 				"VM_ID";
	public static final String COL_ACCESS_RECORD_INSTANCE = 			"INSTANCE";
	public static final String COL_ACCESS_RECORD_STACK = 				"STACK";
	public static final String COL_ACCESS_RECORD_SUCCESS = 				"SUCCESS";
	public static final String COL_ACCESS_RECORD_RESPONSE_STATUS = 		"RESPONSE_STATUS";
}
