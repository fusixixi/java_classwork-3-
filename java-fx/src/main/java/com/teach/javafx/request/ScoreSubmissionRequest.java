package com.teach.javafx.request;

/**
 * ScoreSubmissionRequest 成绩提交请求类
 * 封装与成绩提交审核相关的HTTP请求
 */
public class ScoreSubmissionRequest {

    /**
     * 查询所有成绩提交
     */
    public static DataResponse getAllSubmissions() {
        DataRequest req = new DataRequest();
        return HttpRequestUtil.request("/api/scoreSubmission/all", req);
    }

    /**
     * 根据ID查询提交详情
     */
    public static DataResponse getSubmissionDetail(Integer submissionId) {
        DataRequest req = new DataRequest();
        req.add("submissionId", submissionId);
        return HttpRequestUtil.request("/api/scoreSubmission/" + submissionId, req);
    }

    /**
     * 教师提交成绩
     */
    public static DataResponse submitScore(Integer scoreId, Integer teacherId, Integer studentId,
                                           Integer courseId, Integer submittedScore) {
        DataRequest req = new DataRequest();
        req.add("scoreId", scoreId);
        req.add("teacherId", teacherId);
        req.add("studentId", studentId);
        req.add("courseId", courseId);
        req.add("submittedScore", submittedScore);
        return HttpRequestUtil.request("/api/scoreSubmission/submit", req);
    }

    /**
     * 审批成绩
     */
    public static DataResponse approveScore(Integer submissionId, Integer state, String comment, Integer approverId) {
        DataRequest req = new DataRequest();
        req.add("submissionId", submissionId);
        req.add("state", state);
        req.add("comment", comment);
        req.add("approverId", approverId);
        return HttpRequestUtil.request("/api/scoreSubmission/approve", req);
    }

    /**
     * 查询教师的成绩提交
     */
    public static DataResponse getTeacherSubmissions(Integer teacherId) {
        DataRequest req = new DataRequest();
        req.add("teacherId", teacherId);
        return HttpRequestUtil.request("/api/scoreSubmission/teacher/" + teacherId, req);
    }

    /**
     * 查询教师的待审批成绩
     */
    public static DataResponse getPendingSubmissionsByTeacher(Integer teacherId) {
        DataRequest req = new DataRequest();
        req.add("teacherId", teacherId);
        return HttpRequestUtil.request("/api/scoreSubmission/pending/teacher/" + teacherId, req);
    }

    /**
     * 查询所有待审批成绩
     */
    public static DataResponse getPendingApprovals() {
        DataRequest req = new DataRequest();
        return HttpRequestUtil.request("/api/scoreSubmission/pending/all", req);
    }

    /**
     * 查询已发布的成绩
     */
    public static DataResponse getPublishedScores() {
        DataRequest req = new DataRequest();
        return HttpRequestUtil.request("/api/scoreSubmission/published", req);
    }

    /**
     * 查询学生的成绩提交
     */
    public static DataResponse getStudentScores(Integer studentId) {
        DataRequest req = new DataRequest();
        req.add("studentId", studentId);
        return HttpRequestUtil.request("/api/scoreSubmission/student/" + studentId, req);
    }

    /**
     * 查询学生的未发布成绩
     */
    public static DataResponse getUnpublishedScores(Integer studentId) {
        DataRequest req = new DataRequest();
        req.add("studentId", studentId);
        return HttpRequestUtil.request("/api/scoreSubmission/unpublished/" + studentId, req);
    }

    /**
     * 查询被拒绝的成绩提交
     */
    public static DataResponse getRejectedSubmissions() {
        DataRequest req = new DataRequest();
        return HttpRequestUtil.request("/api/scoreSubmission/rejected", req);
    }

    /**
     * 统计教师待审批成绩数量
     */
    public static DataResponse countPendingByTeacher(Integer teacherId) {
        DataRequest req = new DataRequest();
        req.add("teacherId", teacherId);
        return HttpRequestUtil.request("/api/scoreSubmission/count/pending/" + teacherId, req);
    }

    /**
     * 删除成绩提交记录
     */
    public static DataResponse deleteSubmission(Integer submissionId) {
        DataRequest req = new DataRequest();
        req.add("submissionId", submissionId);
        return HttpRequestUtil.request("/api/scoreSubmission/" + submissionId, req);
    }

    /**
     * 导出成绩提交数据
     */
    public static byte[] exportSubmissionData() {
        DataRequest req = new DataRequest();
        return HttpRequestUtil.requestByteData("/api/scoreSubmission/export", req);
    }
}