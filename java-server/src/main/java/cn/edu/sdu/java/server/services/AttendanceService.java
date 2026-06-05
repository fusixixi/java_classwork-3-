package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.Attendance;
import cn.edu.sdu.java.server.models.Student;
import cn.edu.sdu.java.server.models.Course;
import cn.edu.sdu.java.server.repositorys.AttendanceRepository;
import cn.edu.sdu.java.server.repositorys.StudentRepository;
import cn.edu.sdu.java.server.repositorys.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * AttendanceService 考勤服务类
 * 提供学生考勤管理的业务逻辑
 */
@Service
@Transactional
public class AttendanceService {
    private static final Set<String> VALID_ATTENDANCE_STATUS = Set.of("present", "absent", "late", "leave");

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    /**
     * 记录考勤
     */
    public Attendance recordAttendance(Integer studentId, Integer courseId, LocalDate date,
                                       String status, String remark) {
        validateStatus(status);
        Optional<Student> student = studentRepository.findById(studentId);
        Optional<Course> course = courseRepository.findById(courseId);

        if (student.isEmpty() || course.isEmpty()) {
            throw new RuntimeException("学生或课程不存在");
        }

        Attendance attendance = new Attendance();
        attendance.setStudent(student.get());
        attendance.setCourse(course.get());
        attendance.setAttendanceDate(date);
        attendance.setStatus(status);  // present-出勤，absent-缺勤，late-迟到，leave-请假
        attendance.setRemark(remark);
        attendance.setRecordTime(LocalDateTime.now());

        return attendanceRepository.save(attendance);
    }

    /**
     * 更新考勤状态
     */
    public void updateAttendanceStatus(Integer attendanceId, String status, String remark) {
        validateStatus(status);
        Optional<Attendance> attendance = attendanceRepository.findById(attendanceId);
        if (attendance.isEmpty()) {
            throw new RuntimeException("考勤记录不存在");
        }

        Attendance att = attendance.get();
        att.setStatus(status);
        att.setRemark(remark);
        attendanceRepository.save(att);
    }

    /**
     * 查询学生的所有考勤记录
     */
    public List<Attendance> getStudentAttendance(Integer studentId) {
        Optional<Student> student = studentRepository.findById(studentId);
        if (student.isEmpty()) {
            throw new RuntimeException("学生不存在");
        }
        return attendanceRepository.findByStudent(student.get());
    }

    /**
     * 查询学生在某课程的考勤记录
     */
    public List<Attendance> getCourseAttendance(Integer studentId, Integer courseId) {
        Optional<Student> student = studentRepository.findById(studentId);
        Optional<Course> course = courseRepository.findById(courseId);

        if (student.isEmpty() || course.isEmpty()) {
            throw new RuntimeException("学生或课程不存在");
        }

        return attendanceRepository.findByStudentAndCourse(student.get(), course.get());
    }

    /**
     * 计算学生在某课程的出勤率
     */
    public double calculateAttendanceRate(Integer studentId, Integer courseId) {
        long presentDays = attendanceRepository.countPresentDays(studentId, courseId);
        long totalClasses = attendanceRepository.countTotalClasses(studentId, courseId);

        if (totalClasses == 0) {
            return 0.0;
        }

        return (double) presentDays / totalClasses * 100;
    }

    /**
     * 查询学生的缺勤记录
     */
    public List<Attendance> getAbsentRecords(Integer studentId) {
        ensureStudentExists(studentId);
        return attendanceRepository.findAbsentRecords(studentId);
    }

    /**
     * 查询学生的迟到记录
     */
    public List<Attendance> getLateRecords(Integer studentId) {
        ensureStudentExists(studentId);
        return attendanceRepository.findLateRecords(studentId);
    }

    /**
     * 查询指定日期的所有考勤记录
     */
    public List<Attendance> getAttendanceByDate(LocalDate date) {
        return attendanceRepository.findByAttendanceDate(date);
    }

    /**
     * 获取考勤详情
     */
    public Attendance getAttendanceDetail(Integer attendanceId) {
        Optional<Attendance> attendance = attendanceRepository.findById(attendanceId);
        if (attendance.isEmpty()) {
            throw new RuntimeException("考勤记录不存在");
        }
        return attendance.get();
    }

    /**
     * 删除考勤记录
     */
    public void deleteAttendance(Integer attendanceId) {
        if (!attendanceRepository.existsById(attendanceId)) {
            throw new RuntimeException("考勤记录不存在");
        }
        attendanceRepository.deleteById(attendanceId);
    }

    /**
     * 查询所有考勤记录
     */
    public List<Attendance> getAllAttendance() {
        return attendanceRepository.findAll();
    }

    private void validateStatus(String status) {
        if (status == null || !VALID_ATTENDANCE_STATUS.contains(status)) {
            throw new RuntimeException("考勤状态无效");
        }
    }

    private void ensureStudentExists(Integer studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new RuntimeException("学生不存在");
        }
    }
}