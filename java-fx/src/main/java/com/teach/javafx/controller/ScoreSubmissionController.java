package com.teach.javafx.controller;

import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.*;
import com.teach.javafx.util.CommonMethod;
import com.teach.javafx.controller.base.MessageDialog;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScoreSubmissionController extends ToolController {
    private boolean isSuccess(DataResponse res) {
        return res != null && (Integer.valueOf(0).equals(res.getCode()) || Integer.valueOf(200).equals(res.getCode()));
    }
    @FXML
    private TableView<Map> dataTableView;
    @FXML
    private TableColumn<Map, String> submissionIdColumn;
    @FXML
    private TableColumn<Map, String> teacherNameColumn;
    @FXML
    private TableColumn<Map, String> studentNumColumn;
    @FXML
    private TableColumn<Map, String> studentNameColumn;
    @FXML
    private TableColumn<Map, String> courseNameColumn;
    @FXML
    private TableColumn<Map, String> submittedScoreColumn;
    @FXML
    private TableColumn<Map, String> stateColumn;
    @FXML
    private TableColumn<Map, String> submissionTimeColumn;

    @FXML
    private TextField teacherNameField;
    @FXML
    private TextField studentNumField;
    @FXML
    private TextField studentNameField;
    @FXML
    private TextField courseNameField;
    @FXML
    private TextField submittedScoreField;
    @FXML
    private ComboBox<OptionItem> stateComboBox;
    @FXML
    private TextArea approvalCommentArea;
    @FXML
    private TextField submissionTimeField;
    @FXML
    private TextField approvalTimeField;

    @FXML
    private TextField queryTeacherNameField;
    @FXML
    private TextField queryStudentNumField;
    @FXML
    private ComboBox<OptionItem> queryStateComboBox;

    private Integer submissionId = null;
    private ArrayList<Map> submissionList = new ArrayList();
    private List<OptionItem> stateList;
    private ObservableList<Map> observableList = FXCollections.observableArrayList();

    private void setTableViewData() {
        observableList.clear();
        observableList.addAll(submissionList);
        dataTableView.setItems(observableList);
    }

    @FXML
    public void initialize() {
        DataRequest req = new DataRequest();
        DataResponse res = HttpRequestUtil.request("/api/scoreSubmission/all", req);
        if (isSuccess(res)) {
            submissionList = (ArrayList<Map>) res.getData();
        }

        submissionIdColumn.setCellValueFactory(new MapValueFactory<>("submissionId"));
        teacherNameColumn.setCellValueFactory(new MapValueFactory<>("teacherName"));
        studentNumColumn.setCellValueFactory(new MapValueFactory<>("studentNum"));
        studentNameColumn.setCellValueFactory(new MapValueFactory<>("studentName"));
        courseNameColumn.setCellValueFactory(new MapValueFactory<>("courseName"));
        submittedScoreColumn.setCellValueFactory(new MapValueFactory<>("submittedScore"));
        stateColumn.setCellValueFactory(new MapValueFactory<>("stateName"));
        submissionTimeColumn.setCellValueFactory(new MapValueFactory<>("submissionTime"));

        TableView.TableViewSelectionModel<Map> tsm = dataTableView.getSelectionModel();
        ObservableList<Integer> selectedIndices = tsm.getSelectedIndices();
        selectedIndices.addListener((ListChangeListener<Integer>) change -> onTableRowSelect());

        setTableViewData();

        stateList = new ArrayList<>();
        stateList.add(new OptionItem(1, "1", "待审批"));
        stateList.add(new OptionItem(2, "2", "审批中"));
        stateList.add(new OptionItem(3, "3", "已通过"));
        stateList.add(new OptionItem(4, "4", "已拒绝"));
        stateList.add(new OptionItem(5, "5", "已发布"));
        stateComboBox.getItems().addAll(stateList);
        queryStateComboBox.getItems().addAll(stateList);
    }

    public void clearPanel() {
        submissionId = null;
        teacherNameField.setText("");
        studentNumField.setText("");
        studentNameField.setText("");
        courseNameField.setText("");
        submittedScoreField.setText("");
        stateComboBox.getSelectionModel().select(-1);
        approvalCommentArea.setText("");
        submissionTimeField.setText("");
        approvalTimeField.setText("");
    }

    protected void changeSubmissionInfo() {
        Map form = dataTableView.getSelectionModel().getSelectedItem();
        if (form == null) {
            clearPanel();
            return;
        }
        submissionId = CommonMethod.getInteger(form, "submissionId");
        DataRequest req = new DataRequest();
        req.add("submissionId", submissionId);
        DataResponse res = HttpRequestUtil.request("/api/scoreSubmission/" + submissionId, req);
        if (!isSuccess(res)) {
            MessageDialog.showDialog(res.getMsg());
            return;
        }
        form = (Map) res.getData();
        teacherNameField.setText(CommonMethod.getString(form, "teacherName"));
        studentNumField.setText(CommonMethod.getString(form, "studentNum"));
        studentNameField.setText(CommonMethod.getString(form, "studentName"));
        courseNameField.setText(CommonMethod.getString(form, "courseName"));
        submittedScoreField.setText(CommonMethod.getString(form, "submittedScore"));
        stateComboBox.getSelectionModel().select(CommonMethod.getOptionItemIndexByValue(stateList, CommonMethod.getString(form, "state")));
        approvalCommentArea.setText(CommonMethod.getString(form, "approvalComment"));
        submissionTimeField.setText(CommonMethod.getString(form, "submissionTime"));
        approvalTimeField.setText(CommonMethod.getString(form, "approvalTime"));
    }

    private void onTableRowSelect() {
        changeSubmissionInfo();
    }

    @FXML
    protected void onQueryButtonClick() {
        String teacherName = queryTeacherNameField.getText();
        String studentNum = queryStudentNumField.getText();
        OptionItem stateItem = queryStateComboBox.getSelectionModel().getSelectedItem();

        DataRequest req = new DataRequest();
        req.add("teacherName", teacherName);
        req.add("studentNum", studentNum);
        if (stateItem != null) {
            req.add("state", stateItem.getValue());
        }

        DataResponse res = HttpRequestUtil.request("/api/scoreSubmission/all", req);
        if (isSuccess(res)) {
            submissionList = (ArrayList<Map>) res.getData();
            setTableViewData();
        }
    }

    @FXML
    protected void onPendingButtonClick() {
        DataRequest req = new DataRequest();
        DataResponse res = HttpRequestUtil.request("/api/scoreSubmission/pending/all", req);
        if (isSuccess(res)) {
            submissionList = (ArrayList<Map>) res.getData();
            setTableViewData();
        }
    }

    @FXML
    protected void onApproveButtonClick() {
        Map form = dataTableView.getSelectionModel().getSelectedItem();
        if (form == null) {
            MessageDialog.showDialog("请选择要审批的成绩");
            return;
        }

        OptionItem stateItem = stateComboBox.getSelectionModel().getSelectedItem();
        if (stateItem == null) {
            MessageDialog.showDialog("请选择审批结果");
            return;
        }

        String comment = approvalCommentArea.getText();
        submissionId = CommonMethod.getInteger(form, "submissionId");

        DataRequest req = new DataRequest();
        req.add("submissionId", submissionId);
        req.add("state", stateItem.getValue());
        req.add("comment", comment);
        DataResponse res = HttpRequestUtil.request("/api/scoreSubmission/approve", req);
        if (isSuccess(res)) {
            MessageDialog.showDialog("审批成功！");
            onQueryButtonClick();
        } else {
            MessageDialog.showDialog(res.getMsg());
        }
    }

    @FXML
    protected void onPublishButtonClick() {
        Map form = dataTableView.getSelectionModel().getSelectedItem();
        if (form == null) {
            MessageDialog.showDialog("请选择要发布的成绩");
            return;
        }

        int ret = MessageDialog.choiceDialog("确认要发布此成绩吗?");
        if (ret != MessageDialog.CHOICE_YES) {
            return;
        }

        submissionId = CommonMethod.getInteger(form, "submissionId");
        DataRequest req = new DataRequest();
        req.add("submissionId", submissionId);
        req.add("state", "5");
        DataResponse res = HttpRequestUtil.request("/api/scoreSubmission/approve", req);
        if (isSuccess(res)) {
            MessageDialog.showDialog("发布成功！");
            onQueryButtonClick();
        } else {
            MessageDialog.showDialog(res.getMsg());
        }
    }

    public void doNew() {
        clearPanel();
    }

    public void doSave() {
        onApproveButtonClick();
    }

    public void doDelete() {
        MessageDialog.showDialog("不能直接删除成绩提交记录");
    }

    public void doExport() {
        DataRequest req = new DataRequest();
        byte[] bytes = HttpRequestUtil.requestByteData("/api/scoreSubmission/export", req);
        if (bytes == null || bytes.length == 0) {
            MessageDialog.showDialog("没有数据可导出");
            return;
        }
        try {
            FileChooser fileDialog = new FileChooser();
            fileDialog.setTitle("选择保存位置");
            fileDialog.setInitialFileName("scoreSubmission.xlsx");
            fileDialog.getExtensionFilters().add(new FileChooser.ExtensionFilter("XLSX文件", "*.xlsx"));
            File file = fileDialog.showSaveDialog(null);
            if (file != null) {
                FileOutputStream out = new FileOutputStream(file);
                out.write(bytes);
                out.close();
                MessageDialog.showDialog("导出成功");
            }
        } catch (Exception e) {
            MessageDialog.showDialog("导出失败: " + e.getMessage());
        }
    }
}