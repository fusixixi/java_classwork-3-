package cn.edu.sdu.java.server.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * ResponseUtil 响应工具类
 * 用于统一封装HTTP响应数据
 */
public class ResponseUtil {

    /**
     * 成功响应
     * @param message 消息
     * @param data 数据
     * @return ResponseEntity
     */
    public static ResponseEntity<?> success(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", message);
        response.put("data", data);
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * 成功响应（只返回数据）
     * @param data 数据
     * @return ResponseEntity
     */
    public static ResponseEntity<?> success(Object data) {
        return success("操作成功", data);
    }

    /**
     * 成功响应（只返回消息）
     * @param message 消息
     * @return ResponseEntity
     */
    public static ResponseEntity<?> successMessage(String message) {
        return success(message, null);
    }

    /**
     * 错误响应
     * @param message 错误消息
     * @return ResponseEntity
     */
    public static ResponseEntity<?> error(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 400);
        response.put("message", message);
        response.put("data", null);
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 错误响应（带状态码）
     * @param code 状态码
     * @param message 错误消息
     * @return ResponseEntity
     */
    public static ResponseEntity<?> error(int code, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", code);
        response.put("message", message);
        response.put("data", null);
        response.put("timestamp", System.currentTimeMillis());

        HttpStatus status = code == 401 ? HttpStatus.UNAUTHORIZED :
                code == 403 ? HttpStatus.FORBIDDEN :
                        code == 404 ? HttpStatus.NOT_FOUND :
                                HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status).body(response);
    }

    /**
     * 未授权响应
     * @param message 错误消息
     * @return ResponseEntity
     */
    public static ResponseEntity<?> unauthorized(String message) {
        return error(401, message);
    }

    /**
     * 禁止访问响应
     * @param message 错误消息
     * @return ResponseEntity
     */
    public static ResponseEntity<?> forbidden(String message) {
        return error(403, message);
    }

    /**
     * 未找到响应
     * @param message 错误消息
     * @return ResponseEntity
     */
    public static ResponseEntity<?> notFound(String message) {
        return error(404, message);
    }

    /**
     * 服务器错误响应
     * @param message 错误消息
     * @return ResponseEntity
     */
    public static ResponseEntity<?> serverError(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 500);
        response.put("message", message);
        response.put("data", null);
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}