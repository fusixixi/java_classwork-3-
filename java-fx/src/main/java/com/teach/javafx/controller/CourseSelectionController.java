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

public class CourseSelectionController extends ToolController {
    private boolean isSuccess(DataResponse res) {
        return res != null && (Integer.valueOf(0).equals(res.getCode()) || Integer.valueOf(200).equals(res.getCode()));
    }
    @FXML
    private TableView<Map> dataTableView;
    @FXML
    private TableColumn<Map, String> selectionIdColumn;
    @FXML
    private TableColumn<Map, String> studentNumColumn;
    @FXML
    private TableColumn<Map, String> studentNameColumn;
    @FXML
    private TableColumn<Map, String> courseNumColumn;
    @FXML
    private TableColumn<Map, String> courseNameColumn;
    @FXML
    private TableColumn<Map, String> creditColumn;
    @FXML
    private TableColumn<Map, String> selectTimeColumn;
    @FXML
    private TableColumn<Map, String> stateColumn;

    @FXML
    private TextField studentNumField;
    @FXML
    private TextField studentNameField;
    @FXML
    private TextField courseNumField;
    @FXML
    private TextField courseNameField;
    @FXML
    private TextField creditField;
    @FXML
    private TextField selectTimeField;
    @FXML
    private ComboBox<OptionItem> stateComboBox;

    @FXML
    private TextField queryStudentNumField;
    @FXML
    private TextField queryStudentNameField;

    private Integer selectionId = null;
    private ArrayList<Map> selectionList = new ArrayList();
    private List<OptionItem> stateList;
    private ObservableList<Map> observableList = FXCollections.observableArrayList();

    private void setTableViewData() {
        observableList.clear();
        observableList.addAll(selectionList);
        dataTableView.setItems(observableList);
    }

    @FXML
    public void initialize() {
        DataRequest req = new DataRequest();
        DataResponse res = HttpRequestUtil.request("/api/courseSelection/all", req);
        if (isSuccess(res)) {
            selectionList = (ArrayList<Map>) res.getData();
        }

        selectionIdColumn.setCellValueFactory(new MapValueFactory<>("selectionId"));
        studentNumColumn.setCellValueFactory(new MapValueFactory<>("studentNum"));
        studentNameColumn.setCellValueFactory(new MapValueFactory<>("studentName"));
        courseNumColumn.setCellValueFactory(new MapValueFactory<>("courseNum"));
        courseNameColumn.setCellValueFactory(new MapValueFactory<>("courseName"));
        creditColumn.setCellValueFactory(new MapValueFactory<>("credit"));
        selectTimeColumn.setCellValueFactory(new MapValueFactory<>("selectTime"));
        stateColumn.setCellValueFactory(new MapValueFactory<>("stateName"));

        TableView.TableViewSelectionModel<Map> tsm = dataTableView.getSelectionModel();
        ObservableList<Integer> selectedIndices = tsm.getSelectedIndices();
        selectedIndices.addListener((ListChangeListener<Integer>) change -> onTableRowSelect());

        setTableViewData();

        stateList = new ArrayList<>();
        stateList.add(new OptionItem(1, "1", "已选"));
        stateList.add(new OptionItem(2, "2", "已取消"));
        stateList.add(new OptionItem(3, "3", "已完成"));
        stateComboBox.getItems().addAll(stateList);
    }

    public void clearPanel() {
        selectionId = null;
        studentNumField.setText("");
        studentNameField.setText("");
        courseNumField.setText("");
        courseNameField.setText("");
        creditField.setText("");
        selectTimeField.setText("");
        stateComboBox.getSelectionModel().select(-1);
    }

    protected void changeSelectionInfo() {
        Map form = dataTableView.getSelectionModel().getSelectedItem();
        if (form == null) {
            clearPanel();
            return;
        }
        selectionId = CommonMethod.getInteger(form, "selectionId");
        DataRequest req = new DataRequest();
        req.add("selectionId", selectionId);
        DataResponse res = HttpRequestUtil.request("/api/courseSelection/" + selectionId, req);
        if (!isSuccess(res)) {
            MessageDialog.showDialog(res.getMsg());
            return;
        }
        form = (Map) res.getData();
        studentNumField.setText(CommonMethod.getString(form, "studentNum"));
        studentNameField.setText(CommonMethod.getString(form, "studentName"));
        courseNumField.setText(CommonMethod.getString(form, "courseNum"));
        courseNameField.setText(CommonMethod.getString(form, "courseName"));
        creditField.setText(CommonMethod.getString(form, "credit"));
        selectTimeField.setText(CommonMethod.getString(form, "selectTime"));
        stateComboBox.getSelectionModel().select(CommonMethod.getOptionItemIndexByValue(stateList, CommonMethod.getString(form, "state")));
    }

    private void onTableRowSelect() {
        changeSelectionInfo();
    }

    @FXML
    protected void onQueryButtonClick() {
        String studentNum = queryStudentNumField.getText();
        String studentName = queryStudentNameField.getText();
        DataRequest req = new DataRequest();
        req.add("studentNum", studentNum);
        req.add("studentName", studentName);
        DataResponse res = HttpRequestUtil.request("/api/courseSelection/all", req);
        if (isSuccess(res)) {
            selectionList = (ArrayList<Map>) res.getData();
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
            MessageDialog.showDialog("没有选择，不能取消");
            return;
        }
        int ret = MessageDialog.choiceDialog("确认要取消选课吗?");
        if (ret != MessageDialog.CHOICE_YES) {
            return;
        }
        selectionId = CommonMethod.getInteger(form, "selectionId");
        DataRequest req = new DataRequest();
        req.add("selectionId", selectionId);
        DataResponse res = HttpRequestUtil.request("/api/courseSelection/cancel", req);
        if (isSuccess(res)) {
            MessageDialog.showDialog("取消选课成功！");
            onQueryButtonClick();
        } else if (res != null) {
            MessageDialog.showDialog(res.getMsg());
        }
    }

    @FXML
    protected void onSaveButtonClick() {
        String studentNum = studentNumField.getText();
        String courseNum = courseNumField.getText();
        if (studentNum.isEmpty() || courseNum.isEmpty()) {
            MessageDialog.showDialog("学生学号和课程编号不能为空");
            return;
        }
        DataRequest req = new DataRequest();
        req.add("studentNum", studentNum);
        req.add("courseNum", courseNum);
        DataResponse res = HttpRequestUtil.request("/api/courseSelection/select", req);
        if (isSuccess(res)) {
            MessageDialog.showDialog("选课成功！");
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
        String studentName = queryStudentNameField.getText();
        DataRequest req = new DataRequest();
        req.add("studentNum", studentNum);
        req.add("studentName", studentName);
        byte[] bytes = HttpRequestUtil.requestByteData("/api/courseSelection/export", req);
        if (bytes == null || bytes.length == 0) {
            MessageDialog.showDialog("没有数据可导出");
            return;
        }
        try {
            FileChooser fileDialog = new FileChooser();
            fileDialog.setTitle("选择保存位置");
            fileDialog.setInitialFileName("courseSelection.xlsx");
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