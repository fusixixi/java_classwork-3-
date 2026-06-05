package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.MainApplication;
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
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * StudentController 登录交互控制类 对应 student_panel.fxml  对应于学生管理的后台业务处理的控制器，主要获取数据和保存数据的方法不同
 *
 * @FXML 属性 对应fxml文件中的
 * @FXML 方法 对应于fxml文件中的 on***Click的属性
 */
public class StudentLeaveController extends ToolController {
    @FXML
    private TableView<Map> dataTableView;  //学生信息表
    @FXML
    private TableColumn<Map, String> studentNumColumn;   //学生信息表 编号列
    @FXML
    private TableColumn<Map, String> studentNameColumn; //学生信息表 名称列
    @FXML
    private TableColumn<Map, String> teacherNameColumn; //学生信息表 名称列
    @FXML
    private TableColumn<Map, String> leaveDateColumn;  //学生信息表 院系列
    @FXML
    private TableColumn<Map, String> reasonColumn; //学生信息表 专业列
    @FXML
    private TableColumn<Map, String> stateNameColumn; //学生信息表 性别列
    @FXML
    private TableColumn<Map, String> teacherCommentColumn; //学生信息表 班级列
    @FXML
    private TableColumn<Map, String> adminCommentColumn; //学生信息表 证件号码列

    @FXML
    private TextField studentNumField; //学生信息  学号输入域
    @FXML
    private TextField studentNameField;  //学生信息  名称输入域
    @FXML
    private TextField leaveDateField; //学生信息  院系输入域
    @FXML
    private TextField reasonField; //学生信息  专业输入域
    @FXML
    private TextField teacherCommentField; //学生信息  班级输入域
    @FXML
    private TextField adminCommentField; //学生信息  证件号码输入域
    @FXML
    private ComboBox<OptionItem> teacherComboBox;  //学生信息  性别输入域
    @FXML
    private ComboBox<OptionItem> stateComboBox;  //学生信息  性别输入域

    @FXML
    private TextField searchTextField;  //查询 姓名学号输入域
    @FXML
    private Label searchLabel;
    @FXML
    private Label stateLabel;

    // 验证相关字段
    @FXML
    private Label leaveDateErrorLabel;  // 请假日期错误提示
    @FXML
    private Label reasonErrorLabel;  // 请假原因错误提示
    @FXML
    private Label teacherCommentErrorLabel;  // 教师意见错误提示

    private InputValidator validator;  // 验证器

    @FXML
    private Button addButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button submitButton;
    @FXML
    private Button passButton;
    @FXML
    private Button notPassButton;

    private Integer studentLeaveId = null;  //当前编辑修改的学生的主键
    private List<OptionItem> teacherList;

    private ArrayList<Map> studentLeaveList = new ArrayList();  // 学生信息列表数据
    private List<OptionItem> stateList;   //性别选择列表数据
    private ObservableList<Map> observableList = FXCollections.observableArrayList();  // TableView渲染列表
    private String roleName;

    /**
     * 初始化验证器，设置实时验证
     */
    private void initValidator() {
        validator = new InputValidator();

        // 设置错误标签样式
        if (leaveDateErrorLabel != null) {
            InputValidator.setupErrorLabel(leaveDateErrorLabel);
        }
        if (reasonErrorLabel != null) {
            InputValidator.setupErrorLabel(reasonErrorLabel);
        }
        if (teacherCommentErrorLabel != null) {
            InputValidator.setupErrorLabel(teacherCommentErrorLabel);
        }

        // 添加实时验证监听器
        if (leaveDateField != null && leaveDateErrorLabel != null) {
            validator.addRealTimeValidator(leaveDateField, leaveDateErrorLabel,
                InputValidator::isNotEmpty, "请假日期不能为空");
        }

        if (reasonField != null && reasonErrorLabel != null) {
            validator.addRealTimeValidator(reasonField, reasonErrorLabel,
                InputValidator::isNotEmpty, "请假原因不能为空");
        }

        if (teacherCommentField != null && teacherCommentErrorLabel != null) {
            validator.addRealTimeValidator(teacherCommentField, teacherCommentErrorLabel,
                value -> true, "教师意见不能为空");  // 教师意见是可选的
        }
    }

    /**
     * 执行表单验证
     * @return 是否验证通过
     */
    private boolean validateForm() {
        boolean allValid = true;

        // 验证请假日期（必填）
        if (leaveDateField != null && leaveDateErrorLabel != null) {
            if (!validator.validateField(leaveDateField, leaveDateErrorLabel,
                    InputValidator::isNotEmpty, "请假日期不能为空")) {
                allValid = false;
            }
        }

        // 验证请假原因（必填，且长度>=5）
        if (reasonField != null && reasonErrorLabel != null) {
            String reason = reasonField.getText();
            if (reason == null || reason.trim().isEmpty()) {
                InputValidator.showError(reasonErrorLabel, "请假原因不能为空");
                InputValidator.addErrorStyle(reasonField);
                allValid = false;
            } else if (reason.trim().length() < 5) {
                InputValidator.showError(reasonErrorLabel, "请假原因至少5个字符");
                InputValidator.addErrorStyle(reasonField);
                allValid = false;
            } else {
                InputValidator.hideError(reasonErrorLabel);
                InputValidator.removeErrorStyle(reasonField);
            }
        }

        // 验证请假日期格式
        if (leaveDateField != null && leaveDateErrorLabel != null) {
            String leaveDate = leaveDateField.getText();
            if (leaveDate != null && !leaveDate.isEmpty()) {
                if (!leaveDate.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                    InputValidator.showError(leaveDateErrorLabel, "日期格式应为YYYY-MM-DD");
                    InputValidator.addErrorStyle(leaveDateField);
                    allValid = false;
                }
            }
        }

        return allValid;
    }

    /**
     * 清除验证状态
     */
    private void clearValidation() {
        if (validator != null) {
            validator.clearAllValidation();
        }
    }

    /**
     * 将学生数据集合设置到面板上显示
     */
    private void setTableViewData() {
        observableList.clear();
        for (Map map : studentLeaveList) {
            observableList.addAll(FXCollections.observableArrayList(map));
        }
        dataTableView.setItems(observableList);
    }

    /**
     * 页面加载对象创建完成初始化方法，页面中控件属性的设置，初始数据显示等初始操作都在这里完成，其他代码都事件处理方法里
     */

    @FXML
    public void initialize() {
        // 初始化验证器
        initValidator();

        DataResponse res;
        DataRequest req = new DataRequest();
        teacherList = HttpRequestUtil.requestOptionItemList("/api/studentLeave/getTeacherItemOptionList",req); //从后台获取所有学生信息列表集合
        teacherComboBox.getItems().addAll(teacherList);
        studentNumColumn.setCellValueFactory(new MapValueFactory<>("studentNum"));  //设置列值工程属性
        studentNameColumn.setCellValueFactory(new MapValueFactory<>("studentName"));
        studentNameColumn.setCellValueFactory(new MapValueFactory<>("studentName"));
        teacherNameColumn.setCellValueFactory(new MapValueFactory<>("teacherName"));
        leaveDateColumn.setCellValueFactory(new MapValueFactory<>("leaveDate"));
        reasonColumn.setCellValueFactory(new MapValueFactory<>("reason"));
        stateNameColumn.setCellValueFactory(new MapValueFactory<>("stateName"));
        teacherCommentColumn.setCellValueFactory(new MapValueFactory<>("teacherComment"));
        adminCommentColumn.setCellValueFactory(new MapValueFactory<>("adminComment"));
        TableView.TableViewSelectionModel<Map> tsm = dataTableView.getSelectionModel();
        ObservableList<Integer> list = tsm.getSelectedIndices();
        list.addListener(this::onTableRowSelect);
        setTableViewData();
        stateList = HttpRequestUtil.getDictionaryOptionItemList("SHZTM");
        stateList.addFirst(new OptionItem(-1,"-1","请选择..."));
        stateComboBox.getItems().addAll(stateList);
        String roleName = AppStore.getJwt().getRole();
        studentNumField.setDisable(true);
        studentNameField.setDisable(true);
        switch(roleName) {
            case "ROLE_STUDENT" -> {
                stateLabel.setVisible(false);
                searchLabel.setVisible(false);
                stateComboBox.setVisible(false);
                searchTextField.setVisible(false);
                addButton.setVisible(true);
                saveButton.setVisible(true);
                submitButton.setVisible(true);
                passButton.setVisible(false);
                notPassButton.setVisible(false);
                teacherComboBox.setDisable(false);
                if (adminCommentField != null) adminCommentField.setDisable(true);
                teacherCommentField.setDisable(true);
            }
            case "ROLE_TEACHER" -> {
                stateLabel.setVisible(false);
                searchLabel.setVisible(false);
                stateComboBox.setVisible(false);
                searchTextField.setVisible(false);
                addButton.setVisible(false);
                saveButton.setVisible(false);
                submitButton.setVisible(false);
                passButton.setVisible(true);
                notPassButton.setVisible(true);
                teacherComboBox.setDisable(true);
                if (adminCommentField != null) adminCommentField.setDisable(true);
                teacherCommentField.setDisable(false);
            }
            case "ROLE_ADMIN" -> {
                stateLabel.setVisible(true);
                searchLabel.setVisible(true);
                stateComboBox.setVisible(true);
                searchTextField.setVisible(true);
                addButton.setVisible(false);
                saveButton.setVisible(false);
                submitButton.setVisible(false);
                passButton.setVisible(true);
                notPassButton.setVisible(true);
                teacherComboBox.setDisable(true);
                if (adminCommentField != null) adminCommentField.setDisable(false);
                teacherCommentField.setDisable(true);
            }
        }
        onQueryButtonClick();
    }


    public void clearPanel() {
        studentLeaveId = null;
        studentNumField.setText("");
        studentNameField.setText("");
        teacherComboBox.getSelectionModel().clearSelection();
        leaveDateField.setText("");
        reasonField.setText("");
        teacherCommentField.setText("");
        if (adminCommentField != null) adminCommentField.setText("");
        clearValidation();
    }

    protected void changeStudentInfo() {
        Map<String,Object> form = dataTableView.getSelectionModel().getSelectedItem();
        if (form == null) {
            clearPanel();
            return;
        }
        studentLeaveId = CommonMethod.getInteger(form, "studentLeaveId");
        studentNumField.setText(CommonMethod.getString(form, "studentNum"));
        studentNameField.setText(CommonMethod.getString(form, "studentName"));
        teacherComboBox.getSelectionModel().select(CommonMethod.getOptionItemIndexByValue(teacherList, CommonMethod.getString(form, "teacherId")));
        leaveDateField.setText(CommonMethod.getString(form, "leaveDate"));
        reasonField.setText(CommonMethod.getString(form, "reason"));
        teacherCommentField.setText(CommonMethod.getString(form, "teacherComment"));
        if (adminCommentField != null) adminCommentField.setText(CommonMethod.getString(form, "adminComment"));
    }


    public void onTableRowSelect(ListChangeListener.Change<? extends Integer> change) {
        changeStudentInfo();
    }

    @FXML
    protected void onQueryButtonClick() {
        String search = searchTextField.getText();
        DataRequest req = new DataRequest();
        OptionItem op;
        op = stateComboBox.getSelectionModel().getSelectedItem();
        if(op != null) {
            req.add("state",Integer.parseInt(op.getValue()));
        }
        req.add("search", search);
        DataResponse res = HttpRequestUtil.request("/api/studentLeave/getStudentLeaveList", req);
        if (res != null && res.getCode() == 0) {
            studentLeaveList = (ArrayList<Map>) res.getData();
            setTableViewData();
        }
        clearPanel();
    }


    @FXML
    protected void onAddButtonClick() {
        clearPanel();
    }

    @FXML
    protected void onSaveButtonClick() {
        doSave(0);
    }
    @FXML
    protected void onSubmitButtonClick() {
        doSave(1);
    }
    @FXML
    protected void onPassButtonClick() {
        doCheck(1);
    }

    @FXML
    protected void onNotPassButtonClick() {
        doCheck(2);
    }

    protected void doSave(Integer state){
        // 执行表单验证
        if (!validateForm()) {
            MessageDialog.showDialog("请检查输入信息是否正确");
            return;
        }

        Map<String,Object> form = new HashMap<>();
        DataRequest req = new DataRequest();
        OptionItem op;
        op = teacherComboBox.getSelectionModel().getSelectedItem();
        if(op != null) {
            req.add("teacherId",Integer.parseInt(op.getValue()));
        }
        req.add("studentLeaveId",studentLeaveId);
        req.add("leaveDate", leaveDateField.getText());
        req.add("reason", reasonField.getText());
        req.add("state", state);
        DataResponse res = HttpRequestUtil.request("/api/studentLeave/studentLeaveSave", req);
        if (res.getCode() == 0) {
            MessageDialog.showDialog("保存提交成功！");
            onQueryButtonClick();
        } else {
            MessageDialog.showDialog(res.getMsg());
        }
    }
    protected void doCheck(Integer state){
        Map<String,Object> form = new HashMap<>();
        DataRequest req = new DataRequest();
        req.add("studentLeaveId",studentLeaveId);
        req.add("teacherComment", teacherCommentField.getText());
        req.add("adminComment", adminCommentField != null ? adminCommentField.getText() : "");
        req.add("state", state);
        DataResponse res = HttpRequestUtil.request("/api/studentLeave/studentLeaveCheck", req);
        if (res.getCode() == 0) {
            MessageDialog.showDialog("审核成功！");
            onQueryButtonClick();
        } else {
            MessageDialog.showDialog(res.getMsg());
        }
    }
}
