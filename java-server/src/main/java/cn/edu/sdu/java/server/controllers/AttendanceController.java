package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.models.Attendance;
import cn.edu.sdu.java.server.models.Course;
import cn.edu.sdu.java.server.models.Student;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.CourseRepository;
import cn.edu.sdu.java.server.repositorys.StudentRepository;
import cn.edu.sdu.java.server.services.AttendanceService;
import cn.edu.sdu.java.server.util.ResponseUtil;
import cn.edu.sdu.java.server.util.CommonMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * AttendanceController 考勤管理控制器
 * 处理与考勤相关的HTTP请求
 */
@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private CourseRepository courseRepository;

    /**
     * 记录考勤
     * POST /api/attendance/record
     */
    @PostMapping("/record")
    public ResponseEntity<?> recordAttendance(
            @RequestParam(required = false) Integer studentId,
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String remark,
            @RequestBody(required = false) DataRequest dataRequest) {
        try {
            if (dataRequest != null) {
                if (studentId == null) {
                    studentId = dataRequest.getInteger("studentId");
                }
                if (courseId == null) {
                    courseId = dataRequest.getInteger("courseId");
                }
                if (date == null || date.isEmpty()) {
                    date = dataRequest.getString("date");
                }
                if (date == null || date.isEmpty()) {
                    date = dataRequest.getString("attendanceDate");
                }
                if (status == null || status.isEmpty()) {
                    status = dataRequest.getString("status");
                }
                if (remark == null) {
                    remark = dataRequest.getString("remark");
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
                if (courseId == null) {
                    String courseName = dataRequest.getString("courseName");
                    if (courseName != null && !courseName.isEmpty()) {
                        List<Course> courseList = courseRepository.findByName(courseName);
                        if (courseList.size() > 1) {
                            return ResponseUtil.error("存在同名课程，请改用课程编号");
                        }
                        if (courseList.size() == 1) {
                            courseId = courseList.get(0).getCourseId();
                        }
                    }
                }
            }
            if (studentId == null || courseId == null || date == null || status == null) {
                return ResponseUtil.error("缺少必要参数");
            }
            LocalDate attendanceDate = LocalDate.parse(date);
            Attendance attendance = attendanceService.recordAttendance(studentId, courseId, attendanceDate, status, remark);
            return ResponseUtil.success("考勤记录成功", attendance);
        } catch (Exception e) {
            return ResponseUtil.error("考勤记录失败：" + e.getMessage());
        }
    }

    /**
     * 更新考勤状态
     * PUT /api/attendance/{attendanceId}
     */
    @PutMapping("/{attendanceId}")
    public ResponseEntity<?> updateAttendanceStatus(
            @PathVariable Integer attendanceId,
            @RequestParam String status,
            @RequestParam(required = false) String remark) {
        try {
            attendanceService.updateAttendanceStatus(attendanceId, status, remark);
            return ResponseUtil.success("更新成功", null);
        } catch (Exception e) {
            return ResponseUtil.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 查询学生的所有考勤记录
     * GET /api/attendance/student/{studentId}
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> getStudentAttendance(@PathVariable Integer studentId) {
        try {
            List<Attendance> attendances = attendanceService.getStudentAttendance(studentId);
            return ResponseUtil.success("查询成功", attendances);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询学生在某课程的考勤记录
     * GET /api/attendance/course/{studentId}/{courseId}
     */
    @GetMapping("/course/{studentId}/{courseId}")
    public ResponseEntity<?> getCourseAttendance(
            @PathVariable Integer studentId,
            @PathVariable Integer courseId) {
        try {
            List<Attendance> attendances = attendanceService.getCourseAttendance(studentId, courseId);
            return ResponseUtil.success("查询成功", attendances);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 计算学生在某课程的出勤率
     * GET /api/attendance/rate/{studentId}/{courseId}
     */
    @GetMapping("/rate/{studentId}/{courseId}")
    public ResponseEntity<?> calculateAttendanceRate(
            @PathVariable Integer studentId,
            @PathVariable Integer courseId) {
        try {
            double rate = attendanceService.calculateAttendanceRate(studentId, courseId);
            return ResponseUtil.success("查询成功", rate);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询学生的缺勤记录
     * GET /api/attendance/absent/{studentId}
     */
    @GetMapping("/absent/{studentId}")
    public ResponseEntity<?> getAbsentRecords(@PathVariable Integer studentId) {
        try {
            List<Attendance> attendances = attendanceService.getAbsentRecords(studentId);
            return ResponseUtil.success("查询成功", attendances);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询学生的迟到记录
     * GET /api/attendance/late/{studentId}
     */
    @GetMapping("/late/{studentId}")
    public ResponseEntity<?> getLateRecords(@PathVariable Integer studentId) {
        try {
            List<Attendance> attendances = attendanceService.getLateRecords(studentId);
            return ResponseUtil.success("查询成功", attendances);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询指定日期的所有考勤记录
     * GET /api/attendance/date/{date}
     */
    @GetMapping("/date/{date}")
    public ResponseEntity<?> getAttendanceByDate(@PathVariable String date) {
        try {
            LocalDate attendanceDate = LocalDate.parse(date);
            List<Attendance> attendances = attendanceService.getAttendanceByDate(attendanceDate);
            return ResponseUtil.success("查询成功", attendances);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取考勤详情
     * GET /api/attendance/{attendanceId}
     */
    @GetMapping("/{attendanceId}")
    public ResponseEntity<?> getAttendanceDetail(@PathVariable Integer attendanceId) {
        try {
            Attendance attendance = attendanceService.getAttendanceDetail(attendanceId);
            return ResponseUtil.success("查询成功", attendance);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 删除考勤记录
     * DELETE /api/attendance/{attendanceId}
     */
    @DeleteMapping("/{attendanceId}")
    public ResponseEntity<?> deleteAttendance(@PathVariable Integer attendanceId) {
        try {
            attendanceService.deleteAttendance(attendanceId);
            return ResponseUtil.success("删除成功", null);
        } catch (Exception e) {
            return ResponseUtil.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 查询所有考勤记录
     * GET /api/attendance/all
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllAttendance() {
        try {
            List<Attendance> attendances = attendanceService.getAllAttendance();
            return ResponseUtil.success("查询成功", attendances);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    @PostMapping("/all")
    public DataResponse getAllAttendancePost(@RequestBody(required = false) DataRequest dataRequest) {
        try {
            String studentNum = dataRequest == null ? null : dataRequest.getString("studentNum");
            String studentName = dataRequest == null ? null : dataRequest.getString("studentName");
            String courseName = dataRequest == null ? null : dataRequest.getString("courseName");
            List<Attendance> attendances = attendanceService.getAllAttendance();
            List<Map<String,Object>> dataList = new ArrayList<>();
            for (Attendance attendance : attendances) {
                if (attendance.getStudent() == null || attendance.getStudent().getPerson() == null || attendance.getCourse() == null) {
                    continue;
                }
                if (studentNum != null && !studentNum.isBlank() && !attendance.getStudent().getPerson().getNum().contains(studentNum)) {
                    continue;
                }
                if (studentName != null && !studentName.isBlank() && !attendance.getStudent().getPerson().getName().contains(studentName)) {
                    continue;
                }
                if (courseName != null && !courseName.isBlank() && !attendance.getCourse().getName().contains(courseName)) {
                    continue;
                }
                dataList.add(toAttendanceMap(attendance));
            }
            return CommonMethod.getReturnData(dataList);
        } catch (Exception e) {
            return CommonMethod.getReturnMessageError("查询失败：" + e.getMessage());
        }
    }

    private Map<String,Object> toAttendanceMap(Attendance attendance) {
        Map<String,Object> map = new HashMap<>();
        map.put("attendanceId", attendance.getAttendanceId());
        map.put("studentNum", attendance.getStudent().getPerson().getNum());
        map.put("studentName", attendance.getStudent().getPerson().getName());
        map.put("courseName", attendance.getCourse().getName());
        map.put("attendanceDate", attendance.getAttendanceDate() == null ? "" : attendance.getAttendanceDate().toString());
        map.put("status", attendance.getStatus());
        map.put("statusName", getAttendanceStatusName(attendance.getStatus()));
        map.put("remark", attendance.getRemark());
        map.put("recordTime", attendance.getRecordTime() == null ? "" : attendance.getRecordTime().toString().replace('T',' '));
        return map;
    }

    private String getAttendanceStatusName(String status) {
        if ("present".equals(status)) {
            return "出勤";
        }
        if ("absent".equals(status)) {
            return "缺勤";
        }
        if ("late".equals(status)) {
            return "迟到";
        }
        if ("leave".equals(status)) {
            return "请假";
        }
        return status;
    }
}