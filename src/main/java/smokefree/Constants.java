package smokefree;

/**
 * Local SDK constants.
 */

@SuppressWarnings("checkstyle:javadocmethod")
abstract class Constants {
    static final String DOMAIN_QUERY_PARAM_CLIENT_ID = "client_id";

    static final String DOMAIN_QUERY_PARAM_REDIRECT_URI = "redirect_uri";
    static final String TOKEN_AUTH_TYPE_CODE = "code";
    static final String TOKEN_GRANT_TYPE = "grant_type";
    static final String TOKEN_GRANT_TYPE_AUTH_CODE = "authorization_code";

    static final String HTTP_HEADER_PROP_CONTENT_TYPE = "Content-Type";
    static final String HTTP_HEADER_PROP_CONTENT_TYPE_DEFAULT = "application/x-www-form-urlencoded";
    static final String HTTP_REQUEST_TYPE_POST = "POST";
    static final String REDIRECT_URL = "http://localhost:8086/oauth/code";

}