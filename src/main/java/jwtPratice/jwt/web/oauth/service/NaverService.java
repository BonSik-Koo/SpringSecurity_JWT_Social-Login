package jwtPratice.jwt.web.oauth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jwtPratice.jwt.domain.model.User;
import jwtPratice.jwt.domain.repository.UserRepository;
import jwtPratice.jwt.web.oauth.provider.Token.NaverToken;
import jwtPratice.jwt.web.oauth.provider.profile.KakaoProfile;
import jwtPratice.jwt.web.oauth.provider.profile.NaverProfile;
import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NaverService {

    private final UserRepository userRepository;

    private final String client_id = "5EqPiMEcHUi0GoU53TMk";
    private final String client_secret = "OosqHTwmkF";
    private final String redirect_uri = "http://localhost:8880/login/oauth2/code/naver";
    private final String accessTokenUri = "https://nid.naver.com/oauth2.0/token";
    private final String UserInfoUri = "https://openapi.naver.com/v1/nid/me";

    /**
     * 카카오로 부터 엑세스 토큰을 받는 함수
     */
    public NaverToken getAccessToken(String code) {

        //요청 param (body)
        MultiValueMap<String , String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id",client_id );
        params.add("redirect_uri",redirect_uri);
        params.add("code", code);
        params.add("client_secret", client_secret);


        //request
        WebClient wc = WebClient.create(accessTokenUri);
        String response = wc.post()
                .uri(accessTokenUri)
                .body(BodyInserters.fromFormData(params))
                .header("Content-type","application/x-www-form-urlencoded;charset=utf-8" ) //요청 헤더
                .retrieve()
                .bodyToMono(String.class)
                .block();

        //json형태로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        NaverToken naverToken =null;

        try {
            naverToken = objectMapper.readValue(response, NaverToken.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return naverToken;
    }

    /**
     * 사용자 정보 가져오기
     */
    public NaverProfile findProfile(String token) {

        //Http 요청
        WebClient wc = WebClient.create(UserInfoUri);
        String response = wc.get()
                .uri(UserInfoUri)
                .header("Authorization", "Bearer " + token)
                .header("Content-type", "application/xml;charset=utf-8")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        ObjectMapper objectMapper = new ObjectMapper();
        NaverProfile naverProfile = null;

        try {
            naverProfile = objectMapper.readValue(response, NaverProfile.class);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        System.out.println("===========NaverProfile=================");
        System.out.println(naverProfile);
        return naverProfile;
    }

    /**
     * 카카오 로그인 사용자 강제 회원가입
     */
    @Transactional
    public User saveUser(String access_token) {
        NaverProfile profile = findProfile(access_token); //사용자 정보 받아오기
        User user = userRepository.findByUserid(profile.response.getId());

        //처음이용자 강제 회원가입
        if(user ==null) {
            user = User.builder()
                    .userid(profile.response.getId())
                    .password(null) //필요없으니 일단 아무거도 안넣음. 원하는데로 넣으면 됌
                    .nickname(profile.response.getNickname())
                    .profileImg(profile.response.profile_image)
                    .email(profile.response.email)
                    .roles("USER")
                    .createTime(LocalDateTime.now())
                    .provider("Naver")
                    .build();

            userRepository.save(user);
        }

        return user;
    }
}
