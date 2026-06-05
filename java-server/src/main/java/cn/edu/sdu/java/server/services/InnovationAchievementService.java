package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.InnovationAchievement;
import cn.edu.sdu.java.server.models.Student;
import cn.edu.sdu.java.server.repositorys.InnovationAchievementRepository;
import cn.edu.sdu.java.server.repositorys.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * InnovationAchievementService 创新成果服务类
 * 提供学生创新成果管理的业务逻辑
 */
@Service
@Transactional
public class InnovationAchievementService {
    private static final int STATE_PENDING = 1;
    private static final int STATE_APPROVED = 3;
    private static final int STATE_REJECTED = 4;
    private static final Set<Integer> VALID_APPROVAL_STATES = Set.of(STATE_APPROVED, STATE_REJECTED);

    @Autowired
    private InnovationAchievementRepository innovationAchievementRepository;

    @Autowired
    private StudentRepository studentRepository;

    /**
     * 学生提交创新成果
     */
    public InnovationAchievement submitAchievement(Integer studentId, String title,
                                                   String description, String category,
                                                   LocalDate achievementDate, String attachmentPath) {
        Optional<Student> student = studentRepository.findById(studentId);
        if (student.isEmpty()) {
            throw new RuntimeException("学生不存在");
        }

        InnovationAchievement achievement = new InnovationAchievement();
        achievement.setStudent(student.get());
        achievement.setTitle(title);
        achievement.setDescription(description);
        achievement.setCategory(category);
        achievement.setAchievementDate(achievementDate);
        achievement.setAttachmentPath(attachmentPath);
        achievement.setSubmitTime(LocalDateTime.now());
        achievement.setState(STATE_PENDING);  // 1-待审批
        achievement.setCreateTime(LocalDateTime.now());
        achievement.setUpdateTime(LocalDateTime.now());

        return innovationAchievementRepository.save(achievement);
    }

    /**
     * 审批创新成果
     */
    public void approveAchievement(Integer achievementId, Integer newState, String comment) {
        if (newState == null || !VALID_APPROVAL_STATES.contains(newState)) {
            throw new RuntimeException("审批状态无效");
        }
        Optional<InnovationAchievement> achievement = innovationAchievementRepository.findById(achievementId);
        if (achievement.isEmpty()) {
            throw new RuntimeException("创新成果不存在");
        }

        InnovationAchievement ia = achievement.get();
        ia.setState(newState);  // 3-已通过，4-已拒绝
        ia.setUpdateTime(LocalDateTime.now());
        innovationAchievementRepository.save(ia);
    }

    /**
     * 查询学生的所有创新成果
     */
    public List<InnovationAchievement> getStudentAchievements(Integer studentId) {
        Optional<Student> student = studentRepository.findById(studentId);
        if (student.isEmpty()) {
            throw new RuntimeException("学生不存在");
        }
        return innovationAchievementRepository.findByStudent(student.get());
    }

    /**
     * 查询学生已通过的创新成果
     */
    public List<InnovationAchievement> getApprovedAchievements(Integer studentId) {
        Optional<Student> student = studentRepository.findById(studentId);
        if (student.isEmpty()) {
            throw new RuntimeException("学生不存在");
        }
        return innovationAchievementRepository.findByStudentAndState(student.get(), STATE_APPROVED);
    }

    /**
     * 查询待审批的创新成果
     */
    public List<InnovationAchievement> getPendingAchievements() {
        return innovationAchievementRepository.findPendingAchievements();
    }

    /**
     * 查询已通过的创新成果
     */
    public List<InnovationAchievement> getAllApprovedAchievements() {
        return innovationAchievementRepository.findApprovedAchievements();
    }

    /**
     * 按类型查询创新成果
     */
    public List<InnovationAchievement> getAchievementsByCategory(String category) {
        return innovationAchievementRepository.findByCategory(category);
    }

    /**
     * 按标题模糊查询
     */
    public List<InnovationAchievement> searchAchievements(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return innovationAchievementRepository.findAll();
        }
        return innovationAchievementRepository.findByTitleContaining(keyword.trim());
    }

    /**
     * 统计学生已通过的创新成果数量
     */
    public long countApprovedByStudent(Integer studentId) {
        return innovationAchievementRepository.countApprovedByStudent(studentId);
    }

    /**
     * 统计指定类型的创新成果总数
     */
    public long countByCategory(String category) {
        return innovationAchievementRepository.countByCategory(category);
    }

    /**
     * 获取创新成果详情
     */
    public InnovationAchievement getAchievementDetail(Integer achievementId) {
        Optional<InnovationAchievement> achievement = innovationAchievementRepository.findById(achievementId);
        if (achievement.isEmpty()) {
            throw new RuntimeException("创新成果不存在");
        }
        return achievement.get();
    }

    /**
     * 删除创新成果
     */
    public void deleteAchievement(Integer achievementId) {
        if (!innovationAchievementRepository.existsById(achievementId)) {
            throw new RuntimeException("创新成果不存在");
        }
        innovationAchievementRepository.deleteById(achievementId);
    }

    /**
     * 查询所有创新成果
     */
    public List<InnovationAchievement> getAllAchievements() {
        return innovationAchievementRepository.findAll();
    }
}