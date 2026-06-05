package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.models.InnovationAchievement;
import cn.edu.sdu.java.server.models.Student;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.StudentRepository;
import cn.edu.sdu.java.server.services.InnovationAchievementService;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * InnovationAchievementController 创新成果管理控制器
 * 处理与创新成果相关的HTTP请求
 */
@RestController
@RequestMapping("/api/innovationAchievement")
@CrossOrigin(origins = "*", maxAge = 3600)
public class InnovationAchievementController {

    @Autowired
    private InnovationAchievementService innovationAchievementService;
    @Autowired
    private StudentRepository studentRepository;

    /**
     * 学生提交创新成果
     * POST /api/innovationAchievement/submit
     */
    @PostMapping("/submit")
    public ResponseEntity<?> submitAchievement(
            @RequestParam(required = false) Integer studentId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String achievementDate,
            @RequestParam(required = false) String attachmentPath,
            @RequestBody(required = false) DataRequest dataRequest) {
        try {
            if (dataRequest != null) {
                if (studentId == null) {
                    studentId = dataRequest.getInteger("studentId");
                }
                if (title == null || title.isEmpty()) {
                    title = dataRequest.getString("title");
                }
                if (description == null) {
                    description = dataRequest.getString("description");
                }
                if (category == null || category.isEmpty()) {
                    category = dataRequest.getString("category");
                }
                if (achievementDate == null || achievementDate.isEmpty()) {
                    achievementDate = dataRequest.getString("achievementDate");
                }
                if (attachmentPath == null) {
                    attachmentPath = dataRequest.getString("attachmentPath");
                }
                if (studentId == null) {
                    String studentNum = dataRequest.getString("studentNum");
                    if (studentNum != null && !studentNum.isEmpty()) {
                        Optional<Student> student = studentRepository.findByPersonNum(studentNum);
                        if (student.isPresent()) {
                            studentId = student.get().getPersonId();
                        }
                    }
                }
            }
            if (studentId == null || title == null || title.isEmpty() || achievementDate == null || achievementDate.isEmpty()) {
                return ResponseUtil.error("缺少必要参数");
            }
            LocalDate date = LocalDate.parse(achievementDate);
            InnovationAchievement achievement = innovationAchievementService.submitAchievement(
                    studentId, title, description, category, date, attachmentPath);
            return ResponseUtil.success("提交成功", achievement);
        } catch (Exception e) {
            return ResponseUtil.error("提交失败：" + e.getMessage());
        }
    }

    /**
     * 审批创新成果
     * POST /api/innovationAchievement/approve
     */
    @PostMapping("/approve")
    public ResponseEntity<?> approveAchievement(
            @RequestParam Integer achievementId,
            @RequestParam Integer state,
            @RequestParam(required = false) String comment) {
        try {
            innovationAchievementService.approveAchievement(achievementId, state, comment);
            return ResponseUtil.success("审批成功", null);
        } catch (Exception e) {
            return ResponseUtil.error("审批失败：" + e.getMessage());
        }
    }

    /**
     * 查询学生的所有创新成果
     * GET /api/innovationAchievement/student/{studentId}
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> getStudentAchievements(@PathVariable Integer studentId) {
        try {
            List<InnovationAchievement> achievements = innovationAchievementService.getStudentAchievements(studentId);
            return ResponseUtil.success("查询成功", achievements);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询学生已通过的创新成果
     * GET /api/innovationAchievement/approved/{studentId}
     */
    @GetMapping("/approved/{studentId}")
    public ResponseEntity<?> getApprovedAchievements(@PathVariable Integer studentId) {
        try {
            List<InnovationAchievement> achievements = innovationAchievementService.getApprovedAchievements(studentId);
            return ResponseUtil.success("查询成功", achievements);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询待审批的创新成果
     * GET /api/innovationAchievement/pending
     */
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingAchievements() {
        try {
            List<InnovationAchievement> achievements = innovationAchievementService.getPendingAchievements();
            return ResponseUtil.success("查询成功", achievements);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询已通过的创新成果
     * GET /api/innovationAchievement/all/approved
     */
    @GetMapping("/all/approved")
    public ResponseEntity<?> getAllApprovedAchievements() {
        try {
            List<InnovationAchievement> achievements = innovationAchievementService.getAllApprovedAchievements();
            return ResponseUtil.success("查询成功", achievements);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 按类型查询创新成果
     * GET /api/innovationAchievement/category/{category}
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<?> getAchievementsByCategory(@PathVariable String category) {
        try {
            List<InnovationAchievement> achievements = innovationAchievementService.getAchievementsByCategory(category);
            return ResponseUtil.success("查询成功", achievements);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 按标题模糊查询
     * GET /api/innovationAchievement/search
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchAchievements(@RequestParam String keyword) {
        try {
            List<InnovationAchievement> achievements = innovationAchievementService.searchAchievements(keyword);
            return ResponseUtil.success("查询成功", achievements);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 统计学生已通过的创新成果数量
     * GET /api/innovationAchievement/count/approved/{studentId}
     */
    @GetMapping("/count/approved/{studentId}")
    public ResponseEntity<?> countApprovedByStudent(@PathVariable Integer studentId) {
        try {
            long count = innovationAchievementService.countApprovedByStudent(studentId);
            return ResponseUtil.success("查询成功", count);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 统计指定类型的创新成果总数
     * GET /api/innovationAchievement/count/category/{category}
     */
    @GetMapping("/count/category/{category}")
    public ResponseEntity<?> countByCategory(@PathVariable String category) {
        try {
            long count = innovationAchievementService.countByCategory(category);
            return ResponseUtil.success("查询成功", count);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取创新成果详情
     * GET /api/innovationAchievement/{achievementId}
     */
    @GetMapping("/{achievementId}")
    public ResponseEntity<?> getAchievementDetail(@PathVariable Integer achievementId) {
        try {
            InnovationAchievement achievement = innovationAchievementService.getAchievementDetail(achievementId);
            return ResponseUtil.success("查询成功", achievement);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 删除创新成果
     * DELETE /api/innovationAchievement/{achievementId}
     */
    @DeleteMapping("/{achievementId}")
    public ResponseEntity<?> deleteAchievement(@PathVariable Integer achievementId) {
        try {
            innovationAchievementService.deleteAchievement(achievementId);
            return ResponseUtil.success("删除成功", null);
        } catch (Exception e) {
            return ResponseUtil.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 查询所有创新成果
     * GET /api/innovationAchievement/all
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllAchievements() {
        try {
            List<InnovationAchievement> achievements = innovationAchievementService.getAllAchievements();
            return ResponseUtil.success("查询成功", achievements);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    @PostMapping("/all")
    public DataResponse getAllAchievementsPost(@RequestBody(required = false) DataRequest dataRequest) {
        try {
            String studentNum = dataRequest == null ? null : dataRequest.getString("studentNum");
            String title = dataRequest == null ? null : dataRequest.getString("title");
            List<InnovationAchievement> achievements = innovationAchievementService.getAllAchievements();
            List<Map<String,Object>> dataList = new ArrayList<>();
            for (InnovationAchievement achievement : achievements) {
                if (achievement.getStudent() == null || achievement.getStudent().getPerson() == null) {
                    continue;
                }
                if (studentNum != null && !studentNum.isBlank() && !achievement.getStudent().getPerson().getNum().contains(studentNum)) {
                    continue;
                }
                if (title != null && !title.isBlank() && (achievement.getTitle() == null || !achievement.getTitle().contains(title))) {
                    continue;
                }
                dataList.add(toAchievementMap(achievement));
            }
            return CommonMethod.getReturnData(dataList);
        } catch (Exception e) {
            return CommonMethod.getReturnMessageError("查询失败：" + e.getMessage());
        }
    }

    private Map<String,Object> toAchievementMap(InnovationAchievement achievement) {
        Map<String,Object> map = new HashMap<>();
        map.put("achievementId", achievement.getAchievementId());
        map.put("studentNum", achievement.getStudent().getPerson().getNum());
        map.put("studentName", achievement.getStudent().getPerson().getName());
        map.put("title", achievement.getTitle());
        map.put("description", achievement.getDescription());
        map.put("category", achievement.getCategory());
        map.put("achievementDate", achievement.getAchievementDate() == null ? "" : achievement.getAchievementDate().toString());
        map.put("submitTime", achievement.getSubmitTime() == null ? "" : achievement.getSubmitTime().toString().replace('T',' '));
        map.put("state", achievement.getState());
        map.put("stateName", getStateName(achievement.getState()));
        map.put("attachmentPath", achievement.getAttachmentPath());
        map.put("approvalComment", "");
        return map;
    }

    private String getStateName(Integer state) {
        if (state == null) {
            return "";
        }
        if (state == 1) {
            return "待审批";
        }
        if (state == 2) {
            return "审批中";
        }
        if (state == 3) {
            return "已通过";
        }
        if (state == 4) {
            return "已拒绝";
        }
        return state.toString();
    }
}