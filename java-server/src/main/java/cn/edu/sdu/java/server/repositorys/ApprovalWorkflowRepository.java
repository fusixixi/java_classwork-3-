package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.ApprovalWorkflow;
import cn.edu.sdu.java.server.models.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ApprovalWorkflowRepository 审批流程数据访问接口
 * 提供审批流程的数据库操作方法
 */
@Repository
public interface ApprovalWorkflowRepository extends JpaRepository<ApprovalWorkflow, Integer> {

    /**
     * 根据工作流类型查询审批流程
     */
    List<ApprovalWorkflow> findByWorkflowType(String workflowType);

    /**
     * 根据关联ID查询审批流程
     */
    List<ApprovalWorkflow> findByRelatedId(Integer relatedId);

    /**
     * 根据申请人查询审批流程
     */
    List<ApprovalWorkflow> findByApplicant(Person applicant);

    /**
     * 根据当前审批人查询待审批的流程
     */
    List<ApprovalWorkflow> findByCurrentApproverAndState(Person approver, String state);

    /**
     * 根据状态查询审批流程
     */
    List<ApprovalWorkflow> findByState(String state);

    /**
     * 查询指定审批人的待审批工作流
     */
    @Query("SELECT aw FROM ApprovalWorkflow aw WHERE aw.currentApprover.personId = :approverId " +
            "AND aw.state = 'pending' ORDER BY aw.applyTime DESC")
    List<ApprovalWorkflow> findPendingWorkflowsByApprover(@Param("approverId") Integer approverId);

    /**
     * 查询指定工作流类型的待审批流程
     */
    @Query("SELECT aw FROM ApprovalWorkflow aw WHERE aw.workflowType = :type " +
            "AND aw.state = 'pending' ORDER BY aw.applyTime DESC")
    List<ApprovalWorkflow> findPendingByType(@Param("type") String type);

    /**
     * 查询申请人的所有审批流程
     */
    @Query("SELECT aw FROM ApprovalWorkflow aw WHERE aw.applicant.personId = :applicantId " +
            "ORDER BY aw.applyTime DESC")
    List<ApprovalWorkflow> findByApplicantId(@Param("applicantId") Integer applicantId);

    /**
     * 统计审批人的待审批数量
     */
    @Query("SELECT COUNT(aw) FROM ApprovalWorkflow aw WHERE aw.currentApprover.personId = :approverId " +
            "AND aw.state = 'pending'")
    long countPendingByApprover(@Param("approverId") Integer approverId);

    /**
     * 查询指定关联业务的最新审批流程
     */
    ApprovalWorkflow findFirstByRelatedIdOrderByCreateTimeDesc(Integer relatedId);
}