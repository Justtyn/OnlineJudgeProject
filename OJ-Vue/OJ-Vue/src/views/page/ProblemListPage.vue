<script setup>
import {ref, reactive, onMounted} from 'vue'
import {ElMessage} from 'element-plus'
import request from "@/utils/request.js";


// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '-'

  try {
    // 处理ISO格式的时间字符串
    if (dateTimeStr.includes('T')) {
      return dateTimeStr.replace('T', ' ').split('.')[0]
    }

    // 处理时间戳
    if (!isNaN(dateTimeStr)) {
      const date = new Date(Number(dateTimeStr))
      if (isNaN(date.getTime())) return '-'

      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false
      }).replace(',', '')
    }

    // 其他格式直接返回
    return dateTimeStr
  } catch (e) {
    console.error('时间格式化错误:', e)
    return '-'
  }
}

// 查询条件
const queryForm = reactive({
  id: '',
  name: '',
  setter: ''
})

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 表格数据
const tableData = ref([])

// 问题表单数据
const problemForm = reactive({
  name: '',
  setter: '',
  desc: '',
  descInput: '',
  descOutput: '',
  sampleInput: '',
  sampleOutput: '',
  hint: '',
  createTime: '',
  acCount: 0,
  submitCount: 0
})

// 表单规则
const rules = {
  name: [{required: true, message: '请输入问题名称', trigger: 'blur'}],
  setter: [{required: true, message: '请输入出题人', trigger: 'blur'}],
  desc: [{required: true, message: '请输入题目描述', trigger: 'blur'}],
  descInput: [{required: true, message: '请输入输入描述', trigger: 'blur'}],
  descOutput: [{required: true, message: '请输入输出描述', trigger: 'blur'}],
  sampleInput: [{required: true, message: '请输入输入样例', trigger: 'blur'}],
  sampleOutput: [{required: true, message: '请输入输出样例', trigger: 'blur'}]
}

// 表单引用
const addFormRef = ref(null)
const editFormRef = ref(null)

// 重置表单
const resetForm = () => {
  problemForm.name = ''
  problemForm.setter = ''
  problemForm.desc = ''
  problemForm.descInput = ''
  problemForm.descOutput = ''
  problemForm.sampleInput = ''
  problemForm.sampleOutput = ''
  problemForm.hint = ''
  problemForm.createTime = new Date().toISOString().split('T')[0]  // 设置当前日期
  problemForm.acCount = 0
  problemForm.submitCount = 0
}

// 加载数据
const loadData = async () => {
  try {
    let response;
    const baseParams = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    };

    // 根据查询条件选择对应的API接口
    if (queryForm.id) {
      // 按ID查询
      response = await request.get(`/problem/${queryForm.id}`);
      if (response.data.code === '200') {
        // 将单个对象转换为数组格式
        tableData.value = [response.data.data];
        total.value = 1;
      }
    } else if (queryForm.name) {
      // 按名称查询
      response = await request.get('/problem/name', {
        params: {
          ...baseParams,
          name: queryForm.name
        }
      });
      handleResponse(response);
    } else if (queryForm.setter) {
      // 按出题人查询
      response = await request.get('/problem/setter', {
        params: {
          ...baseParams,
          setter: queryForm.setter
        }
      });
      handleResponse(response);
    } else {
      // 无查询条件，获取所有数据
      response = await request.get('/problem/page', {
        params: baseParams
      });
      handleResponse(response);
    }
  } catch (error) {
    console.error('请求异常:', error);
    ElMessage.error('获取数据失败：' + (error.message || '未知错误'));
  }
}

// 处理响应数据
const handleResponse = (response) => {
  const res = response.data;
  if (res.code === '200') {
    tableData.value = res.data.list;
    total.value = res.data.total;
    console.log('成功获取数据:', tableData.value);
  } else {
    console.error('请求失败:', res.msg);
    ElMessage.error(res.msg || '获取数据失败');
  }
}

// 查询方法
const handleQuery = () => {
  // 检查是否输入了多个查询条件
  const conditions = [queryForm.id, queryForm.name, queryForm.setter].filter(Boolean);
  if (conditions.length > 1) {
    ElMessage.warning('请只选择一个查询条件');
    return;
  }
  
  currentPage.value = 1;
  loadData();
}

// 刷新方法
const handleRefresh = () => {
  queryForm.id = '';
  queryForm.name = '';
  queryForm.setter = '';
  currentPage.value = 1;
  loadData();
}

// 分页变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  loadData()
}

// 每页条数变化
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  loadData()
}

// 添加题目对话框
const addDialogVisible = ref(false)

// 编辑题目对话框
const editDialogVisible = ref(false)
const currentProblem = ref(null)

// 打开编辑对话框
const handleEdit = (row) => {
  currentProblem.value = {...row}

  // 将API返回的字段映射到表单字段
  problemForm.name = row.name || '';
  problemForm.setter = row.setter || '';
  problemForm.desc = row.desc || '';
  problemForm.descInput = row.descInput || '';
  problemForm.descOutput = row.descOutput || '';
  problemForm.sampleInput = row.sampleInput || '';
  problemForm.sampleOutput = row.sampleOutput || '';
  problemForm.hint = row.hint || '';
  problemForm.createTime = row.createTime || '';
  problemForm.acCount = row.acCount || 0;
  problemForm.submitCount = row.submitCount || 0;

  editDialogVisible.value = true;
}

// 打开添加对话框
const handleAdd = () => {
  resetForm()
  addDialogVisible.value = true
}

// 取消操作
const handleCancel = () => {
  resetForm()
  addDialogVisible.value = false
  editDialogVisible.value = false
}

// 保存操作
const handleSave = async () => {
  const formRef = addDialogVisible.value ? addFormRef.value : editFormRef.value
  
  try {
    await formRef.validate()
    
    const submitData = {
      ...problemForm,
      createTime: problemForm.createTime || new Date().toISOString().split('T')[0],
      acCount: problemForm.acCount || 0,
      submitCount: problemForm.submitCount || 0
    }

    if (addDialogVisible.value) {
      // 添加操作
      const response = await request.post('/problem', submitData)
      const res = response.data
      if (res.code === '200') {
        ElMessage.success('添加成功')
        handleCancel()
        loadData()
      } else {
        ElMessage.error(res.msg || '添加失败')
      }
    } else {
      // 编辑操作
      const response = await request.put('/problem', {
        id: currentProblem.value.id,
        ...submitData
      })
      const res = response.data
      if (res.code === '200') {
        ElMessage.success('修改成功')
        handleCancel()
        loadData()
      } else {
        ElMessage.error(res.msg || '修改失败')
      }
    }
  } catch (error) {
    if (error.message) {
      ElMessage.error('表单验证失败，请检查必填项')
    } else {
      ElMessage.error('操作失败')
      console.error(error)
    }
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="problem-container">
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <template #header>
        <div class="card-header">
          <span class="title">问题列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon>
              <Plus/>
            </el-icon>
            添加题目
          </el-button>
        </div>
      </template>

      <el-form :model="queryForm" inline>
        <el-form-item>
          <el-input
              v-model="queryForm.id"
              placeholder="按题号查询"
              clearable
          />
        </el-form-item>
        <el-form-item>
          <el-input
              v-model="queryForm.name"
              placeholder="按问题名称查询"
              clearable
          />
        </el-form-item>
        <el-form-item>
          <el-input
              v-model="queryForm.setter"
              placeholder="按出题人查询"
              clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleRefresh">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格区域 -->
    <el-card class="table-card">
      <el-table
          :data="tableData"
          style="width: 100%"
          border
          stripe
          highlight-current-row
      >
        <el-table-column prop="id" label="题号" width="100" align="center"/>
        <el-table-column prop="name" label="问题" min-width="120" show-overflow-tooltip/>
        <el-table-column prop="setter" label="出题人" width="100" align="center"/>
        <el-table-column prop="createTime" label="出题时间" width="150" align="center">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="acCount" label="AC" width="100" align="center">
          <template #default="scope">
            {{ scope.row.acCount || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="submitCount" label="Submit" width="100" align="center">
          <template #default="scope">
            {{ scope.row.submitCount || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="$router.push(`/problem/${scope.row.id}`)">
              查看
            </el-button>
            <el-button link type="primary" @click="handleEdit(scope.row)">
              编辑
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 30, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @current-change="handleCurrentChange"
            @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <!-- 添加题目对话框 -->
    <el-dialog
        v-model="addDialogVisible"
        title="添加题目"
        width="50%"
    >
      <el-form
          ref="addFormRef"
          :model="problemForm"
          :rules="rules"
          label-width="100px"
      >
        <el-form-item label="问题名称" prop="name">
          <el-input v-model="problemForm.name" placeholder="请输入问题名称"/>
        </el-form-item>
        <el-form-item label="出题人" prop="setter">
          <el-input v-model="problemForm.setter" placeholder="请输入出题人"/>
        </el-form-item>
        <el-form-item label="题目描述" prop="desc">
          <el-input v-model="problemForm.desc" type="textarea" rows="4" placeholder="请输入题目描述"/>
        </el-form-item>
        <el-form-item label="输入描述" prop="descInput">
          <el-input v-model="problemForm.descInput" type="textarea" rows="2" placeholder="请输入输入描述"/>
        </el-form-item>
        <el-form-item label="输出描述" prop="descOutput">
          <el-input v-model="problemForm.descOutput" type="textarea" rows="2" placeholder="请输入输出描述"/>
        </el-form-item>
        <el-form-item label="输入样例" prop="sampleInput">
          <el-input v-model="problemForm.sampleInput" type="textarea" rows="2" placeholder="请输入输入样例"/>
        </el-form-item>
        <el-form-item label="输出样例" prop="sampleOutput">
          <el-input v-model="problemForm.sampleOutput" type="textarea" rows="2" placeholder="请输入输出样例"/>
        </el-form-item>
        <el-form-item label="提示说明" prop="hint">
          <el-input v-model="problemForm.hint" type="textarea" rows="2" placeholder="请输入提示说明"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCancel">取消</el-button>
          <el-button type="primary" @click="handleSave">确认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 编辑题目对话框 -->
    <el-dialog
        v-model="editDialogVisible"
        title="编辑题目"
        width="50%"
    >
      <el-form
          ref="editFormRef"
          :model="problemForm"
          :rules="rules"
          label-width="100px"
      >
        <el-form-item label="问题名称" prop="name">
          <el-input v-model="problemForm.name" placeholder="请输入问题名称"/>
        </el-form-item>
        <el-form-item label="出题人" prop="setter">
          <el-input v-model="problemForm.setter" placeholder="请输入出题人"/>
        </el-form-item>
        <el-form-item label="题目描述" prop="desc">
          <el-input v-model="problemForm.desc" type="textarea" rows="4" placeholder="请输入题目描述"/>
        </el-form-item>
        <el-form-item label="输入描述" prop="descInput">
          <el-input v-model="problemForm.descInput" type="textarea" rows="2" placeholder="请输入输入描述"/>
        </el-form-item>
        <el-form-item label="输出描述" prop="descOutput">
          <el-input v-model="problemForm.descOutput" type="textarea" rows="2" placeholder="请输入输出描述"/>
        </el-form-item>
        <el-form-item label="输入样例" prop="sampleInput">
          <el-input v-model="problemForm.sampleInput" type="textarea" rows="2" placeholder="请输入输入样例"/>
        </el-form-item>
        <el-form-item label="输出样例" prop="sampleOutput">
          <el-input v-model="problemForm.sampleOutput" type="textarea" rows="2" placeholder="请输入输出样例"/>
        </el-form-item>
        <el-form-item label="提示说明" prop="hint">
          <el-input v-model="problemForm.hint" type="textarea" rows="2" placeholder="请输入提示说明"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCancel">取消</el-button>
          <el-button type="primary" @click="handleSave">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.problem-container {
  padding: 20px;
  background-color: #f5f7fa;
  height: 80vh;
}

.search-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: bold;
}

.table-card {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

:deep(.el-card__header) {
  padding: 15px 20px;
}

:deep(.el-card__body) {
  padding: 20px;
}
</style>