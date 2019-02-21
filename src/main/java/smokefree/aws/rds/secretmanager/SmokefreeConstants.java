package smokefree.aws.rds.secretmanager;

public class SmokefreeConstants {

	public static final String DBNAME = "dbname";

	public static final String DB_PORT = "port";

	public static final String DB_HOST = "host";

	public static final String DB_ENGINE = "engine";

	public static final String DB_PASSWORD = "password";

	public static final String DB_USERNAME = "username";

	public static final Object COLON = ":";

	public static final Object DOUBLE_SLASH = "//";
	public static final Object SINGLE_SLASH = "/";

	/**
	 * Constants for JWT Claim attributes
	 */
	public static class JWTClaimSet {
		public static final String COGNITO_USER_NAME = "cognito:username";
		public static final String USER_NAME = "user_name";
		public static final String USER_ID = "user_id";
		public static final String EMAIL_ADDRESS = "email";
	}

	public static final Long MAXIMUM_PLAYGROUNDS_DISTANCE = 100L;
	public static final Long MAXIMUM_PLAYGROUNDS_ALLOWED = 1000L;
	public static final Long MAXIMUM_VOLUNTEERS_PER_PLAYGROUND = 200L;

}
