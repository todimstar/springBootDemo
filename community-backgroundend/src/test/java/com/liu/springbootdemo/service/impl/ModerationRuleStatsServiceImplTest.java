package com.liu.springbootdemo.service.impl;

import com.liu.springbootdemo.POJO.vo.ModerationRuleStatsVO;
import com.liu.springbootdemo.mapper.ModerationRuleStatsMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 规则命中统计服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class ModerationRuleStatsServiceImplTest {

    @Mock
    private ModerationRuleStatsMapper moderationRuleStatsMapper;

    @InjectMocks
    private ModerationRuleStatsServiceImpl moderationRuleStatsService;

    // ============ incrementHitCounts 测试 ============

    @Nested
    class IncrementHitCountsTests {

        @Test
        void incrementHitCounts_singleRule_callsMapper() {
            moderationRuleStatsService.incrementHitCounts(List.of(1L));

            verify(moderationRuleStatsMapper).incrementHitCount(1L);
        }

        @Test
        void incrementHitCounts_multipleRules_callsMapperForEach() {
            moderationRuleStatsService.incrementHitCounts(Arrays.asList(1L, 2L, 3L));

            verify(moderationRuleStatsMapper).incrementHitCount(1L);
            verify(moderationRuleStatsMapper).incrementHitCount(2L);
            verify(moderationRuleStatsMapper).incrementHitCount(3L);
        }

        @Test
        void incrementHitCounts_emptyList_doesNotCallMapper() {
            moderationRuleStatsService.incrementHitCounts(Collections.emptyList());

            verify(moderationRuleStatsMapper, never()).incrementHitCount(anyLong());
        }

        @Test
        void incrementHitCounts_nullList_doesNotCallMapper() {
            moderationRuleStatsService.incrementHitCounts(null);

            verify(moderationRuleStatsMapper, never()).incrementHitCount(anyLong());
        }

        @Test
        void incrementHitCounts_mapperThrows_doesNotPropagate() {
            doThrow(new RuntimeException("DB error")).when(moderationRuleStatsMapper).incrementHitCount(1L);

            assertDoesNotThrow(() -> moderationRuleStatsService.incrementHitCounts(List.of(1L, 2L)));

            // 第一个失败，第二个仍然执行
            verify(moderationRuleStatsMapper).incrementHitCount(1L);
            verify(moderationRuleStatsMapper).incrementHitCount(2L);
        }
    }

    // ============ getStatsByRuleId 测试 ============

    @Nested
    class GetStatsByRuleIdTests {

        @Test
        void getStatsByRuleId_found_returnsStats() {
            ModerationRuleStatsVO vo = createStatsVO(1L, "keyword", "赌博", 150);
            when(moderationRuleStatsMapper.findByRuleId(1L)).thenReturn(vo);

            ModerationRuleStatsVO result = moderationRuleStatsService.getStatsByRuleId(1L);

            assertNotNull(result);
            assertEquals(1L, result.getRuleId());
            assertEquals("keyword", result.getRuleType());
            assertEquals("赌博", result.getPattern());
            assertEquals(150, result.getHitCount());
        }

        @Test
        void getStatsByRuleId_notFound_returnsNull() {
            when(moderationRuleStatsMapper.findByRuleId(999L)).thenReturn(null);

            ModerationRuleStatsVO result = moderationRuleStatsService.getStatsByRuleId(999L);

            assertNull(result);
        }
    }

    // ============ getAllStats 测试 ============

    @Nested
    class GetAllStatsTests {

        @Test
        void getAllStats_hasData_returnsAll() {
            ModerationRuleStatsVO vo1 = createStatsVO(1L, "keyword", "赌博", 150);
            ModerationRuleStatsVO vo2 = createStatsVO(2L, "regex", "v信.*", 80);
            ModerationRuleStatsVO vo3 = createStatsVO(3L, "blacklist", "evil.com", 0);
            when(moderationRuleStatsMapper.findAllStats()).thenReturn(Arrays.asList(vo1, vo2, vo3));

            List<ModerationRuleStatsVO> results = moderationRuleStatsService.getAllStats();

            assertEquals(3, results.size());
            assertEquals(150, results.get(0).getHitCount());
            assertEquals(80, results.get(1).getHitCount());
            assertEquals(0, results.get(2).getHitCount());
        }

        @Test
        void getAllStats_noData_returnsEmptyList() {
            when(moderationRuleStatsMapper.findAllStats()).thenReturn(Collections.emptyList());

            List<ModerationRuleStatsVO> results = moderationRuleStatsService.getAllStats();

            assertNotNull(results);
            assertTrue(results.isEmpty());
        }
    }

    // ============ 辅助方法 ============

    private ModerationRuleStatsVO createStatsVO(Long ruleId, String ruleType, String pattern, long hitCount) {
        return new ModerationRuleStatsVO(ruleId, ruleType, pattern, "测试规则",
                true, hitCount, hitCount > 0 ? LocalDateTime.now() : null);
    }
}
