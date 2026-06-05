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
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

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
    private static final String ROLE_STUDENT = "ROLE_STUDENT";
    private static final Integer[] EXPORT_COLUMN_WIDTHS = {8, 12, 12, 16, 14, 10, 24, 20};
    private static final String[] EXPORT_TITLES = {"序号", "学生学号", "学生姓名", "课程名称", "考勤日期", "状态", "备注", "记录时间"};

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
            if (isStudentRole()) {
                return ResponseUtil.error("学生端仅可查看自己的考勤信息，不能修改");
            }
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
            if (isStudentRole()) {
                return ResponseUtil.error("学生端仅可查看自己的考勤信息，不能修改");
            }
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
            if (isStudentRole() && !studentId.equals(CommonMethod.getPersonId())) {
                return ResponseUtil.error("学生端仅可查看自己的考勤信息");
            }
            double rate = attendanceService.calculateAttendanceRate(studentId, courseId);
            return ResponseUtil.success("查询成功", rate);
        } catch (Exception e) {
            return ResponseUtil.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 计算学生在某课程的出勤率（支持前端POST请求）
     * POST /api/attendance/rate
     */
    @PostMapping("/rate")
    public DataResponse calculateAttendanceRatePost(@RequestBody(required = false) DataRequest dataRequest) {
        try {
            if (dataRequest == null) {
                return CommonMethod.getReturnMessageError("缺少必要参数");
            }
            Integer studentId = resolveStudentId(dataRequest);
            Integer courseId = resolveCourseId(dataRequest);
            if (isStudentRole()) {
                Integer currentPersonId = CommonMethod.getPersonId();
                if (studentId == null) {
                    studentId = currentPersonId;
                } else if (!studentId.equals(currentPersonId)) {
                    return CommonMethod.getReturnMessageError("学生端仅可查看自己的考勤信息");
                }
            }
            if (studentId == null || courseId == null) {
                return CommonMethod.getReturnMessageError("缺少必要参数");
            }
            double rate = attendanceService.calculateAttendanceRate(studentId, courseId);
            return CommonMethod.getReturnData(rate);
        } catch (Exception e) {
            return CommonMethod.getReturnMessageError("查询失败：" + e.getMessage());
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
            if (isStudentRole() && !attendance.getStudent().getPersonId().equals(CommonMethod.getPersonId())) {
                return ResponseUtil.error("学生端仅可查看自己的考勤信息");
            }
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
            if (isStudentRole()) {
                return ResponseUtil.error("学生端仅可查看自己的考勤信息，不能修改");
            }
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
            List<Map<String,Object>> dataList = buildFilteredAttendanceData(dataRequest);
            return CommonMethod.getReturnData(dataList);
        } catch (Exception e) {
            return CommonMethod.getReturnMessageError("查询失败：" + e.getMessage());
        }
    }

    /**
     * 导出考勤记录
     * POST /api/attendance/export
     */
    @PostMapping("/export")
    public ResponseEntity<StreamingResponseBody> exportAttendance(@RequestBody(required = false) DataRequest dataRequest) {
        try {
            List<Map<String,Object>> dataList = buildFilteredAttendanceData(dataRequest);
            XSSFWorkbook wb = new XSSFWorkbook();
            XSSFCellStyle style = CommonMethod.createCellStyle(wb, 11);
            XSSFSheet sheet = wb.createSheet("attendance");
            for (int j = 0; j < EXPORT_COLUMN_WIDTHS.length; j++) {
                sheet.setColumnWidth(j, EXPORT_COLUMN_WIDTHS[j] * 256);
            }
            XSSFRow row = sheet.createRow(0);
            XSSFCell[] cells = new XSSFCell[EXPORT_COLUMN_WIDTHS.length];
            for (int j = 0; j < EXPORT_COLUMN_WIDTHS.length; j++) {
                cells[j] = row.createCell(j);
                cells[j].setCellStyle(style);
                cells[j].setCellValue(EXPORT_TITLES[j]);
            }
            for (int i = 0; i < dataList.size(); i++) {
                Map<String,Object> item = dataList.get(i);
                row = sheet.createRow(i + 1);
                for (int j = 0; j < EXPORT_COLUMN_WIDTHS.length; j++) {
                    cells[j] = row.createCell(j);
                    cells[j].setCellStyle(style);
                }
                cells[0].setCellValue(i + 1);
                cells[1].setCellValue(CommonMethod.getString(item, "studentNum"));
                cells[2].setCellValue(CommonMethod.getString(item, "studentName"));
                cells[3].setCellValue(CommonMethod.getString(item, "courseName"));
                cells[4].setCellValue(CommonMethod.getString(item, "attendanceDate"));
                cells[5].setCellValue(CommonMethod.getString(item, "statusName"));
                cells[6].setCellValue(CommonMethod.getString(item, "remark"));
                cells[7].setCellValue(CommonMethod.getString(item, "recordTime"));
            }

            StreamingResponseBody stream = wb::write;
            return ResponseEntity.ok().contentType(CommonMethod.exelType).body(stream);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private Map<String,Object> toAttendanceMap(Attendance attendance) {
        Map<String,Object> map = new HashMap<>();
        map.put("attendanceId", attendance.getAttendanceId());
        map.put("studentId", attendance.getStudent().getPersonId());
        map.put("courseId", attendance.getCourse().getCourseId());
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

    private List<Map<String,Object>> buildFilteredAttendanceData(DataRequest dataRequest) {
        String studentNum = dataRequest == null ? null : dataRequest.getString("studentNum");
        String studentName = dataRequest == null ? null : dataRequest.getString("studentName");
        String courseName = dataRequest == null ? null : dataRequest.getString("courseName");
        Integer currentPersonId = CommonMethod.getPersonId();
        boolean studentRole = isStudentRole();
        List<Attendance> attendances = attendanceService.getAllAttendance();
        List<Map<String,Object>> dataList = new ArrayList<>();
        for (Attendance attendance : attendances) {
            if (attendance.getStudent() == null || attendance.getStudent().getPerson() == null || attendance.getCourse() == null) {
                continue;
            }
            if (studentRole && !attendance.getStudent().getPersonId().equals(currentPersonId)) {
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
        return dataList;
    }

    private Integer resolveStudentId(DataRequest dataRequest) {
        Integer studentId = dataRequest.getInteger("studentId");
        if (studentId != null) {
            return studentId;
        }
        String studentNum = dataRequest.getString("studentNum");
        if (studentNum == null || studentNum.isEmpty()) {
            return null;
        }
        Optional<Student> student = studentRepository.findByPersonNum(studentNum);
        return student.map(Student::getPersonId).orElse(null);
    }

    private Integer resolveCourseId(DataRequest dataRequest) {
        Integer courseId = dataRequest.getInteger("courseId");
        if (courseId != null) {
            return courseId;
        }
        String courseNum = dataRequest.getString("courseNum");
        if (courseNum != null && !courseNum.isEmpty()) {
            Optional<Course> course = courseRepository.findByNum(courseNum);
            if (course.isPresent()) {
                return course.get().getCourseId();
            }
        }
        String courseName = dataRequest.getString("courseName");
        if (courseName == null || courseName.isEmpty()) {
            return null;
        }
        List<Course> courseList = courseRepository.findByName(courseName);
        if (courseList.size() > 1) {
            throw new RuntimeException("存在同名课程，请改用课程编号");
        }
        if (courseList.size() == 1) {
            return courseList.get(0).getCourseId();
        }
        return null;
    }

    private boolean isStudentRole() {
        return ROLE_STUDENT.equals(CommonMethod.getRoleName());
    }
}