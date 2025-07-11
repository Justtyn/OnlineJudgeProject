package com.example.onlinejudge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.onlinejudge.entity.Homework;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface HomeworkMapper extends BaseMapper<Homework> {

        @Select({
                        "SELECT",
                        "  CASE",
                        "    WHEN t.submission_rate < 20  THEN '0-20%'",
                        "    WHEN t.submission_rate < 40  THEN '20-40%'",
                        "    WHEN t.submission_rate < 60  THEN '40-60%'",
                        "    WHEN t.submission_rate < 80  THEN '60-80%'",
                        "    ELSE '80-100%'",
                        "  END AS submission_rate_range,",
                        "  COUNT(*) AS cnt",
                        "FROM (",
                        "  SELECT",
                        "    h.id,",
                        "    CASE WHEN sc.student_count > 0",
                        "      THEN ps.sub_count * 100.0 / sc.student_count",
                        "      ELSE 0",
                        "    END AS submission_rate",
                        "  FROM homework h",
                        "  /* 统计每道作业下所有题目的总提交数 */",
                        "  LEFT JOIN (",
                        "    SELECT hp.homework_id, COUNT(*) AS sub_count",
                        "    FROM homework_problem hp",
                        "    JOIN oj_status st ON st.problem_id = hp.problem_id",
                        "    GROUP BY hp.homework_id",
                        "  ) ps ON h.id = ps.homework_id",
                        "  /* 查询每个作业对应班级的学生总数 */",
                        "  LEFT JOIN (",
                        "    SELECT class_id, COUNT(*) AS student_count",
                        "    FROM student",
                        "    GROUP BY class_id",
                        "  ) sc ON h.class_id = sc.class_id",
                        ") AS t",
                        "GROUP BY submission_rate_range"
        })
        List<Map<String, Object>> selectSubmissionRateDistribution();

        @Select({
                        "SELECT",
                        "  CASE",
                        "    WHEN avg_mins <= 30   THEN '0-30分钟'",
                        "    WHEN avg_mins <= 60   THEN '31-60分钟'",
                        "    WHEN avg_mins <= 120  THEN '1-2小时'",
                        "    WHEN avg_mins <= 1440 THEN '2-24小时'",
                        "    ELSE '24小时以上'",
                        "  END AS avg_completion_time_range,",
                        "  COUNT(*) AS cnt",
                        "FROM (",
                        "  SELECT",
                        "    h.id,",
                        "    AVG(TIMESTAMPDIFF(MINUTE, h.start_time, st.first_ac_time)) AS avg_mins",
                        "  FROM homework h",
                        "  JOIN homework_problem hp ON h.id = hp.homework_id",
                        "  JOIN (",
                        "    SELECT problem_id, user_id, MIN(creat_time) AS first_ac_time",
                        "    FROM oj_status",
                        "    WHERE result = 'AC'",
                        "    GROUP BY problem_id, user_id",
                        "  ) st ON st.problem_id = hp.problem_id",
                        "  GROUP BY h.id",
                        ") t",
                        "GROUP BY avg_completion_time_range"
        })
        List<Map<String, Object>> selectAvgCompletionTimeDistribution();

        @Select({
                        "SELECT",
                        "  difficulty,",
                        "  COUNT(*) AS cnt",
                        "FROM homework",
                        "GROUP BY difficulty"
        })
        List<Map<String, Object>> selectDifficultyDistribution();

        @Select("SELECT c.name as course_name, " +
                        "COUNT(h.id) as homework_count, " +
                        "AVG(CASE WHEN h.student_count > 0 " +
                        "THEN h.submission_count * 100.0 / h.student_count ELSE 0 END) as avg_submission_rate " +
                        "FROM course c " +
                        "LEFT JOIN homework h ON c.id = h.course_id " +
                        "GROUP BY c.id")
        List<Map<String, Object>> selectCourseHomeworkStats();

        @Select({
                        "SELECT",
                        "  DATE(st.creat_time) AS date,",
                        "  COUNT(*)            AS cnt",
                        "FROM oj_status st",
                        "  JOIN homework_problem hp ON st.problem_id = hp.problem_id",
                        "WHERE st.creat_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)",
                        "GROUP BY date",
                        "ORDER BY date"
        })
        List<Map<String, Object>> selectWeeklySubmissionTrend();

        @Select("SELECT COUNT(*) FROM homework WHERE status = 'COMPLETED'")
        long getCompletedCount();

        @Select("SELECT COUNT(*) FROM homework WHERE status = 'ONGOING'")
        long getOngoingCount();

        @Select("SELECT AVG(CASE WHEN student_count > 0 " +
                        "THEN (submission_count * 100.0 / student_count) " +
                        "ELSE 0 END) " +
                        "FROM homework")
        double getAvgCompletionRate();
}
