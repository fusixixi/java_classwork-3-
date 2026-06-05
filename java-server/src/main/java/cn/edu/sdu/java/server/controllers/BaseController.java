package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.payload.response.MyTreeNode;
import cn.edu.sdu.java.server.payload.response.OptionItemList;
import cn.edu.sdu.java.server.services.BaseService;
import cn.edu.sdu.java.server.util.CommonMethod;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/base")
public class BaseController {
    private static final Logger log = LoggerFactory.getLogger(BaseController.class);
    private final BaseService baseService;

    public BaseController(BaseService baseService) {
        this.baseService = baseService;
    }

    @PostMapping("/getMenuList")
    public DataResponse getMenuList(@Valid @RequestBody DataRequest dataRequest) {
        return baseService.getMenuList(dataRequest);
    }

    @PostMapping("/getDataBaseUserName")
    public DataResponse getDataBaseUserName(@Valid @RequestBody DataRequest dataRequest) {
        return baseService.getDataBaseUserName();
    }

    @PostMapping("/getDictionaryTreeNodeList")
    public DataResponse getDictionaryTreeNodeList(@Valid @RequestBody DataRequest dataRequest) {
        return CommonMethod.getReturnData(baseService.getDictionaryTreeNodeList());
    }

    @PostMapping("/getMenuTreeNodeList")
    public DataResponse getMenuTreeNodeList(@Valid @RequestBody DataRequest dataRequest) {
        return CommonMethod.getReturnData(baseService.getMenuTreeNodeList());
    }

    @PostMapping("/deleteDictionary")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public DataResponse deleteDictionary(@Valid @RequestBody DataRequest dataRequest) {
        return baseService.deleteDictionary(dataRequest);
    }

    @PostMapping("/dictionarySave")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public DataResponse dictionarySave(@Valid @RequestBody DataRequest dataRequest) {
        return baseService.dictionarySave(dataRequest);
    }

    @PostMapping("/getDictionaryOptionItemList")
    public OptionItemList getDictionaryOptionItemList(@Valid @RequestBody DataRequest dataRequest) {
        return baseService.getDictionaryOptionItemList(dataRequest);
    }

    @PostMapping("/getRoleOptionItemList")
    public OptionItemList getRoleOptionItemList(@Valid @RequestBody DataRequest dataRequest) {
        return baseService.getRoleOptionItemList(dataRequest);
    }

    @PostMapping("/menuDelete")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public DataResponse menuDelete(@Valid @RequestBody DataRequest dataRequest) {
        return baseService.menuDelete(dataRequest);
    }

    @PostMapping("/menuSave")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public DataResponse menuSave(@Valid @RequestBody DataRequest dataRequest) {
        return baseService.menuSave(dataRequest);
    }

    @PostMapping("/getFileByteData")
    public ResponseEntity<StreamingResponseBody> getFileByteData(@Valid @RequestBody DataRequest dataRequest) {
        return baseService.getFileByteData(dataRequest);
    }

    @PostMapping("/getBlobByteData")
    public ResponseEntity<StreamingResponseBody> getBlobByteData(@Valid @RequestBody DataRequest dataRequest) {
        return baseService.getBlobByteData(dataRequest);
    }

    @PostMapping("/updatePassword")
    public DataResponse updatePassword(@Valid @RequestBody DataRequest dataRequest) {
        return baseService.updatePassword(dataRequest);
    }

    @PostMapping("/uploadHtmlString")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public DataResponse uploadHtmlString(@Valid @RequestBody DataRequest dataRequest) {
        return baseService.uploadHtmlString(dataRequest);
    }

    @GetMapping("/htmlGetBaseHtmlPage")
    public ResponseEntity<StreamingResponseBody> htmlGetBaseHtmlPage(HttpServletRequest request) {
        return baseService.htmlGetBaseHtmlPage(request);
    }

    @PostMapping("/getPhotoImageStr")
    public DataResponse getPhotoImageStr(@Valid @RequestBody DataRequest dataRequest) {
        return baseService.getPhotoImageStr(dataRequest);
    }

    @PostMapping("/uploadPhotoWeb")
    public DataResponse uploadPhotoWeb(@RequestParam Map<String, Object> pars, @RequestParam("file") MultipartFile file) {
        return baseService.uploadPhotoWeb(pars, file);
    }

    @PostMapping("/uploadPhoto")
    public DataResponse uploadPhoto(@RequestBody byte[] barr,
                                    @RequestParam(name = "remoteFile") String remoteFile) {
        return baseService.uploadPhoto(barr, remoteFile);
    }

    @PostMapping("/uploadPhotoBlob")
    public DataResponse uploadPhotoBlob(@RequestBody byte[] barr,
                                        @RequestParam(name = "remoteFile") String remoteFile) {
        return baseService.uploadPhotoBlob(barr, remoteFile);
    }

    @PostMapping("/uploadPhotoBlobWeb")
    public DataResponse uploadPhotoBlobWeb(@RequestParam Map<String, Object> pars, @RequestParam("file") MultipartFile file) {
        return baseService.uploadPhotoBlobWeb(pars, file);
    }
}
