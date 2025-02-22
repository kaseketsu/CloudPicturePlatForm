<template>
  <div id="homePage">
    <div class="search-bar">
      <!-- 搜索框 -->
      <a-input-search
        v-model:value="searchParams.searchText"
        placeholder="从海量图片中搜索"
        enter-button="搜索"
        size="large"
        @search="onSearch"
      />
    </div>
    <!-- 标签分类和标签筛选 -->
    <a-tabs v-model:active-key="selectedCategory" @change="onSearch" style="margin-left: 6px">
      <a-tab-pane key="all" tab="全部" />
      <a-tab-pane v-for="category in categoryList" :tab="category" :key="category" />
    </a-tabs>
    <div class="tag-bar">
      <span style="margin-right: 8px">标签: </span>
      <a-space :size="[0, 8]" wrap>
        <a-checkable-tag
          v-for="(tag, index) in tagList"
          :key="tag"
          v-model:checked="selectedTagList[index]"
          @change="onSearch"
        >
          {{ tag }}
        </a-checkable-tag>
      </a-space>
    </div>

    <!-- 图片列表-->
    <PictureList :dataList="dataList" :loading="loading" />
    <a-pagination
      v-model:current="searchParams.current"
      v-model:page-size="searchParams.pageSize"
      :total="total"
      @change="onPageChange"
      style="text-align: right"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  listPictureByPageUsingPost,
  listPictureTagCategoryUsingGet,
  listPictureVoByPageUsingPost,
} from '@/api/pictureController.ts'
import { useRouter } from 'vue-router'
import PictureList from '@/components/PictureList.vue'

const dataList = ref<API.PictureVO[]>([])
const total = ref(0)
const loading = ref(true)

//搜索条件
const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 12,
  sortField: 'createTime',
  sortOrder: 'descend',
})

//页面加载时获取数据
onMounted(() => {
  fetchData()
  getTagCategoryOptions()
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
    ...searchParams,
    tags: [] as string[],
  }
  if (selectedCategory.value !== 'all') {
    params.category = selectedCategory.value
  }
  selectedTagList.value.forEach((useTag, index) => {
    if (useTag) {
      params.tags.push(tagList.value[index])
    }
  })
  const res = await listPictureVoByPageUsingPost(params)
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败' + res.data.message)
  }
  loading.value = false
}

const onSearch = () => {
  searchParams.current = 1
  fetchData()
}

//标签和分类列表
const tagList = ref<string[]>([])
const categoryList = ref<string[]>([])
const selectedCategory = ref<string>('all')
const selectedTagList = ref<boolean[]>([])
/**
 * 获取标签和图片
 */
const getTagCategoryOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    tagList.value = res.data.data.tagList ?? []
    categoryList.value = res.data.data.categoryList ?? []
  } else {
    message.error('获取分类和标签失败' + res.data.message)
  }
}

</script>
<style scoped>
#homePage {
  margin-bottom: 16px;
}

#homePage .search-bar {
  max-width: 480px;
  margin: 0 auto 16px;
}

#homePage .tag-bar {
  margin-bottom: 20px;
  margin-left: 6px;
}
</style>
