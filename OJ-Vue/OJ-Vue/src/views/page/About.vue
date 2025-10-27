<template>
  <div class="about-container">
    <h1 class="page-title">关于系统</h1>
    
    <div class="accordion">
      <!-- 第一个手风琴项 -->
      <div class="accordion-item">
        <div class="accordion-header" @click="toggleItem(0)" :class="{ 'active': isOpen[0] }">
          <div class="color-bar color-1"></div>
          <h2>Q：系统的返回信息都是什么意思？</h2>
          <span class="accordion-icon">{{ isOpen[0] ? '−' : '+' }}</span>
        </div>
        <div class="accordion-content" :class="{ 'active': isOpen[0] }">
          <div class="status-list">
            <div class="status-item">
              <span class="status-badge status-pending">In Queue</span>
              <span class="status-description">您的代码已提交并正在等待评测系统处理，请耐心等待。</span>
            </div>
            <div class="status-item">
              <span class="status-badge status-pending">Processing</span>
              <span class="status-description">评测系统正在处理您的代码，结果将很快出现。</span>
            </div>
            <div class="status-item">
              <span class="status-badge status-success">Accepted</span>
              <span class="status-description">恭喜！您的代码通过了所有测试用例，解答正确。</span>
            </div>
            <div class="status-item">
              <span class="status-badge status-error">Wrong Answer</span>
              <span class="status-description">您的代码输出结果与预期答案不符，请检查算法逻辑。</span>
            </div>
            <div class="status-item">
              <span class="status-badge status-warning">Time Limit Exceeded</span>
              <span class="status-description">您的代码执行时间超出了题目限制，需要优化算法提高效率。</span>
            </div>
            <div class="status-item">
              <span class="status-badge status-error">Compilation Error</span>
              <span class="status-description">您的代码无法编译通过，请检查语法错误。</span>
            </div>
            <div class="status-item">
              <span class="status-badge status-error">Runtime Error (SIGSEGV)</span>
              <span class="status-description">程序执行时发生段错误，通常是因为非法内存访问，如数组越界。</span>
            </div>
            <div class="status-item">
              <span class="status-badge status-error">Runtime Error (SIGXFSZ)</span>
              <span class="status-description">程序生成的文件超过系统允许的大小限制。</span>
            </div>
            <div class="status-item">
              <span class="status-badge status-error">Runtime Error (SIGFPE)</span>
              <span class="status-description">程序执行时发生浮点错误，如除以零或浮点溢出。</span>
            </div>
            <div class="status-item">
              <span class="status-badge status-error">Runtime Error (SIGABRT)</span>
              <span class="status-description">程序执行被中止，通常是由于断言失败或调用abort()函数。</span>
            </div>
            <div class="status-item">
              <span class="status-badge status-error">Runtime Error (NZEC)</span>
              <span class="status-description">程序以非零状态退出，表示执行过程中出现了错误。</span>
            </div>
            <div class="status-item">
              <span class="status-badge status-error">Runtime Error (Other)</span>
              <span class="status-description">程序执行时发生了其他类型的运行时错误。</span>
            </div>
            <div class="status-item">
              <span class="status-badge status-error">Internal Error</span>
              <span class="status-description">评测系统内部错误，请联系管理员处理。</span>
            </div>
            <div class="status-item">
              <span class="status-badge status-error">Exec Format Error</span>
              <span class="status-description">执行文件格式错误，无法运行生成的程序。</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 第二个手风琴项 -->
      <div class="accordion-item">
        <div class="accordion-header" @click="toggleItem(1)" :class="{ 'active': isOpen[1] }">
          <div class="color-bar color-2"></div>
          <h2>Q：如何使用本系统？</h2>
          <span class="accordion-icon">{{ isOpen[1] ? '−' : '+' }}</span>
        </div>
        <div class="accordion-content" :class="{ 'active': isOpen[1] }">
          <div class="guide-section">
            <div class="guide-item" v-for="(item, index) in guideItems" :key="index" :style="{ animationDelay: index * 0.2 + 's' }">
              <div class="guide-icon">{{ item.icon }}</div>
              <div class="guide-content">
                <h3>{{ item.title }}</h3>
                <p>{{ item.description }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 第三个手风琴项 -->
      <div class="accordion-item">
        <div class="accordion-header" @click="toggleItem(2)" :class="{ 'active': isOpen[2] }">
          <div class="color-bar color-3"></div>
          <h2>Q：系统有哪些功能？</h2>
          <span class="accordion-icon">{{ isOpen[2] ? '−' : '+' }}</span>
        </div>
        <div class="accordion-content" :class="{ 'active': isOpen[2] }">
          <div class="features-grid">
            <div class="feature-card">
              <div class="feature-icon">📚</div>
              <div class="feature-title">题库系统</div>
              <div class="feature-desc">提供丰富多样的编程题目，包括每日挑战、班级作业等，支持多语言提交和自动评测。</div>
            </div>
            
            <div class="feature-card">
              <div class="feature-icon">⚙️</div>
              <div class="feature-title">在线评测</div>
              <div class="feature-desc">支持多种编程语言的在线编译和运行，自动评测代码正确性、运行时间和内存占用，提供详细的评测报告。</div>
            </div>
            
            <div class="feature-card">
              <div class="feature-icon">🏆</div>
              <div class="feature-title">排行榜</div>
              <div class="feature-desc">实时更新用户解题数量和排名情况，激励用户持续学习和进步。</div>
            </div>
            
            <div class="feature-card">
              <div class="feature-icon">📊</div>
              <div class="feature-title">数据分析</div>
              <div class="feature-desc">提供个人解题统计、能力分析和进步趋势，帮助用户了解自己的学习情况和提升方向。</div>
            </div>
            
            <div class="feature-card">
              <div class="feature-icon">👥</div>
              <div class="feature-title">社区互动</div>
              <div class="feature-desc">提供讨论区功能，用户可以分享解题思路、讨论最优解法，促进学习交流和知识分享。</div>
            </div>
            
            <div class="feature-card">
              <div class="feature-icon">🎓</div>
              <div class="feature-title">班级管理</div>
              <div class="feature-desc">支持班级创建和管理，提供班级专属题目、作业和统计数据，方便教师进行教学管理。</div>
            </div>
            
            <div class="feature-card">
              <div class="feature-icon">📢</div>
              <div class="feature-title">公告系统</div>
              <div class="feature-desc">发布系统公告、比赛通知和重要更新，确保用户及时获取最新信息。</div>
            </div>
            
            <div class="feature-card">
              <div class="feature-icon">📈</div>
              <div class="feature-title">数据可视化</div>
              <div class="feature-desc">通过图表直观展示用户解题情况、题目难度分布等数据，帮助用户更好地了解学习进度。</div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 第四个手风琴项 -->
      <div class="accordion-item">
        <div class="accordion-header" @click="toggleItem(3)" :class="{ 'active': isOpen[3] }">
          <div class="color-bar color-4"></div>
          <h2>Q：如何联系我们？</h2>
          <span class="accordion-icon">{{ isOpen[3] ? '−' : '+' }}</span>
        </div>
        <div class="accordion-content" :class="{ 'active': isOpen[3] }">
          <div class="contact-section">
            <div class="contact-item">
              <div class="contact-icon">✉️</div>
              <div class="contact-info">
                <h3>电子邮件</h3>
                <p>1046220903@qq.com（作者邮箱）</p>
                <p>sweu24025@stu.xujc.com（反馈与建议）</p>
              </div>
            </div>
            
            <!-- <div class="contact-item">
              <div class="contact-icon">☎️</div>
              <div class="contact-info">
                <h3>客服电话</h3>
                <p>400-123-4567（工作日 9:00-18:00）</p>
              </div>
            </div>
            
            <div class="contact-item">
              <div class="contact-icon">💬</div>
              <div class="contact-info">
                <h3>在线客服</h3>
                <p>点击网站右下角的"在线客服"按钮，获取即时帮助</p>
              </div>
            </div> -->
            
            <div class="contact-item">
              <div class="contact-icon">🏢</div>
              <div class="contact-info">
                <h3>联系地址</h3>
                <p>福建省漳州市厦门大学漳州校区</p>
              </div>
            </div>
            
            <div class="contact-item">
              <div class="contact-icon">📱</div>
              <div class="contact-info">
                <h3>社交媒体</h3>
                <div class="social-links">
                  <a href="https://justtyn.github.io/" class="social-link">Blog：Justyn</a>
                  <a href="https://github.com/Justtyn" class="social-link">GitHub：Justtyn</a>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 第五个手风琴项 -->
      <div class="accordion-item">
        <div class="accordion-header" @click="toggleItem(4)" :class="{ 'active': isOpen[4] }">
          <div class="color-bar color-5"></div>
          <h2>Q：数据结构与算法学习资源推荐</h2>
          <span class="accordion-icon">{{ isOpen[4] ? '−' : '+' }}</span>
        </div>
        <div class="accordion-content" :class="{ 'active': isOpen[4] }">
          <div class="resources-section">
            <div class="resource-category">
              <h3>一、入门基础</h3>
              <div class="resource-group">
                <h4>在线刷题平台</h4>
                <ul>
                  <li><a href="https://leetcode-cn.com/" target="_blank">LeetCode（力扣）</a> - 国内最受欢迎的在线刷题平台</li>
                  <li><a href="https://www.hackerrank.com/domains/algorithms" target="_blank">HackerRank</a> - 多领域算法练习平台</li>
                  <li><a href="https://codeforces.com/" target="_blank">Codeforces</a> - 国际顶级竞赛平台</li>
                  <li><a href="https://atcoder.jp/" target="_blank">AtCoder</a> - 日本高质量竞赛平台</li>
                  <li><a href="https://neetcode.io/practice" target="_blank">NeetCode</a> - 面试题目录与刷题路径</li>
                </ul>
              </div>
              
              <div class="resource-group">
                <h4>在线教程与博客</h4>
                <ul>
                  <li><a href="https://www.geeksforgeeks.org/data-structures/" target="_blank">GeeksforGeeks</a> - 详尽的算法与数据结构教程</li>
                  <li><a href="https://www.programiz.com/dsa" target="_blank">Programiz</a> - 初学者友好的可视化教程</li>
                  <li><a href="https://rosettacode.org/wiki/Category:Data_Structures" target="_blank">Rosetta Code</a> - 多语言算法实现示例</li>
                </ul>
              </div>
            </div>

            <div class="resource-category">
              <h3>二、可视化工具</h3>
              <ul>
                <li><a href="https://visualgo.net/" target="_blank">VisuAlgo</a> - 数据结构与算法动画演示</li>
                <li><a href="https://www.cs.usfca.edu/~galles/visualization/Algorithms.html" target="_blank">USFCA 数据结构可视化</a> - 美国旧金山大学可视化平台</li>
                <li><a href="https://csvistool.com/" target="_blank">CS 1332 Visualization Tool</a> - 佐治亚理工大学可视化工具</li>
              </ul>
            </div>

            <div class="resource-category">
              <h3>三、系统化课程</h3>
              <div class="resource-group">
                <h4>大学公开课</h4>
                <ul>
                  <li><a href="https://ocw.mit.edu/courses/6-006-introduction-to-algorithms-spring-2020/" target="_blank">MIT OCW 6.006 Introduction to Algorithms</a></li>
                </ul>
              </div>
              
              <div class="resource-group">
                <h4>MOOCs 平台</h4>
                <ul>
                  <li><a href="https://www.coursera.org/specializations/algorithms" target="_blank">Coursera Algorithms Specialization</a></li>
                  <li><a href="https://www.udemy.com/topic/algorithms/" target="_blank">Udemy 算法课程</a></li>
                </ul>
              </div>
            </div>

            <div class="resource-category">
              <h3>四、竞赛与实践</h3>
              <ul>
                <li><a href="https://codeforces.com/" target="_blank">Codeforces</a> - 全球算法竞赛平台</li>
                <li><a href="https://atcoder.jp/" target="_blank">AtCoder</a> - 日本竞赛平台</li>
                <li><a href="https://www.topcoder.com/" target="_blank">Topcoder</a> - 提供多种比赛形式</li>
              </ul>
            </div>

            <div class="resource-category">
              <h3>五、社区与论坛</h3>
              <ul>
                <li><a href="https://www.reddit.com/r/learnprogramming/" target="_blank">Reddit r/learnprogramming</a> - 编程学习社区</li>
                <li><a href="https://stackoverflow.com/" target="_blank">Stack Overflow</a> - 技术问答平台</li>
                <li><a href="https://www.geeksforgeeks.org/" target="_blank">GeeksforGeeks 讨论区</a> - 算法讨论社区</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AboutPage',
  data() {
    return {
      isOpen: [false, false, false, false, false],
      guideItems: [
        {
          icon: '👤',
          title: '1. 注册与登录',
          description: '首次使用需要注册账号，点击右上角的"注册"按钮，填写必要信息后即可创建账号。已有账号的用户可直接登录系统。'
        },
        {
          icon: '📚',
          title: '2. 浏览题目',
          description: '在导航栏选择"题库"，可以浏览所有可用的编程题目。您可以按难度、类型或关键词筛选题目，也可以查看每日挑战题目。'
        },
        {
          icon: '💻',
          title: '3. 提交代码',
          description: '选择一道题目后，阅读题目描述、输入输出要求和示例。在代码编辑器中编写您的解答，选择编程语言后点击"提交"按钮。系统支持多种编程语言，包括C++、Java、Python等。'
        },
        {
          icon: '📊',
          title: '4. 查看结果',
          description: '提交后，系统会自动评测您的代码。您可以在"提交记录"页面查看所有提交的状态和详细结果，包括运行时间、内存使用等信息。'
        },
        {
          icon: '💬',
          title: '5. 参与讨论',
          description: '在"讨论区"中，您可以查看其他用户的解题思路，分享自己的解法，或者提出疑问。这是一个很好的学习和交流平台。'
        },
        {
          icon: '🏆',
          title: '6. 查看排名',
          description: '在"排行榜"页面，您可以查看用户解题数量和比赛排名情况，了解自己在系统中的表现。'
        },
        {
          icon: '🎓',
          title: '7. 班级功能',
          description: '如果您是班级成员，可以访问班级专属的题目和作业，查看班级排名和统计数据。'
        },
        {
          icon: '👤',
          title: '8. 个人中心',
          description: '在个人中心，您可以查看自己的解题统计、提交历史、收藏的题目等信息，管理个人资料和设置。'
        }
      ]
    }
  },
  methods: {
    toggleItem(index) {
      this.isOpen[index] = !this.isOpen[index];
    }
  }
}
</script>

<style scoped>
.about-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
  animation: fadeIn 0.8s ease-out;
  user-select: none; /* 禁止选择文本 */
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.page-title {
  text-align: center;
  margin-bottom: 30px;
  color: var(--color-heading);
  font-weight: 600;
  animation: slideIn 0.8s ease-out;
  position: relative;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-50px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.accordion {
  width: 100%;
}

.accordion-item {
  margin-bottom: 15px;
  border: none;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 5px rgba(0,0,0,0.05);
  transition: all 0.3s ease;
  transform-origin: top;
  animation: fadeInUp 0.5s ease-out forwards;
  opacity: 0;
  transform: translateY(20px);
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.accordion-item:nth-child(1) { animation-delay: 0.1s; }
.accordion-item:nth-child(2) { animation-delay: 0.2s; }
.accordion-item:nth-child(3) { animation-delay: 0.3s; }
.accordion-item:nth-child(4) { animation-delay: 0.4s; }
.accordion-item:nth-child(5) { animation-delay: 0.5s; }

.accordion-item:hover {
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
  transform: translateY(-2px);
}

.accordion-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background-color: #f8f8f8;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.accordion-header.active {
  background-color: var(--bg-color-mute);
}

.color-bar {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 6px;
  transition: width 0.3s ease;
}

.accordion-header.active .color-bar {
  width: 10px;
}

.accordion-header:hover .color-bar {
  width: 8px;
}

.color-1 {
  background-color: #4CAF50; /* 绿色 */
}

.color-2 {
  background-color: #2196F3; /* 蓝色 */
}

.color-3 {
  background-color: #FF9800; /* 橙色 */
}

.color-4 {
  background-color: #E91E63; /* 粉色 */
}

.color-5 {
  background-color: #9C27B0; /* 紫色 */
}

.accordion-header h2 {
  margin: 0;
  margin-left: 15px;
  font-size: 18px;
  font-weight: 500;
  transition: all 0.3s ease;
  color: var(--color-heading); /* 设置文字为黑色 */
}

.accordion-header:hover h2 {
  color: #64B5F6; /* 鼠标悬停时文字变为淡蓝色 */
}

.accordion-header.active h2 {
  margin-left: 20px;
  font-weight: 600;
}

.accordion-icon {
  font-size: 24px;
  font-weight: bold;
  color: var(--color-text);
  transition: transform 0.3s ease, color 0.3s ease;
}

.accordion-header:hover .accordion-icon {
  color: #64B5F6; /* 鼠标悬停时图标变为淡蓝色 */
}

.accordion-header.active .accordion-icon {
  color: var(--color-heading);
  transform: rotate(360deg);
}

.accordion-content {
  padding: 0;
  max-height: 0;
  background-color: var(--color-background);
  overflow: hidden;
  transition: all 0.3s ease;
  opacity: 0;
  color: var(--color-heading); /* 设置内容文字为黑色 */
}

.accordion-content.active {
  padding: 20px;
  padding-left: 30px;
  max-height: 400px; /* 设置一个固定高度 */
  overflow-y: auto; /* 添加垂直滚动条 */
  opacity: 1;
}

/* 状态列表样式 */
.status-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-bottom: 10px; /* 添加底部内边距，确保最后一项完全可见 */
}

.status-item {
  display: flex;
  align-items: center;
  gap: 15px;
  transition: all 0.3s ease;
  padding: 8px;
  border-radius: 4px;
}

.status-item:hover {
  background-color: var(--bg-color-mute);
  transform: translateX(5px);
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 4px;
  font-weight: bold;
  min-width: 120px;
  text-align: center;
  flex-shrink: 0;
  transition: all 0.3s ease;
}

.status-item:hover .status-badge {
  transform: scale(1.05);
}

.status-description {
  flex: 1;
  line-height: 1.5;
}

/* 不同状态的样式 - 复用StatusListPage的样式 */
.status-success {
  background-color: #f0f9eb;
  color: #67c23a;
  border: none;
}

.status-error {
  background-color: #fef0f0;
  color: #f56c6c;
  border: none;
}

.status-warning {
  background-color: #fdf6ec;
  color: #e6a23c;
  border: none;
}

.status-pending {
  background-color: var(--bg-color-mute);
  color: #909399;
  border: none;
}

.status-unknown {
  background-color: #f5f7fa;
  color: #909399;
  border: none;
}

/* 使用指南样式 */
.guide-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 20px 0;
}

.guide-item {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  padding: 20px;
  background: var(--color-background);
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  animation: slideInRight 0.5s ease-out forwards;
  opacity: 0;
  transform: translateX(50px);
}

.guide-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 15px rgba(0, 0, 0, 0.2);
}

.guide-icon {
  font-size: 2.5em;
  background: linear-gradient(135deg, #2196F3, #1976D2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: bounce 2s infinite;
}

.guide-content {
  flex: 1;
}

.guide-content h3 {
  margin: 0 0 10px 0;
  color: var(--color-heading);
  font-size: 1.2em;
  font-weight: 600;
}

.guide-content p {
  margin: 0;
  color: var(--color-text);
  line-height: 1.6;
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(50px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  padding: 20px 0;
}

.feature-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 20px;
  border: none;
  border-radius: 8px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.05);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  animation: scaleIn 0.5s ease-out forwards;
  opacity: 0;
  transform: scale(0.9);
}

.feature-card:nth-child(1) { animation-delay: 0.1s; }
.feature-card:nth-child(2) { animation-delay: 0.2s; }
.feature-card:nth-child(3) { animation-delay: 0.3s; }
.feature-card:nth-child(4) { animation-delay: 0.4s; }
.feature-card:nth-child(5) { animation-delay: 0.5s; }
.feature-card:nth-child(6) { animation-delay: 0.6s; }
.feature-card:nth-child(7) { animation-delay: 0.7s; }
.feature-card:nth-child(8) { animation-delay: 0.8s; }

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.feature-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(45deg, transparent, rgba(255,255,255,0.1), transparent);
  transform: translateX(-100%);
  transition: transform 0.6s ease;
}

.feature-card:hover {
  transform: translateY(-10px) scale(1.02);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.15);
}

.feature-card:hover::before {
  transform: translateX(100%);
}

.feature-icon {
  font-size: 48px;
  margin-bottom: 10px;
  transition: all 0.3s ease;
  animation: float 3s ease-in-out infinite;
}

.feature-card:hover .feature-icon {
  transform: scale(1.1) rotate(5deg);
}

.feature-title {
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 10px;
  color: var(--color-heading);
}

.feature-desc {
  text-align: center;
  color: var(--color-text);
  line-height: 1.6;
}

.contact-section {
  display: flex;
  flex-direction: column; /* 改为纵向排列 */
  gap: 15px;
  padding-bottom: 10px; /* 添加底部内边距 */
}

.contact-item {
  display: flex;
  align-items: flex-start;
  gap: 15px;
  padding: 10px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.contact-item:hover {
  background-color: var(--bg-color-mute);
  transform: translateX(5px);
}

.contact-item:last-child {
  border-bottom: none;
}

.contact-icon {
  font-size: 24px;
  flex-shrink: 0;
  transition: all 0.3s ease;
}

.contact-item:hover .contact-icon {
  transform: scale(1.2) rotate(10deg);
}

.contact-info {
  flex: 1;
}

.contact-info h3 {
  margin-top: 0;
  margin-bottom: 8px;
}

.contact-info p {
  margin: 0;
  line-height: 1.6;
}

.social-links {
  display: flex;
  flex-direction: column; /* 改为纵向排列 */
  gap: 8px;
}

.social-link {
  color: #2196F3;
  text-decoration: none;
  transition: all 0.3s ease;
  position: relative;
}

.social-link::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 2px;
  background-color: #2196F3;
  transition: width 0.3s ease;
}

.social-link:hover {
  color: #1976D2;
}

.social-link:hover::after {
  width: 100%;
}

/* 自定义滚动条样式 */
.accordion-content.active::-webkit-scrollbar {
  width: 8px;
  background: var(--bg-color-mute);
  border-radius: 4px;
}

.accordion-content.active::-webkit-scrollbar-thumb {
  background: #2196F3;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.accordion-content.active::-webkit-scrollbar-thumb:hover {
  background: #1976D2;
}

/* 优化链接样式 */
.resource-category a {
  position: relative;
  color: #2196F3;
  text-decoration: none;
  transition: all 0.3s ease;
  padding: 2px 0;
}

.resource-category a::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background: #2196F3;
  transition: width 0.3s ease;
}

.resource-category a:hover {
  color: #1976D2;
}

.resource-category a:hover::after {
  width: 100%;
}

/* 优化卡片悬停效果 */
.feature-card:hover {
  transform: translateY(-10px) scale(1.02);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.15);
}

.feature-icon {
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

/* 优化标题动画 */
.page-title::after {
  content: '';
  position: absolute;
  bottom: -5px;
  left: 0;
  width: 0;
  height: 3px;
  background: linear-gradient(90deg, #2196F3, #1976D2);
  transition: width 0.3s ease;
}

.page-title:hover::after {
  width: 100%;
}

.resources-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding-bottom: 10px;
}

.resource-category {
  background-color: var(--bg-color-soft);
  border-radius: 8px;
  padding: 15px;
  transition: all 0.3s ease;
  animation: slideInLeft 0.5s ease-out forwards;
  opacity: 0;
  transform: translateX(-50px);
}

.resource-category:hover {
  transform: translateX(5px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.resource-category h3 {
  color: var(--color-heading);
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 18px;
  border-bottom: 2px solid var(--border-color);
  padding-bottom: 8px;
}

.resource-group {
  margin-bottom: 15px;
}

.resource-group h4 {
  color: var(--color-text);
  margin: 10px 0;
  font-size: 16px;
}

.resource-category ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.resource-category li {
  margin: 8px 0;
  padding-left: 20px;
  position: relative;
}

.resource-category li::before {
  content: '•';
  position: absolute;
  left: 0;
  color: #9C27B0;
}

.resource-category a {
  color: #2196F3;
  text-decoration: none;
  transition: all 0.3s ease;
}

.resource-category a:hover {
  color: #1976D2;
  text-decoration: underline;
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-50px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}
</style>
