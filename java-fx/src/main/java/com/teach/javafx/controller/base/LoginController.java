package com.teach.javafx.controller.base;

import com.teach.javafx.AppStore;
import com.teach.javafx.MainApplication;
import com.teach.javafx.request.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import java.io.IOException;

/**
 * LoginController 登录交互控制类 对应 base/login-view.fxml
 *  @FXML  属性 对应fxml文件中的 fx:id 属性 如TextField usernameField 对应 fx:id="usernameField"
 *  @FXML 方法 对应于fxml文件中的 on***Click的属性  如onLoginButtonClick() 对应onAction="#onLoginButtonClick"
 */
public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;
    @FXML
    private VBox vbox;
    @FXML
    private Label errorLabel;
    @FXML
    private ComboBox<String> roleComboBox;
    /**
     * 页面加载对象创建完成初始话方法，页面中控件属性的设置，初始数据显示等初始操作都在这里完成，其他代码都事件处理方法里
     */
    @FXML
    public void initialize() {
        roleComboBox.getItems().setAll("学生", "教师", "管理员");
        roleComboBox.getSelectionModel().selectFirst();
        // 添加输入监听器，用户开始输入时清除错误提示
        usernameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (errorLabel.isVisible()) {
                errorLabel.setVisible(false);
            }
        });
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (errorLabel.isVisible()) {
                errorLabel.setVisible(false);
            }
        });
    }

    /**
     *  点击登录按钮 执行onLoginButtonClick 方法 从面板上获取用户名和密码，请求后台登录服务，登录成功加载主框架，切换舞台到主框架，登录不成功，提示错误信息
     */
    @FXML
    protected void onAdminLoginButtonClick() {
        loginByRole("ROLE_ADMIN", "admin", "123456");
    }
    @FXML
    protected void onStudentLoginButtonClick() {
        loginByRole("ROLE_STUDENT", "2022030001", "123456");
    }
    @FXML
    protected void onTeacherLoginButtonClick() {
        loginByRole("ROLE_TEACHER", "200799013517", "123456");
    }
    @FXML
    protected void onRoleLoginButtonClick() {
        String role = roleComboBox.getValue();
        if (role == null || role.isBlank()) {
            roleComboBox.getSelectionModel().selectFirst();
            role = roleComboBox.getValue();
        }
        if ("管理员".equals(role)) {
            onAdminLoginButtonClick();
        } else if ("教师".equals(role)) {
            onTeacherLoginButtonClick();
        } else {
            onStudentLoginButtonClick();
        }
    }

    private void loginByRole(String expectedRole, String defaultUserName, String defaultPassword) {
        String inputUser = usernameField.getText();
        String inputPassword = passwordField.getText();
        
        // 验证输入是否为空
        if (inputUser == null || inputUser.isBlank() || inputPassword == null || inputPassword.isBlank()) {
            errorLabel.setText("请输入账号密码");
            errorLabel.setVisible(true);
            return;
        }
        
        // 隐藏错误提示
        errorLabel.setVisible(false);
        
        String userName = inputUser.trim();
        String password = inputPassword;
        onLoginButtonClick(userName, password, expectedRole);
    }

    protected void onLoginButtonClick(String username, String password, String expectedRole) {
        LoginRequest loginRequest = new LoginRequest(username,password);
        String msg = HttpRequestUtil.login(loginRequest);
        if(msg != null) {
            MessageDialog.showDialog( msg);
            return;
        }
        if (AppStore.getJwt() == null || AppStore.getJwt().getRole() == null || !expectedRole.equals(AppStore.getJwt().getRole())) {
            AppStore.setJwt(null);
            MessageDialog.showDialog("当前账号不是对应身份，请检查用户名或切换登录入口。");
            return;
        }
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("base/main-frame.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load(), -1, -1);
            AppStore.setMainFrameController((MainFrameController) fxmlLoader.getController());
            MainApplication.resetStage("教学管理系统", scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}