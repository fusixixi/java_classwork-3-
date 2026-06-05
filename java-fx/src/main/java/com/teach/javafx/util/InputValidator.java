package com.teach.javafx.util;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * 输入验证工具类
 * 提供实时验证和表单提交验证功能
 */
public class InputValidator {

    // 错误消息常量
    public static final String MSG_PHONE_INVALID = "请输入有效的联系电话";
    public static final String MSG_STUDENT_ID_EMPTY = "学号不能为空";
    public static final String MSG_PASSWORD_EMPTY = "密码不能为空";
    public static final String MSG_NAME_EMPTY = "姓名不能为空";
    public static final String MSG_EMAIL_INVALID = "请输入有效的邮箱地址";

    // 验证状态存储
    private final Map<TextField, Boolean> validationStates = new HashMap<>();
    private final Map<TextField, Label> fieldErrorLabels = new HashMap<>();

    /**
     * 验证手机号码（11位数字）
     */
    public static boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) {
            return true; // 空值由必填验证处理
        }
        String normalized = phone.trim();
        if (!normalized.matches("^[0-9-]{7,20}$")) {
            return false;
        }
        String digitsOnly = normalized.replace("-", "");
        return digitsOnly.length() >= 7 && digitsOnly.length() <= 20;
    }

    /**
     * 验证非空
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * 验证邮箱格式
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return true; // 空值由必填验证处理
        }
        return email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }

    /**
     * 验证学号格式（非空）
     */
    public static boolean isValidStudentId(String studentId) {
        return isNotEmpty(studentId);
    }

    /**
     * 设置错误标签样式
     */
    public static void setupErrorLabel(Label errorLabel) {
        errorLabel.setTextFill(Color.RED);
        errorLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #d32f2f;");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    /**
     * 显示错误信息
     */
    public static void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    /**
     * 隐藏错误信息
     */
    public static void hideError(Label errorLabel) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    /**
     * 为TextField添加实时验证监听器
     * @param textField 输入框
     * @param errorLabel 错误标签
     * @param validator 验证函数
     * @param errorMessage 错误消息
     */
    public void addRealTimeValidator(TextField textField, Label errorLabel,
                                     Predicate<String> validator, String errorMessage) {
        fieldErrorLabels.put(textField, errorLabel);
        validationStates.put(textField, true);

        setupErrorLabel(errorLabel);

        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean isValid = validator.test(newValue);
            validationStates.put(textField, isValid);

            if (isValid) {
                hideError(errorLabel);
                removeErrorStyle(textField);
            } else {
                showError(errorLabel, errorMessage);
                addErrorStyle(textField);
            }
        });
    }

    /**
     * 添加必填验证
     */
    public void addRequiredValidator(TextField textField, Label errorLabel, String fieldName) {
        String message = fieldName + "不能为空";
        addRealTimeValidator(textField, errorLabel, InputValidator::isNotEmpty, message);
    }

    /**
     * 添加手机号验证
     */
    public void addPhoneValidator(TextField textField, Label errorLabel) {
        addRealTimeValidator(textField, errorLabel,
            phone -> phone == null || phone.isEmpty() || isValidPhoneNumber(phone),
            MSG_PHONE_INVALID);
    }

    /**
     * 添加邮箱验证
     */
    public void addEmailValidator(TextField textField, Label errorLabel) {
        addRealTimeValidator(textField, errorLabel,
            email -> email == null || email.isEmpty() || isValidEmail(email),
            MSG_EMAIL_INVALID);
    }

    /**
     * 手动验证单个字段
     */
    public boolean validateField(TextField textField, Label errorLabel,
                                 Predicate<String> validator, String errorMessage) {
        String value = textField.getText();
        boolean isValid = validator.test(value);

        validationStates.put(textField, isValid);

        if (isValid) {
            hideError(errorLabel);
            removeErrorStyle(textField);
        } else {
            showError(errorLabel, errorMessage);
            addErrorStyle(textField);
        }

        return isValid;
    }

    /**
     * 验证所有字段
     * @return 是否全部通过验证
     */
    public boolean validateAll() {
        boolean allValid = true;
        for (Map.Entry<TextField, Boolean> entry : validationStates.entrySet()) {
            if (!entry.getValue()) {
                allValid = false;
                TextField field = entry.getKey();
                Label errorLabel = fieldErrorLabels.get(field);
                if (errorLabel != null && !errorLabel.isVisible()) {
                    // 触发重新验证
                    field.setText(field.getText());
                }
            }
        }
        return allValid;
    }

    /**
     * 清除所有验证状态
     */
    public void clearAllValidation() {
        for (Map.Entry<TextField, Label> entry : fieldErrorLabels.entrySet()) {
            hideError(entry.getValue());
            removeErrorStyle(entry.getKey());
            validationStates.put(entry.getKey(), true);
        }
    }

    /**
     * 添加错误样式
     */
    public static void addErrorStyle(TextField textField) {
        if (!textField.getStyleClass().contains("input-error")) {
            textField.getStyleClass().add("input-error");
        }
    }

    /**
     * 移除错误样式
     */
    public static void removeErrorStyle(TextField textField) {
        textField.getStyleClass().remove("input-error");
    }

    /**
     * 获取验证状态
     */
    public boolean isValid(TextField textField) {
        return validationStates.getOrDefault(textField, true);
    }

    /**
     * 注册字段（不添加监听器，仅用于手动验证）
     */
    public void registerField(TextField textField, Label errorLabel) {
        fieldErrorLabels.put(textField, errorLabel);
        validationStates.put(textField, true);
        setupErrorLabel(errorLabel);
    }
}
