<template>
  <page-layout 
    title="题目管理" 
    :show-back="false"
  >
    <div class="problems-container">
      <div class="search-bar">
        <a-form layout="inline" :model="queryForm">
          <a-form-item>
            <a-input v-model:value="queryForm.id" placeholder="按题号查询" allow-clear />
          </a-form-item>
          <a-form-item>
            <a-input v-model:value="queryForm.name" placeholder="按问题名称查询" allow-clear />
          </a-form-item>
          <a-form-item>
            <a-input v-model:value="queryForm.setter" placeholder="按出题人查询" allow-clear />
          </a-form-item>
          <a-form-item>
            <a-button type="primary" @click="handleQuery">查询</a-button>
            <a-button style="margin-left: 8px" @click="handleReset">重置</a-button>
          </a-form-item>
        </a-form>
      </div>

      <div class="operation-bar">
        <a-space>
          <a-button type="primary" @click="handleAdd">
            <template #icon>
              <plus-outlined />
            </template>
            新增题目
          </a-button>
          <a-button @click="fetchProblems">
            <template #icon>
              <reload-outlined />
            </template>
            刷新
          </a-button>
        </a-space>
      </div>
      
      <a-table
        :columns="columns"
        :data-source="problemList"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="id"
        :scroll="{ x: 1200 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" @click="handleViewDetail(record)">
                查看
              </a-button>
              <a-button type="link" @click="handleEdit(record)">
                编辑
              </a-button>
              <a-popconfirm
                title="确定要删除这道题目吗？"
                @confirm="handleDelete(record.id)"
              >
                <a-button type="link" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>
  </page-layout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import PageLayout from '@/components/layout/PageLayout.vue'
import request from "@/utils/request.js"
import { useRouter } from 'vue-router'
import { PlusOutlined, ReloadOutlined } from '@ant-design/icons-vue'

interface Problem {
  id: number
  name: string
  setter: string
  desc: string
  descInput: string
  descOutput: string
  sampleInput: string
  sampleOutput: string
  hint: string
  createTime: string
  acCount: number
  submitCount: number
  dailyChallenge: string
}

// 查询条件表单
const queryForm = reactive({
  id: '',
  name: '',
  setter: ''
})

const router = useRouter()

const columns = [
  {
    title: '题号',
    dataIndex: 'id',
    key: 'id',
    width: 80,
  },
  {
    title: '标题',
    dataIndex: 'name',
    key: 'name',
    ellipsis: true,
  },
  {
    title: '出题人',
    dataIndex: 'setter',
    key: 'setter',
    width: 120,
  },
  {
    title: '通过率',
    key: 'acceptance',
    width: 100,
    customRender: ({ record }) => {
      const rate = record.submitCount === 0 ? 0 : ((record.acCount / record.submitCount) * 100).toFixed(1)
      return `${rate}%`
    }
  },
  {
    title: '提交次数',
    dataIndex: 'submitCount',
    key: 'submitCount',
    width: 100,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180,
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right'
  },
]

const problemList = ref<Problem[]>([])
const loading = ref(false)
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
})

const fetchProblems = async () => {
  loading.value = true
  try {
    let response;
    const baseParams = {
      pageNum: pagination.value.current,
      pageSize: pagination.value.pageSize
    };

    // 根据查询条件选择对应的API接口
    if (queryForm.id) {
      // 按ID查询
      response = await request.get(`/problem/${queryForm.id}`);
      if (response.data.code === '200') {
        // 将单个对象转换为数组格式
        problemList.value = [response.data.data];
        pagination.value.total = 1;
      }
    } else if (queryForm.name) {
      // 按名称查询
      response = await request.get('/problem/search', {
        params: {
          keyword: queryForm.name
        }
      });
      if (response.data.code === '200') {
        problemList.value = response.data.data;
        pagination.value.total = response.data.data.length;
      }
    } else if (queryForm.setter) {
      // 按出题人查询
      response = await request.get('/problem/setter', {
        params: {
          ...baseParams,
          setter: queryForm.setter
        }
      });
      handleResponseData(response);
    } else {
      // 无查询条件，获取所有数据
      response = await request.get('/problem/page', {
        params: baseParams
      });
      handleResponseData(response);
    }
  } catch (error) {
    message.error('获取题目列表失败');
    console.error(error);
  } finally {
    loading.value = false;
  }
}

// 处理响应数据
const handleResponseData = (response) => {
  const res = response.data;
  if (res.code === '200') {
    // 检查是否包含records（针对模糊查询）还是list（针对分页查询）
    if (res.data.records) {
      problemList.value = res.data.records;
      pagination.value.total = res.data.total;
    } else if (res.data.list) {
      problemList.value = res.data.list;
      pagination.value.total = res.data.total;
    } else if (Array.isArray(res.data)) {
      // 处理直接返回数组的情况
      problemList.value = res.data;
      pagination.value.total = res.data.length;
    } else {
      problemList.value = [];
      pagination.value.total = 0;
    }
  } else {
    message.error(res.msg || '获取题目列表失败');
  }
}

// 查询方法
const handleQuery = () => {
  // 检查是否输入了多个查询条件
  const conditions = [queryForm.id, queryForm.name, queryForm.setter].filter(Boolean);
  if (conditions.length > 1) {
    message.warning('请只选择一个查询条件');
    return;
  }

  pagination.value.current = 1;
  fetchProblems();
}

// 重置方法
const handleReset = () => {
  queryForm.id = '';
  queryForm.name = '';
  queryForm.setter = '';
  pagination.value.current = 1;
  fetchProblems();
}

const handleAdd = () => {
  router.push('/admin/problem/add')
}

const handleEdit = (record: Problem) => {
  router.push(`/admin/problem/edit/${record.id}`)
}

const handleViewDetail = (record: Problem) => {
  router.push(`/problem/${record.id}`)
}

const handleDelete = async (id: number) => {
  try {
    const response = await request.delete(`/problem/${id}`)
    if (response.data.code === '200') {
      message.success('删除成功')
      fetchProblems()
    } else {
      message.error(response.data.msg || '删除失败')
    }
  } catch (error) {
    message.error('删除失败')
    console.error(error)
  }
}

const handleTableChange = (pag: any) => {
  pagination.value.current = pag.current
  pagination.value.pageSize = pag.pageSize
  fetchProblems()
}

onMounted(() => {
  fetchProblems()
})
</script>

<style scoped>
.problems-container {
  padding: 24px;
  background: #f0f2f5;
}

.search-bar {
  margin-bottom: 16px;
  background: #fff;
  padding: 16px;
  border-radius: 4px;
}

.operation-bar {
  margin-bottom: 16px;
  background: #fff;
  padding: 16px;
  border-radius: 4px;
}

:deep(.ant-table-wrapper) {
  background: #fff;
  padding: 16px;
  border-radius: 4px;
}

:deep(.ant-table-row) {
  cursor: pointer;
  transition: all 0.3s;
}

:deep(.ant-table-row:hover) {
  background: #f5f5f5;
}

:deep(.ant-form-item) {
  margin-bottom: 16px;
}
</style> 