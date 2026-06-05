package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.Course;
import cn.edu.sdu.java.server.models.CourseSelection;
import cn.edu.sdu.java.server.models.Score;
import cn.edu.sdu.java.server.models.Student;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.payload.response.OptionItem;
import cn.edu.sdu.java.server.payload.response.OptionItemList;
import cn.edu.sdu.java.server.repositorys.CourseRepository;
import cn.edu.sdu.java.server.repositorys.CourseSelectionRepository;
import cn.edu.sdu.java.server.repositorys.ScoreRepository;
import cn.edu.sdu.java.server.repositorys.StudentRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ScoreService {
    private static final String ROLE_STUDENT = "ROLE_STUDENT";
    private final CourseRepository courseRepository;
    private final CourseSelectionRepository courseSelectionRepository;
    private final ScoreRepository scoreRepository;
    private final StudentRepository studentRepository;

    public ScoreService(CourseRepository courseRepository, CourseSelectionRepository courseSelectionRepository, ScoreRepository scoreRepository, StudentRepository studentRepository) {
        this.courseRepository = courseRepository;
        this.courseSelectionRepository = courseSelectionRepository;
        this.scoreRepository = scoreRepository;
        this.studentRepository = studentRepository;
    }
    public OptionItemList getStudentItemOptionList( DataRequest dataRequest) {
        List<Student> sList;
        if (isStudentRole()) {
            Integer personId = CommonMethod.getPersonId();
            if (personId == null) {
                sList = Collections.emptyList();
            } else {
                Optional<Student> studentOptional = studentRepository.findById(personId);
                sList = studentOptional.map(List::of).orElse(Collections.emptyList());
            }
        } else {
            sList = studentRepository.findStudentListByNumName("");  //数据库查询操作
        }
        List<OptionItem> itemList = new ArrayList<>();
        for (Student s : sList) {
            itemList.add(new OptionItem( s.getPersonId(),s.getPersonId()+"", s.getPerson().getNum()+"-"+s.getPerson().getName()));
        }
        return new OptionItemList(0, itemList);
    }

    public OptionItemList getCourseItemOptionList(DataRequest dataRequest) {
        List<Course> sList;
        if (isStudentRole()) {
            Integer personId = CommonMethod.getPersonId();
            if (personId == null) {
                sList = Collections.emptyList();
            } else {
                List<CourseSelection> selectionList = courseSelectionRepository.findActiveSelectionsByStudent(personId);
                LinkedHashMap<Integer, Course> courseMap = new LinkedHashMap<>();
                for (CourseSelection selection : selectionList) {
                    if (selection.getCourse() != null && selection.getCourse().getCourseId() != null) {
                        courseMap.put(selection.getCourse().getCourseId(), selection.getCourse());
                    }
                }
                sList = new ArrayList<>(courseMap.values());
            }
        } else {
            sList = courseRepository.findAll();  //数据库查询操作
        }
        List<OptionItem> itemList = new ArrayList<>();
        for (Course c : sList) {
            itemList.add(new OptionItem(c.getCourseId(),c.getCourseId()+"", c.getNum()+"-"+c.getName()));
        }
        return new OptionItemList(0, itemList);
    }

    public DataResponse getScoreList(DataRequest dataRequest) {
        Integer personId = dataRequest.getInteger("personId");
        if(personId == null)
            personId = 0;
        Integer courseId = dataRequest.getInteger("courseId");
        if(courseId == null)
            courseId = 0;
        if (isStudentRole()) {
            Integer currentPersonId = CommonMethod.getPersonId();
            personId = currentPersonId == null ? 0 : currentPersonId;
        }
        List<Score> sList = scoreRepository.findByStudentCourse(personId, courseId);  //数据库查询操作
        List<Map<String,Object>> dataList = new ArrayList<>();
        Map<String,Object> m;
        for (Score s : sList) {
            m = new HashMap<>();
            m.put("scoreId", s.getScoreId()+"");
            m.put("personId",s.getStudent().getPersonId()+"");
            m.put("courseId",s.getCourse().getCourseId()+"");
            m.put("studentNum",s.getStudent().getPerson().getNum());
            m.put("studentName",s.getStudent().getPerson().getName());
            m.put("className",s.getStudent().getClassName());
            m.put("courseNum",s.getCourse().getNum());
            m.put("courseName",s.getCourse().getName());
            m.put("credit",""+s.getCourse().getCredit());
            m.put("mark",""+s.getMark());
            dataList.add(m);
        }
        return CommonMethod.getReturnData(dataList);
    }
    public DataResponse scoreSave(DataRequest dataRequest) {
        Integer personId = dataRequest.getInteger("personId");
        Integer courseId = dataRequest.getInteger("courseId");
        Integer mark = dataRequest.getInteger("mark");
        Integer scoreId = dataRequest.getInteger("scoreId");
        Optional<Score> op;
        Score s = null;
        if(scoreId != null) {
            op= scoreRepository.findById(scoreId);
            if(op.isPresent())
                s = op.get();
        }
        if(s == null) {
            s = new Score();
            s.setStudent(studentRepository.findById(personId).get());
            s.setCourse(courseRepository.findById(courseId).get());
        }
        s.setMark(mark);
        scoreRepository.save(s);
        return CommonMethod.getReturnMessageOK();
    }
    public DataResponse scoreDelete(DataRequest dataRequest) {
        Integer scoreId = dataRequest.getInteger("scoreId");
        Optional<Score> op;
        Score s = null;
        if(scoreId != null) {
            op= scoreRepository.findById(scoreId);
            if(op.isPresent()) {
                s = op.get();
                scoreRepository.delete(s);
            }
        }
        return CommonMethod.getReturnMessageOK();
    }

    private boolean isStudentRole() {
        return ROLE_STUDENT.equals(CommonMethod.getRoleName());
    }
}
