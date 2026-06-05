package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.models.Course;
import cn.edu.sdu.java.server.models.CourseSelection;
import cn.edu.sdu.java.server.models.Student;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.CourseRepository;
import cn.edu.sdu.java.server.repositorys.StudentRepository;
import cn.edu.sdu.java.server.services.CourseSelectionService;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * CourseSelectionController 选课管理控制器
 * 处理与选课相关的HTTP请求
 */
@RestController
@RequestMapping("/api/courseSelection")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CourseSelectionController {

    @Autowired
    private CourseSelectionService courseSelectionService;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private CourseRepository courseRepository;

    /**
     * 学生选课
     * POST /api/courseSelection/select
     * 参数：studentId, courseId
     */
    @PostMapping("/select")
    public ResponseEntity<?> selectCourse(@RequestParam(required = false) Integer studentId,
                                          @RequestParam(required = false) Integer courseId,
                                          @RequestBody(required = false) DataRequest dataRequest) {
        try {
            if (dataRequest != null) {
                if (studentId == null) {
                    studentId = dataRequest.getInteger("studentId");
                }
                if (courseId == null) {
                    courseId = dataRequest.getInteger("courseId");
                }
                if (studentId == null) {
                    String studentNum = dataRequest.getString("studentNum");
                    if (studentNum != null && !studentNum.isEmpty()) {
                        Optional<Student> student = studentRepository.findByPersonNum(studentNum);
                        if (student.isPresent()) {
                            studentId = student.get().getPersonId();
                        }
                    }
                }
                if (courseId == null) {
                    String courseNum = dataRequest.getString("courseNum");
                    if (courseNum != null && !courseNum.isEmpty()) {
                        Optional<Course> course = courseRepository.findByNum(courseNum);
                        if (course.isPresent()) {
                            courseId = course.get().getCourseId();
                        }
                    }
                }
            }
            if (studentId == null || courseId == null) {
                return ResponseUtil.error("缺少必要参数");
            }
            CourseSelection selection = courseSelectionService.selectCourse(studentId, courseId);
            return ResponseUtil.success("选课成功", selection);
        } catch (Exception e) {
            return ResponseUtil.error("选课失败：" + e.getMessage());
        }
    }

    /**
     * 取消选课
     * POST /api/courseSelection/cancel
     * 参数：selectionId
     */
    @PostMapping("/cancel")
    public ResponseEntity<?> cancelSelection(@RequestParam Integer selectionId) {
        try {
            courseSelectionService.cancelSelection(selectionId);
            return ResponseUtil.success("取消选课成功", null);
        } catch (Exception e) {
            return ResponseUtil.error("取消选课失败：" + e.getMessage());
        }
    }

    /**
     * 查询学生的所有选课记录
     * GET /api/courseSelection/student/{studentId}
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> getStudentSelections(@PathVariable Integer studentId) {
        try {
            List<CourseSelection> selections = courseSelectionService.getStudentSelections(studentId);
            return ResponseUtil.success("查询成功", selections);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询学生已选的课程（未取消）
     * GET /api/courseSelection/active/{studentId}
     */
    @GetMapping("/active/{studentId}")
    public ResponseEntity<?> getActiveSelections(@PathVariable Integer studentId) {
        try {
            List<CourseSelection> selections = courseSelectionService.getActiveSelections(studentId);
            return ResponseUtil.success("查询成功", selections);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询学生已选的课程数量
     * GET /api/courseSelection/count/{studentId}
     */
    @GetMapping("/count/{studentId}")
    public ResponseEntity<?> getSelectedCourseCount(@PathVariable Integer studentId) {
        try {
            long count = courseSelectionService.getSelectedCourseCount(studentId);
            return ResponseUtil.success("查询成功", count);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询课程的选课学生数量
     * GET /api/courseSelection/course/count/{courseId}
     */
    @GetMapping("/course/count/{courseId}")
    public ResponseEntity<?> getCourseStudentCount(@PathVariable Integer courseId) {
        try {
            long count = courseSelectionService.getCourseStudentCount(courseId);
            return ResponseUtil.success("查询成功", count);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取选课详情
     * GET /api/courseSelection/{selectionId}
     */
    @GetMapping("/{selectionId}")
    public ResponseEntity<?> getSelectionDetail(@PathVariable Integer selectionId) {
        try {
            CourseSelection selection = courseSelectionService.getSelectionDetail(selectionId);
            return ResponseUtil.success("查询成功", selection);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 标记选课完成
     * POST /api/courseSelection/complete
     * 参数：selectionId
     */
    @PostMapping("/complete")
    public ResponseEntity<?> completeSelection(@RequestParam Integer selectionId) {
        try {
            courseSelectionService.completeSelection(selectionId);
            return ResponseUtil.success("标记完成成功", null);
        } catch (Exception e) {
            return ResponseUtil.error("标记完成失败：" + e.getMessage());
        }
    }

    /**
     * 删除选课记录
     * DELETE /api/courseSelection/{selectionId}
     */
    @DeleteMapping("/{selectionId}")
    public ResponseEntity<?> deleteSelection(@PathVariable Integer selectionId) {
        try {
            courseSelectionService.deleteSelection(selectionId);
            return ResponseUtil.success("删除成功", null);
        } catch (Exception e) {
            return ResponseUtil.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 查询所有选课记录
     * GET /api/courseSelection/all
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllSelections() {
        try {
            List<CourseSelection> selections = courseSelectionService.getAllSelections();
            return ResponseUtil.success("查询成功", selections);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    @PostMapping("/all")
    public DataResponse getAllSelectionsPost(@RequestBody(required = false) DataRequest dataRequest) {
        try {
            String studentNum = dataRequest == null ? null : dataRequest.getString("studentNum");
            String studentName = dataRequest == null ? null : dataRequest.getString("studentName");
            List<CourseSelection> selections = courseSelectionService.getAllSelections();
            List<Map<String,Object>> dataList = new ArrayList<>();
            for (CourseSelection selection : selections) {
                if (selection.getStudent() == null || selection.getStudent().getPerson() == null || selection.getCourse() == null) {
                    continue;
                }
                if (studentNum != null && !studentNum.isBlank() && !selection.getStudent().getPerson().getNum().contains(studentNum)) {
                    continue;
                }
                if (studentName != null && !studentName.isBlank() && !selection.getStudent().getPerson().getName().contains(studentName)) {
                    continue;
                }
                dataList.add(toSelectionMap(selection));
            }
            return CommonMethod.getReturnData(dataList);
        } catch (Exception e) {
            return CommonMethod.getReturnMessageError("查询失败：" + e.getMessage());
        }
    }

    private Map<String,Object> toSelectionMap(CourseSelection selection) {
        Map<String,Object> map = new HashMap<>();
        map.put("selectionId", selection.getSelectionId());
        map.put("studentNum", selection.getStudent().getPerson().getNum());
        map.put("studentName", selection.getStudent().getPerson().getName());
        map.put("courseNum", selection.getCourse().getNum());
        map.put("courseName", selection.getCourse().getName());
        map.put("credit", selection.getCourse().getCredit());
        map.put("selectTime", selection.getSelectTime() == null ? "" : selection.getSelectTime().toString().replace('T',' '));
        map.put("state", selection.getState());
        map.put("stateName", getSelectionStateName(selection.getState()));
        return map;
    }

    private String getSelectionStateName(Integer state) {
        if (state == null) {
            return "";
        }
        if (state == 1) {
            return "已选";
        }
        if (state == 2) {
            return "已取消";
        }
        if (state == 3) {
            return "已完成";
        }
        return state.toString();
    }
}