package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.models.ApprovalWorkflow;
import cn.edu.sdu.java.server.services.ApprovalWorkflowService;
import cn.edu.sdu.java.server.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ApprovalWorkflowController 审批流程管理控制器
 * 处理与审批流程相关的HTTP请求
 */
@RestController
@RequestMapping("/api/approvalWorkflow")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ApprovalWorkflowController {

    @Autowired
    private ApprovalWorkflowService approvalWorkflowService;

    /**
     * 创建审批流程
     * POST /api/approvalWorkflow/create
     */
    @PostMapping("/create")
    public ResponseEntity<?> createWorkflow(
            @RequestParam String workflowType,
            @RequestParam Integer relatedId,
            @RequestParam Integer applicantId,
            @RequestParam Integer firstApproverId,
            @RequestParam Integer totalSteps) {
        try {
            ApprovalWorkflow workflow = approvalWorkflowService.createWorkflow(
                    workflowType, relatedId, applicantId, firstApproverId, totalSteps);
            return ResponseUtil.success("创建审批流程成功", workflow);
        } catch (Exception e) {
            return ResponseUtil.error("创建审批流程失败：" + e.getMessage());
        }
    }

    /**
     * 审批流程
     * POST /api/approvalWorkflow/approve
     */
    @PostMapping("/approve")
    public ResponseEntity<?> approveWorkflow(
            @RequestParam Integer workflowId,
            @RequestParam String result,
            @RequestParam(required = false) String approvalComment) {
        try {
            approvalWorkflowService.approveWorkflow(workflowId, approvalComment, result);
            return ResponseUtil.success("审批成功", null);
        } catch (Exception e) {
            return ResponseUtil.error("审批失败：" + e.getMessage());
        }
    }

    /**
     * 查询待审批的流程
     * GET /api/approvalWorkflow/pending/{approverId}
     */
    @GetMapping("/pending/{approverId}")
    public ResponseEntity<?> getPendingWorkflows(@PathVariable Integer approverId) {
        try {
            List<ApprovalWorkflow> workflows = approvalWorkflowService.getPendingWorkflows(approverId);
            return ResponseUtil.success("查询成功", workflows);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询待审批流程数量
     * GET /api/approvalWorkflow/pending/count/{approverId}
     */
    @GetMapping("/pending/count/{approverId}")
    public ResponseEntity<?> getPendingWorkflowCount(@PathVariable Integer approverId) {
        try {
            long count = approvalWorkflowService.getPendingWorkflowCount(approverId);
            return ResponseUtil.success("查询成功", count);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询工作流类型的待审批流程
     * GET /api/approvalWorkflow/pending/type/{workflowType}
     */
    @GetMapping("/pending/type/{workflowType}")
    public ResponseEntity<?> getPendingByType(@PathVariable String workflowType) {
        try {
            List<ApprovalWorkflow> workflows = approvalWorkflowService.getPendingByType(workflowType);
            return ResponseUtil.success("查询成功", workflows);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询申请人的所有审批流程
     * GET /api/approvalWorkflow/applicant/{applicantId}
     */
    @GetMapping("/applicant/{applicantId}")
    public ResponseEntity<?> getApplicantWorkflows(@PathVariable Integer applicantId) {
        try {
            List<ApprovalWorkflow> workflows = approvalWorkflowService.getApplicantWorkflows(applicantId);
            return ResponseUtil.success("查询成功", workflows);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取审批流程详情
     * GET /api/approvalWorkflow/{workflowId}
     */
    @GetMapping("/{workflowId}")
    public ResponseEntity<?> getWorkflowDetail(@PathVariable Integer workflowId) {
        try {
            ApprovalWorkflow workflow = approvalWorkflowService.getWorkflowDetail(workflowId);
            return ResponseUtil.success("查询成功", workflow);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询指定业务的最新审批流程
     * GET /api/approvalWorkflow/latest/{relatedId}
     */
    @GetMapping("/latest/{relatedId}")
    public ResponseEntity<?> getLatestWorkflow(@PathVariable Integer relatedId) {
        try {
            ApprovalWorkflow workflow = approvalWorkflowService.getLatestWorkflow(relatedId);
            return ResponseUtil.success("查询成功", workflow);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询所有审批流程
     * GET /api/approvalWorkflow/all
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllWorkflows() {
        try {
            List<ApprovalWorkflow> workflows = approvalWorkflowService.getAllWorkflows();
            return ResponseUtil.success("查询成功", workflows);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询指定状态的审批流程
     * GET /api/approvalWorkflow/state/{state}
     */
    @GetMapping("/state/{state}")
    public ResponseEntity<?> getWorkflowsByState(@PathVariable String state) {
        try {
            List<ApprovalWorkflow> workflows = approvalWorkflowService.getWorkflowsByState(state);
            return ResponseUtil.success("查询成功", workflows);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }
}