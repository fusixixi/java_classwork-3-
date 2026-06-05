package cn.edu.sdu.java.server.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * CourseSelection 学生选课表实体类
 * 用于管理学生的选课信息，包括选课时间、选课状态等
 *
 * Integer selectionId - 选课ID，主键，自增
 * Student student - 选课学生，与Student表关联
 * Course course - 选课课程，与Course表关联
 * LocalDateTime selectTime - 选课时间
 * Integer state - 选课状态：1-已选，2-已取消，3-已完成
 * LocalDateTime cancelTime - 取消选课时间
 */
@Entity
@Table(name = "course_selection")
public class CourseSelection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer selectionId;

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

    @Column(name = "select_time")
    private LocalDateTime selectTime;

    @Column(name = "state")
    private Integer state;  // 1-已选，2-已取消，3-已完成

    @Column(name = "cancel_time")
    private LocalDateTime cancelTime;

    // Getters and Setters
    public Integer getSelectionId() {
        return selectionId;
    }

    public void setSelectionId(Integer selectionId) {
        this.selectionId = selectionId;
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

    public LocalDateTime getSelectTime() {
        return selectTime;
    }

    public void setSelectTime(LocalDateTime selectTime) {
        this.selectTime = selectTime;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public LocalDateTime getCancelTime() {
        return cancelTime;
    }

    public void setCancelTime(LocalDateTime cancelTime) {
        this.cancelTime = cancelTime;
    }
}
