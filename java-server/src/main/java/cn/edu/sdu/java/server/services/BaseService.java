package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.*;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.payload.response.MyTreeNode;
import cn.edu.sdu.java.server.payload.response.OptionItem;
import cn.edu.sdu.java.server.payload.response.OptionItemList;
import cn.edu.sdu.java.server.repositorys.*;
import cn.edu.sdu.java.server.util.ComDataUtil;
import cn.edu.sdu.java.server.util.CommonMethod;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BaseService {
    private static final Logger log = LoggerFactory.getLogger(BaseService.class);
    private static final int ADMIN_USER_TYPE_ID = 1;
    private static final int STUDENT_USER_TYPE_ID = 2;
    private static final int TEACHER_USER_TYPE_ID = 3;
    @Value("${attach.folder}")    //环境配置变量获取
    private String attachFolder;  //服务器端数据存储
    @Value("${spring.datasource.username}")    //环境配置变量获取
    private String dataBaseUserName;  //服务器端数据存储
    private final PersonRepository personRepository;
    private final PasswordEncoder encoder;  //密码服务自动注入
    private final UserRepository userRepository;  //用户数据操作自动注入
    private final MenuInfoRepository menuInfoRepository; //菜单数据操作自动注入
    private final DictionaryInfoRepository dictionaryInfoRepository;  //数据字典数据操作自动注入
    private final UserTypeRepository userTypeRepository;   //用户类型数据操作自动注入

    public BaseService(PersonRepository personRepository, PasswordEncoder encoder, UserRepository userRepository, MenuInfoRepository menuInfoRepository, DictionaryInfoRepository dictionaryInfoRepository, UserTypeRepository userTypeRepository) {
        this.personRepository = personRepository;
        this.encoder = encoder;
        this.userRepository = userRepository;
        this.menuInfoRepository = menuInfoRepository;
        this.dictionaryInfoRepository = dictionaryInfoRepository;
        this.userTypeRepository = userTypeRepository;
    }

    public DataResponse getDataBaseUserName() {
        return CommonMethod.getReturnData(dataBaseUserName);
    }
    /**
     *  getDictionaryTreeNode 获取数据字典节点树根节点
     * @return MyTreeNode 数据字典树根节点
     */
    public List<MyTreeNode> getDictionaryTreeNodeList() {
        List<MyTreeNode> childList = new ArrayList<MyTreeNode>();
        List<DictionaryInfo> sList = dictionaryInfoRepository.findRootList();
        if(sList == null)
            return childList;
        for (DictionaryInfo dictionaryInfo : sList) {
            childList.add(getDictionaryTreeNode(null, dictionaryInfo, null));
        }
        return childList;
    }

    /**
     * 获得数据字典的MyTreeNode
     */
    public MyTreeNode getDictionaryTreeNode( Integer pid, DictionaryInfo d,String parentTitle) {
        MyTreeNode  node = new MyTreeNode(d.getId(),d.getValue(),d.getLabel(),null);
        node.setLabel(d.getValue()+"-"+d.getLabel());
        node.setParentTitle(parentTitle);
        node.setPid(pid);
        List<MyTreeNode> childList = new ArrayList<MyTreeNode>();
        node.setChildren(childList);
        List<DictionaryInfo> sList = dictionaryInfoRepository.findByPid(d.getId());
        if(sList == null)
            return node;
        for (DictionaryInfo dictionaryInfo : sList) {
            childList.add(getDictionaryTreeNode(node.getId(), dictionaryInfo, node.getValue()));
        }
        return node;
    }

    /**
     * MyTreeNode getMenuTreeNode(Integer userTypeId) 获得角色的菜单树根节点
     */
    public List<MyTreeNode> getMenuTreeNodeList() {
        List<MyTreeNode> childList = new ArrayList<MyTreeNode>();
        List<MenuInfo> sList = menuInfoRepository.findByUserTypeIds("");
        if(sList == null)
            return childList;
        for (MenuInfo menuInfo : sList) {
            childList.add(getMenuTreeNode(null, menuInfo, ""));
        }
        return childList;
    }
    /**
     * MyTreeNode getMenuTreeNode(Integer userTypeId) 获得角色的某个菜单的菜单树根节点
     */
    public MyTreeNode getMenuTreeNode(Integer pid, MenuInfo d,String parentTitle) {
        MyTreeNode  node = new MyTreeNode(d.getId(),d.getName(),d.getTitle(),null);
        node.setLabel(d.getId()+"-"+d.getTitle());
        node.setUserTypeIds(d.getUserTypeIds());
        node.setParentTitle(parentTitle);
        node.setPid(pid);
        List<MyTreeNode> childList = new ArrayList<MyTreeNode>();
        node.setChildren(childList);
        List<MenuInfo> sList = menuInfoRepository.findByUserTypeIds("",d.getId());
        if(sList == null)
            return node;
        for (MenuInfo menuInfo : sList) {
            childList.add(getMenuTreeNode(node.getId(), menuInfo, node.getTitle()));
        }
        return node;
    }



    public List<Map<String, Object>> getMenuList(Integer userTypeId, Integer pid) {
        List<Map<String, Object>> sList = new ArrayList<>();
        Map<String, Object> ms;
        List<MenuInfo> msList = menuInfoRepository.findByUserTypeIds(userTypeId + "", pid);
        String name, path;
        if (msList != null) {
            for (MenuInfo info : msList) {
                ms = new HashMap<>();
                name = info.getName();
                if(name!= null&& !name.isEmpty()) {
                    path = name.substring(0,1).toLowerCase()+ name.substring(1);
                }else {
                    path = "";
                }
                ms.put("id", info.getId());
                ms.put("name", name);
                ms.put("path", path);
                ms.put("title", info.getTitle());
                ms.put("sList", getMenuList(userTypeId, info.getId()));
                sList.add(ms);
            }
        }
        return sList;
    }

    public DataResponse getMenuList(DataRequest dataRequest) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        Integer userTypeId = dataRequest.getInteger("userTypeId");
        String roleName = CommonMethod.getRoleName();
        if (userTypeId == null) {
            Integer personId = CommonMethod.getPersonId();
            if (personId == null)
                return CommonMethod.getReturnData(dataList);
            userTypeId = userRepository.findById(personId).get().getUserType().getId();
        }
        List<MenuInfo> mList = menuInfoRepository.findByUserTypeIds(userTypeId + "");
        Map<String, Object> m;
        List<Map<String, Object>> sList;
        String name, path;
        for (MenuInfo info : mList) {
            m = new HashMap<>();
            name = info.getName();
            if(name!= null&& !name.isEmpty()) {
                path = name.substring(0,1).toLowerCase()+ name.substring(1);
            }else {
                path = "";
            }
            m.put("id", info.getId());
            m.put("path", path);
            m.put("name", name);
            m.put("title", info.getTitle());
            sList = getMenuList(userTypeId, info.getId());
            m.put("sList", sList);
            dataList.add(m);
        }
        dataList = buildRoleMenuList(roleName, userTypeId, dataList);
        return CommonMethod.getReturnData(dataList);
    }

    private List<Map<String, Object>> buildRoleMenuList(String roleName, Integer userTypeId, List<Map<String, Object>> originList) {
        if (isStudentRole(roleName, userTypeId)) {
            return buildStudentMenuList();
        }
        if (isTeacherRole(roleName, userTypeId)) {
            return buildTeacherMenuList();
        }
        if (isAdminRole(roleName, userTypeId)) {
            ensureTeacherManagementMenu(originList);
        }
        return originList;
    }

    private boolean isAdminRole(String roleName, Integer userTypeId) {
        return "ROLE_ADMIN".equals(roleName) || (userTypeId != null && userTypeId == ADMIN_USER_TYPE_ID);
    }

    private boolean isStudentRole(String roleName, Integer userTypeId) {
        return "ROLE_STUDENT".equals(roleName) || (userTypeId != null && userTypeId == STUDENT_USER_TYPE_ID);
    }

    private boolean isTeacherRole(String roleName, Integer userTypeId) {
        return "ROLE_TEACHER".equals(roleName) || (userTypeId != null && userTypeId == TEACHER_USER_TYPE_ID);
    }

    private List<Map<String, Object>> buildStudentMenuList() {
        List<Map<String, Object>> studentModules = new ArrayList<>();
        studentModules.add(createMenuNode("student-panel", "个人信息", 0, Collections.emptyList()));
        studentModules.add(createMenuNode("base/courseSelection", "我的选课", 0, Collections.emptyList()));
        studentModules.add(createMenuNode("base/attendance", "我的考勤", 0, Collections.emptyList()));
        studentModules.add(createMenuNode("base/innovationAchievement", "我的创新成果", 0, Collections.emptyList()));
        studentModules.add(createMenuNode("student-leave-panel", "我的审批", 0, Collections.emptyList()));
        studentModules.add(createMenuNode("base/scoreSubmission", "我的成绩提交", 0, Collections.emptyList()));
        return new ArrayList<>(List.of(createMenuNode("studentModules", "学生功能", 1, studentModules)));
    }

    private List<Map<String, Object>> buildTeacherMenuList() {
        List<Map<String, Object>> teacherModules = new ArrayList<>();
        teacherModules.add(createMenuNode("course-panel", "我的教学课程", 0, Collections.emptyList()));
        teacherModules.add(createMenuNode("base/attendance", "考勤管理", 0, Collections.emptyList()));
        teacherModules.add(createMenuNode("base/innovationAchievement", "创新成果管理", 0, Collections.emptyList()));
        teacherModules.add(createMenuNode("student-leave-panel", "审批流程管理", 0, Collections.emptyList()));
        teacherModules.add(createMenuNode("base/scoreSubmission", "成绩提交审核", 0, Collections.emptyList()));
        return new ArrayList<>(List.of(createMenuNode("teacherModules", "教师功能", 1, teacherModules)));
    }

    private Map<String, Object> createMenuNode(String name, String title, Integer isLeft, List<Map<String, Object>> children) {
        Map<String, Object> node = new HashMap<>();
        String path = name == null || name.isEmpty() ? "" : name.substring(0, 1).toLowerCase() + name.substring(1);
        node.put("id", null);
        node.put("name", name);
        node.put("path", path);
        node.put("title", title);
        node.put("isLeft", isLeft);
        node.put("sList", children == null ? new ArrayList<>() : children);
        return node;
    }

    private void ensureTeacherManagementMenu(List<Map<String, Object>> originList) {
        if (originList == null) {
            return;
        }
        Map<String, Object> personMenu = null;
        for (Map<String, Object> root : originList) {
            String title = CommonMethod.getString(root, "title");
            if ("人员管理".equals(title)) {
                personMenu = root;
                break;
            }
        }
        if (personMenu == null) {
            personMenu = createMenuNode("personManage", "人员管理", 1, new ArrayList<>());
            originList.add(personMenu);
        }
        List<Map<String, Object>> childList = new ArrayList<>();
        for (Object item : CommonMethod.getList(personMenu, "sList")) {
            if (item instanceof Map<?, ?> map) {
                childList.add((Map<String, Object>) map);
            }
        }
        boolean exist = false;
        for (Map<String, Object> item : childList) {
            if ("教师管理".equals(CommonMethod.getString(item, "title"))) {
                exist = true;
                break;
            }
        }
        if (!exist) {
            childList.add(createMenuNode("teacher-panel", "教师管理", 0, Collections.emptyList()));
        }
        personMenu.put("sList", childList);
    }


    public OptionItemList getRoleOptionItemList(@Valid @RequestBody DataRequest dataRequest) {
        List<UserType> uList = userTypeRepository.findAll();
        List<OptionItem> itemList = new ArrayList<>();
        for (UserType ut : uList) {
            itemList.add(new OptionItem(ut.getId(), null, ut.getName()));
        }
        return new OptionItemList(0, itemList);
    }


    public DataResponse menuDelete(DataRequest dataRequest) {
        Integer id = dataRequest.getInteger("id");
        int count  = menuInfoRepository.countMenuInfoByPid(id);
        if(count > 0) {
            return CommonMethod.getReturnMessageError("存在子菜单，不能删除！");
        }
        Optional<MenuInfo> op = menuInfoRepository.findById(id);
        op.ifPresent(menuInfoRepository::delete);
        return CommonMethod.getReturnMessageOK();
    }

    public DataResponse menuSave(DataRequest dataRequest) {
        Integer editType = dataRequest.getInteger("editType");
        Map<String, Object> node = dataRequest.getMap("node");
        Integer pid = CommonMethod.getInteger(node,"pid");
        Integer id = CommonMethod.getInteger(node,"id");
        String name = CommonMethod.getString(node,"value");
        String title = CommonMethod.getString(node,"title");
        String userTypeIds = CommonMethod.getString(node,"userTypeIds");
        Optional<MenuInfo> op;
        MenuInfo m = null;
        if (id != null) {
            op = menuInfoRepository.findById(id);
            if(op.isPresent()) {
                if(editType == 0 || editType == 1)
                    return CommonMethod.getReturnMessageError("主键已经存在，不能添加");
                m = op.get();
            }
        }
        if (m == null)
            m = new MenuInfo();
        m.setId(id);
        m.setTitle(title);
        m.setName(name);
        m.setPid(pid);
        m.setUserTypeIds(userTypeIds);
        menuInfoRepository.save(m);
        return CommonMethod.getReturnMessageOK();
    }

    public DataResponse deleteDictionary(DataRequest dataRequest) {
        Integer id = dataRequest.getInteger("id");
        int count = dictionaryInfoRepository.countDictionaryInfoByPid(id);
        if(count > 0) {
            return CommonMethod.getReturnMessageError("存在数据项，不能删除！");
        }
        Optional<DictionaryInfo> op = dictionaryInfoRepository.findById(id);
        op.ifPresent(dictionaryInfoRepository::delete);
        return CommonMethod.getReturnMessageOK();
    }

    public DataResponse dictionarySave(DataRequest dataRequest) {
        Integer id = dataRequest.getInteger("id");
        Integer pid = dataRequest.getInteger("pid");
        String value = dataRequest.getString("value");
        String title = dataRequest.getString("title");
        DictionaryInfo m = null;
        if(id != null) {
            Optional<DictionaryInfo> op = dictionaryInfoRepository.findById(id);
            if (op.isPresent()) {
                m = op.get();
            }
        }
        if(m == null) {
            m = new DictionaryInfo();
            m.setPid(pid);
        }
        m.setLabel(title);
        m.setValue(value);
        dictionaryInfoRepository.save(m);
        return CommonMethod.getReturnMessageOK();
    }

    public OptionItemList getDictionaryOptionItemList(DataRequest dataRequest) {
        String code = dataRequest.getString("code");
        List<DictionaryInfo> dList = dictionaryInfoRepository.getDictionaryInfoList(code);
        OptionItem item;
        List<OptionItem> itemList = new ArrayList<>();
        for (DictionaryInfo d : dList) {
            itemList.add(new OptionItem(d.getId(), d.getValue(), d.getLabel()));
        }
        return new OptionItemList(0, itemList);
    }

    public ResponseEntity<StreamingResponseBody> getFileByteData(DataRequest dataRequest) {
        String fileName = dataRequest.getString("fileName");
        try {
            File file = new File(attachFolder + fileName);
            int len = (int) file.length();
            byte[] data = new byte[len];
            FileInputStream in = new FileInputStream(file);
            int size = in.read(data);
            in.close();
            MediaType mType = new MediaType(MediaType.APPLICATION_OCTET_STREAM);
            StreamingResponseBody stream = outputStream -> {
                outputStream.write(data);
            };
            return ResponseEntity.ok()
                    .contentType(mType)
                    .body(stream);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return ResponseEntity.internalServerError().build();
    }


    public DataResponse uploadPhoto(byte[] barr,String remoteFile) {
        try {
            OutputStream os = new FileOutputStream(new File(attachFolder + remoteFile));
            os.write(barr);
            os.close();
            return CommonMethod.getReturnMessageOK();
        } catch (Exception e) {
            return CommonMethod.getReturnMessageError("上传错误");
        }
    }
    public ResponseEntity<StreamingResponseBody> getBlobByteData(DataRequest dataRequest) {
        Integer personId = dataRequest.getInteger("personId");
        try {
            byte [] data;
            Optional<Person> op = personRepository.findById(personId);
            if(op.isPresent()) {
                Person p = op.get();
                data = p.getPhoto();
                if(data == null)
                    return ResponseEntity.notFound().build();
            }else {
                return ResponseEntity.internalServerError().build();
            }
            MediaType mType = new MediaType(MediaType.APPLICATION_OCTET_STREAM);
            StreamingResponseBody stream = outputStream -> {
                outputStream.write(data);
            };
            return ResponseEntity.ok()
                    .contentType(mType)
                    .body(stream);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return ResponseEntity.internalServerError().build();
    }

    public DataResponse uploadPhotoBlob(byte[] barr,String personId ) {
        try {
            Optional<Person> op = personRepository.findById(Integer.parseInt(personId));
            if(op.isEmpty())
                return CommonMethod.getReturnMessageError("人员不存在！");
            Person p = op.get();
            p.setPhoto(barr);
            personRepository.save(p);
            return CommonMethod.getReturnMessageOK();
        } catch (Exception e) {
            return CommonMethod.getReturnMessageError("上传错误");
        }
    }

    public DataResponse updatePassword(DataRequest dataRequest) {
        String oldPassword = dataRequest.getString("oldPassword");  //获取oldPassword
        String newPassword = dataRequest.getString("newPassword");  //获取newPassword
        Optional<User> op = userRepository.findById(Objects.requireNonNull(CommonMethod.getPersonId()));
        if (op.isEmpty())
            return CommonMethod.getReturnMessageError("账户不存在！");  //通知前端操作正常
        User u = op.get();
        if (!encoder.matches(oldPassword, u.getPassword())) {
            return CommonMethod.getReturnMessageError("原始密码不正确！");
        }
        u.setPassword(encoder.encode(newPassword));
        userRepository.save(u);
        return CommonMethod.getReturnMessageOK();  //通知前端操作正常
    }


    @PostMapping("/uploadHtmlString")
    @PreAuthorize(" hasRole('ADMIN') ")
    public DataResponse uploadHtmlString(DataRequest dataRequest) {
        String str = dataRequest.getString("html");
        String html = new String(Base64.getDecoder().decode(str.getBytes(StandardCharsets.UTF_8)));
        int htmlCount = ComDataUtil.getInstance().addHtmlString(html);
        return CommonMethod.getReturnData(htmlCount);
    }

    public ResponseEntity<StreamingResponseBody> htmlGetBaseHtmlPage(HttpServletRequest request) {
        String htmlCountStr = request.getParameter("htmlCount");
        int htmlCount = Integer.parseInt(htmlCountStr);
        String html = ComDataUtil.getInstance().getHtmlString(htmlCount);
        MediaType mType = new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8);
        try {
            byte[] data = html.getBytes();
            StreamingResponseBody stream = outputStream -> {
                outputStream.write(data);
            };
            return ResponseEntity.ok()
                    .contentType(mType)
                    .body(stream);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }



    //  Web 请求
    public DataResponse getPhotoImageStr(DataRequest dataRequest) {
        String fileName = dataRequest.getString("fileName");
        String str = "";
        try {
            File file = new File(attachFolder + fileName);
            int len = (int) file.length();
            byte[] data = new byte[len];
            FileInputStream in = new FileInputStream(file);
            len = in.read(data);
            in.close();
            String imgStr = "data:image/png;base64,";
            String s = new String(Base64.getEncoder().encode(data));
            imgStr = imgStr + s;
            return CommonMethod.getReturnData(imgStr);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return CommonMethod.getReturnMessageError("下载错误！");
    }

    public DataResponse uploadPhotoWeb(Map<String,Object> pars, MultipartFile file) {
        try {
            String remoteFile = CommonMethod.getString(pars, "remoteFile");
            InputStream in = file.getInputStream();
            int size = (int) file.getSize();
            byte[] data = new byte[size];
            int len =  in.read(data);
            in.close();
            OutputStream os = new FileOutputStream(new File(attachFolder + remoteFile));
            os.write(data);
            os.close();
            return CommonMethod.getReturnMessageOK();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return CommonMethod.getReturnMessageOK();
    }
    public DataResponse uploadPhotoBlobWeb(Map<String,Object> pars, MultipartFile file) {
        try {
            String personId = CommonMethod.getString(pars, "remoteFile");
            InputStream in = file.getInputStream();
            int size = (int) file.getSize();
            byte[] data = new byte[size];
            int len =  in.read(data);
            in.close();
            return uploadPhotoBlob(data,personId);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return CommonMethod.getReturnMessageOK();
    }

}
