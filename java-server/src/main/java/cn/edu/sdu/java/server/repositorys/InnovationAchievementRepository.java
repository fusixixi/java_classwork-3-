package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.InnovationAchievement;
import cn.edu.sdu.java.server.models.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * InnovationAchievementRepository 创新成果数据访问接口
 * 提供学生创新成果的数据库操作方法
 */
@Repository
public interface InnovationAchievementRepository extends JpaRepository<InnovationAchievement, Integer> {

    /**
     * 根据学生ID查询创新成果
     */
    List<InnovationAchievement> findByStudent(Student student);

    /**
     * 根据学生ID和审批状态查询创新成果
     */
    List<InnovationAchievement> findByStudentAndState(Student student, Integer state);

    /**
     * 按类型查询所有创新成果
     */
    List<InnovationAchievement> findByCategory(String category);

    /**
     * 按审批状态查询创新成果
     */
    List<InnovationAchievement> findByState(Integer state);

    /**
     * 查询待审批的创新成果
     */
    @Query("SELECT ia FROM InnovationAchievement ia WHERE ia.state = 1 ORDER BY ia.submitTime DESC")
    List<InnovationAchievement> findPendingAchievements();

    /**
     * 查询已通过的创新成果
     */
    @Query("SELECT ia FROM InnovationAchievement ia WHERE ia.state = 3 ORDER BY ia.achievementDate DESC")
    List<InnovationAchievement> findApprovedAchievements();

    /**
     * 按标题模糊查询
     */
    @Query("SELECT ia FROM InnovationAchievement ia WHERE ia.title LIKE %:keyword%")
    List<InnovationAchievement> findByTitleContaining(@Param("keyword") String keyword);

    /**
     * 查询学生指定类型的创新成果
     */
    List<InnovationAchievement> findByStudentAndCategory(Student student, String category);

    /**
     * 统计学生已通过的创新成果数量
     */
    @Query("SELECT COUNT(ia) FROM InnovationAchievement ia WHERE ia.student.personId = :studentId AND ia.state = 3")
    long countApprovedByStudent(@Param("studentId") Integer studentId);

    /**
     * 统计指定类型的创新成果总数
     */
    @Query("SELECT COUNT(ia) FROM InnovationAchievement ia WHERE ia.category = :category AND ia.state = 3")
    long countByCategory(@Param("category") String category);
}