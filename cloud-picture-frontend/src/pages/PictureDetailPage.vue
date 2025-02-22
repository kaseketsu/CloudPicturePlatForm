<template>
  <div id="pictureDetailPage">
    <a-row :gutter="[16, 16]">
      <!-- 图片预览 -->
      <a-col :sm="24" :md="14" :xl="16">
        <a-card title="图片预览">
          <a-image :src="picture.url" style="max-height: 600px; object-fit: contain" />
        </a-card>
      </a-col>
      <!-- 图片信息 -->
      <a-col :sm="24" :md="10" :xl="8">
        <a-card title="图片信息">
          <a-descriptions :column="1">
            <a-descriptions-item label="作者">
              <a-space>
                <a-avatar :size="24" :src="picture.userVO?.userAvatar" />
                <div>{{ picture.userVO?.userName }}</div>
              </a-space>
            </a-descriptions-item>
            <a-descriptions-item label="图片名称"
              >{{ picture.name ?? '未命名' }}
            </a-descriptions-item>
            <a-descriptions-item label="图片简介"
              >{{ picture.introduction ?? '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="图片分类"
              >{{ picture.category ?? '默认' }}
            </a-descriptions-item>
            <a-descriptions-item label="图片标签">
              <a-tag v-for="tag in picture.tags" :key="tag">
                {{ tag }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="图片格式"
              >{{ picture.picFormat ?? '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="图片宽度"
              >{{ picture.picWidth ?? '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="图片高度"
              >{{ picture.picHeight ?? '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="图片宽高比"
              >{{ picture.picScale ?? '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="图片大小"
              >{{ formatSize(picture.picSize) ?? '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="图片色调">
              <a-space>
                {{ picture.picColor ?? '-' }}
                <div
                  v-if="picture.picColor"
                  :style="{ width: '16px', height: '16px', backgroundColor: toHexColor(picture.picColor) }"
                />
              </a-space>
            </a-descriptions-item>
          </a-descriptions>
          <!-- 操作按钮 -->
          <a-space wrap>
            <a-button type="primary" @click="doDownload">
              免费下载
              <template #icon>
                <DownloadOutlined />
              </template>
            </a-button>
            <a-button type="primary" ghost :icon="h(ShareAltOutlined)" @click="doShare">
              分享图片
            </a-button>
            <a-button
              v-if="authCheck"
              type="link"
              :icon="h(EditOutlined)"
              typeof="default"
              style="margin-right: 3px"
              @click="doEdit"
            >
              编辑
            </a-button>
            <a-button
              v-if="authCheck"
              :icon="h(DeleteOutlined)"
              danger
              @click="doDelete"
              style="margin-right: 3px"
            >
              删除
            </a-button>
          </a-space>
        </a-card>
      </a-col>
    </a-row>
    <ShareModel ref="shareModalRef" :link="shareLink" />
  </div>
</template>
<script setup lang="ts">
import { onMounted, reactive, ref, h, computed } from 'vue'
import { message } from 'ant-design-vue'
import { deletePictureUsingPost, getPictureVoByIdUsingGet } from '@/api/pictureController.ts'
import { downloadImage, formatSize, toHexColor } from '../utils'
import { DownloadOutlined, EditOutlined, DeleteOutlined, ShareAltOutlined } from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { useRouter } from 'vue-router'
import ShareModel from '@/components/ShareModel.vue'
import ImageCropper from '@/components/ImageCropper.vue'

interface Props {
  id: string | number
}

const props = defineProps<Props>()
const picture = ref<API.PictureVO>({})

const shareModalRef = ref()
const shareLink = ref<string>('')
/**
 * 分享图片
 */
const doShare = () => {
  shareLink.value = `${window.location.protocol}//${window.location.host}/picture/${picture.value.id}`
  if (shareModalRef.value) {
    shareModalRef.value.openModal()
  }
}

//获取图片信息
const fetchPictureDetail = async () => {
  try {
    const res = await getPictureVoByIdUsingGet({
      id: props.id,
    })
    if (res.data.code === 0 && res.data.data) {
      picture.value = res.data.data
    } else {
      message.error('获取图片详情失败, ' + res.data.message)
    }
  } catch (e: any) {
    message.error('获取图片详情失败, ' + e.message)
  }
}

const loginUserStore = useLoginUserStore()
/**
 * 是否具有权限
 */
const authCheck = computed(() => {
  const loginUser = loginUserStore.loginUser
  if (!loginUser.id) {
    return false
  }
  const user = picture.value.userVO || {}
  return loginUser.id === user.id || loginUser.userRole === 'admin'
})

/**
 * 删除图片
 */
const doDelete = async () => {
  const id = picture.value.id
  if (!id) {
    return
  }
  const res = await deletePictureUsingPost({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
  } else {
    message.error('删除失败')
  }
}

const router = useRouter()
/**
 * 编辑图片
 */
const doEdit = () => {
  router.push({
    path: `/add_picture`,
    query: { id: picture.value.id, spaceId: picture.value.spaceId },
  })
}

/**
 * 下载图片
 */
const doDownload = () => {
  downloadImage(picture.value.url)
}

//页面加载时获取数据
onMounted(() => {
  fetchPictureDetail()
})
</script>
<style scoped>
#pictureDetailPage {
  margin-bottom: 16px;
}
</style>
