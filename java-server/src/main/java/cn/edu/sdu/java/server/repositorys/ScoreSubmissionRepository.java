package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.ScoreSubmission;
import cn.edu.sdu.java.server.models.Teacher;
import cn.edu.sdu.java.server.models.Student;
import cn.edu.sdu.java.server.models.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ScoreSubmissionRepository 成绩提交审核数据访问接口
 * 提供成绩提交和审核的数据库操作方法
 */
@Repository
public interface ScoreSubmissionRepository extends JpaRepository<ScoreSubmission, Integer> {

    /**
     * 根据教师ID查询成绩提交记录
     */
    List<ScoreSubmission> findByTeacher(Teacher teacher);

    /**
     * 根据学生ID查询成绩提交记录
     */
    @Query("SELECT ss FROM ScoreSubmission ss WHERE ss.student.personId = :studentId")
    List<ScoreSubmission> findByStudentId(@Param("studentId") Integer studentId);

    /**
     * 根据课程ID查询成绩提交记录
     */
    List<ScoreSubmission> findByCourse(Course course);

    /**
     * 根据审批状态查询成绩提交记录
     */
    List<ScoreSubmission> findByState(Integer state);

    /**
     * 查询教师待提交的成绩
     */
    @Query("SELECT ss FROM ScoreSubmission ss WHERE ss.teacher.personId = :teacherId " +
            "AND ss.state = 1 ORDER BY ss.submissionTime DESC")
    List<ScoreSubmission> findPendingSubmissionsByTeacher(@Param("teacherId") Integer teacherId);

    /**
     * 查询待审批的成绩
     */
    @Query("SELECT ss FROM ScoreSubmission ss WHERE ss.state = :state ORDER BY ss.submissionTime DESC")
    List<ScoreSubmission> findPendingApprovals(@Param("state") Integer state);

    /**
     * 查询已发布的成绩
     */
    @Query("SELECT ss FROM ScoreSubmission ss WHERE ss.state = 5")
    List<ScoreSubmission> findPublishedScores();

    /**
     * 根据教师和课程查询成绩提交
     */
    List<ScoreSubmission> findByTeacherAndCourse(Teacher teacher, Course course);

    /**
     * 根据学生和课程查询成绩提交
     */
    @Query("SELECT ss FROM ScoreSubmission ss WHERE ss.student.personId = :studentId " +
            "AND ss.course.courseId = :courseId")
    Optional<ScoreSubmission> findByStudentAndCourse(@Param("studentId") Integer studentId,
                                                     @Param("courseId") Integer courseId);

    /**
     * 统计教师的待审批成绩数量
     */
    @Query("SELECT COUNT(ss) FROM ScoreSubmission ss WHERE ss.teacher.personId = :teacherId AND ss.state = 1")
    long countPendingByTeacher(@Param("teacherId") Integer teacherId);

    /**
     * 查询拒绝的成绩提交
     */
    @Query("SELECT ss FROM ScoreSubmission ss WHERE ss.state = 4 ORDER BY ss.approvalTime DESC")
    List<ScoreSubmission> findRejectedSubmissions();

    /**
     * 查询指定学生未发布的成绩
     */
    @Query("SELECT ss FROM ScoreSubmission ss WHERE ss.student.personId = :studentId " +
            "AND ss.state != 5")
    List<ScoreSubmission> findUnpublishedByStudent(@Param("studentId") Integer studentId);
}