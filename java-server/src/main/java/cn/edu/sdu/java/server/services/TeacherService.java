package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.*;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.PersonRepository;
import cn.edu.sdu.java.server.repositorys.TeacherRepository;
import cn.edu.sdu.java.server.repositorys.UserRepository;
import cn.edu.sdu.java.server.repositorys.UserTypeRepository;
import cn.edu.sdu.java.server.util.ComDataUtil;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.DateTimeTool;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TeacherService {
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_TEACHER = "ROLE_TEACHER";
    private final PersonRepository personRepository;
    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final UserTypeRepository userTypeRepository;
    private final PasswordEncoder encoder;

    public TeacherService(PersonRepository personRepository, TeacherRepository teacherRepository, UserRepository userRepository, UserTypeRepository userTypeRepository, PasswordEncoder encoder) {
        this.personRepository = personRepository;
        this.teacherRepository = teacherRepository;
        this.userRepository = userRepository;
        this.userTypeRepository = userTypeRepository;
        this.encoder = encoder;
    }

    public Map<String, Object> getMapFromTeacher(Teacher t) {
        Map<String, Object> map = new HashMap<>();
        if (t == null) {
            return map;
        }
        Person p = t.getPerson();
        if (p == null) {
            return map;
        }
        map.put("personId", t.getPersonId());
        map.put("num", p.getNum());
        map.put("name", p.getName());
        map.put("dept", p.getDept());
        map.put("card", p.getCard());
        map.put("gender", p.getGender());
        map.put("genderName", ComDataUtil.getInstance().getDictionaryLabelByValue("XBM", p.getGender()));
        map.put("birthday", p.getBirthday());
        map.put("email", p.getEmail());
        map.put("phone", p.getPhone());
        map.put("address", p.getAddress());
        // 密码提示：已设置密码
        map.put("passwordHint", "****");
        map.put("title", t.getTitle());
        map.put("degree", t.getDegree());
        return map;
    }

    public DataResponse getTeacherList(DataRequest dataRequest) {
        String numName = dataRequest.getString("numName");
        if (isTeacherRole() && !isAdminRole()) {
            List<Map<String, Object>> ownList = new ArrayList<>();
            Optional<Teacher> selfOp = getCurrentLoginTeacher();
            if (selfOp.isPresent()) {
                Map<String, Object> self = getMapFromTeacher(selfOp.get());
                if (isBlank(numName) || containsIgnoreCase(CommonMethod.getString(self, "num"), numName)
                        || containsIgnoreCase(CommonMethod.getString(self, "name"), numName)) {
                    ownList.add(self);
                }
            }
            return CommonMethod.getReturnData(ownList);
        }
        List<Teacher> tList = teacherRepository.findTeacherListByNumName(numName);
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (Teacher teacher : tList) {
            dataList.add(getMapFromTeacher(teacher));
        }
        return CommonMethod.getReturnData(dataList);
    }

    public DataResponse getTeacherInfo(DataRequest dataRequest) {
        Integer personId = dataRequest.getInteger("personId");
        if (isTeacherRole() && !isAdminRole()) {
            Optional<Teacher> selfOp = getCurrentLoginTeacher();
            if (selfOp.isEmpty()) {
                return CommonMethod.getReturnMessageError("教师不存在！");
            }
            if (personId != null && !personId.equals(selfOp.get().getPersonId())) {
                return CommonMethod.getReturnMessageError("无权限查看其他教师信息");
            }
            return CommonMethod.getReturnData(getMapFromTeacher(selfOp.get()));
        }
        Optional<Teacher> op = personId == null ? Optional.empty() : teacherRepository.findById(personId);
        return CommonMethod.getReturnData(op.map(this::getMapFromTeacher).orElseGet(HashMap::new));
    }

    public DataResponse teacherDelete(DataRequest dataRequest) {
        Integer personId = dataRequest.getInteger("personId");
        if (personId == null || personId <= 0) {
            return CommonMethod.getReturnMessageError("教师主键为空");
        }
        Optional<Teacher> op = teacherRepository.findById(personId);
        if (op.isEmpty()) {
            return CommonMethod.getReturnMessageError("教师不存在");
        }
        Optional<User> userOp = userRepository.findById(personId);
        userOp.ifPresent(userRepository::delete);
        Teacher t = op.get();
        Person p = t.getPerson();
        teacherRepository.delete(t);
        if (p != null) {
            personRepository.delete(p);
        }
        return CommonMethod.getReturnMessageOK();
    }

    public DataResponse teacherEditSave(DataRequest dataRequest) {
        Integer personId = dataRequest.getInteger("personId");
        Map<String, Object> form = dataRequest.getMap("form");
        String num = CommonMethod.getString(form, "num");
        boolean teacherRoleOnly = isTeacherRole() && !isAdminRole();
        if (teacherRoleOnly) {
            Optional<Teacher> selfOp = getCurrentLoginTeacher();
            if (selfOp.isEmpty()) {
                return CommonMethod.getReturnMessageError("教师不存在！");
            }
            Teacher self = selfOp.get();
            if (personId != null && !personId.equals(self.getPersonId())) {
                return CommonMethod.getReturnMessageError("无权限修改其他教师信息");
            }
            personId = self.getPersonId();
            if (!isBlank(num) && !num.equals(self.getPerson().getNum())) {
                return CommonMethod.getReturnMessageError("教师账号不允许修改");
            }
        }
        if (num == null || num.isEmpty()) {
            return CommonMethod.getReturnMessageError("工号不能为空");
        }
        Teacher t = null;
        if (personId != null) {
            Optional<Teacher> op = teacherRepository.findById(personId);
            if (op.isPresent()) {
                t = op.get();
            }
        }
        Optional<Person> nOp = personRepository.findByNum(num);
        Person currentPerson = t == null ? null : t.getPerson();
        if (nOp.isPresent()) {
            if (currentPerson == null || !num.equals(currentPerson.getNum())) {
                return CommonMethod.getReturnMessageError("工号已经存在，不能添加或修改！");
            }
        }

        Person p;
        User u;
        if (t == null) {
            p = new Person();
            p.setNum(num);
            p.setType("2");
            personRepository.saveAndFlush(p);
            personId = p.getPersonId();
            u = new User();
            u.setPersonId(personId);
            u.setUserName(num);
            // 如果表单中提供了密码，则使用提供的密码，否则使用默认密码123456
            String password = CommonMethod.getString(form, "password");
            if (password == null || password.isEmpty()) {
                password = "123456";
            }
            u.setPassword(encoder.encode(password));
            u.setUserType(userTypeRepository.findByName(EUserType.ROLE_TEACHER.name()));
            u.setCreateTime(DateTimeTool.parseDateTime(new Date()));
            u.setCreatorId(CommonMethod.getPersonId());
            userRepository.saveAndFlush(u);
            t = new Teacher();
            t.setPersonId(personId);
            teacherRepository.saveAndFlush(t);
        } else {
            p = t.getPerson();
            if (p == null) {
                return CommonMethod.getReturnMessageError("教师人员信息异常，无法保存");
            }
        }

        if (!num.equals(p.getNum())) {
            Optional<User> uOp = userRepository.findByPersonPersonId(personId);
            if (uOp.isPresent()) {
                u = uOp.get();
                u.setUserName(num);
                userRepository.saveAndFlush(u);
            }
            p.setNum(num);
        }
        p.setName(CommonMethod.getString(form, "name"));
        p.setDept(CommonMethod.getString(form, "dept"));
        p.setCard(CommonMethod.getString(form, "card"));
        p.setGender(CommonMethod.getString(form, "gender"));
        p.setBirthday(CommonMethod.getString(form, "birthday"));
        p.setEmail(CommonMethod.getString(form, "email"));
        p.setPhone(CommonMethod.getString(form, "phone"));
        p.setAddress(CommonMethod.getString(form, "address"));
        personRepository.saveAndFlush(p);

        t.setTitle(CommonMethod.getString(form, "title"));
        t.setDegree(CommonMethod.getString(form, "degree"));
        teacherRepository.saveAndFlush(t);
        return CommonMethod.getReturnData(t.getPersonId());
    }

    private boolean isAdminRole() {
        return ROLE_ADMIN.equals(CommonMethod.getRoleName());
    }

    private boolean isTeacherRole() {
        return ROLE_TEACHER.equals(CommonMethod.getRoleName());
    }

    private Optional<Teacher> getCurrentLoginTeacher() {
        String username = CommonMethod.getUsername();
        if (isBlank(username)) {
            return Optional.empty();
        }
        return teacherRepository.findByPersonNum(username);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        if (isBlank(source) || isBlank(keyword)) {
            return false;
        }
        return source.toLowerCase().contains(keyword.trim().toLowerCase());
    }
}
