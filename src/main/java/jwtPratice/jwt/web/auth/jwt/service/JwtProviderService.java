package jwtPratice.jwt.web.auth.jwt.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jwtPratice.jwt.web.auth.jwt.JwtProperties;
import jwtPratice.jwt.web.auth.jwt.JwtToken;
import jwtPratice.jwt.web.auth.jwt.refreshToken.RefreshToken;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * JWT 토큰 생성과 관련된 서비스
 */
@Service
public class JwtProviderService {

    /**
     * accessToken, refreshToken 생성
     */
    public JwtToken createJwtToken(Long id, String userId) {

        //Access token 생성
        String accessToken = JWT.create()
                .withSubject(userId)
                .withExpiresAt(new Date(System.currentTimeMillis() + JwtProperties.AccessToken_TIME))
                .withClaim("id", id)
                .withClaim("userid", userId)
                .sign(Algorithm.HMAC512(JwtProperties.SECRET));

        //Refresh token 생성
        String refreshToken = JWT.create()
                .withSubject(userId)
                .withExpiresAt(new Date(System.currentTimeMillis() + JwtProperties.RefreshToken_TIME))
                .withClaim("id", id)
                .withClaim("userid", userId)
                .sign(Algorithm.HMAC512(JwtProperties.SECRET));

        return JwtToken.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * access token 생성
     */
    public String createAccessToken(Long id , String userId) {

        String accessToken = JWT.create()
                .withSubject(userId)
                .withExpiresAt(new Date(System.currentTimeMillis() + JwtProperties.AccessToken_TIME))
                .withClaim("id", id)
                .withClaim("userid", userId)
                .sign(Algorithm.HMAC512(JwtProperties.SECRET));

        return accessToken;
    }


    /**
     * refreshToken validation 체크(refresh token 이 넘어왔을때)
     * 정상 - access 토큰 생성후 반환
     * 비정상 - null
     */
    public String validRefreshToken(RefreshToken refreshToken) {

        String RefreshToken = refreshToken.getRefreshToken();

        try {
            DecodedJWT verify = JWT.require(Algorithm.HMAC512(JwtProperties.SECRET)).build().verify(RefreshToken);

            //refresh 토큰의 만료시간이 지나지 않아 access 토큰만 새로 생성
            if(!verify.getExpiresAt().before(new Date())) {
                String accessToken = createAccessToken(verify.getClaim("id").asLong(), verify.getClaim("userid").asString());
                return accessToken;
            }
        }catch (Exception e) {

            //refresh 토큰이 만료됨
            return null;
        }
        return null;
    }


}
