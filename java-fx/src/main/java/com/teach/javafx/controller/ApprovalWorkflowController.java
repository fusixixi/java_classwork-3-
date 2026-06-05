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

public class ApprovalWorkflowController extends ToolController {
    private boolean isSuccess(DataResponse res) {
        return res != null && (Integer.valueOf(0).equals(res.getCode()) || Integer.valueOf(200).equals(res.getCode()));
    }
    @FXML
    private TableView<Map> dataTableView;
    @FXML
    private TableColumn<Map, String> workflowIdColumn;
    @FXML
    private TableColumn<Map, String> workflowTypeColumn;
    @FXML
    private TableColumn<Map, String> applicantNameColumn;
    @FXML
    private TableColumn<Map, String> currentApproverColumn;
    @FXML
    private TableColumn<Map, String> approvalStepColumn;
    @FXML
    private TableColumn<Map, String> stateColumn;
    @FXML
    private TableColumn<Map, String> applyTimeColumn;

    @FXML
    private TextField workflowIdField;
    @FXML
    private ComboBox<OptionItem> workflowTypeComboBox;
    @FXML
    private TextField applicantNameField;
    @FXML
    private TextField currentApproverField;
    @FXML
    private TextField approvalStepField;
    @FXML
    private TextField totalStepsField;
    @FXML
    private ComboBox<OptionItem> stateComboBox;
    @FXML
    private TextArea approvalCommentArea;
    @FXML
    private TextField applyTimeField;
    @FXML
    private TextField approvalTimeField;

    @FXML
    private ComboBox<OptionItem> queryTypeComboBox;
    @FXML
    private ComboBox<OptionItem> queryStateComboBox;

    private Integer workflowId = null;
    private ArrayList<Map> workflowList = new ArrayList();
    private List<OptionItem> typeList;
    private List<OptionItem> stateList;
    private ObservableList<Map> observableList = FXCollections.observableArrayList();

    private void setTableViewData() {
        observableList.clear();
        observableList.addAll(workflowList);
        dataTableView.setItems(observableList);
    }

    @FXML
    public void initialize() {
        DataRequest req = new DataRequest();
        DataResponse res = HttpRequestUtil.request("/api/approvalWorkflow/all", req);
        if (isSuccess(res)) {
            workflowList = (ArrayList<Map>) res.getData();
        }

        workflowIdColumn.setCellValueFactory(new MapValueFactory<>("workflowId"));
        workflowTypeColumn.setCellValueFactory(new MapValueFactory<>("workflowType"));
        applicantNameColumn.setCellValueFactory(new MapValueFactory<>("applicantName"));
        currentApproverColumn.setCellValueFactory(new MapValueFactory<>("currentApproverName"));
        approvalStepColumn.setCellValueFactory(new MapValueFactory<>("approvalStep"));
        stateColumn.setCellValueFactory(new MapValueFactory<>("state"));
        applyTimeColumn.setCellValueFactory(new MapValueFactory<>("applyTime"));

        TableView.TableViewSelectionModel<Map> tsm = dataTableView.getSelectionModel();
        ObservableList<Integer> selectedIndices = tsm.getSelectedIndices();
        selectedIndices.addListener((ListChangeListener<Integer>) change -> onTableRowSelect());

        setTableViewData();

        typeList = new ArrayList<>();
        typeList.add(new OptionItem(1, "leave", "请假"));
        typeList.add(new OptionItem(2, "achievement", "创新成果"));
        typeList.add(new OptionItem(3, "score", "成绩审核"));
        queryTypeComboBox.getItems().addAll(typeList);

        stateList = new ArrayList<>();
        stateList.add(new OptionItem(1, "pending", "待审批"));
        stateList.add(new OptionItem(2, "approved", "已通过"));
        stateList.add(new OptionItem(3, "rejected", "已拒绝"));
        stateList.add(new OptionItem(4, "completed", "已完成"));
        stateComboBox.getItems().addAll(stateList);
        queryStateComboBox.getItems().addAll(stateList);
    }

    public void clearPanel() {
        workflowId = null;
        workflowIdField.setText("");
        workflowTypeComboBox.getSelectionModel().select(-1);
        applicantNameField.setText("");
        currentApproverField.setText("");
        approvalStepField.setText("");
        totalStepsField.setText("");
        stateComboBox.getSelectionModel().select(-1);
        approvalCommentArea.setText("");
        applyTimeField.setText("");
        approvalTimeField.setText("");
    }

    protected void changeWorkflowInfo() {
        Map form = dataTableView.getSelectionModel().getSelectedItem();
        if (form == null) {
            clearPanel();
            return;
        }
        workflowId = CommonMethod.getInteger(form, "workflowId");
        DataRequest req = new DataRequest();
        req.add("workflowId", workflowId);
        DataResponse res = HttpRequestUtil.request("/api/approvalWorkflow/" + workflowId, req);
        if (!isSuccess(res)) {
            MessageDialog.showDialog(res.getMsg());
            return;
        }
        form = (Map) res.getData();
        workflowIdField.setText(CommonMethod.getString(form, "workflowId"));
        workflowTypeComboBox.getSelectionModel().select(CommonMethod.getOptionItemIndexByValue(typeList, CommonMethod.getString(form, "workflowType")));
        applicantNameField.setText(CommonMethod.getString(form, "applicantName"));
        currentApproverField.setText(CommonMethod.getString(form, "currentApproverName"));
        approvalStepField.setText(CommonMethod.getString(form, "approvalStep"));
        totalStepsField.setText(CommonMethod.getString(form, "totalSteps"));
        stateComboBox.getSelectionModel().select(CommonMethod.getOptionItemIndexByValue(stateList, CommonMethod.getString(form, "state")));
        approvalCommentArea.setText(CommonMethod.getString(form, "approvalComment"));
        applyTimeField.setText(CommonMethod.getString(form, "applyTime"));
        approvalTimeField.setText(CommonMethod.getString(form, "approvalTime"));
    }

    private void onTableRowSelect() {
        changeWorkflowInfo();
    }

    @FXML
    protected void onQueryButtonClick() {
        OptionItem typeItem = queryTypeComboBox.getSelectionModel().getSelectedItem();
        OptionItem stateItem = queryStateComboBox.getSelectionModel().getSelectedItem();

        DataRequest req = new DataRequest();
        if (typeItem != null) {
            req.add("workflowType", typeItem.getValue());
        }
        if (stateItem != null) {
            req.add("state", stateItem.getValue());
        }

        DataResponse res = HttpRequestUtil.request("/api/approvalWorkflow/all", req);
        if (isSuccess(res)) {
            workflowList = (ArrayList<Map>) res.getData();
            setTableViewData();
        }
    }

    @FXML
    protected void onPendingButtonClick() {
        DataRequest req = new DataRequest();
        DataResponse res = HttpRequestUtil.request("/api/approvalWorkflow/pending/all", req);
        if (isSuccess(res)) {
            workflowList = (ArrayList<Map>) res.getData();
            setTableViewData();
        }
    }

    @FXML
    protected void onApproveButtonClick() {
        Map form = dataTableView.getSelectionModel().getSelectedItem();
        if (form == null) {
            MessageDialog.showDialog("请选择要审批的流程");
            return;
        }

        OptionItem stateItem = stateComboBox.getSelectionModel().getSelectedItem();
        if (stateItem == null) {
            MessageDialog.showDialog("请选择审批结果");
            return;
        }

        String comment = approvalCommentArea.getText();
        workflowId = CommonMethod.getInteger(form, "workflowId");

        DataRequest req = new DataRequest();
        req.add("workflowId", workflowId);
        req.add("result", stateItem.getValue());
        req.add("approvalComment", comment);
        DataResponse res = HttpRequestUtil.request("/api/approvalWorkflow/approve", req);
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
        onApproveButtonClick();
    }

    public void doDelete() {
        MessageDialog.showDialog("不能删除审批流程");
    }

    public void doExport() {
        DataRequest req = new DataRequest();
        byte[] bytes = HttpRequestUtil.requestByteData("/api/approvalWorkflow/export", req);
        if (bytes == null || bytes.length == 0) {
            MessageDialog.showDialog("没有数据可导出");
            return;
        }
        try {
            FileChooser fileDialog = new FileChooser();
            fileDialog.setTitle("选择保存位置");
            fileDialog.setInitialFileName("approvalWorkflow.xlsx");
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