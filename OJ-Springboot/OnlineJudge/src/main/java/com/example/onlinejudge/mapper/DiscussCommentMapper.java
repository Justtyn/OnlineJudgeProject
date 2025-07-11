// DiscussCommentMapper.java
package com.example.onlinejudge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.onlinejudge.entity.DiscussComment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DiscussCommentMapper extends BaseMapper<DiscussComment> {

    /** 查询某讨论所有评论（按时间升序） */
    @Select("SELECT * FROM discuss_comment WHERE discuss_id = #{discussId} ORDER BY create_time ASC")
    List<DiscussComment> selectByDiscussId(Integer discussId);

    /** 插入新评论 */
    @Insert("INSERT INTO discuss_comment(discuss_id, user_id, parent_comment_id, reply_to_user_id, content) " +
            "VALUES(#{discussId}, #{userId}, #{parentCommentId}, #{replyToUserId}, #{content})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DiscussComment comment);

    /** 更新评论内容 */
    @Update("UPDATE discuss_comment SET content = #{content} WHERE id = #{id}")
    void update(DiscussComment comment);

    /** 删除评论 */
    @Delete("DELETE FROM discuss_comment WHERE id = #{id}")
    void deleteById(Integer id);
}
