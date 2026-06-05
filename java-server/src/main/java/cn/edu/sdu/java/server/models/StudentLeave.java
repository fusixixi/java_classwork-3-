package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import java.util.Date;

@Entity
@Table(	name = "student_leave",
        uniqueConstraints = {
        })
public class StudentLeave {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer studentLeaveId;

    @ManyToOne
    @JoinColumn(name="studentId")
    private Student student;

    @ManyToOne
    @JoinColumn(name="teacherId")
    private Teacher teacher;

    @Size(max=50)
    private String leaveDate;
    
    @Size(max=100)
    private String reason;
    
    private Integer state;
    
    private Date applyTime;
    
    @Size(max=100)
    private String teacherComment;
    
    private Date teacherTime;
    
    @Size(max=100)
    private String adminComment;
    
    private Date adminTime;

    // Getters and Setters
    public Integer getStudentLeaveId() {
        return studentLeaveId;
    }

    public void setStudentLeaveId(Integer studentLeaveId) {
        this.studentLeaveId = studentLeaveId;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    public String getLeaveDate() {
        return leaveDate;
    }

    public void setLeaveDate(String leaveDate) {
        this.leaveDate = leaveDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Date getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(Date applyTime) {
        this.applyTime = applyTime;
    }

    public String getTeacherComment() {
        return teacherComment;
    }

    public void setTeacherComment(String teacherComment) {
        this.teacherComment = teacherComment;
    }

    public Date getTeacherTime() {
        return teacherTime;
    }

    public void setTeacherTime(Date teacherTime) {
        this.teacherTime = teacherTime;
    }

    public String getAdminComment() {
        return adminComment;
    }

    public void setAdminComment(String adminComment) {
        this.adminComment = adminComment;
    }

    public Date getAdminTime() {
        return adminTime;
    }

    public void setAdminTime(Date adminTime) {
        this.adminTime = adminTime;
    }
}
