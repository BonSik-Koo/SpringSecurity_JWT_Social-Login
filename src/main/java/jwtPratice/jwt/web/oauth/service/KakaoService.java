package jwtPratice.jwt.web.oauth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jwtPratice.jwt.domain.model.User;
import jwtPratice.jwt.domain.repository.UserRepository;
import jwtPratice.jwt.web.oauth.provider.Token.KakaoToken;
import jwtPratice.jwt.web.oauth.provider.profile.KakaoProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KakaoService {

    private final UserRepository userRepository;

    private final String client_id = "77586d2dca03a869da31de487922651f";
    private final String client_secret = "5aS0GaASyRTyVtgUSTc9mR4fDxCuPa9T";
    private final String redirect_uri = "http://localhost:8880/login/oauth2/code/kakao";
    private final String accessTokenUri = "https://kauth.kakao.com/oauth/token";
    private final String UserInfoUri = "https://kapi.kakao.com/v2/user/me";

    /**
     * 카카오로 부터 엑세스 토큰을 받는 함수
     */
    public KakaoToken getAccessToken(String code) {

        //요청 param (body)
        MultiValueMap<String , String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id",client_id );
        params.add("redirect_uri", redirect_uri);
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

        System.out.println("response:" + response);


        //json형태로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        KakaoToken kakaoToken =null;

        try {
            kakaoToken = objectMapper.readValue(response, KakaoToken.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return kakaoToken;
    }

    /**
     * 사용자 정보 가져오기
     */
    public KakaoProfile findProfile(String token) {

        //Http 요청
        WebClient wc = WebClient.create(UserInfoUri);
        String response = wc.post()
                .uri(UserInfoUri)
                .header("Authorization", "Bearer " + token)
                .header("Content-type", "application/x-www-form-urlencoded;charset=utf-8")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        ObjectMapper objectMapper = new ObjectMapper();
        KakaoProfile kakaoProfile = null;

        try {
            kakaoProfile = objectMapper.readValue(response, KakaoProfile.class);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        System.out.println("===========KakaoProfile=================");
        System.out.println(kakaoProfile);

        return kakaoProfile;
    }

    /**
     * 카카오 로그인 사용자 강제 회원가입
     */
    @Transactional
    public User saveUser(String access_token) {
        KakaoProfile profile = findProfile(access_token); //사용자 정보 받아오기
        User user = userRepository.findByUserid(profile.getId());

        //처음이용자 강제 회원가입
        if(user ==null) {
            user = User.builder()
                    .userid(profile.getId())
                    .password(null) //필요없으니 일단 아무거도 안넣음. 원하는데로 넣으면 됌
                    .nickname(profile.getKakao_account().getProfile().getNickname())
                    .profileImg(profile.getKakao_account().getProfile().getProfile_image_url())
                    .email(profile.getKakao_account().getEmail())
                    .roles("USER")
                    .createTime(LocalDateTime.now())
                    .provider("Kakao")
                    .build();

            userRepository.save(user);
        }

        return user;
    }
}
