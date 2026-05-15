package com.notara.usuarios.dto;

public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private UserInfo user;

    public LoginResponse(String accessToken, String refreshToken, UserInfo user) {
        this.accessToken  = accessToken;
        this.refreshToken = refreshToken;
        this.user         = user;
    }

    public String getAccessToken()  { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public UserInfo getUser()       { return user; }

    public static class UserInfo {
        private Long   id;
        private String name;
        private String email;

        public UserInfo(Long id, String name, String email) {
            this.id    = id;
            this.name  = name;
            this.email = email;
        }

        public Long   getId()    { return id; }
        public String getName()  { return name; }
        public String getEmail() { return email; }
    }
}
