package com.teach.javafx.request;

/**
 * InnovationAchievementRequest 创新成果请求类
 * 封装与创新成果相关的HTTP请求
 */
public class InnovationAchievementRequest {

    /**
     * 查询所有创新成果
     */
    public static DataResponse getAllAchievements() {
        DataRequest req = new DataRequest();
        return HttpRequestUtil.request("/api/innovationAchievement/all", req);
    }

    /**
     * 根据ID查询成果详情
     */
    public static DataResponse getAchievementDetail(Integer achievementId) {
        DataRequest req = new DataRequest();
        req.add("achievementId", achievementId);
        return HttpRequestUtil.request("/api/innovationAchievement/" + achievementId, req);
    }

    /**
     * 学生提交创新成果
     */
    public static DataResponse submitAchievement(Integer studentId, String title, String description,
                                                 String category, String achievementDate, String attachmentPath) {
        DataRequest req = new DataRequest();
        req.add("studentId", studentId);
        req.add("title", title);
        req.add("description", description);
        req.add("category", category);
        req.add("achievementDate", achievementDate);
        req.add("attachmentPath", attachmentPath);
        return HttpRequestUtil.request("/api/innovationAchievement/submit", req);
    }

    /**
     * 审批创新成果
     */
    public static DataResponse approveAchievement(Integer achievementId, Integer state, String comment) {
        DataRequest req = new DataRequest();
        req.add("achievementId", achievementId);
        req.add("state", state);
        req.add("comment", comment);
        return HttpRequestUtil.request("/api/innovationAchievement/approve", req);
    }

    /**
     * 查询学生的创新成果
     */
    public static DataResponse getStudentAchievements(Integer studentId) {
        DataRequest req = new DataRequest();
        req.add("studentId", studentId);
        return HttpRequestUtil.request("/api/innovationAchievement/student/" + studentId, req);
    }

    /**
     * 查询已通过的创新成果
     */
    public static DataResponse getApprovedAchievements(Integer studentId) {
        DataRequest req = new DataRequest();
        req.add("studentId", studentId);
        return HttpRequestUtil.request("/api/innovationAchievement/approved/" + studentId, req);
    }

    /**
     * 查询待审批的创新成果
     */
    public static DataResponse getPendingAchievements() {
        DataRequest req = new DataRequest();
        return HttpRequestUtil.request("/api/innovationAchievement/pending", req);
    }

    /**
     * 查询所有已通过的创新成果
     */
    public static DataResponse getAllApprovedAchievements() {
        DataRequest req = new DataRequest();
        return HttpRequestUtil.request("/api/innovationAchievement/all/approved", req);
    }

    /**
     * 按类型查询创新成果
     */
    public static DataResponse getAchievementsByCategory(String category) {
        DataRequest req = new DataRequest();
        req.add("category", category);
        return HttpRequestUtil.request("/api/innovationAchievement/category/" + category, req);
    }

    /**
     * 搜索创新成果
     */
    public static DataResponse searchAchievements(String keyword) {
        DataRequest req = new DataRequest();
        req.add("keyword", keyword);
        return HttpRequestUtil.request("/api/innovationAchievement/search", req);
    }

    /**
     * 统计学生已通过的成果数量
     */
    public static DataResponse countApprovedByStudent(Integer studentId) {
        DataRequest req = new DataRequest();
        req.add("studentId", studentId);
        return HttpRequestUtil.request("/api/innovationAchievement/count/approved/" + studentId, req);
    }

    /**
     * 统计指定类型的成果数量
     */
    public static DataResponse countByCategory(String category) {
        DataRequest req = new DataRequest();
        req.add("category", category);
        return HttpRequestUtil.request("/api/innovationAchievement/count/category/" + category, req);
    }

    /**
     * 删除创新成果
     */
    public static DataResponse deleteAchievement(Integer achievementId) {
        DataRequest req = new DataRequest();
        req.add("achievementId", achievementId);
        return HttpRequestUtil.request("/api/innovationAchievement/" + achievementId, req);
    }

    /**
     * 导出创新成果数据
     */
    public static byte[] exportAchievementData(String studentNum) {
        DataRequest req = new DataRequest();
        req.add("studentNum", studentNum);
        return HttpRequestUtil.requestByteData("/api/innovationAchievement/export", req);
    }
}