package com.teach.javafx.controller;

import com.teach.javafx.controller.base.LocalDateStringConverter;
import com.teach.javafx.controller.base.MessageDialog;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.request.OptionItem;
import com.teach.javafx.util.CommonMethod;
import com.teach.javafx.util.InputValidator;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeacherController extends ToolController {
    private ImageView photoImageView;
    @FXML
    private TableView<Map> dataTableView;
    @FXML
    private TableColumn<Map, String> numColumn;
    @FXML
    private TableColumn<Map, String> nameColumn;
    @FXML
    private TableColumn<Map, String> deptColumn;
    @FXML
    private TableColumn<Map, String> titleColumn;
    @FXML
    private TableColumn<Map, String> degreeColumn;
    @FXML
    private TableColumn<Map, String> phoneColumn;
    @FXML
    private TableColumn<Map, String> emailColumn;
    @FXML
    private TableColumn<Map, String> passwordColumn;

    @FXML
    private TextField numField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField deptField;
    @FXML
    private TextField cardField;
    @FXML
    private ComboBox<OptionItem> genderComboBox;
    @FXML
    private DatePicker birthdayPick;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField addressField;
    @FXML
    private TextField titleField;
    @FXML
    private TextField degreeField;
    @FXML
    private TextField numNameTextField;
    @FXML
    private TextField passwordField;
    @FXML
    private CheckBox showPasswordCheckBox;
    @FXML
    private Button photoButton;

    // 验证相关字段
    @FXML
    private Label numErrorLabel;  // 工号错误提示
    @FXML
    private Label passwordErrorLabel;  // 密码错误提示
    @FXML
    private Label phoneErrorLabel;  // 电话错误提示
    @FXML
    private Label nameErrorLabel;  // 姓名错误提示
    @FXML
    private Label emailErrorLabel;  // 邮箱错误提示

    private InputValidator validator;  // 验证器

    private Integer personId = null;
    private boolean isNewTeacher = false;
    private ArrayList<Map> teacherList = new ArrayList<>();
    private List<OptionItem> genderList;
    private final ObservableList<Map> observableList = FXCollections.observableArrayList();

    /**
     * 初始化验证器，设置实时验证
     */
    private void initValidator() {
        validator = new InputValidator();

        // 设置错误标签样式
        if (numErrorLabel != null) {
            InputValidator.setupErrorLabel(numErrorLabel);
        }
        if (passwordErrorLabel != null) {
            InputValidator.setupErrorLabel(passwordErrorLabel);
        }
        if (phoneErrorLabel != null) {
            InputValidator.setupErrorLabel(phoneErrorLabel);
        }
        if (nameErrorLabel != null) {
            InputValidator.setupErrorLabel(nameErrorLabel);
        }
        if (emailErrorLabel != null) {
            InputValidator.setupErrorLabel(emailErrorLabel);
        }

        // 添加实时验证监听器
        if (numField != null && numErrorLabel != null) {
            validator.addRealTimeValidator(numField, numErrorLabel,
                InputValidator::isNotEmpty, "工号不能为空");
        }

        if (passwordField != null && passwordErrorLabel != null) {
            validator.addRealTimeValidator(passwordField, passwordErrorLabel,
                value -> !isNewTeacher || InputValidator.isNotEmpty(value),
                InputValidator.MSG_PASSWORD_EMPTY);
        }

        if (phoneField != null && phoneErrorLabel != null) {
            validator.addPhoneValidator(phoneField, phoneErrorLabel);
        }

        if (nameField != null && nameErrorLabel != null) {
            validator.addRequiredValidator(nameField, nameErrorLabel, "姓名");
        }

        if (emailField != null && emailErrorLabel != null) {
            validator.addEmailValidator(emailField, emailErrorLabel);
        }
    }

    /**
     * 执行表单验证
     * @return 是否验证通过
     */
    private boolean validateForm() {
        boolean allValid = true;

        // 验证工号（必填）
        if (numField != null && numErrorLabel != null) {
            if (!validator.validateField(numField, numErrorLabel,
                    InputValidator::isNotEmpty, "工号不能为空")) {
                allValid = false;
            }
        }

        // 验证密码（新增时必填）
        if (isNewTeacher && passwordField != null && passwordErrorLabel != null) {
            if (!validator.validateField(passwordField, passwordErrorLabel,
                    InputValidator::isNotEmpty, InputValidator.MSG_PASSWORD_EMPTY)) {
                allValid = false;
            }
        }

        // 验证手机号
        if (phoneField != null && phoneErrorLabel != null) {
            String phone = phoneField.getText();
            if (phone != null && !phone.isEmpty() && !InputValidator.isValidPhoneNumber(phone)) {
                InputValidator.showError(phoneErrorLabel, InputValidator.MSG_PHONE_INVALID);
                InputValidator.addErrorStyle(phoneField);
                allValid = false;
            } else {
                InputValidator.hideError(phoneErrorLabel);
                InputValidator.removeErrorStyle(phoneField);
            }
        }

        // 验证姓名（必填）
        if (nameField != null && nameErrorLabel != null) {
            if (!validator.validateField(nameField, nameErrorLabel,
                    InputValidator::isNotEmpty, InputValidator.MSG_NAME_EMPTY)) {
                allValid = false;
            }
        }

        // 验证邮箱
        if (emailField != null && emailErrorLabel != null) {
            String email = emailField.getText();
            if (email != null && !email.isEmpty() && !InputValidator.isValidEmail(email)) {
                InputValidator.showError(emailErrorLabel, InputValidator.MSG_EMAIL_INVALID);
                InputValidator.addErrorStyle(emailField);
                allValid = false;
            } else {
                InputValidator.hideError(emailErrorLabel);
                InputValidator.removeErrorStyle(emailField);
            }
        }

        return allValid;
    }

    @FXML
    public void initialize() {
        // 初始化验证器
        initValidator();
        photoImageView = new ImageView();
        photoImageView.setFitHeight(100);
        photoImageView.setFitWidth(100);
        photoButton.setGraphic(photoImageView);
        photoButton.setText("上传照片");

        DataRequest req = new DataRequest();
        req.add("numName", "");
        DataResponse res = HttpRequestUtil.request("/api/teacher/getTeacherList", req);
        if (res != null && res.getCode() == 0) {
            teacherList = (ArrayList<Map>) res.getData();
        }
        numColumn.setCellValueFactory(new MapValueFactory<>("num"));
        nameColumn.setCellValueFactory(new MapValueFactory<>("name"));
        deptColumn.setCellValueFactory(new MapValueFactory<>("dept"));
        titleColumn.setCellValueFactory(new MapValueFactory<>("title"));
        degreeColumn.setCellValueFactory(new MapValueFactory<>("degree"));
        phoneColumn.setCellValueFactory(new MapValueFactory<>("phone"));
        emailColumn.setCellValueFactory(new MapValueFactory<>("email"));
        passwordColumn.setCellValueFactory(new MapValueFactory<>("passwordHint"));
        dataTableView.getSelectionModel().getSelectedIndices().addListener(this::onTableRowSelect);
        genderList = HttpRequestUtil.getDictionaryOptionItemList("XBM");
        genderComboBox.getItems().addAll(genderList);
        birthdayPick.setConverter(new LocalDateStringConverter("yyyy-MM-dd"));
        setTableViewData();
    }

    private void setTableViewData() {
        observableList.clear();
        observableList.addAll(teacherList);
        dataTableView.setItems(observableList);
    }

    public void clearPanel() {
        personId = null;
        isNewTeacher = true;
        numField.setText("");
        passwordField.setText("");
        nameField.setText("");
        deptField.setText("");
        cardField.setText("");
        genderComboBox.getSelectionModel().select(-1);
        birthdayPick.getEditor().setText("");
        emailField.setText("");
        phoneField.setText("");
        addressField.setText("");
        titleField.setText("");
        degreeField.setText("");
        photoImageView.setImage(null);
        // 清除验证状态
        if (validator != null) {
            validator.clearAllValidation();
        }
    }

    private void changeTeacherInfo() {
        Map<String, Object> form = dataTableView.getSelectionModel().getSelectedItem();
        if (form == null) {
            clearPanel();
            return;
        }
        isNewTeacher = false;
        personId = CommonMethod.getInteger(form, "personId");
        DataRequest req = new DataRequest();
        req.add("personId", personId);
        DataResponse res = HttpRequestUtil.request("/api/teacher/getTeacherInfo", req);
        if (res == null || res.getCode() != 0) {
            MessageDialog.showDialog(res == null ? "教师信息获取失败" : res.getMsg());
            return;
        }
        form = (Map<String, Object>) res.getData();
        numField.setText(CommonMethod.getString(form, "num"));
        nameField.setText(CommonMethod.getString(form, "name"));
        deptField.setText(CommonMethod.getString(form, "dept"));
        cardField.setText(CommonMethod.getString(form, "card"));
        genderComboBox.getSelectionModel().select(CommonMethod.getOptionItemIndexByValue(genderList, CommonMethod.getString(form, "gender")));
        birthdayPick.getEditor().setText(CommonMethod.getString(form, "birthday"));
        emailField.setText(CommonMethod.getString(form, "email"));
        phoneField.setText(CommonMethod.getString(form, "phone"));
        addressField.setText(CommonMethod.getString(form, "address"));
        titleField.setText(CommonMethod.getString(form, "title"));
        degreeField.setText(CommonMethod.getString(form, "degree"));
        displayPhoto();
    }

    public void onTableRowSelect(ListChangeListener.Change<? extends Integer> change) {
        changeTeacherInfo();
    }

    @FXML
    protected void onQueryButtonClick() {
        DataRequest req = new DataRequest();
        req.add("numName", numNameTextField.getText());
        DataResponse res = HttpRequestUtil.request("/api/teacher/getTeacherList", req);
        if (res != null && res.getCode() == 0) {
            teacherList = (ArrayList<Map>) res.getData();
            setTableViewData();
        }
    }

    @FXML
    protected void onAddButtonClick() {
        clearPanel();
    }

    @FXML
    protected void onDeleteButtonClick() {
        Map form = dataTableView.getSelectionModel().getSelectedItem();
        if (form == null) {
            MessageDialog.showDialog("没有选择，不能删除");
            return;
        }
        int ret = MessageDialog.choiceDialog("确认要删除吗?");
        if (ret != MessageDialog.CHOICE_YES) {
            return;
        }
        DataRequest req = new DataRequest();
        req.add("personId", CommonMethod.getInteger(form, "personId"));
        DataResponse res = HttpRequestUtil.request("/api/teacher/teacherDelete", req);
        if (res != null && res.getCode() == 0) {
            MessageDialog.showDialog("删除成功！");
            onQueryButtonClick();
        } else if (res != null) {
            MessageDialog.showDialog(res.getMsg());
        }
    }

    @FXML
    protected void onSaveButtonClick() {
        // 执行表单验证
        if (!validateForm()) {
            MessageDialog.showDialog("请检查输入信息是否正确");
            return;
        }

        Map<String, Object> form = new HashMap<>();
        form.put("num", numField.getText());
        form.put("name", nameField.getText());
        form.put("dept", deptField.getText());
        form.put("card", cardField.getText());
        if (genderComboBox.getSelectionModel().getSelectedItem() != null) {
            form.put("gender", genderComboBox.getSelectionModel().getSelectedItem().getValue());
        }
        form.put("birthday", birthdayPick.getEditor().getText());
        form.put("email", emailField.getText());
        form.put("phone", phoneField.getText());
        form.put("address", addressField.getText());
        form.put("title", titleField.getText());
        form.put("degree", degreeField.getText());
        // 新增教师时包含密码
        if (isNewTeacher && passwordField.getText() != null && !passwordField.getText().isEmpty()) {
            form.put("password", passwordField.getText());
        }
        DataRequest req = new DataRequest();
        req.add("personId", personId);
        req.add("form", form);
        DataResponse res = HttpRequestUtil.request("/api/teacher/teacherEditSave", req);
        if (res != null && res.getCode() == 0) {
            personId = CommonMethod.getIntegerFromObject(res.getData());
            isNewTeacher = false;
            passwordField.setText(""); // 保存后清空密码字段
            MessageDialog.showDialog("提交成功！");
            onQueryButtonClick();
        } else if (res != null) {
            MessageDialog.showDialog(res.getMsg());
        }
    }

    public void doNew() {
        clearPanel();
    }

    public void doSave() {
        onSaveButtonClick();
    }

    public void doDelete() {
        onDeleteButtonClick();
    }

    @FXML
    protected void onResetPwdButtonClick() {
        String num = numField.getText();
        if (num == null || num.isEmpty()) {
            MessageDialog.showDialog("请先选择或输入教师工号");
            return;
        }
        int ret = MessageDialog.choiceDialog("确认要将教师 " + num + " 的密码重置为默认密码 123456 吗？");
        if (ret != MessageDialog.CHOICE_YES) {
            return;
        }
        DataRequest req = new DataRequest();
        req.add("username", num);
        req.add("password", "123456");
        DataResponse res = HttpRequestUtil.request("/api/user/resetPassword", req);
        if (res != null && res.getCode() == 0) {
            MessageDialog.showDialog("密码重置成功！");
        } else if (res != null) {
            MessageDialog.showDialog(res.getMsg());
        } else {
            MessageDialog.showDialog("密码重置失败");
        }
    }

    @FXML
    protected void onShowPasswordCheckBoxAction() {
        // 密码可见性切换 - 当选中"显示"时，输入框内容保持可见
        // TextField本身支持直接输入，无需额外处理
        if (showPasswordCheckBox.isSelected()) {
            passwordField.setAccessibleText(passwordField.getText());
        } else {
            // 取消选中时不进行额外处理，用户输入时自动显示
        }
    }

    public void displayPhoto() {
        if (personId == null) {
            photoImageView.setImage(null);
            return;
        }
        DataRequest req = new DataRequest();
        req.add("personId", personId + "");
        byte[] bytes = HttpRequestUtil.requestByteData("/api/base/getBlobByteData", req);
        if (bytes != null) {
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            Image img = new Image(in);
            photoImageView.setImage(img);
        } else {
            photoImageView.setImage(null);
        }
    }

    @FXML
    public void onPhotoButtonClick() {
        if (personId == null) {
            MessageDialog.showDialog("请先保存教师信息后再上传照片");
            return;
        }
        FileChooser fileDialog = new FileChooser();
        fileDialog.setTitle("图片上传");
        fileDialog.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("图片文件", "*.jpg", "*.jpeg", "*.png", "*.bmp"));
        File file = fileDialog.showOpenDialog(null);
        if (file == null) {
            return;
        }
        DataResponse res = HttpRequestUtil.uploadFile("/api/base/uploadPhotoBlob", file.getPath(), personId + "");
        if (res.getCode() == 0) {
            MessageDialog.showDialog("上传成功！");
            displayPhoto();
        } else {
            MessageDialog.showDialog(res.getMsg());
        }
    }
}
