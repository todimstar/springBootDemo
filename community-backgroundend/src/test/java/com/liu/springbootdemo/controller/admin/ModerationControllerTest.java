package com.liu.springbootdemo.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liu.springbootdemo.POJO.Result.PageResult;
import com.liu.springbootdemo.POJO.dto.request.ModerationActionDTO;
import com.liu.springbootdemo.POJO.vo.ModerationQueueVO;
import com.liu.springbootdemo.common.enums.ErrorCode;
import com.liu.springbootdemo.common.exception.BusinessException;
import com.liu.springbootdemo.common.exception.GlobalExceptionHandler;
import com.liu.springbootdemo.service.ModerationQueueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 审核管理接口测试 (Controller 层)
 * 使用 Standalone MockMvc 测试 HTTP 路由、参数校验、异常处理、响应格式
 */
@ExtendWith(MockitoExtension.class)
class ModerationControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ModerationQueueService moderationQueueService;

    @InjectMocks
    private ModerationController moderationController;

    private static final String BASE_URL = "/api/admin/moderation";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(moderationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ============ 分页查询接口测试 ============

    @Nested
    class PageQueryApiTests {

        @Test
        void pageQuery_success_returnsPageResult() throws Exception {
            ModerationQueueVO vo = createQueueVO(1L, "post", 80);
            PageResult pageResult = new PageResult(1L, List.of(vo));
            when(moderationQueueService.pageQuery(1, 10, null)).thenReturn(pageResult);

            mockMvc.perform(get(BASE_URL + "/queue")
                            .param("page", "1")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.total").value(1))
                    .andExpect(jsonPath("$.data.results[0].id").value(1))
                    .andExpect(jsonPath("$.data.results[0].riskScore").value(80))
                    .andExpect(jsonPath("$.data.results[0].targetType").value("post"));
        }

        @Test
        void pageQuery_withTargetTypeFilter_success() throws Exception {
            ModerationQueueVO vo = createQueueVO(2L, "comment", 50);
            PageResult pageResult = new PageResult(1L, List.of(vo));
            when(moderationQueueService.pageQuery(1, 10, "comment")).thenReturn(pageResult);

            mockMvc.perform(get(BASE_URL + "/queue")
                            .param("page", "1")
                            .param("pageSize", "10")
                            .param("targetType", "comment"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.total").value(1))
                    .andExpect(jsonPath("$.data.results[0].targetType").value("comment"));
        }

        @Test
        void pageQuery_defaultParams_success() throws Exception {
            PageResult pageResult = new PageResult(0L, List.of());
            // ModerationQueuePageQueryDTO 的 setPage(null) → 1, setPageSize(null) → 10
            when(moderationQueueService.pageQuery(eq(1), eq(10), isNull())).thenReturn(pageResult);

            mockMvc.perform(get(BASE_URL + "/queue")
                            .param("page", "1")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.total").value(0));
        }
    }

    // ============ 审核详情接口测试 ============

    @Nested
    class GetByIdApiTests {

        @Test
        void getById_success_returnsDetail() throws Exception {
            ModerationQueueVO vo = createQueueVO(1L, "post", 65);
            when(moderationQueueService.getById(1L)).thenReturn(vo);

            mockMvc.perform(get(BASE_URL + "/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.riskScore").value(65))
                    .andExpect(jsonPath("$.data.targetType").value("post"))
                    .andExpect(jsonPath("$.data.authorName").value("testuser"))
                    .andExpect(jsonPath("$.data.contentSummary").value("测试内容摘要"));
        }

        @Test
        void getById_notFound_returnsBadRequest() throws Exception {
            when(moderationQueueService.getById(999L))
                    .thenThrow(new BusinessException(ErrorCode.PARAM_ERROR, "审核记录不存在"));

            mockMvc.perform(get(BASE_URL + "/999"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(1));
        }
    }

    // ============ 审核操作接口测试 ============

    @Nested
    class ExecuteActionApiTests {

        @Test
        void executeAction_approve_success() throws Exception {
            ModerationActionDTO dto = new ModerationActionDTO();
            dto.setAction("approve");
            dto.setReason("内容正常，审核通过");

            doNothing().when(moderationQueueService).executeAction(1L, "approve", "内容正常，审核通过");

            mockMvc.perform(post(BASE_URL + "/1/action")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0));

            verify(moderationQueueService).executeAction(1L, "approve", "内容正常，审核通过");
        }

        @Test
        void executeAction_reject_success() throws Exception {
            ModerationActionDTO dto = new ModerationActionDTO();
            dto.setAction("reject");
            dto.setReason("违规内容");

            mockMvc.perform(post(BASE_URL + "/1/action")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0));

            verify(moderationQueueService).executeAction(1L, "reject", "违规内容");
        }

        @Test
        void executeAction_takedown_success() throws Exception {
            ModerationActionDTO dto = new ModerationActionDTO();
            dto.setAction("takedown");
            dto.setReason("严重违规需下架");

            mockMvc.perform(post(BASE_URL + "/1/action")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0));

            verify(moderationQueueService).executeAction(1L, "takedown", "严重违规需下架");
        }

        @Test
        void executeAction_shadowBan_success() throws Exception {
            ModerationActionDTO dto = new ModerationActionDTO();
            dto.setAction("shadow_ban");
            dto.setReason("隐蔽处理");

            mockMvc.perform(post(BASE_URL + "/1/action")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0));

            verify(moderationQueueService).executeAction(1L, "shadow_ban", "隐蔽处理");
        }

        @Test
        void executeAction_invalidAction_returns400() throws Exception {
            doThrow(new BusinessException(ErrorCode.PARAM_ERROR, "非法操作类型: invalid"))
                    .when(moderationQueueService).executeAction(eq(1L), eq("invalid"), anyString());

            ModerationActionDTO dto = new ModerationActionDTO();
            dto.setAction("invalid");
            dto.setReason("测试");

            mockMvc.perform(post(BASE_URL + "/1/action")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(1));
        }

        @Test
        void executeAction_emptyAction_returns400() throws Exception {
            String json = "{\"action\":\"\",\"reason\":\"理由\"}";

            mockMvc.perform(post(BASE_URL + "/1/action")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void executeAction_emptyReason_returns400() throws Exception {
            String json = "{\"action\":\"approve\",\"reason\":\"\"}";

            mockMvc.perform(post(BASE_URL + "/1/action")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void executeAction_nullAction_returns400() throws Exception {
            String json = "{\"reason\":\"理由\"}";

            mockMvc.perform(post(BASE_URL + "/1/action")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void executeAction_nullReason_returns400() throws Exception {
            String json = "{\"action\":\"approve\"}";

            mockMvc.perform(post(BASE_URL + "/1/action")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void executeAction_idempotent_success() throws Exception {
            // 幂等：已审核记录再次提交，服务层不抛异常
            doNothing().when(moderationQueueService).executeAction(anyLong(), anyString(), anyString());

            ModerationActionDTO dto = new ModerationActionDTO();
            dto.setAction("approve");
            dto.setReason("再次通过");

            mockMvc.perform(post(BASE_URL + "/1/action")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0));
        }

        @Test
        void executeAction_noRequestBody_returns400() throws Exception {
            mockMvc.perform(post(BASE_URL + "/1/action")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void executeAction_queueNotFound_returns400() throws Exception {
            doThrow(new BusinessException(ErrorCode.PARAM_ERROR, "审核记录不存在"))
                    .when(moderationQueueService).executeAction(eq(999L), eq("approve"), anyString());

            ModerationActionDTO dto = new ModerationActionDTO();
            dto.setAction("approve");
            dto.setReason("通过");

            mockMvc.perform(post(BASE_URL + "/999/action")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(1));
        }

        @Test
        void executeAction_postNotFound_returns404() throws Exception {
            doThrow(new BusinessException(ErrorCode.POST_NOT_FOUND, "关联帖子不存在"))
                    .when(moderationQueueService).executeAction(eq(1L), eq("approve"), anyString());

            ModerationActionDTO dto = new ModerationActionDTO();
            dto.setAction("approve");
            dto.setReason("通过");

            mockMvc.perform(post(BASE_URL + "/1/action")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(1));
        }
    }

    // ============ 辅助方法 ============

    private static ModerationQueueVO createQueueVO(Long id, String targetType, int riskScore) {
        ModerationQueueVO vo = new ModerationQueueVO();
        vo.setId(id);
        vo.setTargetId(id * 100);
        vo.setTargetType(targetType);
        vo.setAuthorId(1L);
        vo.setAuthorName("testuser");
        vo.setContentSummary("测试内容摘要");
        vo.setRiskScore(riskScore);
        vo.setHitRules("[{\"ruleId\":1,\"ruleType\":\"keyword\",\"hitSnippet\":\"敏感词\",\"weightScore\":50}]");
        vo.setStatus("pending");
        vo.setCreatedAt(LocalDateTime.now());
        vo.setUpdatedAt(LocalDateTime.now());
        return vo;
    }
}
