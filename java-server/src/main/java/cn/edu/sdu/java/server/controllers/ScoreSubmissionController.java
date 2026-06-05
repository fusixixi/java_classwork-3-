package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.models.ScoreSubmission;
import cn.edu.sdu.java.server.services.ScoreSubmissionService;
import cn.edu.sdu.java.server.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ScoreSubmissionController 成绩提交审核控制器
 * 处理与成绩提交和审核相关的HTTP请求
 */
@RestController
@RequestMapping("/api/scoreSubmission")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ScoreSubmissionController {

    @Autowired
    private ScoreSubmissionService scoreSubmissionService;

    /**
     * 教师提交成绩
     * POST /api/scoreSubmission/submit
     */
    @PostMapping("/submit")
    public ResponseEntity<?> submitScore(
            @RequestParam Integer scoreId,
            @RequestParam Integer teacherId,
            @RequestParam Integer studentId,
            @RequestParam Integer courseId,
            @RequestParam Integer submittedScore) {
        try {
            ScoreSubmission submission = scoreSubmissionService.submitScore(
                    scoreId, teacherId, studentId, courseId, submittedScore);
            return ResponseUtil.success("成绩提交成功", submission);
        } catch (Exception e) {
            return ResponseUtil.error("成绩提交失败：" + e.getMessage());
        }
    }

    /**
     * 审批成绩
     * POST /api/scoreSubmission/approve
     */
    @PostMapping("/approve")
    public ResponseEntity<?> approveScore(
            @RequestParam Integer submissionId,
            @RequestParam Integer state,
            @RequestParam(required = false) String comment,
            @RequestParam Integer approverId) {
        try {
            scoreSubmissionService.approveScore(submissionId, state, comment, approverId);
            return ResponseUtil.success("审批成功", null);
        } catch (Exception e) {
            return ResponseUtil.error("审批失败：" + e.getMessage());
        }
    }

    /**
     * 查询教师的成绩提交记录
     * GET /api/scoreSubmission/teacher/{teacherId}
     */
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<?> getTeacherSubmissions(@PathVariable Integer teacherId) {
        try {
            List<ScoreSubmission> submissions = scoreSubmissionService.getTeacherSubmissions(teacherId);
            return ResponseUtil.success("查询成功", submissions);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询教师待审批的成绩
     * GET /api/scoreSubmission/pending/teacher/{teacherId}
     */
    @GetMapping("/pending/teacher/{teacherId}")
    public ResponseEntity<?> getPendingSubmissionsByTeacher(@PathVariable Integer teacherId) {
        try {
            List<ScoreSubmission> submissions = scoreSubmissionService.getPendingSubmissionsByTeacher(teacherId);
            return ResponseUtil.success("查询成功", submissions);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询所有待审批的成绩
     * GET /api/scoreSubmission/pending/all
     */
    @GetMapping("/pending/all")
    public ResponseEntity<?> getPendingApprovals() {
        try {
            List<ScoreSubmission> submissions = scoreSubmissionService.getPendingApprovals();
            return ResponseUtil.success("查询成功", submissions);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询已发布的成绩
     * GET /api/scoreSubmission/published
     */
    @GetMapping("/published")
    public ResponseEntity<?> getPublishedScores() {
        try {
            List<ScoreSubmission> submissions = scoreSubmissionService.getPublishedScores();
            return ResponseUtil.success("查询成功", submissions);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询学生的成绩提交记录
     * GET /api/scoreSubmission/student/{studentId}
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> getStudentScores(@PathVariable Integer studentId) {
        try {
            List<ScoreSubmission> submissions = scoreSubmissionService.getStudentScores(studentId);
            return ResponseUtil.success("查询成功", submissions);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询学生未发布的成绩
     * GET /api/scoreSubmission/unpublished/{studentId}
     */
    @GetMapping("/unpublished/{studentId}")
    public ResponseEntity<?> getUnpublishedScores(@PathVariable Integer studentId) {
        try {
            List<ScoreSubmission> submissions = scoreSubmissionService.getUnpublishedScores(studentId);
            return ResponseUtil.success("查询成功", submissions);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询拒绝的成绩提交
     * GET /api/scoreSubmission/rejected
     */
    @GetMapping("/rejected")
    public ResponseEntity<?> getRejectedSubmissions() {
        try {
            List<ScoreSubmission> submissions = scoreSubmissionService.getRejectedSubmissions();
            return ResponseUtil.success("查询成功", submissions);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 统计教师待审批的成绩数量
     * GET /api/scoreSubmission/count/pending/{teacherId}
     */
    @GetMapping("/count/pending/{teacherId}")
    public ResponseEntity<?> countPendingByTeacher(@PathVariable Integer teacherId) {
        try {
            long count = scoreSubmissionService.countPendingByTeacher(teacherId);
            return ResponseUtil.success("查询成功", count);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取成绩提交详情
     * GET /api/scoreSubmission/{submissionId}
     */
    @GetMapping("/{submissionId}")
    public ResponseEntity<?> getSubmissionDetail(@PathVariable Integer submissionId) {
        try {
            ScoreSubmission submission = scoreSubmissionService.getSubmissionDetail(submissionId);
            return ResponseUtil.success("查询成功", submission);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 删除成绩提交记录
     * DELETE /api/scoreSubmission/{submissionId}
     */
    @DeleteMapping("/{submissionId}")
    public ResponseEntity<?> deleteSubmission(@PathVariable Integer submissionId) {
        try {
            scoreSubmissionService.deleteSubmission(submissionId);
            return ResponseUtil.success("删除成功", null);
        } catch (Exception e) {
            return ResponseUtil.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 查询所有成绩提交记录
     * GET /api/scoreSubmission/all
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllSubmissions() {
        try {
            List<ScoreSubmission> submissions = scoreSubmissionService.getAllSubmissions();
            return ResponseUtil.success("查询成功", submissions);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }
}