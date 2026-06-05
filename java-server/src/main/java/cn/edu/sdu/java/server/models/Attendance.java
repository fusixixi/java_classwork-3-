package cn.edu.sdu.java.server.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Attendance 学生考勤表实体类
 * 用于记录学生的课程考勤信息
 *
 * Integer attendanceId - 考勤ID，主键，自增
 * Student student - 考勤学生，与Student表关联
 * Course course - 考勤课程，与Course表关联
 * LocalDate attendanceDate - 考勤日期
 * String status - 考勤状态：present-出勤，absent-缺勤，late-迟到，leave-请假
 * String remark - 备注
 * LocalDateTime recordTime - 记录时间
 */
@Entity
@Table(name = "attendance")
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer attendanceId;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "student_id")
    @JsonIgnore
    private Student student;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "course_id")
    @JsonIgnore
    private Course course;

    @NotNull
    @Column(name = "attendance_date")
    private LocalDate attendanceDate;

    @Size(max = 20)
    @Column(name = "status")
    private String status;  // present-出勤，absent-缺勤，late-迟到，leave-请假

    @Size(max = 100)
    @Column(name = "remark")
    private String remark;

    @Column(name = "record_time")
    private LocalDateTime recordTime;

    // Getters and Setters
    public Integer getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(Integer attendanceId) {
        this.attendanceId = attendanceId;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(LocalDateTime recordTime) {
        this.recordTime = recordTime;
    }
}
