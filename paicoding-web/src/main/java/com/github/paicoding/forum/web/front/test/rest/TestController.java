package com.github.paicoding.forum.web.front.test.rest;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.exception.ForumAdviceException;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.Status;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.core.dal.DsAno;
import com.github.paicoding.forum.core.dal.DsSelectExecutor;
import com.github.paicoding.forum.core.dal.MasterSlaveDsEnum;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.core.util.EmailUtil;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.service.chatai.service.impl.chatgpt.ChatGptIntegration;
import com.github.paicoding.forum.service.statistics.service.StatisticsSettingService;
import com.github.paicoding.forum.web.front.test.vo.EmailReqVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用于一些功能测试的入口，默认都使用从库，不支持修改数据
 *
 * @author YiHui
 * @date 2023/3/19
 */
@Slf4j
@DsAno(MasterSlaveDsEnum.SLAVE)
@RestController
@RequestMapping(path = "test")
public class TestController {
    private AtomicInteger cnt = new AtomicInteger(1);

    @Autowired
    private ChatGptIntegration chatGptHelper;

    /**
     * 测试邮件发送
     *
     * @param req
     * @return
     */
    @Permission(role = UserRole.ADMIN)
    @RequestMapping(path = "email")
    public ResVo<String> email(EmailReqVo req) {
        if (StringUtils.isBlank(req.getTo()) || req.getTo().indexOf("@") <= 0) {
            return ResVo.fail(Status.newStatus(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "非法的邮箱接收人"));
        }
        if (StringUtils.isBlank(req.getTitle())) {
            req.setTitle("技术派的测试邮件发送");
        }
        if (StringUtils.isBlank(req.getContent())) {
            req.setContent("技术派的测试发送内容");
        } else {
            // 测试邮件内容，不支持发送邮件正文，避免出现垃圾情况
            req.setContent(StringEscapeUtils.escapeHtml4(req.getContent()));
        }

        boolean ans = EmailUtil.sendMail(req.getTitle(), req.getTo(), req.getContent());
        log.info("测试邮件发送，计数：{}，发送内容：{}", cnt.addAndGet(1), req);
        return ResVo.ok(String.valueOf(ans));
    }

    @RequestMapping(path = "alarm")
    public ResVo<String> alarm(String content) {
        content = StringEscapeUtils.escapeHtml4(content);
        log.error("测试异常报警: {}", content);
        return ResVo.ok("移除日志接收完成！");
    }

    @RequestMapping(path = "testControllerAdvice")
    @ResponseBody
    public String testControllerAdvice() {
        throw new ForumAdviceException(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "测试ControllerAdvice异常");
    }

    /**
     * 测试 Knife4j
     *
     * @return
     */
    @RequestMapping(value = "/testKnife4j", method = RequestMethod.POST)
    public String testKnife4j() {
        return "沉默王二又帅又丑";
    }

    // POST 请求，使用 HttpServletRequest 获取请求参数
    @PostMapping(path = "testPost")
    public String testPost(HttpServletRequest request) {
        String name = request.getParameter("name");
        String age = request.getParameter("age");
        return "name=" + name + ", age=" + age;
    }

    // POST 请求，使用 HttpServletRequest 获取请求参数，使用 JSON 把参数转为字符串
    @PostMapping(path = "testPostJson")
    public String testPostJson(HttpServletRequest request) {
        return JsonUtil.toStr(request.getParameterMap());
    }

    // POST 请求，使用 HttpServletRequest 获取 JSON 请求参数
    @PostMapping(path = "testPostJson2")
    public String testPostJson2(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();  // body中即是JSON格式的请求参数
    }

    @PostMapping(path = "testPostJson3")
    public String testPostJson3(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("testPostJson3 第一次: {}", sb);

        StringBuilder sb1 = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb1.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("testPostJson3 第二次: {}", sb1);

        return sb1.toString();  // body中即是JSON格式的请求参数
    }

    @Autowired
    private StatisticsSettingService statisticsSettingService;

    /**
     * 只读测试，如果有更新就会报错
     *
     * @return
     */
    @GetMapping(path = "ds/read")
    public String readOnly() {
        // 保存请求计数
        statisticsSettingService.saveRequestCount(ReqInfoContext.getReqInfo().getClientIp());
        return "使用从库：更新成功!";
    }

    /**
     * 只读测试，如果有更新就会报错
     *
     * @return
     */
    @GetMapping(path = "ds/write2")
    public String write2() {
        log.info("------------------- 业务逻辑进入 ----------------------------");
        long old = statisticsSettingService.getStatisticsCount().getPvCount();
        DsSelectExecutor.execute(MasterSlaveDsEnum.MASTER, () -> statisticsSettingService.saveRequestCount(ReqInfoContext.getReqInfo()
                .getClientIp()));
        // 保存请求计数
        long n = statisticsSettingService.getStatisticsCount().getPvCount();
        log.info("------------------- 业务逻辑结束 ----------------------------");
        return "编程式切换主库：更新成功! old=" + old + " new=" + n;
    }


    @DsAno(MasterSlaveDsEnum.MASTER)
    @GetMapping(path = "ds/write")
    public String write() {
        // 保存请求计数
        long old = statisticsSettingService.getStatisticsCount().getPvCount();
        statisticsSettingService.saveRequestCount(ReqInfoContext.getReqInfo().getClientIp());
        long n = statisticsSettingService.getStatisticsCount().getPvCount();
        return "使用主库：更新成功! old=" + old + " new=" + n;
    }
}
