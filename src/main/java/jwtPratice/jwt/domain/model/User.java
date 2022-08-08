package jwtPratice.jwt.domain.model;

import jwtPratice.jwt.web.auth.jwt.refreshToken.RefreshToken;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity @Data
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name ="user_id")
    private Long id;

    private String userid; //일반사용자-입력한 아이디, 카카오 사용자-카카오 고유 id(provider id)
    private String password;
    private String roles; //USER,ADMIN 게 넣을것이다.

    private String provider;
    private String nickname;
    private String profileImg;
    private String email;

    private LocalDateTime createTime;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "refreshToken")
    private RefreshToken jwtRefreshToken;

    @Builder
    public User(String userid, String password, String roles, String nickname, String profileImg,
                String email,LocalDateTime createTime,String provider) {
        this.userid = userid;
        this.password = password;
        this.roles = roles;
        this.nickname = nickname;
        this.profileImg = profileImg;
        this.email = email;
        this.createTime = createTime;
        this.provider = provider;
    }


    /**
     *  refresh 생성자, setter
     */
    public void createRefreshToken(RefreshToken refreshToken) {
        this.jwtRefreshToken = refreshToken;
    }
    public void SetRefreshToken(String refreshToken) {
        this.jwtRefreshToken.setRefreshToken(refreshToken);
    }

    /**
     * 사용자가 다양한 권한을 가지고 있을수 있음
     */
    public List<String> getRoleList() {
        if(this.roles.length()>0) {
            return Arrays.asList(this.roles.split(","));
        }
        return new ArrayList<>();
    }
}
