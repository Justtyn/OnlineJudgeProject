// DiscussMapper.java
package com.example.onlinejudge.mapper;

import com.example.onlinejudge.entity.Discuss;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DiscussMapper {

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
    void insert(Discuss discuss);

    /** 更新讨论（标题/内容/关联题目）*/
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
}
