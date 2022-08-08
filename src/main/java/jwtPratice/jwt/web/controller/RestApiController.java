package jwtPratice.jwt.web.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import jwtPratice.jwt.domain.model.User;
import jwtPratice.jwt.web.auth.jwt.JwtToken;
import jwtPratice.jwt.web.auth.jwt.service.JwtService;
import jwtPratice.jwt.web.oauth.provider.Token.KakaoToken;
import jwtPratice.jwt.domain.repository.UserRepository;
import jwtPratice.jwt.web.oauth.provider.Token.NaverToken;
import jwtPratice.jwt.web.oauth.service.KakaoService;
import jwtPratice.jwt.web.oauth.service.NaverService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Map;


@RestController
@RequiredArgsConstructor
public class RestApiController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final KakaoService kakaoService;
    private final NaverService naverService;

    /**
     * JWT 를 이용한 로그인
     */
    @GetMapping("/home")
    public String home() {
        return "home";
    }

    @PostMapping("/join")
    public String join(@ModelAttribute User user){

        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setRoles("USER");
        user.setCreateTime(LocalDateTime.now());
        userRepository.save(user);

        return "회원가입완료";
   }

   @GetMapping("/api/v1/user")
    public String test1() {
        return "success";
   }

    @GetMapping("/api/v1/manager")
    public String test2() {
        return "success";
    }
    @GetMapping("/api/v1/admin")
    public String test3() {
        return "success";
    }



    /**
     *   JWT를 이용한 네이버 로그인
     */

    /**front-end로 부터 받은 인가 코드 받기 및 사용자 정보 받기,회원가입 */
    @GetMapping("/api/oauth/token/kakao")
    public Map<String,String> KakaoLogin(@RequestParam("code") String code) {

        //access 토큰 받기
        KakaoToken oauthToken = kakaoService.getAccessToken(code);

        //사용자 정보받기 및 회원가입
        User saveUser = kakaoService.saveUser(oauthToken.getAccess_token());

        //jwt토큰 저장
        JwtToken jwtTokenDTO = jwtService.joinJwtToken(saveUser.getUserid());

        return jwtService.successLoginResponse(jwtTokenDTO);
    }
    //test로 직접 인가 코드 받기
    @GetMapping("/login/oauth2/code/kakao")
    public String KakaoCode(@RequestParam("code") String code) {
        return "카카오 로그인 인증완료, code: "  + code;
    }


    /**
     * JWT를 이용한 네이버 로그인
     */

    @GetMapping("/api/oauth/token/naver")
    public Map<String, String> NaverLogin(@RequestParam("code") String code) {

        NaverToken oauthToken = naverService.getAccessToken(code);

        User saveUser = naverService.saveUser(oauthToken.getAccess_token());

        JwtToken jwtToken = jwtService.joinJwtToken(saveUser.getUserid());

        return jwtService.successLoginResponse(jwtToken);
    }
    @GetMapping("/login/oauth2/code/naver")
    public String NaverCode(@RequestParam("code") String code) {
        return "네이버 로그인 인증완료, code: "  + code;
    }




    /**
     * refresh token 재발급
     * @return
     */
    @GetMapping("/refresh/{userId}")
    public Map<String,String> refreshToken(@PathVariable("userId") String userid, @RequestHeader("refreshToken") String refreshToken,
                                           HttpServletResponse response) throws JsonProcessingException {

        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        JwtToken jwtToken = jwtService.validRefreshToken(userid, refreshToken);
        Map<String, String> jsonResponse = jwtService.recreateTokenResponse(jwtToken);

        return jsonResponse;
    }

}
