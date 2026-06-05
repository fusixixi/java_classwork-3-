package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * UserController 实现用户管理相关Web服务
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 重置用户密码
     * @param dataRequest username 用户名  password 新密码
     * @return DataResponse 操作结果
     */
    @PostMapping("/resetPassword")
    public DataResponse resetPassword(@Valid @RequestBody DataRequest dataRequest) {
        return authService.resetPassword(dataRequest);
    }
}
