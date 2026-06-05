package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.request.JwtResponse;
import com.teach.javafx.util.CommonMethod;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StudentResumeController {
    private static final String ROLE_STUDENT = "ROLE_STUDENT";

    @FXML
    private Label studentNameLabel;
    @FXML
    private Label studentNumLabel;
    @FXML
    private Label studentClassLabel;
    @FXML
    private TextArea resumeTextArea;
    @FXML
    private ImageView photoImageView;
    @FXML
    private StackPane photoPlaceholderPane;

    @FXML
    public void initialize() {
        JwtResponse jwt = AppStore.getJwt();
        if (jwt == null || !ROLE_STUDENT.equals(jwt.getRole())) {
            resumeTextArea.setText("仅学生端支持查看我的简历。");
            return;
        }
        loadResumeData(jwt.getId());
        loadPhoto(jwt.getId());
    }

    private void loadResumeData(Integer personId) {
        DataRequest req = new DataRequest();
        req.add("personId", personId);
        DataResponse res = HttpRequestUtil.request("/api/student/getStudentResumeData", req);
        if (res == null || res.getCode() != 0 || !(res.getData() instanceof Map<?, ?> data)) {
            resumeTextArea.setText("简历数据加载失败。");
            return;
        }
        Map<String, Object> info = data.get("info") instanceof Map<?, ?> map ? (Map<String, Object>) map : null;
        List<Map<String, Object>> scoreList = asMapList(data.get("scoreList"));
        List<Map<String, Object>> attendanceRateList = asMapList(data.get("attendanceRateList"));
        List<Map<String, Object>> achievementList = asMapList(data.get("achievementList"));

        studentNameLabel.setText(info == null ? "" : CommonMethod.getString(info, "name"));
        studentNumLabel.setText("学号：" + (info == null ? "" : CommonMethod.getString(info, "num")));
        studentClassLabel.setText("班级：" + (info == null ? "" : CommonMethod.getString(info, "className")));
        resumeTextArea.setText(buildResumeText(achievementList, attendanceRateList, scoreList));
    }

    private List<Map<String, Object>> asMapList(Object obj) {
        if (!(obj instanceof List<?> list)) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                result.add((Map<String, Object>) map);
            }
        }
        return result;
    }

    private String buildResumeText(List<Map<String, Object>> achievementList,
                                   List<Map<String, Object>> attendanceRateList,
                                   List<Map<String, Object>> scoreList) {
        StringBuilder sb = new StringBuilder();
        sb.append("创新成果：\n");
        if (achievementList.isEmpty()) {
            sb.append("暂无创新成果记录\n");
        } else {
            int idx = 1;
            for (Map<String, Object> item : achievementList) {
                sb.append(idx++)
                        .append(". ")
                        .append(CommonMethod.getString(item, "title"))
                        .append("（")
                        .append(CommonMethod.getString(item, "category"))
                        .append("，")
                        .append(CommonMethod.getString(item, "stateName"))
                        .append("，")
                        .append(CommonMethod.getString(item, "achievementDate"))
                        .append("）\n");
            }
        }

        sb.append("\n各科考勤率：\n");
        if (attendanceRateList.isEmpty()) {
            sb.append("暂无考勤记录\n");
        } else {
            for (Map<String, Object> item : attendanceRateList) {
                sb.append("- ")
                        .append(CommonMethod.getString(item, "courseName"))
                        .append("：")
                        .append(CommonMethod.getString(item, "attendanceRate"))
                        .append("（")
                        .append(CommonMethod.getString(item, "presentCount"))
                        .append("/")
                        .append(CommonMethod.getString(item, "totalCount"))
                        .append("）\n");
            }
        }

        sb.append("\n成绩：\n");
        if (scoreList.isEmpty()) {
            sb.append("暂无成绩记录\n");
        } else {
            for (Map<String, Object> item : scoreList) {
                sb.append("- ")
                        .append(CommonMethod.getString(item, "courseName"))
                        .append("：")
                        .append(CommonMethod.getString(item, "mark"))
                        .append(" 分\n");
            }
        }
        return sb.toString();
    }

    private void loadPhoto(Integer personId) {
        DataRequest req = new DataRequest();
        req.add("personId", personId + "");
        byte[] bytes = HttpRequestUtil.requestByteData("/api/base/getBlobByteData", req);
        if (bytes == null || bytes.length == 0) {
            showPlaceholder();
            return;
        }
        try {
            photoImageView.setImage(new Image(new ByteArrayInputStream(bytes)));
            photoImageView.setVisible(true);
            photoImageView.setManaged(true);
            photoPlaceholderPane.setVisible(false);
            photoPlaceholderPane.setManaged(false);
        } catch (Exception e) {
            showPlaceholder();
        }
    }

    private void showPlaceholder() {
        photoImageView.setImage(null);
        photoImageView.setVisible(false);
        photoImageView.setManaged(false);
        photoPlaceholderPane.setVisible(true);
        photoPlaceholderPane.setManaged(true);
    }
}
