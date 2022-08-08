package jwtPratice.jwt.web.auth.jwt.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jwtPratice.jwt.domain.model.User;
import jwtPratice.jwt.domain.repository.UserRepository;
import jwtPratice.jwt.web.auth.jwt.JwtProperties;
import jwtPratice.jwt.web.auth.jwt.JwtToken;
import jwtPratice.jwt.web.auth.jwt.refreshToken.RefreshToken;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 실제 JWT 토큰과 관련된 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JwtService {

    private final JwtProviderService jwtProviderService;
    private final UserRepository userRepository;

    /**
     * access, refresh 토큰 생성
     */
    @Transactional
    public JwtToken joinJwtToken(String userId) {

        User user = userRepository.findByUserid(userId);
        RefreshToken userRefreshToken = user.getJwtRefreshToken();

        //처음 서비스를 이용하는 사용자(refresh 토큰이 없는 사용자)
        if(userRefreshToken ==null) {

            //access, refresh 토큰 생성
            JwtToken jwtToken = jwtProviderService.createJwtToken(user.getId(), user.getUserid());

            //refreshToken 생성
            RefreshToken refreshToken = new RefreshToken(jwtToken.getRefreshToken());

            //DB에 저장(refresh 토큰 저장)
            user.createRefreshToken(refreshToken);

            return jwtToken;
        }
        //refresh 토큰이 있는 사용자(기존 사용자)
        else {

            String accessToken = jwtProviderService.validRefreshToken(userRefreshToken);

            //refresh 토큰 기간이 유효
            if(accessToken !=null) {
                return new JwtToken(accessToken, userRefreshToken.getRefreshToken());
            }
            else { //refresh 토큰 기간만료
                //새로운 access, refresh 토큰 생성
                JwtToken newJwtToken = jwtProviderService.createJwtToken(user.getId(), user.getUserid());

                user.SetRefreshToken(newJwtToken.getRefreshToken());
                return newJwtToken;
            }

        }

    }

    /**
     * access 토큰 validate
     */
    public String validAccessToken(String accessToken) {

        try {
            DecodedJWT verify = JWT.require(Algorithm.HMAC512(JwtProperties.SECRET)).build().verify(accessToken);

            if(!verify.getExpiresAt().before(new Date())) {
                return verify.getClaim("userid").asString();
            }

        }catch (Exception e) {
            /**
             * 여기도 accesstoken이 기간 만료인지 , 정상적이지 않은 accesstoken 인지 구분해야하나??!???????????????????????????????????
             */
            return null;
        }
        return null;
    }

    /**
     * refresh 토큰 validate
     */
    @Transactional
    public JwtToken validRefreshToken(String userid, String refreshToken) {

        User findUser = userRepository.findByUserid(userid);

        //전달받은 refresh 토큰과 DB의 refresh 토큰이 일치하는지 확인
        RefreshToken findRefreshToken = sameCheckRefreshToken(findUser, refreshToken);

        //refresh 토큰이 만료되지 않았으면 access 토큰이 null 이 아이다.
        String accessToken = jwtProviderService.validRefreshToken(findRefreshToken);

        //refresh 토큰의 유효기간이 남아 access 토큰만 생성
        if(accessToken!=null) {
            return new JwtToken(accessToken, refreshToken);
        }
        //refresh 토큰이 만료됨 -> access, refresh 토큰 모두 재발급
       else {
            JwtToken newJwtToken = jwtProviderService.createJwtToken(findUser.getId(), findUser.getUserid());
            findUser.SetRefreshToken(newJwtToken.getRefreshToken());
            return newJwtToken;
        }

    }
    public RefreshToken sameCheckRefreshToken(User findUser, String refreshToken) {

        //DB 에서 찾기
        RefreshToken jwtRefreshToken = findUser.getJwtRefreshToken();

        if(jwtRefreshToken.getRefreshToken().equals(refreshToken)){
            return jwtRefreshToken;
        }
        return null;
    }



    /**
     * json response 부분 따로 분리하기!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     */
    //로그인시 응답 json response
    public Map<String , String> successLoginResponse(JwtToken jwtToken) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("status", "200");
        map.put("message", "accessToken, refreshToken이 생성되었습니다.");
        map.put("accessToken", jwtToken.getAccessToken());
        map.put("refreshToken", jwtToken.getRefreshToken());
        return map;
    }

    //인증 요구 json response (jwt 토큰이 필요한 요구)
    public Map<String, String> requiredJwtTokenResponse() {
        Map<String ,String> map = new LinkedHashMap<>();
        map.put("status", "401");
        map.put("message", "인증이 필요한 페이지 입니다. 로그인을 해주세요");
        return map;
    }

    //accessToken이 만료된 경우의 reponse
    public Map<String, String> requiredRefreshTokenResponse() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("status", "401");
        map.put("message", "accessToken이 만료되었거나 잘못된 값입니다.");
        return map;
    }

    //refresh 토큰 재발급 response
    public Map<String, String> recreateTokenResponse(JwtToken jwtToken) {
        Map<String ,String > map = new LinkedHashMap<>();
        map.put("status", "200");
        map.put("message", "refresh, access 토큰이 재발급되었습니다.");
        map.put("accessToken", jwtToken.getAccessToken());
        map.put("refreshToken", jwtToken.getRefreshToken());
        return map;
    }

}
