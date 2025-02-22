<template>
  <div class="picture-list">
    <!-- 图片列表-->
    <a-list
      :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 5, xxl: 6 }"
      :data-source="dataList"
      :loading="loading"
    >
      <template #renderItem="{ item: picture }">
        <a-list-item style="padding: 8px">
          <!-- 单张图片 -->
          <a-card hoverable @click="doClickPicture(picture)">
            <template #cover>
              <img
                :alt="picture.name"
                :src="picture.thumbnailUrl ?? picture.url"
                style="height: 180px; object-fit: cover"
              />
            </template>
            <a-card-meta :title="picture.name">
              <template #description>
                <a-flex>
                  <a-tag color="green">
                    {{ picture.category ?? '默认' }}
                  </a-tag>
                  <a-tag v-for="tag in picture.tags" :key="tag">
                    {{ tag }}
                  </a-tag>
                </a-flex>
              </template>
            </a-card-meta>
            <template #actions v-if="showOp">
              <share-alt-outlined @click="(e) => doShare(picture, e)" />
              <search-outlined @click="(e) => doSearch(picture, e)" />
              <edit-outlined @click="(e) => doEdit(picture, e)" />
              <delete-outlined @click="(e) => doDelete(picture, e)" />
            </template>
          </a-card>
        </a-list-item>
      </template>
    </a-list>
    <ShareModel ref="shareModalRef" :link="shareLink" />
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import {
  DeleteOutlined,
  EditOutlined,
  SearchOutlined,
  ShareAltOutlined,
} from '@ant-design/icons-vue'
import { deletePictureUsingPost } from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import ShareModel from '@/components/ShareModel.vue'
import { ref } from 'vue'

interface Props {
  dataList?: API.PictureVO[]
  loading?: boolean
  showOp?: boolean
  onReload?: () => void
}

const props = withDefaults(defineProps<Props>(), {
  dataList: () => [],
  loading: false,
  showOp: false,
})

const router = useRouter()
/**
 * 跳转图片详情页
 * @param picture
 */
const doClickPicture = (picture: API.PictureVO) => {
  router.push({
    path: `/picture/${picture.id}`,
  })
}

/**
 * 删除图片
 */
const doDelete = async (picture: API.PictureVO, e) => {
  //阻止冒泡
  e.stopPropagation()
  const id = picture.id
  if (!id) {
    return
  }
  const res = await deletePictureUsingPost({ id })
  if (res.data.code === 0) {
    props.onReload?.()
    message.success('删除成功')
  } else {
    message.error('删除失败')
  }
}

/**
 * 编辑图片
 */
const doEdit = (picture: API.PictureVO, e) => {
  //阻止冒泡
  e.stopPropagation()
  router.push({
    path: `/add_picture`,
    query: { id: picture.id, spaceId: picture.spaceId },
  })
}

/**
 * 搜索图片
 */
const doSearch = (picture: API.PictureVO, e) => {
  //阻止冒泡
  e.stopPropagation()
  //打开新页面
  window.open(`/search_picture?pictureId=${picture.id}`)
}

const shareModalRef = ref()
const shareLink = ref<string>('')
/**
 * 分享图片
 */
const doShare = (picture: API.PictureVO, e) => {
  //阻止冒泡
  e.stopPropagation()
  shareLink.value = `${window.location.protocol}//${window.location.host}/picture/${picture.id}`
  if (shareModalRef.value) {
    shareModalRef.value.openModal()
  }
}
</script>
<style scoped></style>
