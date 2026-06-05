package com.teach.javafx.request;

import java.util.HashMap;
import java.util.Map;

/**
 * CourseSelectionRequest 选课请求类
 * 封装与选课相关的HTTP请求
 */
public class CourseSelectionRequest {

    /**
     * 查询所有选课信息
     */
    public static DataResponse getAllSelections() {
        DataRequest req = new DataRequest();
        return HttpRequestUtil.request("/api/courseSelection/all", req);
    }

    /**
     * 根据ID查询选课详情
     */
    public static DataResponse getSelectionDetail(Integer selectionId) {
        DataRequest req = new DataRequest();
        req.add("selectionId", selectionId);
        return HttpRequestUtil.request("/api/courseSelection/" + selectionId, req);
    }

    /**
     * 学生选课
     */
    public static DataResponse selectCourse(Integer studentId, Integer courseId) {
        DataRequest req = new DataRequest();
        req.add("studentId", studentId);
        req.add("courseId", courseId);
        return HttpRequestUtil.request("/api/courseSelection/select", req);
    }

    /**
     * 取消选课
     */
    public static DataResponse cancelSelection(Integer selectionId) {
        DataRequest req = new DataRequest();
        req.add("selectionId", selectionId);
        return HttpRequestUtil.request("/api/courseSelection/cancel", req);
    }

    /**
     * 查询学生的选课记录
     */
    public static DataResponse getStudentSelections(Integer studentId) {
        DataRequest req = new DataRequest();
        req.add("studentId", studentId);
        return HttpRequestUtil.request("/api/courseSelection/student/" + studentId, req);
    }

    /**
     * 查询活跃的选课记录
     */
    public static DataResponse getActiveSelections(Integer studentId) {
        DataRequest req = new DataRequest();
        req.add("studentId", studentId);
        return HttpRequestUtil.request("/api/courseSelection/active/" + studentId, req);
    }

    /**
     * 查询学生选课数量
     */
    public static DataResponse getSelectedCourseCount(Integer studentId) {
        DataRequest req = new DataRequest();
        req.add("studentId", studentId);
        return HttpRequestUtil.request("/api/courseSelection/count/" + studentId, req);
    }

    /**
     * 标记选课完成
     */
    public static DataResponse completeSelection(Integer selectionId) {
        DataRequest req = new DataRequest();
        req.add("selectionId", selectionId);
        return HttpRequestUtil.request("/api/courseSelection/complete", req);
    }

    /**
     * 删除选课记录
     */
    public static DataResponse deleteSelection(Integer selectionId) {
        DataRequest req = new DataRequest();
        req.add("selectionId", selectionId);
        return HttpRequestUtil.request("/api/courseSelection/" + selectionId, req);
    }

    /**
     * 导出选课数据
     */
    public static byte[] exportSelectionData(String studentNum, String studentName) {
        DataRequest req = new DataRequest();
        req.add("studentNum", studentNum);
        req.add("studentName", studentName);
        return HttpRequestUtil.requestByteData("/api/courseSelection/export", req);
    }
}