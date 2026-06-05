package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.Student;
import cn.edu.sdu.java.server.models.StudentLeave;
import cn.edu.sdu.java.server.models.Teacher;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.payload.response.OptionItem;
import cn.edu.sdu.java.server.payload.response.OptionItemList;
import cn.edu.sdu.java.server.repositorys.StudentLeaveRepository;
import cn.edu.sdu.java.server.repositorys.StudentRepository;
import cn.edu.sdu.java.server.repositorys.TeacherRepository;
import cn.edu.sdu.java.server.util.ComDataUtil;
import cn.edu.sdu.java.server.util.CommonMethod;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StudentLeaveService {
    private static final int STATE_PENDING = 0;
    private static final int STATE_TEACHER_APPROVED = 1;
    private static final int STATE_TEACHER_REJECTED = 2;
    private static final int STATE_ADMIN_APPROVED = 3;
    private static final int STATE_ADMIN_REJECTED = 4;

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final StudentLeaveRepository studentLeaveRepository;

    public StudentLeaveService(StudentRepository studentRepository, TeacherRepository teacherRepository, StudentLeaveRepository studentLeaveRepository) {
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.studentLeaveRepository = studentLeaveRepository;
    }

    public OptionItemList getTeacherItemOptionList(DataRequest dataRequest) {
        List<Teacher> sList = teacherRepository.findAll();  //数据库查询操作
        List<OptionItem> itemList = new ArrayList<>();
        for (Teacher t : sList) {
            itemList.add(new OptionItem(t.getPersonId(), t.getPersonId() + "", t.getPerson().getNum() + "-" + t.getPerson().getName()));
        }
        return new OptionItemList(0, itemList);
    }

    public DataResponse getStudentLeaveList(DataRequest dataRequest) {
        String roleName = CommonMethod.getRoleName();
        String userName = CommonMethod.getUsername();
        Integer state = dataRequest.getInteger("state");
        if(state == null)
            state = -1;
        String search = dataRequest.getString("search");
        if (roleName == null || userName == null) {
            return CommonMethod.getReturnData(Collections.emptyList());
        }
        List<StudentLeave> slList = switch (roleName) {
            case "ROLE_STUDENT" -> studentLeaveRepository.getStudentLeaveList(-1, search, userName, "");
            case "ROLE_TEACHER" -> studentLeaveRepository.getStudentLeaveList(-1, search, "", userName);
            case "ROLE_ADMIN" -> studentLeaveRepository.getStudentLeaveList(state, search, "", "");
            default -> Collections.emptyList();
        };
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> map;
        Student s;
        Teacher t;
        ComDataUtil di = ComDataUtil.getInstance();
        if (!slList.isEmpty()) {
            for (StudentLeave sl : slList) {
                map = new HashMap<>();
                s = sl.getStudent();
                t = sl.getTeacher();
                map.put("studentLeaveId", sl.getStudentLeaveId());
                map.put("studentNum", s.getPerson().getNum());
                map.put("studentName", s.getPerson().getName());
                map.put("studentId", s.getPersonId());
                map.put("teacherName", buildTeacherDisplayName(t));
                map.put("state", sl.getState());
                map.put("stateName", di.getDictionaryLabelByValue("SHZTM", sl.getState()+""));
                map.put("reason", sl.getReason());
                map.put("leaveDate", sl.getLeaveDate());
                map.put("adminComment", sl.getAdminComment());
                map.put("teacherId", t == null ? null : t.getPersonId());
                map.put("teacherComment", sl.getTeacherComment());
                dataList.add(map);
            }
        }
        return CommonMethod.getReturnData(dataList);
    }

    public DataResponse studentLeaveSave(DataRequest dataRequest) {
        Integer studentLeaveId = dataRequest.getInteger("studentLeaveId");
        Integer teacherId = dataRequest.getInteger("teacherId");
        String leaveDate = dataRequest.getString("leaveDate");
        String reason = dataRequest.getString("reason");
        if (leaveDate == null || leaveDate.trim().isEmpty() || reason == null || reason.trim().isEmpty()) {
            return CommonMethod.getReturnMessageError("请假日期和请假原因不能为空");
        }
        StudentLeave sl = null;
        if(studentLeaveId != null && studentLeaveId > 0) {
            Optional<StudentLeave> op = studentLeaveRepository.findById(studentLeaveId);
            if(op.isPresent())
                sl = op.get();
        }
        if(sl == null) {
            sl = new StudentLeave();
            sl.setState(STATE_PENDING);
            sl.setApplyTime(new Date());
            sl.setTeacherComment("");
            sl.setAdminComment("");
            Optional<Student> student = studentRepository.findByPersonNum(CommonMethod.getUsername());
            if (student.isEmpty()) {
                return CommonMethod.getReturnMessageError("当前学生不存在");
            }
            sl.setStudent(student.get());
        } else if (sl.getState() != null && sl.getState() >= STATE_TEACHER_APPROVED) {
            return CommonMethod.getReturnMessageError("已完成审批的请假单不能修改");
        }
        if(teacherId != null && teacherId > 0) {
            Optional<Teacher> op = teacherRepository.findById(teacherId);
            if(op.isPresent()) {
                sl.setTeacher(op.get());
            } else {
                return CommonMethod.getReturnMessageError("审批教师不存在");
            }
        } else if (sl.getTeacher() == null) {
            return CommonMethod.getReturnMessageError("请选择审批教师");
        }
        sl.setLeaveDate(leaveDate.trim());
        sl.setReason(reason.trim());
        sl.setState(STATE_PENDING);
        sl.setTeacherComment("");
        sl.setAdminComment("");
        sl.setTeacherTime(null);
        sl.setAdminTime(null);
        studentLeaveRepository.save(sl);
        return CommonMethod.getReturnMessageOK();
    }
    public DataResponse studentLeaveCheck(DataRequest dataRequest) {
        String roleName = CommonMethod.getRoleName();
        Integer state = dataRequest.getInteger("state");
        Integer studentLeaveId = dataRequest.getInteger("studentLeaveId");
        String teacherComment = dataRequest.getString("teacherComment");
        String adminComment = dataRequest.getString("adminComment");
        StudentLeave sl = null;
        if(studentLeaveId != null && studentLeaveId > 0) {
            Optional<StudentLeave> op = studentLeaveRepository.findById(studentLeaveId);
            if(op.isPresent())
                sl = op.get();
        }
        if(sl == null) {
            return CommonMethod.getReturnMessageOK();
        }
        if ("ROLE_ADMIN".equals(roleName)) {
            if (!isValidDecisionState(state)) {
                return CommonMethod.getReturnMessageError("管理员审批状态无效");
            }
            sl.setAdminComment(adminComment);
            sl.setAdminTime(new Date());
            sl.setState(state+2);
        } else if("ROLE_TEACHER".equals(roleName)) {
            if (!isValidDecisionState(state)) {
                return CommonMethod.getReturnMessageError("教师审批状态无效");
            }
            sl.setTeacherComment(teacherComment);
            sl.setTeacherTime(new Date());
            sl.setState(state);
        } else {
            return CommonMethod.getReturnMessageError("无审批权限");
        }
        studentLeaveRepository.save(sl);
        return CommonMethod.getReturnMessageOK();
    }

    private boolean isValidDecisionState(Integer state) {
        return state != null && (state == STATE_TEACHER_APPROVED || state == STATE_TEACHER_REJECTED);
    }

    private String buildTeacherDisplayName(Teacher teacher) {
        if (teacher == null || teacher.getPerson() == null) {
            return "";
        }
        return teacher.getPerson().getNum() + "-" + teacher.getPerson().getName();
    }
}