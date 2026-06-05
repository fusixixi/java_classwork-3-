package com.teach.javafx.controller;

import com.teach.javafx.controller.base.LocalDateStringConverter;
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

public class InnovationAchievementController extends ToolController {
    private boolean isSuccess(DataResponse res) {
        return res != null && (Integer.valueOf(0).equals(res.getCode()) || Integer.valueOf(200).equals(res.getCode()));
    }
    @FXML
    private TableView<Map> dataTableView;
    @FXML
    private TableColumn<Map, String> achievementIdColumn;
    @FXML
    private TableColumn<Map, String> studentNumColumn;
    @FXML
    private TableColumn<Map, String> studentNameColumn;
    @FXML
    private TableColumn<Map, String> titleColumn;
    @FXML
    private TableColumn<Map, String> categoryColumn;
    @FXML
    private TableColumn<Map, String> achievementDateColumn;
    @FXML
    private TableColumn<Map, String> submitTimeColumn;
    @FXML
    private TableColumn<Map, String> stateColumn;

    @FXML
    private TextField studentNumField;
    @FXML
    private TextField studentNameField;
    @FXML
    private TextField titleField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private ComboBox<OptionItem> categoryComboBox;
    @FXML
    private DatePicker achievementDatePick;
    @FXML
    private TextField attachmentField;
    @FXML
    private ComboBox<OptionItem> stateComboBox;
    @FXML
    private TextArea approvalCommentArea;

    @FXML
    private TextField queryStudentNumField;
    @FXML
    private TextField queryTitleField;

    private Integer achievementId = null;
    private ArrayList<Map> achievementList = new ArrayList();
    private List<OptionItem> categoryList;
    private List<OptionItem> stateList;
    private ObservableList<Map> observableList = FXCollections.observableArrayList();

    private void setTableViewData() {
        observableList.clear();
        observableList.addAll(achievementList);
        dataTableView.setItems(observableList);
    }

    @FXML
    public void initialize() {
        DataRequest req = new DataRequest();
        DataResponse res = HttpRequestUtil.request("/api/innovationAchievement/all", req);
        if (isSuccess(res)) {
            achievementList = (ArrayList<Map>) res.getData();
        }

        achievementIdColumn.setCellValueFactory(new MapValueFactory<>("achievementId"));
        studentNumColumn.setCellValueFactory(new MapValueFactory<>("studentNum"));
        studentNameColumn.setCellValueFactory(new MapValueFactory<>("studentName"));
        titleColumn.setCellValueFactory(new MapValueFactory<>("title"));
        categoryColumn.setCellValueFactory(new MapValueFactory<>("category"));
        achievementDateColumn.setCellValueFactory(new MapValueFactory<>("achievementDate"));
        submitTimeColumn.setCellValueFactory(new MapValueFactory<>("submitTime"));
        stateColumn.setCellValueFactory(new MapValueFactory<>("stateName"));

        TableView.TableViewSelectionModel<Map> tsm = dataTableView.getSelectionModel();
        ObservableList<Integer> selectedIndices = tsm.getSelectedIndices();
        selectedIndices.addListener((ListChangeListener<Integer>) change -> onTableRowSelect());

        setTableViewData();

        categoryList = new ArrayList<>();
        categoryList.add(new OptionItem(1, "项目", "项目"));
        categoryList.add(new OptionItem(2, "竞赛", "竞赛"));
        categoryList.add(new OptionItem(3, "发明", "发明"));
        categoryList.add(new OptionItem(4, "论文", "论文"));
        categoryComboBox.getItems().addAll(categoryList);

        stateList = new ArrayList<>();
        stateList.add(new OptionItem(1, "1", "待审批"));
        stateList.add(new OptionItem(2, "2", "审批中"));
        stateList.add(new OptionItem(3, "3", "已通过"));
        stateList.add(new OptionItem(4, "4", "已拒绝"));
        stateComboBox.getItems().addAll(stateList);

        achievementDatePick.setConverter(new LocalDateStringConverter("yyyy-MM-dd"));
    }

    public void clearPanel() {
        achievementId = null;
        studentNumField.setText("");
        studentNameField.setText("");
        titleField.setText("");
        descriptionArea.setText("");
        categoryComboBox.getSelectionModel().select(-1);
        achievementDatePick.getEditor().setText("");
        attachmentField.setText("");
        stateComboBox.getSelectionModel().select(-1);
        approvalCommentArea.setText("");
    }

    protected void changeAchievementInfo() {
        Map form = dataTableView.getSelectionModel().getSelectedItem();
        if (form == null) {
            clearPanel();
            return;
        }
        achievementId = CommonMethod.getInteger(form, "achievementId");
        DataRequest req = new DataRequest();
        req.add("achievementId", achievementId);
        DataResponse res = HttpRequestUtil.request("/api/innovationAchievement/" + achievementId, req);
        if (!isSuccess(res)) {
            MessageDialog.showDialog(res.getMsg());
            return;
        }
        form = (Map) res.getData();
        studentNumField.setText(CommonMethod.getString(form, "studentNum"));
        studentNameField.setText(CommonMethod.getString(form, "studentName"));
        titleField.setText(CommonMethod.getString(form, "title"));
        descriptionArea.setText(CommonMethod.getString(form, "description"));
        categoryComboBox.getSelectionModel().select(CommonMethod.getOptionItemIndexByValue(categoryList, CommonMethod.getString(form, "category")));
        achievementDatePick.getEditor().setText(CommonMethod.getString(form, "achievementDate"));
        attachmentField.setText(CommonMethod.getString(form, "attachmentPath"));
        stateComboBox.getSelectionModel().select(CommonMethod.getOptionItemIndexByValue(stateList, CommonMethod.getString(form, "state")));
        approvalCommentArea.setText(CommonMethod.getString(form, "approvalComment"));
    }

    private void onTableRowSelect() {
        changeAchievementInfo();
    }

    @FXML
    protected void onQueryButtonClick() {
        String studentNum = queryStudentNumField.getText();
        String title = queryTitleField.getText();
        DataRequest req = new DataRequest();
        req.add("studentNum", studentNum);
        req.add("title", title);
        DataResponse res = HttpRequestUtil.request("/api/innovationAchievement/all", req);
        if (isSuccess(res)) {
            achievementList = (ArrayList<Map>) res.getData();
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
        int ret = MessageDialog.choiceDialog("确认要删除此成果吗?");
        if (ret != MessageDialog.CHOICE_YES) {
            return;
        }
        achievementId = CommonMethod.getInteger(form, "achievementId");
        DataRequest req = new DataRequest();
        req.add("achievementId", achievementId);
        DataResponse res = HttpRequestUtil.request("/api/innovationAchievement/" + achievementId, req);
        if (isSuccess(res)) {
            MessageDialog.showDialog("删除成功！");
            onQueryButtonClick();
        } else if (res != null) {
            MessageDialog.showDialog(res.getMsg());
        }
    }

    @FXML
    protected void onSaveButtonClick() {
        String studentNum = studentNumField.getText();
        String title = titleField.getText();
        String description = descriptionArea.getText();
        String achievementDate = achievementDatePick.getEditor().getText();

        if (studentNum.isEmpty() || title.isEmpty() || achievementDate.isEmpty()) {
            MessageDialog.showDialog("学生学号、成果名称和完成日期不能为空");
            return;
        }

        OptionItem categoryItem = categoryComboBox.getSelectionModel().getSelectedItem();
        if (categoryItem == null) {
            MessageDialog.showDialog("请选择成果类型");
            return;
        }

        DataRequest req = new DataRequest();
        req.add("studentNum", studentNum);
        req.add("title", title);
        req.add("description", description);
        req.add("category", categoryItem.getValue());
        req.add("achievementDate", achievementDate);
        req.add("attachmentPath", attachmentField.getText());
        DataResponse res = HttpRequestUtil.request("/api/innovationAchievement/submit", req);
        if (isSuccess(res)) {
            MessageDialog.showDialog("提交成功！");
            onQueryButtonClick();
        } else {
            MessageDialog.showDialog(res.getMsg());
        }
    }

    @FXML
    protected void onUploadButtonClick() {
        FileChooser fileDialog = new FileChooser();
        fileDialog.setTitle("选择附件");
        fileDialog.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("所有文件", "*.*"),
                new FileChooser.ExtensionFilter("PDF文件", "*.pdf"),
                new FileChooser.ExtensionFilter("Word文件", "*.docx"));
        File file = fileDialog.showOpenDialog(null);
        if (file != null) {
            attachmentField.setText(file.getPath());
        }
    }

    @FXML
    protected void onApproveButtonClick() {
        Map form = dataTableView.getSelectionModel().getSelectedItem();
        if (form == null) {
            MessageDialog.showDialog("请选择要审批的成果");
            return;
        }

        OptionItem stateItem = stateComboBox.getSelectionModel().getSelectedItem();
        if (stateItem == null) {
            MessageDialog.showDialog("请选择审批结果");
            return;
        }

        String comment = approvalCommentArea.getText();
        achievementId = CommonMethod.getInteger(form, "achievementId");

        DataRequest req = new DataRequest();
        req.add("achievementId", achievementId);
        req.add("state", stateItem.getValue());
        req.add("comment", comment);
        DataResponse res = HttpRequestUtil.request("/api/innovationAchievement/approve", req);
        if (isSuccess(res)) {
            MessageDialog.showDialog("审批成功！");
            onQueryButtonClick();
        } else {
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

    public void doExport() {
        String studentNum = queryStudentNumField.getText();
        DataRequest req = new DataRequest();
        req.add("studentNum", studentNum);
        byte[] bytes = HttpRequestUtil.requestByteData("/api/innovationAchievement/export", req);
        if (bytes == null || bytes.length == 0) {
            MessageDialog.showDialog("没有数据可导出");
            return;
        }
        try {
            FileChooser fileDialog = new FileChooser();
            fileDialog.setTitle("选择保存位置");
            fileDialog.setInitialFileName("innovationAchievement.xlsx");
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