package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.CourseSelection;
import cn.edu.sdu.java.server.models.Student;
import cn.edu.sdu.java.server.models.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * CourseSelectionRepository 选课信息数据访问接口
 * 提供选课信息的数据库操作方法
 */
@Repository
public interface CourseSelectionRepository extends JpaRepository<CourseSelection, Integer> {

    /**
     * 根据学生ID查询所有选课记录
     */
    List<CourseSelection> findByStudent(Student student);

    /**
     * 根据学生ID和状态查询选课记录
     */
    List<CourseSelection> findByStudentAndState(Student student, Integer state);

    /**
     * 根据课程ID查询所有选课学生
     */
    List<CourseSelection> findByCourse(Course course);

    /**
     * 根据学生ID和课程ID查询选课记录
     */
    Optional<CourseSelection> findByStudentAndCourse(Student student, Course course);

    /**
     * 查询指定学生已选的课程数量
     */
    @Query("SELECT COUNT(cs) FROM CourseSelection cs WHERE cs.student.personId = :studentId AND cs.state = 1")
    long countSelectedCourses(@Param("studentId") Integer studentId);

    /**
     * 查询指定课程的选课人数
     */
    @Query("SELECT COUNT(cs) FROM CourseSelection cs WHERE cs.course.courseId = :courseId AND cs.state = 1")
    long countStudentsByCourse(@Param("courseId") Integer courseId);

    /**
     * 查询学生未取消的选课记录
     */
    @Query("SELECT cs FROM CourseSelection cs WHERE cs.student.personId = :studentId AND cs.state != 2")
    List<CourseSelection> findActiveSelectionsByStudent(@Param("studentId") Integer studentId);
}