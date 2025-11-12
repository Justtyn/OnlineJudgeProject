package com.example.onlinejudge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.onlinejudge.entity.Problem;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * ProblemMapper 接口
 * 负责处理与题目(Problem)相关的数据库操作
 * 使用 MyBatis 注解方式定义 SQL 语句
 */
@Mapper
public interface ProblemMapper extends BaseMapper<Problem> {

    /**
     * 分页查询所有题目
     *
     * @param pageNum  页码(从0开始的偏移量)
     * @param pageSize 每页显示的记录数
     * @return 返回指定页的题目列表
     */
    @Select("SELECT * FROM oj_problem ORDER BY id LIMIT #{pageNum}, #{pageSize}")
    List<Problem> selectPage(@Param("pageNum") Integer pageNum, @Param("pageSize") Integer pageSize);

    /**
     * 查询题目总数
     *
     * @return 返回题目总数
     */
    @Select("SELECT COUNT(*) FROM oj_problem")
    Long selectTotal();

    /**
     * 根据ID查询题目
     *
     * @param id 题目ID
     * @return 返回指定ID的题目
     */
    @Select("SELECT * FROM oj_problem WHERE id = #{id}")
    Problem selectById(Integer id);

    /**
     * 根据题目名称模糊查询并分页
     *
     * @param name     题目名称(支持模糊查询)
     * @param pageNum  页码(从0开始的偏移量)
     * @param pageSize 每页显示的记录数
     * @return 返回符合条件的题目列表
     */
    @Select("SELECT * FROM oj_problem WHERE name LIKE CONCAT('%', #{name}, '%') ORDER BY id LIMIT #{pageNum}, #{pageSize}")
    List<Problem> selectByName(@Param("name") String name, @Param("pageNum") Integer pageNum, @Param("pageSize") Integer pageSize);

    /**
     * 根据题目名称查询符合条件的总记录数
     *
     * @param name 题目名称(支持模糊查询)
     * @return 返回符合条件的记录总数
     */
    @Select("SELECT COUNT(*) FROM oj_problem WHERE name LIKE CONCAT('%', #{name}, '%')")
    Long selectTotalByName(String name);

    /**
     * 根据出题人模糊查询并分页
     *
     * @param setter   出题人(支持模糊查询)
     * @param pageNum  页码(从0开始的偏移量)
     * @param pageSize 每页显示的记录数
     * @return 返回符合条件的题目列表
     */
    @Select("SELECT * FROM oj_problem WHERE setter LIKE CONCAT('%', #{setter}, '%') ORDER BY id LIMIT #{pageNum}, #{pageSize}")
    List<Problem> selectBySetter(@Param("setter") String setter, @Param("pageNum") Integer pageNum, @Param("pageSize") Integer pageSize);

    /**
     * 根据出题人查询符合条件的总记录数
     *
     * @param setter 出题人(支持模糊查询)
     * @return 返回符合条件的记录总数
     */
    @Select("SELECT COUNT(*) FROM oj_problem WHERE setter LIKE CONCAT('%', #{setter}, '%')")
    Long selectTotalBySetter(String setter);

    /**
     * 插入新题目
     * 使用 @Options 注解获取自动生成的主键值并赋值给 problem 对象的 id 属性
     *
     * @param problem 要插入的题目对象
     */
    @Insert("INSERT INTO oj_problem(name, setter, create_time, ac_count, submit_count, `desc`, desc_input, desc_output, sample_input, sample_output, hint) " +
            "VALUES(#{name}, #{setter}, #{createTime}, #{acCount}, #{submitCount}, #{desc}, #{descInput}, #{descOutput}, #{sampleInput}, #{sampleOutput}, #{hint})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Problem problem);

    /**
     * 更新题目信息
     *
     * @param problem 包含更新信息的题目对象
     */
    @Update("UPDATE oj_problem SET name = #{name}, setter = #{setter}, create_time = #{createTime}, " +
            "ac_count = #{acCount}, submit_count = #{submitCount}, `desc` = #{desc}, " +
            "desc_input = #{descInput}, desc_output = #{descOutput}, " +
            "sample_input = #{sampleInput}, sample_output = #{sampleOutput}, hint = #{hint} " +
            "WHERE id = #{id}")
    void update(Problem problem);

    /**
     * 根据ID删除题目
     *
     * @param id 要删除的题目ID
     */
    @Delete("DELETE FROM oj_problem WHERE id = #{id}")
    void deleteById(Integer id);

    /**
     * 原子性地增加提交次数
     *
     * @param id 题目ID
     */
    @Update("UPDATE oj_problem SET submit_count = submit_count + 1 WHERE id = #{id}")
    void incrementSubmitCount(Integer id);

    /**
     * 原子性地增加通过次数
     *
     * @param id 题目ID
     */
    @Update("UPDATE oj_problem SET ac_count = ac_count + 1 WHERE id = #{id}")
    void incrementAcCount(Integer id);

    /**
     * 查询所有题目
     *
     * @return 返回所有题目列表
     */
    @Select("SELECT * FROM oj_problem ORDER BY id")
    List<Problem> selectAll();

    /**
     * 查询所有题目，只返回id和name字段
     *
     * @return 返回所有题目的id和name列表
     */
    @Select("SELECT id, name FROM oj_problem ORDER BY id")
    List<Problem> selectAllSimple();

    /**
     * 获取每日挑战题目
     *
     * @return 返回10道随机题目
     */
    @Select("SELECT * FROM oj_problem WHERE daily_challenge = 'FALSE' ORDER BY RAND() LIMIT 10")
    List<Problem> selectDailyChallenge();

    /**
     * 更新题目的每日挑战状态
     *
     * @param id             题目ID
     * @param dailyChallenge 是否为每日挑战题，"TRUE"或"FALSE"
     */
    @Update("UPDATE oj_problem SET daily_challenge = #{dailyChallenge} WHERE id = #{id}")
    void updateDailyChallengeStatus(@Param("id") Integer id, @Param("dailyChallenge") String dailyChallenge);

    /**
     * 重置所有题目的每日挑战状态为FALSE
     */
    @Update("UPDATE oj_problem SET daily_challenge = 'FALSE'")
    void resetDailyChallengeStatus();

    /**
     * 获取当前选中的每日挑战题目
     *
     * @return 返回当前选中的题目列表
     */
    @Select("SELECT * FROM oj_problem WHERE daily_challenge = 'TRUE'")
    List<Problem> selectCurrentDailyChallenge();

    @Select("SELECT difficulty, COUNT(*) as count " +
            "FROM oj_problem " +
            "GROUP BY difficulty")
    Map<String, Long> selectDifficultyDistribution();

    @Select("SELECT difficulty, " +
            "ROUND(AVG(CASE WHEN submit_count > 0 " +
            "THEN (ac_count * 100.0 / submit_count) ELSE 0 END), 2) as avg_pass_rate " +
            "FROM oj_problem " +
            "GROUP BY difficulty")
    Map<String, Double> selectAvgPassRateByDifficulty();

    @Select("SELECT id, title, difficulty, " +
            "submit_count, ac_count, " +
            "CASE WHEN submit_count > 0 " +
            "THEN ROUND(ac_count * 100.0 / submit_count, 2) " +
            "ELSE 0 END as pass_rate " +
            "FROM oj_problem " +
            "WHERE submit_count >= 10 " +  // 至少有10次提交才统计
            "ORDER BY pass_rate ASC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectHardestProblems(int limit);

    @Select("SELECT * FROM oj_problem WHERE name LIKE CONCAT('%', #{name}, '%') ORDER BY id")
    List<Problem> selectAllByName(@Param("name") String name);

    /**
     * 根据题目名称和内容进行模糊查询
     *
     * @param keyword 查询关键词(支持模糊查询)
     * @return 返回符合条件的题目列表
     */
    @Select("SELECT * FROM oj_problem WHERE name LIKE CONCAT('%', #{keyword}, '%') OR `desc` LIKE CONCAT('%', #{keyword}, '%') ORDER BY id")
    List<Problem> selectByKeyword(@Param("keyword") String keyword);
} 