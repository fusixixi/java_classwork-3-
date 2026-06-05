package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.Attendance;
import cn.edu.sdu.java.server.models.Student;
import cn.edu.sdu.java.server.models.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * AttendanceRepository 考勤信息数据访问接口
 * 提供学生考勤信息的数据库操作方法
 */
@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {

    /**
     * 根据学生ID查询考勤记录
     */
    List<Attendance> findByStudent(Student student);

    /**
     * 根据学生ID和课程ID查询考勤记录
     */
    List<Attendance> findByStudentAndCourse(Student student, Course course);

    /**
     * 根据课程ID查询所有考勤记录
     */
    List<Attendance> findByCourse(Course course);

    /**
     * 查询指定学生在指定课程的出勤率
     */
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.personId = :studentId " +
            "AND a.course.courseId = :courseId AND a.status = 'present'")
    long countPresentDays(@Param("studentId") Integer studentId, @Param("courseId") Integer courseId);

    /**
     * 查询指定学生在指定课程的总上课次数
     */
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.personId = :studentId " +
            "AND a.course.courseId = :courseId")
    long countTotalClasses(@Param("studentId") Integer studentId, @Param("courseId") Integer courseId);

    /**
     * 查询指定日期的考勤记录
     */
    List<Attendance> findByAttendanceDate(LocalDate date);

    /**
     * 根据学生ID和状态查询考勤记录
     */
    List<Attendance> findByStudentAndStatus(Student student, String status);

    /**
     * 查询学生缺勤记录
     */
    @Query("SELECT a FROM Attendance a WHERE a.student.personId = :studentId AND a.status = 'absent'")
    List<Attendance> findAbsentRecords(@Param("studentId") Integer studentId);

    /**
     * 查询学生迟到记录
     */
    @Query("SELECT a FROM Attendance a WHERE a.student.personId = :studentId AND a.status = 'late'")
    List<Attendance> findLateRecords(@Param("studentId") Integer studentId);
}