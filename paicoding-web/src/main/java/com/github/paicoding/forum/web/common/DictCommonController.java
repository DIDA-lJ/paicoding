package com.github.paicoding.forum.web.common;

import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.service.config.service.DictCommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 通用
 *
 * @author LouZai
 * @date 2022/9/19
 */
@RestController
@Slf4j
@RequestMapping(path = "common/")
public class DictCommonController {

    @Autowired
    private DictCommonService dictCommonService;

    @ResponseBody
    @GetMapping(path = "/dict")
    public ResVo<Map<String, Object>> list() {
        log.debug("获取字典");
        Map<String, Object> bannerDTOPageVo = dictCommonService.getDict();
        return ResVo.ok(bannerDTOPageVo);
    }
}
