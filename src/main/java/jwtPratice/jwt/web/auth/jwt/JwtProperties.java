package jwtPratice.jwt.web.auth.jwt;

public interface JwtProperties {

    String SECRET = "구본식"; //우리 서버만 알고 있는 비밀값
    int AccessToken_TIME =  2000; // (1/1000초)
    int RefreshToken_TIME = 200000 ;//* 12 * 24;
    String HEADER_STRING = "accessToken";
}
