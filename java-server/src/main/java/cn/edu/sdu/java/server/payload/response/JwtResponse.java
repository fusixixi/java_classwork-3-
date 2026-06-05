package cn.edu.sdu.java.server.payload.response;

/**
 * JwtResponse JWT数据返回对象 包含客户登录的信息
 * String token token字符串
 * String type JWT 类型
 * Integer id 用户的ID user_id
 * String username 用户的登录名
 * String role 用户角色 ROLE_ADMIN, ROLE_STUDENT, ROLE_TEACHER
 */
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Integer id;
    private String username;
    private String perName;
    private String role;
    private String serverName;

    public JwtResponse(String token, Integer id, String username, String perName, String role) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.perName = perName;
        this.role = role;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPerName() {
        return perName;
    }

    public void setPerName(String perName) {
        this.perName = perName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
}
