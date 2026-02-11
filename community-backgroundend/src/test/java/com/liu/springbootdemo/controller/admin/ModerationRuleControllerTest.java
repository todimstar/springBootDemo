package com.liu.springbootdemo.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liu.springbootdemo.POJO.Result.PageResult;
import com.liu.springbootdemo.POJO.dto.request.ModerationRuleCreateDTO;
import com.liu.springbootdemo.POJO.dto.request.ModerationRuleUpdateDTO;
import com.liu.springbootdemo.POJO.vo.ModerationRuleVO;
import com.liu.springbootdemo.common.enums.ErrorCode;
import com.liu.springbootdemo.common.exception.BusinessException;
import com.liu.springbootdemo.common.exception.GlobalExceptionHandler;
import com.liu.springbootdemo.service.ModerationAuditLogService;
import com.liu.springbootdemo.service.ModerationQueueService;
import com.liu.springbootdemo.service.ModerationRuleService;
import com.liu.springbootdemo.service.ModerationRuleStatsService;
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
 * 审核规则管理接口测试 (Controller 层)
 */
@ExtendWith(MockitoExtension.class)
class ModerationRuleControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ModerationQueueService moderationQueueService;

    @Mock
    private ModerationAuditLogService moderationAuditLogService;

    @Mock
    private ModerationRuleService moderationRuleService;

    @Mock
    private ModerationRuleStatsService moderationRuleStatsService;

    @InjectMocks
    private ModerationController moderationController;

    private static final String RULES_URL = "/api/admin/moderation/rules";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(moderationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ============ 分页查询规则列表测试 ============

    @Nested
    class PageQueryRulesTests {

        @Test
        void pageQueryRules_success() throws Exception {
            ModerationRuleVO vo = createRuleVO(1L, "keyword", "赌博", 80);
            PageResult pageResult = new PageResult(1L, List.of(vo));
            when(moderationRuleService.pageQuery(1, 10, null, null)).thenReturn(pageResult);

            mockMvc.perform(get(RULES_URL)
                            .param("page", "1")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.total").value(1))
                    .andExpect(jsonPath("$.data.results[0].ruleType").value("keyword"))
                    .andExpect(jsonPath("$.data.results[0].pattern").value("赌博"));
        }

        @Test
        void pageQueryRules_withRuleTypeFilter() throws Exception {
            ModerationRuleVO vo = createRuleVO(1L, "regex", "v信.*", 60);
            PageResult pageResult = new PageResult(1L, List.of(vo));
            when(moderationRuleService.pageQuery(1, 10, "regex", null)).thenReturn(pageResult);

            mockMvc.perform(get(RULES_URL)
                            .param("page", "1")
                            .param("pageSize", "10")
                            .param("ruleType", "regex"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.results[0].ruleType").value("regex"));
        }

        @Test
        void pageQueryRules_withEnabledFilter() throws Exception {
            PageResult pageResult = new PageResult(0L, List.of());
            when(moderationRuleService.pageQuery(1, 10, null, false)).thenReturn(pageResult);

            mockMvc.perform(get(RULES_URL)
                            .param("page", "1")
                            .param("pageSize", "10")
                            .param("enabled", "false"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.total").value(0));
        }
    }

    // ============ 新增规则测试 ============

    @Nested
    class CreateRuleTests {

        @Test
        void createRule_success() throws Exception {
            ModerationRuleCreateDTO dto = new ModerationRuleCreateDTO();
            dto.setRuleType("keyword");
            dto.setPattern("赌博");
            dto.setWeightScore(80);
            dto.setEnabled(true);
            dto.setDescription("赌博关键词");

            ModerationRuleVO vo = createRuleVO(1L, "keyword", "赌博", 80);
            when(moderationRuleService.createRule("keyword", "赌博", 80, true, "赌博关键词")).thenReturn(vo);

            mockMvc.perform(post(RULES_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.ruleType").value("keyword"))
                    .andExpect(jsonPath("$.data.pattern").value("赌博"));
        }

        @Test
        void createRule_emptyRuleType_returns400() throws Exception {
            ModerationRuleCreateDTO dto = new ModerationRuleCreateDTO();
            dto.setRuleType("");
            dto.setPattern("赌博");
            dto.setWeightScore(80);

            mockMvc.perform(post(RULES_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void createRule_emptyPattern_returns400() throws Exception {
            ModerationRuleCreateDTO dto = new ModerationRuleCreateDTO();
            dto.setRuleType("keyword");
            dto.setPattern("");
            dto.setWeightScore(80);

            mockMvc.perform(post(RULES_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void createRule_nullWeightScore_returns400() throws Exception {
            String json = "{\"ruleType\":\"keyword\",\"pattern\":\"test\"}";

            mockMvc.perform(post(RULES_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void createRule_weightScoreExceeds100_returns400() throws Exception {
            ModerationRuleCreateDTO dto = new ModerationRuleCreateDTO();
            dto.setRuleType("keyword");
            dto.setPattern("test");
            dto.setWeightScore(101);

            mockMvc.perform(post(RULES_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void createRule_invalidRuleType_returns400() throws Exception {
            ModerationRuleCreateDTO dto = new ModerationRuleCreateDTO();
            dto.setRuleType("invalid");
            dto.setPattern("test");
            dto.setWeightScore(50);

            when(moderationRuleService.createRule(eq("invalid"), anyString(), anyInt(), anyBoolean(), any()))
                    .thenThrow(new BusinessException(ErrorCode.PARAM_ERROR, "非法规则类型: invalid"));

            mockMvc.perform(post(RULES_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void createRule_noRequestBody_returns400() throws Exception {
            mockMvc.perform(post(RULES_URL)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    // ============ 修改规则测试 ============

    @Nested
    class UpdateRuleTests {

        @Test
        void updateRule_success() throws Exception {
            ModerationRuleUpdateDTO dto = new ModerationRuleUpdateDTO();
            dto.setPattern("赌博网站");
            dto.setWeightScore(90);

            ModerationRuleVO vo = createRuleVO(1L, "keyword", "赌博网站", 90);
            when(moderationRuleService.updateRule(eq(1L), isNull(), eq("赌博网站"), eq(90), isNull(), isNull()))
                    .thenReturn(vo);

            mockMvc.perform(put(RULES_URL + "/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.pattern").value("赌博网站"))
                    .andExpect(jsonPath("$.data.weightScore").value(90));
        }

        @Test
        void updateRule_notFound_returns400() throws Exception {
            ModerationRuleUpdateDTO dto = new ModerationRuleUpdateDTO();
            dto.setPattern("test");

            when(moderationRuleService.updateRule(eq(999L), any(), any(), any(), any(), any()))
                    .thenThrow(new BusinessException(ErrorCode.PARAM_ERROR, "规则不存在"));

            mockMvc.perform(put(RULES_URL + "/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void updateRule_weightScoreOutOfRange_returns400() throws Exception {
            ModerationRuleUpdateDTO dto = new ModerationRuleUpdateDTO();
            dto.setWeightScore(101);

            mockMvc.perform(put(RULES_URL + "/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ============ 删除规则测试 ============

    @Nested
    class DeleteRuleTests {

        @Test
        void deleteRule_success() throws Exception {
            doNothing().when(moderationRuleService).deleteRule(1L);

            mockMvc.perform(delete(RULES_URL + "/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0));

            verify(moderationRuleService).deleteRule(1L);
        }

        @Test
        void deleteRule_notFound_returns400() throws Exception {
            doThrow(new BusinessException(ErrorCode.PARAM_ERROR, "规则不存在"))
                    .when(moderationRuleService).deleteRule(999L);

            mockMvc.perform(delete(RULES_URL + "/999"))
                    .andExpect(status().isBadRequest());
        }
    }

    // ============ 辅助方法 ============

    private ModerationRuleVO createRuleVO(Long id, String ruleType, String pattern, int weightScore) {
        return new ModerationRuleVO(id, ruleType, pattern, weightScore, true,
                "测试规则", LocalDateTime.of(2026, 2, 11, 20, 0), LocalDateTime.of(2026, 2, 11, 20, 0));
    }
}
