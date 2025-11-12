package com.example.onlinejudge.controller;

import com.example.onlinejudge.common.Result;
import com.example.onlinejudge.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Api(tags = "数据分析统计接口")
@RestController
@RequestMapping("/analysis")
public class DataAnalysisController {

    @Resource
    private StudentService studentService;
    @Resource
    private ProblemService problemService;
    @Resource
    private StatusService statusService;
    @Resource
    private CourseService courseService;
    @Resource
    private HomeworkService homeworkService;
    @Resource
    private DiscussService discussService;
    @Resource
    private SolutionService solutionService;
    @Resource
    private AnnouncementService announcementService;
    @Resource
    private ScoreService scoreService;

    @ApiOperation("获取系统总体统计数据")
    @GetMapping("/overview")
    public Result getOverviewStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // 用户相关统计
            stats.put("totalStudents", studentService.count());

            // 题目相关统计
            stats.put("totalProblems", problemService.count());
            stats.put("totalSubmissions", statusService.count());

            // 班级和作业统计
            stats.put("totalClasses", scoreService.count());
            stats.put("totalHomeworks", homeworkService.count());

            // 讨论和题解统计
            stats.put("totalDiscussions", discussService.count());
            stats.put("totalSolutions", solutionService.count());

            // 公告统计
            stats.put("totalAnnouncements", announcementService.count());

            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("500", "获取统计数据失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取提交状态分析")
    @GetMapping("/submission-analysis")
    public Result getSubmissionAnalysis() {
        Map<String, Object> analysis = new HashMap<>();

        // 提交状态分布
        analysis.put("statusDistribution", statusService.getStatusDistribution());

        // 语言使用分布
        analysis.put("languageDistribution", statusService.getLanguageDistribution());

        return Result.success(analysis);
    }

    @ApiOperation("获取题目难度分析")
    @GetMapping("/problem-difficulty")
    public Result getProblemDifficultyAnalysis() {
        Map<String, Object> analysis = new HashMap<>();

        // 题目难度分布
        analysis.put("difficultyDistribution", problemService.getDifficultyDistribution());

        // 各难度题目的平均通过率
        analysis.put("avgPassRateByDifficulty", problemService.getAvgPassRateByDifficulty());

        return Result.success(analysis);
    }

    @ApiOperation("获取学生成绩分析")
    @GetMapping("/student-performance")
    public Result getStudentPerformanceAnalysis() {
        Map<String, Object> analysis = new HashMap<>();

        // AC题目数量分布
        analysis.put("acCountDistribution", studentService.getAcCountDistribution());

        // 提交次数分布
        analysis.put("submitCountDistribution", studentService.getSubmitCountDistribution());

        return Result.success(analysis);
    }

    @ApiOperation("获取课程活跃度分析")
    @GetMapping("/course-activity")
    public Result getCourseActivityAnalysis() {
        Map<String, Object> analysis = new HashMap<>();

        // 课程学生人数分布
        analysis.put("studentCountDistribution", courseService.getStudentCountDistribution());

        // 课程作业数量分布
        analysis.put("homeworkCountDistribution", courseService.getHomeworkCountDistribution());

        return Result.success(analysis);
    }

    @ApiOperation("获取时间段内的活动统计")
    @GetMapping("/activity-stats")
    public Result getActivityStats(
            @ApiParam("开始日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @ApiParam("结束日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();

        // 提交次数
        stats.put("submissions", statusService.getSubmissionCount(startDate, endDate));

        // 新增题解数
        stats.put("newSolutions", solutionService.getNewSolutionCount(startDate, endDate));

        // 新增讨论数
        stats.put("newDiscussions", discussService.getNewDiscussionCount(startDate, endDate));

        return Result.success(stats);
    }

    @ApiOperation("获取讨论区活跃度分析")
    @GetMapping("/discussion-activity")
    public Result getDiscussionActivityAnalysis() {
        Map<String, Object> analysis = new HashMap<>();

        // 获取讨论区统计数据
        Map<String, Object> stats = discussService.getDiscussStats();
        analysis.put("totalDiscussions", stats.get("totalDiscussions"));
        analysis.put("totalComments", stats.get("totalComments"));
        analysis.put("totalParticipants", stats.get("totalParticipants"));

        return Result.success(analysis);
    }

    @ApiOperation("获取作业完成情况分析")
    @GetMapping("/homework-completion")
    public Result getHomeworkCompletionAnalysis() {
        Map<String, Object> analysis = new HashMap<>();

        // 作业提交率分布
        analysis.put("submissionRateDistribution", homeworkService.getSubmissionRateDistribution());

        // 作业平均完成时间分布
        analysis.put("avgCompletionTimeDistribution", homeworkService.getAvgCompletionTimeDistribution());

        return Result.success(analysis);
    }

    @ApiOperation("获取题解质量分析")
    @GetMapping("/solution-quality")
    public Result getSolutionQualityAnalysis() {
        Map<String, Object> analysis = new HashMap<>();

        // 获取题解统计数据
        Map<String, Object> stats = solutionService.getSolutionStats();
        analysis.put("totalSolutions", stats.get("totalSolutions"));
        analysis.put("totalLikes", stats.get("totalLikes"));
        analysis.put("totalContributors", stats.get("totalContributors"));

        return Result.success(analysis);
    }

    @ApiOperation("获取公告影响力分析")
    @GetMapping("/announcement-impact")
    public Result getAnnouncementImpactAnalysis() {
        Map<String, Object> analysis = new HashMap<>();

        // 公告类型分布
        analysis.put("typeDistribution", announcementService.getTypeDistribution());

        // 公告发布时间分布
        analysis.put("timeDistribution", announcementService.getTimeDistribution());

        return Result.success(analysis);
    }
} 