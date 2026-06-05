package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.ScoreSubmission;
import cn.edu.sdu.java.server.models.Score;
import cn.edu.sdu.java.server.models.Teacher;
import cn.edu.sdu.java.server.models.Student;
import cn.edu.sdu.java.server.models.Course;
import cn.edu.sdu.java.server.repositorys.ScoreSubmissionRepository;
import cn.edu.sdu.java.server.repositorys.ScoreRepository;
import cn.edu.sdu.java.server.repositorys.TeacherRepository;
import cn.edu.sdu.java.server.repositorys.StudentRepository;
import cn.edu.sdu.java.server.repositorys.CourseRepository;
import cn.edu.sdu.java.server.repositorys.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * ScoreSubmissionService 成绩提交审核服务类
 * 提供成绩提交和审核的业务逻辑
 */
@Service
@Transactional
public class ScoreSubmissionService {
    private static final int MIN_SCORE = 0;
    private static final int MAX_SCORE = 100;
    private static final int STATE_PENDING = 1;
    private static final int STATE_APPROVED = 3;
    private static final int STATE_REJECTED = 4;
    private static final int STATE_PUBLISHED = 5;
    private static final Set<Integer> VALID_APPROVAL_STATES = Set.of(STATE_APPROVED, STATE_REJECTED, STATE_PUBLISHED);
    private static final Set<Integer> APPROVABLE_SOURCE_STATES = Set.of(STATE_PENDING, STATE_APPROVED);

    @Autowired
    private ScoreSubmissionRepository scoreSubmissionRepository;

    @Autowired
    private ScoreRepository scoreRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private PersonRepository personRepository;

    /**
     * 教师提交成绩
     */
    public ScoreSubmission submitScore(Integer scoreId, Integer teacherId,
                                       Integer studentId, Integer courseId,
                                       Integer submittedScore) {
        validateScore(submittedScore);
        Optional<Score> score = scoreRepository.findById(scoreId);
        Optional<Teacher> teacher = teacherRepository.findById(teacherId);
        Optional<Student> student = studentRepository.findById(studentId);
        Optional<Course> course = courseRepository.findById(courseId);

        if (score.isEmpty() || teacher.isEmpty() || student.isEmpty() || course.isEmpty()) {
            throw new RuntimeException("成绩、教师、学生或课程不存在");
        }
        if (score.get().getStudent() == null || score.get().getCourse() == null) {
            throw new RuntimeException("成绩记录数据不完整");
        }
        if (!score.get().getStudent().getPersonId().equals(studentId) || !score.get().getCourse().getCourseId().equals(courseId)) {
            throw new RuntimeException("成绩记录与学生或课程不匹配");
        }

        ScoreSubmission submission = new ScoreSubmission();
        submission.setScore(score.get());
        submission.setTeacher(teacher.get());
        submission.setStudent(student.get());
        submission.setCourse(course.get());
        submission.setSubmittedScore(submittedScore);
        submission.setSubmissionTime(LocalDateTime.now());
        submission.setState(STATE_PENDING);  // 1-待审批

        return scoreSubmissionRepository.save(submission);
    }

    /**
     * 审批成绩
     */
    public void approveScore(Integer submissionId, Integer newState, String comment, Integer approverId) {
        if (newState == null || !VALID_APPROVAL_STATES.contains(newState)) {
            throw new RuntimeException("审批状态无效");
        }
        if (approverId == null || !personRepository.existsById(approverId)) {
            throw new RuntimeException("审批人不存在");
        }
        Optional<ScoreSubmission> submission = scoreSubmissionRepository.findById(submissionId);
        if (submission.isEmpty()) {
            throw new RuntimeException("成绩提交记录不存在");
        }

        ScoreSubmission ss = submission.get();
        if (!APPROVABLE_SOURCE_STATES.contains(ss.getState())) {
            throw new RuntimeException("当前状态不允许审批");
        }
        if (newState == STATE_PUBLISHED && ss.getState() != STATE_APPROVED) {
            throw new RuntimeException("仅已通过记录可以发布");
        }
        ss.setState(newState);  // 2-审批中，3-已通过，4-已拒绝，5-已发布
        ss.setApprovalComment(comment);
        ss.setApprovalTime(LocalDateTime.now());

        if (newState == STATE_PUBLISHED) {  // 已发布
            ss.setPublishTime(LocalDateTime.now());
            // 更新score表的成绩
            Score score = ss.getScore();
            score.setMark(ss.getSubmittedScore());
            scoreRepository.save(score);
        }

        scoreSubmissionRepository.save(ss);
    }

    /**
     * 查询教师的成绩提交记录
     */
    public List<ScoreSubmission> getTeacherSubmissions(Integer teacherId) {
        Optional<Teacher> teacher = teacherRepository.findById(teacherId);
        if (teacher.isEmpty()) {
            throw new RuntimeException("教师不存在");
        }
        return scoreSubmissionRepository.findByTeacher(teacher.get());
    }

    /**
     * 查询教师待审批的成绩
     */
    public List<ScoreSubmission> getPendingSubmissionsByTeacher(Integer teacherId) {
        return scoreSubmissionRepository.findPendingSubmissionsByTeacher(teacherId);
    }

    /**
     * 查询所有待审批的成绩
     */
    public List<ScoreSubmission> getPendingApprovals() {
        return scoreSubmissionRepository.findPendingApprovals(STATE_PENDING);
    }

    /**
     * 查询已发布的成绩
     */
    public List<ScoreSubmission> getPublishedScores() {
        return scoreSubmissionRepository.findPublishedScores();
    }

    /**
     * 查询学生的成绩提交记录
     */
    public List<ScoreSubmission> getStudentScores(Integer studentId) {
        return scoreSubmissionRepository.findByStudentId(studentId);
    }

    /**
     * 查询学生未发布的成绩
     */
    public List<ScoreSubmission> getUnpublishedScores(Integer studentId) {
        return scoreSubmissionRepository.findUnpublishedByStudent(studentId);
    }

    /**
     * 查询拒绝的成绩提交
     */
    public List<ScoreSubmission> getRejectedSubmissions() {
        return scoreSubmissionRepository.findRejectedSubmissions();
    }

    /**
     * 统计教师待审批的成绩数量
     */
    public long countPendingByTeacher(Integer teacherId) {
        return scoreSubmissionRepository.countPendingByTeacher(teacherId);
    }

    /**
     * 获取成绩提交详情
     */
    public ScoreSubmission getSubmissionDetail(Integer submissionId) {
        Optional<ScoreSubmission> submission = scoreSubmissionRepository.findById(submissionId);
        if (submission.isEmpty()) {
            throw new RuntimeException("成绩提交记录不存在");
        }
        return submission.get();
    }

    /**
     * 删除成绩提交记录
     */
    public void deleteSubmission(Integer submissionId) {
        if (!scoreSubmissionRepository.existsById(submissionId)) {
            throw new RuntimeException("成绩提交记录不存在");
        }
        scoreSubmissionRepository.deleteById(submissionId);
    }

    /**
     * 查询所有成绩提交记录
     */
    public List<ScoreSubmission> getAllSubmissions() {
        return scoreSubmissionRepository.findAll();
    }

    private void validateScore(Integer submittedScore) {
        if (submittedScore == null || submittedScore < MIN_SCORE || submittedScore > MAX_SCORE) {
            throw new RuntimeException("成绩范围必须在" + MIN_SCORE + "到" + MAX_SCORE + "之间");
        }
    }
}