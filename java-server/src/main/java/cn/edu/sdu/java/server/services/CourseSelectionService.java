package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.CourseSelection;
import cn.edu.sdu.java.server.models.Student;
import cn.edu.sdu.java.server.models.Course;
import cn.edu.sdu.java.server.repositorys.CourseSelectionRepository;
import cn.edu.sdu.java.server.repositorys.StudentRepository;
import cn.edu.sdu.java.server.repositorys.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * CourseSelectionService 选课服务类
 * 提供选课业务逻辑处理
 */
@Service
@Transactional
public class CourseSelectionService {

    @Autowired
    private CourseSelectionRepository courseSelectionRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    /**
     * 学生选课
     */
    public CourseSelection selectCourse(Integer studentId, Integer courseId) {
        Optional<Student> student = studentRepository.findById(studentId);
        Optional<Course> course = courseRepository.findById(courseId);

        if (student.isEmpty() || course.isEmpty()) {
            throw new RuntimeException("学生或课程不存在");
        }

        // 检查是否已选过该课程
        Optional<CourseSelection> existing = courseSelectionRepository
                .findByStudentAndCourse(student.get(), course.get());
        if (existing.isPresent() && existing.get().getState() == 1) {
            throw new RuntimeException("该学生已选过此课程");
        }

        CourseSelection selection = new CourseSelection();
        selection.setStudent(student.get());
        selection.setCourse(course.get());
        selection.setSelectTime(LocalDateTime.now());
        selection.setState(1);  // 1-已选

        return courseSelectionRepository.save(selection);
    }

    /**
     * 取消选课
     */
    public void cancelSelection(Integer selectionId) {
        Optional<CourseSelection> selection = courseSelectionRepository.findById(selectionId);
        if (selection.isEmpty()) {
            throw new RuntimeException("选课记录不存在");
        }

        CourseSelection cs = selection.get();
        cs.setState(2);  // 2-已取消
        cs.setCancelTime(LocalDateTime.now());
        courseSelectionRepository.save(cs);
    }

    /**
     * 查询学生的所有选课记录
     */
    public List<CourseSelection> getStudentSelections(Integer studentId) {
        Optional<Student> student = studentRepository.findById(studentId);
        if (student.isEmpty()) {
            throw new RuntimeException("学生不存在");
        }
        return courseSelectionRepository.findByStudent(student.get());
    }

    /**
     * 查询学生已选的课程（未取消）
     */
    public List<CourseSelection> getActiveSelections(Integer studentId) {
        return courseSelectionRepository.findActiveSelectionsByStudent(studentId);
    }

    /**
     * 查询学生已选的课程数量
     */
    public long getSelectedCourseCount(Integer studentId) {
        return courseSelectionRepository.countSelectedCourses(studentId);
    }

    /**
     * 查询课程的选课学生数量
     */
    public long getCourseStudentCount(Integer courseId) {
        return courseSelectionRepository.countStudentsByCourse(courseId);
    }

    /**
     * 获取选课详情
     */
    public CourseSelection getSelectionDetail(Integer selectionId) {
        Optional<CourseSelection> selection = courseSelectionRepository.findById(selectionId);
        if (selection.isEmpty()) {
            throw new RuntimeException("选课记录不存在");
        }
        return selection.get();
    }

    /**
     * 标记选课完成
     */
    public void completeSelection(Integer selectionId) {
        Optional<CourseSelection> selection = courseSelectionRepository.findById(selectionId);
        if (selection.isEmpty()) {
            throw new RuntimeException("选课记录不存在");
        }

        CourseSelection cs = selection.get();
        cs.setState(3);  // 3-已完成
        courseSelectionRepository.save(cs);
    }

    /**
     * 删除选课记录
     */
    public void deleteSelection(Integer selectionId) {
        courseSelectionRepository.deleteById(selectionId);
    }

    /**
     * 查询所有选课记录
     */
    public List<CourseSelection> getAllSelections() {
        return courseSelectionRepository.findAll();
    }
}