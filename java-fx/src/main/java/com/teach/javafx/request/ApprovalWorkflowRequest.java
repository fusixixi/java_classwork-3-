package com.teach.javafx.request;

/**
 * ApprovalWorkflowRequest 审批流程请求类
 * 封装与审批流程相关的HTTP请求
 */
public class ApprovalWorkflowRequest {

    /**
     * 查询所有审批流程
     */
    public static DataResponse getAllWorkflows() {
        DataRequest req = new DataRequest();
        return HttpRequestUtil.request("/api/approvalWorkflow/all", req);
    }

    /**
     * 根据ID查询流程详情
     */
    public static DataResponse getWorkflowDetail(Integer workflowId) {
        DataRequest req = new DataRequest();
        req.add("workflowId", workflowId);
        return HttpRequestUtil.request("/api/approvalWorkflow/" + workflowId, req);
    }

    /**
     * 创建审批流程
     */
    public static DataResponse createWorkflow(String workflowType, Integer relatedId,
                                              Integer applicantId, Integer firstApproverId, Integer totalSteps) {
        DataRequest req = new DataRequest();
        req.add("workflowType", workflowType);
        req.add("relatedId", relatedId);
        req.add("applicantId", applicantId);
        req.add("firstApproverId", firstApproverId);
        req.add("totalSteps", totalSteps);
        return HttpRequestUtil.request("/api/approvalWorkflow/create", req);
    }

    /**
     * 审批流程
     */
    public static DataResponse approveWorkflow(Integer workflowId, String result, String approvalComment) {
        DataRequest req = new DataRequest();
        req.add("workflowId", workflowId);
        req.add("result", result);
        req.add("approvalComment", approvalComment);
        return HttpRequestUtil.request("/api/approvalWorkflow/approve", req);
    }

    /**
     * 查询待审批的流程
     */
    public static DataResponse getPendingWorkflows(Integer approverId) {
        DataRequest req = new DataRequest();
        req.add("approverId", approverId);
        return HttpRequestUtil.request("/api/approvalWorkflow/pending/" + approverId, req);
    }

    /**
     * 查询待审批流程数量
     */
    public static DataResponse getPendingWorkflowCount(Integer approverId) {
        DataRequest req = new DataRequest();
        req.add("approverId", approverId);
        return HttpRequestUtil.request("/api/approvalWorkflow/pending/count/" + approverId, req);
    }

    /**
     * 查询待审批的特定类型流程
     */
    public static DataResponse getPendingByType(String workflowType) {
        DataRequest req = new DataRequest();
        req.add("workflowType", workflowType);
        return HttpRequestUtil.request("/api/approvalWorkflow/pending/type/" + workflowType, req);
    }

    /**
     * 查询申请人的所有流程
     */
    public static DataResponse getApplicantWorkflows(Integer applicantId) {
        DataRequest req = new DataRequest();
        req.add("applicantId", applicantId);
        return HttpRequestUtil.request("/api/approvalWorkflow/applicant/" + applicantId, req);
    }

    /**
     * 查询指定业务的最新流程
     */
    public static DataResponse getLatestWorkflow(Integer relatedId) {
        DataRequest req = new DataRequest();
        req.add("relatedId", relatedId);
        return HttpRequestUtil.request("/api/approvalWorkflow/latest/" + relatedId, req);
    }

    /**
     * 查询指定状态的流程
     */
    public static DataResponse getWorkflowsByState(String state) {
        DataRequest req = new DataRequest();
        req.add("state", state);
        return HttpRequestUtil.request("/api/approvalWorkflow/state/" + state, req);
    }

    /**
     * 导出审批流程数据
     */
    public static byte[] exportWorkflowData() {
        DataRequest req = new DataRequest();
        return HttpRequestUtil.requestByteData("/api/approvalWorkflow/export", req);
    }
}