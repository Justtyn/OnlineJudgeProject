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
            <h3>1. 注册与登录</h3>
            <p>首次使用需要注册账号，点击右上角的"注册"按钮，填写必要信息后即可创建账号。已有账号的用户可直接登录系统。</p>
            
            <h3>2. 浏览题目</h3>
            <p>在导航栏选择"题库"，可以浏览所有可用的编程题目。您可以按难度、类型或关键词筛选题目。</p>
            
            <h3>3. 提交代码</h3>
            <p>选择一道题目后，阅读题目描述、输入输出要求和示例。在代码编辑器中编写您的解答，选择编程语言后点击"提交"按钮。</p>
            
            <h3>4. 查看结果</h3>
            <p>提交后，系统会自动评测您的代码。您可以在"提交记录"页面查看所有提交的状态和详细结果。</p>
            
            <h3>5. 查看排名</h3>
            <p>在"排行榜"页面，您可以查看用户解题数量和比赛排名情况，了解自己在系统中的表现。</p>
            
            <h3>6. 参与比赛</h3>
            <p>定期查看"比赛"页面，参与系统举办的编程竞赛，与其他用户一较高下，提升自己的编程能力。</p>
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
              <div class="feature-desc">提供丰富多样的编程题目，覆盖基础算法、数据结构、动态规划等多个领域，难度从入门到高级。</div>
            </div>
            
            <div class="feature-card">
              <div class="feature-icon">⚙️</div>
              <div class="feature-title">在线评测</div>
              <div class="feature-desc">支持多种编程语言的在线编译和运行，自动评测代码正确性、运行时间和内存占用。</div>
            </div>
            
            <div class="feature-card">
              <div class="feature-icon">🏆</div>
              <div class="feature-title">比赛系统</div>
              <div class="feature-desc">支持举办各类编程竞赛，包括实时排名、计时和自定义比赛规则，提供竞争环境。</div>
            </div>
            
            <div class="feature-card">
              <div class="feature-icon">📊</div>
              <div class="feature-title">数据分析</div>
              <div class="feature-desc">提供个人解题统计、能力分析和进步趋势，帮助用户了解自己的学习情况和提升方向。</div>
            </div>
            
            <div class="feature-card">
              <div class="feature-icon">👥</div>
              <div class="feature-title">社区互动</div>
              <div class="feature-desc">用户可以分享解题思路、讨论最优解法，促进学习交流和知识分享。</div>
            </div>
            
            <div class="feature-card">
              <div class="feature-icon">🔍</div>
              <div class="feature-title">智能推荐</div>
              <div class="feature-desc">根据用户的解题历史和能力水平，智能推荐适合的题目，帮助用户循序渐进地提升。</div>
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
    </div>
  </div>
</template>

<script>
export default {
  name: 'AboutPage',
  data() {
    return {
      isOpen: [false, false, false, false]
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
}

/* 添加移动端适配样式 */
@media screen and (max-width: 768px) {
  .about-container {
    padding: 10px;
  }
  
  .page-title {
    font-size: 24px;
    margin-bottom: 20px;
  }
  
  .accordion-header h2 {
    font-size: 16px;
  }
  
  .accordion-content {
    padding: 15px;
  }
  
  .status-badge {
    min-width: 100px;
    font-size: 12px;
    padding: 2px 8px;
  }
  
  .status-description {
    font-size: 14px;
  }
  
  .guide-section h3 {
    font-size: 16px;
  }
  
  .guide-section p {
    font-size: 14px;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
  }
  
  .feature-card {
    padding: 15px;
  }
  
  .feature-icon {
    font-size: 36px;
  }
  
  .feature-title {
    font-size: 16px;
  }
  
  .feature-desc {
    font-size: 14px;
  }
  
  .contact-item {
    padding: 8px;
  }
  
  .contact-icon {
    font-size: 20px;
  }
  
  .contact-info h3 {
    font-size: 16px;
  }
  
  .contact-info p {
    font-size: 14px;
  }
}

.page-title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-weight: 600;
}

.accordion {
  width: 100%;
}

.accordion-item {
  margin-bottom: 15px;
  border: 1px solid #ddd;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 5px rgba(0,0,0,0.05);
  transition: all 0.3s ease;
}

.accordion-item:hover {
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
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
  background-color: #f0f0f0;
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

.accordion-header h2 {
  margin: 0;
  margin-left: 15px;
  font-size: 18px;
  font-weight: 500;
  transition: all 0.3s ease;
  color: #000; /* 设置文字为黑色 */
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
  color: #666;
  transition: transform 0.3s ease, color 0.3s ease;
}

.accordion-header:hover .accordion-icon {
  color: #64B5F6; /* 鼠标悬停时图标变为淡蓝色 */
}

.accordion-header.active .accordion-icon {
  color: #333;
  transform: rotate(360deg);
}

.accordion-content {
  padding: 0;
  max-height: 0;
  background-color: #fff;
  overflow: hidden;
  transition: all 0.3s ease;
  opacity: 0;
  color: #000; /* 设置内容文字为黑色 */
}

.accordion-content.active {
  padding: 20px;
  padding-left: 30px;
  max-height: 400px; /* 设置一个固定高度 */
  overflow-y: auto; /* 添加垂直滚动条 */
  opacity: 1;
  border-top: 1px solid #eee;
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
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 4px;
  font-weight: bold;
  min-width: 120px;
  text-align: center;
  flex-shrink: 0; /* 防止标签在窄屏幕上缩小 */
}

.status-description {
  flex: 1;
  line-height: 1.5;
}

/* 不同状态的样式 - 复用StatusListPage的样式 */
.status-success {
  background-color: #f0f9eb;
  color: #67c23a;
  border: 1px solid #c2e7b0;
}

.status-error {
  background-color: #fef0f0;
  color: #f56c6c;
  border: 1px solid #fbc4c4;
}

.status-warning {
  background-color: #fdf6ec;
  color: #e6a23c;
  border: 1px solid #f5dab1;
}

.status-pending {
  background-color: #f0f2f5;
  color: #909399;
  border: 1px solid #d3d4d6;
}

.status-unknown {
  background-color: #f5f7fa;
  color: #909399;
  border: 1px solid #e4e7ed;
}

/* 使用指南样式 */
.guide-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-bottom: 10px; /* 添加底部内边距 */
}

.guide-section h3 {
  margin-top: 15px;
  margin-bottom: 5px;
}

.guide-section p {
  margin: 0;
  line-height: 1.6;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); /* 改为网格布局，自适应列宽 */
  gap: 15px;
  padding-bottom: 10px; /* 添加底部内边距 */
}

.feature-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.05);
}

.feature-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

.feature-title {
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 10px;
}

.feature-desc {
  text-align: center;
}

.contact-section {
  display: flex;
  flex-direction: column; /* 改为纵向排列 */
  gap: 15px;
  padding-bottom: 10px; /* 添加底部内边距 */
}

.contact-item {
  display: flex;
  align-items: flex-start; /* 改为顶部对齐 */
  gap: 15px;
  padding: 10px;
  border-bottom: 1px solid #eee; /* 添加分隔线 */
}

.contact-item:last-child {
  border-bottom: none;
}

.contact-icon {
  font-size: 24px;
  flex-shrink: 0; /* 防止图标缩小 */
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
}

.social-link:hover {
  text-decoration: underline;
}

/* 自定义滚动条样式 */
.accordion-content.active::-webkit-scrollbar {
  width: 8px;
}

.accordion-content.active::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.accordion-content.active::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 4px;
}

.accordion-content.active::-webkit-scrollbar-thumb:hover {
  background: #aaa;
}
</style>
