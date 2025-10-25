// DiscussMapper.java
package com.example.onlinejudge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.onlinejudge.entity.Discuss;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;
import java.time.LocalDate;

@Mapper
public interface DiscussMapper extends BaseMapper<Discuss> {

        /** 分页倒序查询所有讨论 */
        @Select("SELECT * FROM discuss ORDER BY create_time DESC LIMIT #{offset}, #{pageSize}")
        List<Discuss> selectPage(@Param("offset") Integer offset,
                        @Param("pageSize") Integer pageSize);

        /** 查询讨论总数 */
        @Select("SELECT COUNT(*) FROM discuss")
        Long selectTotal();

        /** 根据 ID 查询讨论 */
        @Select("SELECT * FROM discuss WHERE id = #{id}")
        Discuss selectById(Integer id);

        /** 浏览量自增 */
        @Update("UPDATE discuss SET view_num = view_num + 1 WHERE id = #{id}")
        void incrementViewNum(Integer id);

        /** 插入新讨论，自动回填 id */
        @Insert("INSERT INTO discuss(user_id, problem_id, title, content) " +
                        "VALUES(#{userId}, #{problemId}, #{title}, #{content})")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insert(Discuss discuss);

        /** 更新讨论（标题/内容/关联题目） */
        @Update("UPDATE discuss SET problem_id=#{problemId}, title=#{title}, content=#{content} " +
                        "WHERE id = #{id}")
        void update(Discuss discuss);

        /** 删除讨论 */
        @Delete("DELETE FROM discuss WHERE id = #{id}")
        void deleteById(Integer id);

        /** 查询所有讨论（倒序） */
        @Select("SELECT * FROM discuss ORDER BY create_time DESC")
        List<Discuss> selectAll();

        /** 根据标题模糊查询 */
        @Select("SELECT * FROM discuss WHERE title LIKE CONCAT('%', #{title}, '%') ORDER BY create_time DESC")
        List<Discuss> selectByTitle(String title);

        /** 根据题目ID查询讨论 */
        @Select("SELECT * FROM discuss WHERE problem_id = #{problemId} ORDER BY create_time DESC")
        List<Discuss> selectByProblemId(Integer problemId);

        @Select("SELECT topic, COUNT(*) as count FROM discuss GROUP BY topic")
        List<Map<String, Object>> selectTopicDistribution();

        @Select("SELECT DATE(create_time) AS date, COUNT(*) AS count " +
                        "FROM discuss " +
                        "WHERE create_time >= #{startDate} " +
                        "GROUP BY DATE(create_time) " +
                        "ORDER BY date")
        List<Map<String, Object>> selectDailyDiscussionTrend(
                        @Param("startDate") LocalDate startDate);

        @Select("SELECT d.id, d.title, d.topic, " +
                        "u.username as author, " +
                        "COUNT(dc.id) as comment_count, " +
                        "d.view_num as view_count, " +
                        "d.create_time " +
                        "FROM discuss d " +
                        "LEFT JOIN discuss_comment dc ON d.id = dc.discuss_id " +
                        "LEFT JOIN student u ON d.user_id = u.id " +
                        "GROUP BY d.id " +
                        "ORDER BY comment_count DESC, view_count DESC " +
                        "LIMIT #{limit}")
        List<Map<String, Object>> selectHotDiscussions(int limit);

        @Select("SELECT COUNT(*) FROM discuss WHERE create_time BETWEEN #{startDate} AND #{endDate}")
        long countNewDiscussions(
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        @Select("SELECT u.username, " +
                        "COUNT(DISTINCT d.id) as discuss_count, " +
                        "COUNT(DISTINCT dc.id) as comment_count, " +
                        "SUM(d.view_num) as total_views " +
                        "FROM student u " +
                        "LEFT JOIN discuss d ON u.id = d.user_id " +
                        "LEFT JOIN discuss_comment dc ON u.id = dc.user_id " +
                        "GROUP BY u.id " +
                        "HAVING discuss_count > 0 OR comment_count > 0 " +
                        "ORDER BY discuss_count DESC, comment_count DESC " +
                        "LIMIT #{limit}")
        List<Map<String, Object>> selectUserParticipationRanking(int limit);

        @Select("SELECT " +
                        "CASE " +
                        "  WHEN minutes <= 5 THEN '5分钟内' " +
                        "  WHEN minutes <= 30 THEN '30分钟内' " +
                        "  WHEN minutes <= 60 THEN '1小时内' " +
                        "  WHEN minutes <= 1440 THEN '24小时内' " +
                        "  ELSE '24小时以上' " +
                        "END as range, " +
                        "COUNT(*) as count " +
                        "FROM (" +
                        "  SELECT TIMESTAMPDIFF(MINUTE, d.create_time, dc.create_time) as minutes " +
                        "  FROM discuss d " +
                        "  JOIN discuss_comment dc ON d.id = dc.discuss_id" +
                        ") t " +
                        "GROUP BY range")
        List<Map<String, Object>> selectReplyTimeDistribution();

        @Select("SELECT COUNT(*) FROM discuss_comment")
        Long getTotalComments();

        @Select("SELECT COUNT(DISTINCT user_id) FROM " +
                        "(SELECT user_id FROM discuss " +
                        "UNION " +
                        "SELECT user_id FROM discuss_comment) t")
        Long getTotalParticipants();

        @Select("SELECT COUNT(*) FROM discuss " +
                        "WHERE DATE(create_time) = CURDATE()")
        Long getTodayNewDiscussions();
}
