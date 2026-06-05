package com.teach.javafx.request;

import java.time.LocalDate;

/**
 * AttendanceRequest 考勤请求类
 * 封装与考勤相关的HTTP请求
 */
public class AttendanceRequest {

    /**
     * 查询所有考勤记录
     */
    public static DataResponse getAllAttendance() {
        DataRequest req = new DataRequest();
        return HttpRequestUtil.request("/api/attendance/all", req);
    }

    /**
     * 根据ID查询考勤详情
     */
    public static DataResponse getAttendanceDetail(Integer attendanceId) {
        DataRequest req = new DataRequest();
        req.add("attendanceId", attendanceId);
        return HttpRequestUtil.request("/api/attendance/" + attendanceId, req);
    }

    /**
     * 记录考勤
     */
    public static DataResponse recordAttendance(Integer studentId, Integer courseId,
                                                String date, String status, String remark) {
        DataRequest req = new DataRequest();
        req.add("studentId", studentId);
        req.add("courseId", courseId);
        req.add("date", date);
        req.add("status", status);
        req.add("remark", remark);
        return HttpRequestUtil.request("/api/attendance/record", req);
    }

    /**
     * 更新考勤状态
     */
    public static DataResponse updateAttendanceStatus(Integer attendanceId, String status, String remark) {
        DataRequest req = new DataRequest();
        req.add("attendanceId", attendanceId);
        req.add("status", status);
        req.add("remark", remark);
        return HttpRequestUtil.request("/api/attendance/" + attendanceId, req);
    }

    /**
     * 查询学生的考勤记录
     */
    public static DataResponse getStudentAttendance(Integer studentId) {
        DataRequest req = new DataRequest();
        req.add("studentId", studentId);
        return HttpRequestUtil.request("/api/attendance/student/" + studentId, req);
    }

    /**
     * 查询学生在某课程的考勤记录
     */
    public static DataResponse getCourseAttendance(Integer studentId, Integer courseId) {
        DataRequest req = new DataRequest();
        req.add("studentId", studentId);
        req.add("courseId", courseId);
        return HttpRequestUtil.request("/api/attendance/course/" + studentId + "/" + courseId, req);
    }

    /**
     * 计算出勤率
     */
    public static DataResponse calculateAttendanceRate(Integer studentId, Integer courseId) {
        DataRequest req = new DataRequest();
        req.add("studentId", studentId);
        req.add("courseId", courseId);
        return HttpRequestUtil.request("/api/attendance/rate/" + studentId + "/" + courseId, req);
    }

    /**
     * 查询缺勤记录
     */
    public static DataResponse getAbsentRecords(Integer studentId) {
        DataRequest req = new DataRequest();
        req.add("studentId", studentId);
        return HttpRequestUtil.request("/api/attendance/absent/" + studentId, req);
    }

    /**
     * 查询迟到记录
     */
    public static DataResponse getLateRecords(Integer studentId) {
        DataRequest req = new DataRequest();
        req.add("studentId", studentId);
        return HttpRequestUtil.request("/api/attendance/late/" + studentId, req);
    }

    /**
     * 查询指定日期的考勤
     */
    public static DataResponse getAttendanceByDate(String date) {
        DataRequest req = new DataRequest();
        req.add("date", date);
        return HttpRequestUtil.request("/api/attendance/date/" + date, req);
    }

    /**
     * 删除考勤记录
     */
    public static DataResponse deleteAttendance(Integer attendanceId) {
        DataRequest req = new DataRequest();
        req.add("attendanceId", attendanceId);
        return HttpRequestUtil.request("/api/attendance/" + attendanceId, req);
    }

    /**
     * 导出考勤数据
     */
    public static byte[] exportAttendanceData(String studentNum) {
        DataRequest req = new DataRequest();
        req.add("studentNum", studentNum);
        return HttpRequestUtil.requestByteData("/api/attendance/export", req);
    }
}