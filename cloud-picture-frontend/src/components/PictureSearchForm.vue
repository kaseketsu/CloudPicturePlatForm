<template>
  <div class="picture-search-form">
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch" style="margin-bottom: 16px">
      <a-form-item label="关键词" name="keyword">
        <a-input
          v-model:value="searchParams.searchText"
          placeholder="从名称和简介搜索"
          allow-clear
        />
      </a-form-item>
      <a-form-item name="category" label="图片分类">
        <a-auto-complete
          v-model:value="searchParams.category"
          placeholder="请输入图片分类"
          :options="categoryOptions"
          allow-clear
          style="min-width: 180px"
        />
      </a-form-item>
      <a-form-item name="tags" label="图片标签">
        <a-select
          v-model:value="searchParams.tags"
          mode="tags"
          placeholder="请输入图片标签"
          :options="tagOptions"
          allow-clear
          style="min-width: 180px"
        />
      </a-form-item>

      <a-form-item label="日期" name="dateRange">
        <a-range-picker
          style="width: 400px"
          show-time
          v-model:value="dateRange"
          :placeholder="['编辑开始时间', '编辑结束时间']"
          format="YYYY/MM/DD HH:mm:ss"
          :presets="rangePresets"
          @change="onRangeChange"
        />
      </a-form-item>

      <a-form-item label="名称" name="name">
        <a-input v-model:value="searchParams.name" placeholder="请输入名称" allow-clear />
      </a-form-item>

      <a-form-item label="简介" name="introduction">
        <a-input v-model:value="searchParams.introduction" placeholder="请输入简介" allow-clear />
      </a-form-item>

      <a-form-item label="格式" name="picFormat">
        <a-input v-model:value="searchParams.picFormat" placeholder="请输入图片格式" allow-clear />
      </a-form-item>

      <a-form-item label="宽度" name="picWidth">
        <a-input-number v-model:value="searchParams.picWidth" />
      </a-form-item>

      <a-form-item label="高度" name="picHeight">
        <a-input-number v-model:value="searchParams.picHeight" />
      </a-form-item>

      <a-form-item>
        <a-space>
          <a-button type="primary" html-type="submit" style="width: 96px">搜索</a-button>
          <a-button html-type="reset" @click="doClear">重置</a-button>
        </a-space>
      </a-form-item>
    </a-form>
  </div>
</template>
<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { listPictureTagCategoryUsingGet } from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'

interface Props {
  onSearch?: (searchParams: API.PictureQueryRequest) => void
}

const props = defineProps<Props>()

//搜索条件
const searchParams = reactive<API.PictureQueryRequest>({})

type Option = { value: string; label: string }

const tagOptions = ref<Option[]>([])
const categoryOptions = ref<Option[]>([])
const getTagCategoryOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    tagOptions.value = (res.data.data.tagList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })
    categoryOptions.value = (res.data.data.categoryList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })
  } else {
    message.error('获取分类和标签失败' + res.data.message)
  }
}

onMounted(() => {
  getTagCategoryOptions()
})

//搜索数据
const doSearch = () => {
  props?.onSearch?.(searchParams)
}

const dateRange = ref<[]>([])

//清空搜索参数
const doClear = () => {
  //取消所有对象的值
  Object.keys(searchParams).forEach((key) => {
    searchParams[key] = undefined
  })
  //日期单独清空
  dateRange.value = []
  //清空后重新搜索
  props.onSearch?.(searchParams)
}

/**
 * 日期改变时触发
 * @param dates
 * @param dateStrings
 */
const onRangeChange = (dates: any[], dateStrings: string[]) => {
  if (dates?.length >= 2) {
    searchParams.startEditTime = dates[0].toDate()
    searchParams.endEditTime = dates[1].toDate()
  } else {
    searchParams.startEditTime = undefined
    searchParams.endEditTime = undefined
  }
}

//时间范围预设
const rangePresets = ref([
  { label: '过去 7 天', value: [dayjs().add(-7, 'd'), dayjs()] },
  { label: '过去 14 天', value: [dayjs().add(-14, 'd'), dayjs()] },
  { label: '过去 30 天', value: [dayjs().add(-30, 'd'), dayjs()] },
  { label: '过去 90 天', value: [dayjs().add(-90, 'd'), dayjs()] },
])
</script>

<style scoped>
.picture-search-form .ant-form-item {
  margin-top: 16px;
}
</style>
