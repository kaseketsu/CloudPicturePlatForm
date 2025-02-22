<template>
  <div id="spaceManagePage">
    <a-flex justify="space-between">
      <h2>空间管理</h2>
      <a-space>
        <a-button type="primary" href="/add_space" target="_blank">+ 创建空间</a-button>
      </a-space>
    </a-flex>
    <div style="margin-bottom: 16px"/>
    <!--    搜索表单-->
    <a-form layout="inline" :model="searchParams" @finish="doSearch" style="margin-bottom: 16px">
      <a-form-item label="空间名称">
        <a-input
          v-model:value="searchParams.spaceName"
          placeholder="搜索空间名称"
          allow-clear
        />
      </a-form-item>
      <a-form-item name="spaceLevel" label="空间级别">
        <a-select
          v-model:value="searchParams.spaceLevel"
          style="min-width: 180px"
          placeholder="请输入空间级别"
          :options="SPACE_LEVEL_OPTIONS"
          allow-clear
        ></a-select>
      </a-form-item>
      <a-form-item label="用户id">
        <a-input v-model:value="searchParams.userId" placeholder="请输入用户id" allow-clear />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>
    <!--    表格-->
    <a-table
      :columns="columns"
      :data-source="dataList"
      :pagination="pagination"
      @change="doTableChange"
      :scroll="{ x: 'max-content' }"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'spaceLevel'">
          <div>{{ SPACE_LEVEL_MAP[record.spaceLevel] }}</div>
        </template>
        <template v-if="column.dataIndex === 'spaceUseInfo'">
          <div>容量大小: {{ formatSize(record.totalSize) }} / {{ formatSize(record.maxSize)}}</div>
          <div>图片数量: {{ record.totalCount }} / {{ record.maxCount}}</div>
        </template>

        <template v-if="column.dataIndex === 'userId'">
          {{ record.userId }}
        </template>

        <template v-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-if="column.dataIndex === 'editTime'">
          {{ dayjs(record.editTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space wrap>
            <a-button type="link" :href="`/add_space?id=${record.id}`" target="_blank"
              >编辑
            </a-button>
            <a-button danger @click="doDelete(record.id)">删除</a-button>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>
<script lang="ts" setup>
import { SmileOutlined, DownOutlined } from '@ant-design/icons-vue'
import { computed, onMounted, reactive, ref } from 'vue'
import {
  deleteSpaceUsingPost,
  listSpaceByPageUsingPost,
} from '@/api/spaceController.ts'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { formatSize } from '../../utils'
import { SPACE_LEVEL_MAP, SPACE_LEVEL_OPTIONS } from '../../constant/space.ts'

const columns = [
  {
    title: 'id',
    dataIndex: 'id',
    width: 80,
  },
  {
    title: '图片',
    dataIndex: 'url',
  },
  {
    title: '空间名称',
    dataIndex: 'spaceName',
  },
  {
    title: '空间级别',
    dataIndex: 'spaceLevel',
  },
  {
    title: '使用情况',
    dataIndex: 'spaceUseInfo',
  },
  {
    title: '用户id',
    dataIndex: 'userId',
    width: 80,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '编辑时间',
    dataIndex: 'editTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]

//定义数据
const dataList = ref<API.Space[]>([])
const total = ref(0)

//搜索条件
const searchParams = reactive<API.SpaceQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend',
})

//页面加载时获取数据
onMounted(() => {
  fetchData()
})

//分页器
const pagination = computed(() => {
  return {
    current: searchParams.current,
    pageSize: searchParams.pageSize,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total) => `共 ${total} 条`,
  }
})

//改变分页条件
const doTableChange = (page: any) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

//获取数据
const fetchData = async () => {
  console.log(searchParams)
  const res = await listSpaceByPageUsingPost({
    ...searchParams,
  })
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败' + res.data.message)
  }
}

//搜索数据
const doSearch = () => {
  //重置页码
  searchParams.current = 1
  fetchData()
}

//删除数据
const doDelete = async (id: string) => {
  if (!id) {
    return
  }
  const res = await deleteSpaceUsingPost({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    fetchData()
  } else {
    message.error('删除失败')
  }
}

</script>
