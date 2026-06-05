package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.ApprovalWorkflow;
import cn.edu.sdu.java.server.models.Person;
import cn.edu.sdu.java.server.repositorys.ApprovalWorkflowRepository;
import cn.edu.sdu.java.server.repositorys.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * ApprovalWorkflowService 审批流程服务类
 * 提供审批流程管理的业务逻辑
 */
@Service
@Transactional
public class ApprovalWorkflowService {
    private static final String STATE_PENDING = "pending";
    private static final String STATE_REJECTED = "rejected";
    private static final String STATE_COMPLETED = "completed";
    private static final String RESULT_APPROVED = "approved";
    private static final String RESULT_REJECTED = "rejected";
    private static final Set<String> VALID_RESULTS = Set.of(RESULT_APPROVED, RESULT_REJECTED);

    @Autowired
    private ApprovalWorkflowRepository approvalWorkflowRepository;

    @Autowired
    private PersonRepository personRepository;

    /**
     * 创建审批流程
     */
    public ApprovalWorkflow createWorkflow(String workflowType, Integer relatedId,
                                           Integer applicantId, Integer firstApproverId,
                                           Integer totalSteps) {
        if (workflowType == null || workflowType.trim().isEmpty()) {
            throw new RuntimeException("工作流类型不能为空");
        }
        if (relatedId == null || relatedId <= 0 || totalSteps == null || totalSteps <= 0) {
            throw new RuntimeException("工作流参数无效");
        }
        if (applicantId == null || firstApproverId == null) {
            throw new RuntimeException("申请人或审批人不能为空");
        }
        Optional<Person> applicant = personRepository.findById(applicantId);
        Optional<Person> approver = personRepository.findById(firstApproverId);

        if (applicant.isEmpty() || approver.isEmpty()) {
            throw new RuntimeException("申请人或审批人不存在");
        }

        ApprovalWorkflow workflow = new ApprovalWorkflow();
        workflow.setWorkflowType(workflowType);
        workflow.setRelatedId(relatedId);
        workflow.setApplicant(applicant.get());
        workflow.setCurrentApprover(approver.get());
        workflow.setApprovalStep(1);
        workflow.setTotalSteps(totalSteps);
        workflow.setState(STATE_PENDING);
        workflow.setApplyTime(LocalDateTime.now());
        workflow.setCreateTime(LocalDateTime.now());

        return approvalWorkflowRepository.save(workflow);
    }

    /**
     * 审批流程
     */
    public void approveWorkflow(Integer workflowId, String approvalComment, String result) {
        if (result == null || !VALID_RESULTS.contains(result)) {
            throw new RuntimeException("审批结果无效");
        }
        Optional<ApprovalWorkflow> workflow = approvalWorkflowRepository.findById(workflowId);
        if (workflow.isEmpty()) {
            throw new RuntimeException("审批流程不存在");
        }

        ApprovalWorkflow aw = workflow.get();
        if (!STATE_PENDING.equals(aw.getState())) {
            throw new RuntimeException("该审批流程当前不可审批");
        }
        aw.setApprovalComment(approvalComment);
        aw.setApprovalTime(LocalDateTime.now());

        if (RESULT_APPROVED.equals(result)) {
            // 如果是最后一步，标记为已完成
            if (aw.getApprovalStep() >= aw.getTotalSteps()) {
                aw.setState(STATE_COMPLETED);
            } else {
                // 进入下一步
                aw.setApprovalStep(aw.getApprovalStep() + 1);
                aw.setState(STATE_PENDING);
            }
        } else if (RESULT_REJECTED.equals(result)) {
            aw.setState(STATE_REJECTED);
        }

        approvalWorkflowRepository.save(aw);
    }

    /**
     * 查询待审批的流程
     */
    public List<ApprovalWorkflow> getPendingWorkflows(Integer approverId) {
        return approvalWorkflowRepository.findPendingWorkflowsByApprover(approverId);
    }

    /**
     * 查询待审批流程数量
     */
    public long getPendingWorkflowCount(Integer approverId) {
        return approvalWorkflowRepository.countPendingByApprover(approverId);
    }

    /**
     * 查询工作流类型的待审批流程
     */
    public List<ApprovalWorkflow> getPendingByType(String workflowType) {
        return approvalWorkflowRepository.findPendingByType(workflowType);
    }

    /**
     * 查询申请人的所有审批流程
     */
    public List<ApprovalWorkflow> getApplicantWorkflows(Integer applicantId) {
        return approvalWorkflowRepository.findByApplicantId(applicantId);
    }

    /**
     * 获取审批流程详情
     */
    public ApprovalWorkflow getWorkflowDetail(Integer workflowId) {
        Optional<ApprovalWorkflow> workflow = approvalWorkflowRepository.findById(workflowId);
        if (workflow.isEmpty()) {
            throw new RuntimeException("审批流程不存在");
        }
        return workflow.get();
    }

    /**
     * 查询指定业务的最新审批流程
     */
    public ApprovalWorkflow getLatestWorkflow(Integer relatedId) {
        return approvalWorkflowRepository.findFirstByRelatedIdOrderByCreateTimeDesc(relatedId);
    }

    /**
     * 查询所有审批流程
     */
    public List<ApprovalWorkflow> getAllWorkflows() {
        return approvalWorkflowRepository.findAll();
    }

    /**
     * 查询指定状态的审批流程
     */
    public List<ApprovalWorkflow> getWorkflowsByState(String state) {
        return approvalWorkflowRepository.findByState(state);
    }
}