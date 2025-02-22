<template>
  <div id="spaceDetailPage">
    <!-- 空间信息 -->
    <a-flex justify="space-between">
      <div style="display: flex; align-items: center; height: 50px">
        <h2>{{ space.spaceName }}(私有空间)</h2>
      </div>
      <a-space size="middle">
        <a-button type="primary" :href="`/add_picture?spaceId=${id}`" target="_blank"
          >+ 创建图片
        </a-button>
        <a-button type="primary" ghost :icon="h(EditOutlined)" @click="doBatchEdit">批量编辑 </a-button>
        <a-tooltip
          :title="`占用空间 ${formatSize(space.totalSize)} / ${formatSize(space.maxSize)}`"
        >
          <a-progress
            type="circle"
            :size="50"
            :percent="((space.totalSize * 100) / space.maxSize).toFixed(1)"
          />
        </a-tooltip>
      </a-space>
    </a-flex>
    <div style="margin-bottom: 16px" />
    <!-- 空间搜索表单 -->
    <PictureSearchForm :onSearch="onSearch" />
    <!-- 按颜色搜索 -->
    <a-form-item label="按颜色搜索">
      <color-picker format="hex" @pureColorChange="onColorChange" />
    </a-form-item>
    <div style="margin-bottom: 16px"></div>
    <PictureList :dataList="dataList" :loading="loading" :show-op="true" :on-reload="fetchData" />
    <a-pagination
      v-model:current="searchParams.current"
      v-model:page-size="searchParams.pageSize"
      :total="total"
      @change="onPageChange"
      style="text-align: right"
    />
  </div>
  <BatchEditPictureModal
    ref="batchEditPictureRef"
    :spaceId="id"
    :pictureList="dataList"
    :onSuccess="onBatchEditPictureSuccess"
  />
</template>
<script setup lang="ts">
import { onMounted, reactive, ref, h, computed } from 'vue'
import { message } from 'ant-design-vue'
import { getSpaceVoByIdUsingGet } from '@/api/spaceController.ts'
import {
  listPictureVoByPageUsingPost,
  searchPictureByColorUsingPost,
} from '@/api/pictureController.ts'
import { formatSize } from '@/utils'
import PictureList from '@/components/PictureList.vue'
import PictureSearchForm from '@/components/PictureSearchForm.vue'
import { ColorPicker } from 'vue3-colorpicker'
import 'vue3-colorpicker/style.css'
import BatchEditPictureModal from '@/components/BatchEditPictureModal.vue'
import { EditOutlined } from '@ant-design/icons-vue'

interface Props {
  id: string | number
}

const props = defineProps<Props>()
const space = ref<API.SpaceVO>({})
const dataList = ref<API.PictureVO[]>([])
const total = ref(0)
const loading = ref(true)
//获取空间信息
const fetchSpaceDetail = async () => {
  try {
    const res = await getSpaceVoByIdUsingGet({
      id: props.id,
    })
    if (res.data.code === 0 && res.data.data) {
      space.value = res.data.data
      console.log(space.value.maxSize)
    } else {
      message.error('获取空间详情失败, ' + res.data.message)
    }
  } catch (e: any) {
    message.error('获取空间详情失败, ' + e.message)
  }
}

//搜索条件
const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 12,
  sortField: 'createTime',
  sortOrder: 'descend',
})

//分页器
const onPageChange = (page: number, pageSize: number) => {
  searchParams.current = page
  searchParams.pageSize = pageSize
  fetchData()
}

//获取数据
const fetchData = async () => {
  loading.value = true
  //转换搜索参数
  const params = {
    spaceId: props.id,
    ...searchParams,
  }
  const res = await listPictureVoByPageUsingPost(params)
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败' + res.data.message)
  }
  loading.value = false
}

const onSearch = (newSearchParams: API.PictureQueryRequest) => {
  // 使用 Object.assign 避免直接修改原对象
  Object.assign(searchParams, newSearchParams)

  // 重置分页或其他默认设置
  searchParams.current = 1

  // 执行数据请求
  fetchData()
}

const onColorChange = async (color: string) => {
  loading.value = true
  const res = await searchPictureByColorUsingPost({
    picColor: color,
    spaceId: props.id,
  })
  if (res.data.code === 0 && res.data.data) {
    const data = res.data.data
    dataList.value = data
    total.value = data.length
  } else {
    message.error('获取数据失败, ' + res.data.message)
  }
  loading.value = false
}

const batchEditPictureRef = ref()

//操作成功后刷新页面(重新获取数据)
const onBatchEditPictureSuccess = () => {
  fetchData()
}

//打开编辑弹窗
const doBatchEdit = () => {
  if (batchEditPictureRef.value) {
    batchEditPictureRef.value.openModal()
  }
}

//页面加载时获取数据
onMounted(() => {
  fetchSpaceDetail()
  fetchData()
})
</script>
<style scoped>
#spaceDetailPage {
  margin-bottom: 16px;
}
</style>
