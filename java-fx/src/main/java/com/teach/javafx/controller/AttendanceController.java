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

public class AttendanceController extends ToolController {
    private boolean isSuccess(DataResponse res) {
        return res != null && (Integer.valueOf(0).equals(res.getCode()) || Integer.valueOf(200).equals(res.getCode()));
    }
    @FXML
    private TableView<Map> dataTableView;
    @FXML
    private TableColumn<Map, String> attendanceIdColumn;
    @FXML
    private TableColumn<Map, String> studentNumColumn;
    @FXML
    private TableColumn<Map, String> studentNameColumn;
    @FXML
    private TableColumn<Map, String> courseNameColumn;
    @FXML
    private TableColumn<Map, String> attendanceDateColumn;
    @FXML
    private TableColumn<Map, String> statusColumn;
    @FXML
    private TableColumn<Map, String> remarkColumn;
    @FXML
    private TableColumn<Map, String> recordTimeColumn;

    @FXML
    private TextField studentNumField;
    @FXML
    private TextField studentNameField;
    @FXML
    private TextField courseNameField;
    @FXML
    private DatePicker attendanceDatePick;
    @FXML
    private ComboBox<OptionItem> statusComboBox;
    @FXML
    private TextField remarkField;
    @FXML
    private TextField attendanceRateField;

    @FXML
    private TextField queryStudentNumField;
    @FXML
    private TextField queryStudentNameField;
    @FXML
    private TextField queryCourseNameField;

    private Integer attendanceId = null;
    private ArrayList<Map> attendanceList = new ArrayList();
    private List<OptionItem> statusList;
    private ObservableList<Map> observableList = FXCollections.observableArrayList();

    private void setTableViewData() {
        observableList.clear();
        observableList.addAll(attendanceList);
        dataTableView.setItems(observableList);
    }

    @FXML
    public void initialize() {
        DataRequest req = new DataRequest();
        DataResponse res = HttpRequestUtil.request("/api/attendance/all", req);
        if (isSuccess(res)) {
            attendanceList = (ArrayList<Map>) res.getData();
        }

        attendanceIdColumn.setCellValueFactory(new MapValueFactory<>("attendanceId"));
        studentNumColumn.setCellValueFactory(new MapValueFactory<>("studentNum"));
        studentNameColumn.setCellValueFactory(new MapValueFactory<>("studentName"));
        courseNameColumn.setCellValueFactory(new MapValueFactory<>("courseName"));
        attendanceDateColumn.setCellValueFactory(new MapValueFactory<>("attendanceDate"));
        statusColumn.setCellValueFactory(new MapValueFactory<>("statusName"));
        remarkColumn.setCellValueFactory(new MapValueFactory<>("remark"));
        recordTimeColumn.setCellValueFactory(new MapValueFactory<>("recordTime"));

        TableView.TableViewSelectionModel<Map> tsm = dataTableView.getSelectionModel();
        ObservableList<Integer> selectedIndices = tsm.getSelectedIndices();
        selectedIndices.addListener((ListChangeListener<Integer>) change -> onTableRowSelect());

        setTableViewData();

        statusList = new ArrayList<>();
        statusList.add(new OptionItem(1, "present", "出勤"));
        statusList.add(new OptionItem(2, "absent", "缺勤"));
        statusList.add(new OptionItem(3, "late", "迟到"));
        statusList.add(new OptionItem(4, "leave", "请假"));
        statusComboBox.getItems().addAll(statusList);

        attendanceDatePick.setConverter(new LocalDateStringConverter("yyyy-MM-dd"));
    }

    public void clearPanel() {
        attendanceId = null;
        studentNumField.setText("");
        studentNameField.setText("");
        courseNameField.setText("");
        attendanceDatePick.getEditor().setText("");
        statusComboBox.getSelectionModel().select(-1);
        remarkField.setText("");
        attendanceRateField.setText("");
    }

    protected void changeAttendanceInfo() {
        Map form = dataTableView.getSelectionModel().getSelectedItem();
        if (form == null) {
            clearPanel();
            return;
        }
        attendanceId = CommonMethod.getInteger(form, "attendanceId");
        DataRequest req = new DataRequest();
        req.add("attendanceId", attendanceId);
        DataResponse res = HttpRequestUtil.request("/api/attendance/" + attendanceId, req);
        if (!isSuccess(res)) {
            MessageDialog.showDialog(res.getMsg());
            return;
        }
        form = (Map) res.getData();
        studentNumField.setText(CommonMethod.getString(form, "studentNum"));
        studentNameField.setText(CommonMethod.getString(form, "studentName"));
        courseNameField.setText(CommonMethod.getString(form, "courseName"));
        attendanceDatePick.getEditor().setText(CommonMethod.getString(form, "attendanceDate"));
        statusComboBox.getSelectionModel().select(CommonMethod.getOptionItemIndexByValue(statusList, CommonMethod.getString(form, "status")));
        remarkField.setText(CommonMethod.getString(form, "remark"));
    }

    private void onTableRowSelect() {
        changeAttendanceInfo();
    }

    @FXML
    protected void onQueryButtonClick() {
        String studentNum = queryStudentNumField.getText();
        String studentName = queryStudentNameField.getText();
        String courseName = queryCourseNameField.getText();
        DataRequest req = new DataRequest();
        req.add("studentNum", studentNum);
        req.add("studentName", studentName);
        req.add("courseName", courseName);
        DataResponse res = HttpRequestUtil.request("/api/attendance/all", req);
        if (isSuccess(res)) {
            attendanceList = (ArrayList<Map>) res.getData();
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
        int ret = MessageDialog.choiceDialog("确认要删除此考勤记录吗?");
        if (ret != MessageDialog.CHOICE_YES) {
            return;
        }
        attendanceId = CommonMethod.getInteger(form, "attendanceId");
        DataRequest req = new DataRequest();
        req.add("attendanceId", attendanceId);
        DataResponse res = HttpRequestUtil.request("/api/attendance/" + attendanceId, req);
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
        String courseName = courseNameField.getText();
        String dateStr = attendanceDatePick.getEditor().getText();

        if (studentNum.isEmpty() || courseName.isEmpty() || dateStr.isEmpty()) {
            MessageDialog.showDialog("学生学号、课程名称和考勤日期不能为空");
            return;
        }

        OptionItem statusItem = statusComboBox.getSelectionModel().getSelectedItem();
        if (statusItem == null) {
            MessageDialog.showDialog("请选择考勤状态");
            return;
        }

        DataRequest req = new DataRequest();
        req.add("studentNum", studentNum);
        req.add("courseName", courseName);
        req.add("attendanceDate", dateStr);
        req.add("status", statusItem.getValue());
        req.add("remark", remarkField.getText());
        DataResponse res = HttpRequestUtil.request("/api/attendance/record", req);
        if (isSuccess(res)) {
            MessageDialog.showDialog("保存成功！");
            onQueryButtonClick();
        } else {
            MessageDialog.showDialog(res.getMsg());
        }
    }

    @FXML
    protected void onCheckRateButtonClick() {
        String studentNum = studentNumField.getText();
        String courseName = courseNameField.getText();
        if (studentNum.isEmpty() || courseName.isEmpty()) {
            MessageDialog.showDialog("请选择学生和课程");
            return;
        }
        DataRequest req = new DataRequest();
        req.add("studentNum", studentNum);
        req.add("courseName", courseName);
        DataResponse res = HttpRequestUtil.request("/api/attendance/rate", req);
        if (isSuccess(res)) {
            Double rate = (Double) res.getData();
            attendanceRateField.setText(String.format("%.2f%%", rate));
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
        byte[] bytes = HttpRequestUtil.requestByteData("/api/attendance/export", req);
        if (bytes == null || bytes.length == 0) {
            MessageDialog.showDialog("没有数据可导出");
            return;
        }
        try {
            FileChooser fileDialog = new FileChooser();
            fileDialog.setTitle("选择保存位置");
            fileDialog.setInitialFileName("attendance.xlsx");
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