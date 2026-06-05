package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

@Entity
@Table(	name = "student_statistics",
        uniqueConstraints = {
        })
public class StudentStatistics implements Comparable<StudentStatistics> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer statisticsId;

    @ManyToOne
    @JoinColumn(name="personId")
    private Student student;
    
    @Size(max=4)
    private String year;
    
    private Integer courseCount;
    
    private Double avgScore;
    
    private Double gpa;
    
    private Integer leaveCount;
    
    private Integer no;

    @Override
    public int compareTo(StudentStatistics o) {
        return o.gpa.compareTo(gpa);
    }

    // Getters and Setters
    public Integer getStatisticsId() {
        return statisticsId;
    }

    public void setStatisticsId(Integer statisticsId) {
        this.statisticsId = statisticsId;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public Integer getCourseCount() {
        return courseCount;
    }

    public void setCourseCount(Integer courseCount) {
        this.courseCount = courseCount;
    }

    public void setCourseCount(int courseCount) {
        this.courseCount = courseCount;
    }

    public Double getAvgScore() {
        return avgScore;
    }

    public void setAvgScore(Double avgScore) {
        this.avgScore = avgScore;
    }

    public void setAvgScore(double avgScore) {
        this.avgScore = avgScore;
    }

    public Double getGpa() {
        return gpa;
    }

    public void setGpa(Double gpa) {
        this.gpa = gpa;
    }

    public void setGpa(double gpa) {
        this.gpa = gpa;
    }

    public Integer getLeaveCount() {
        return leaveCount;
    }

    public void setLeaveCount(Integer leaveCount) {
        this.leaveCount = leaveCount;
    }

    public void setLeaveCount(int leaveCount) {
        this.leaveCount = leaveCount;
    }

    public Integer getNo() {
        return no;
    }

    public void setNo(Integer no) {
        this.no = no;
    }

    public void setNo(int no) {
        this.no = no;
    }
}
