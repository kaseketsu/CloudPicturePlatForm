<template>
  <div class="url-picture-upload">
    <a-input-group compact>
      <a-input
        v-model:value="fileUrl"
        style="width: calc(100% - 120px)"
        placeholder="请输入图片url"
      />
      <a-button type="primary" style="width: 120px" :loading="loading" @click="handleUpload"
        >提交</a-button
      >
    </a-input-group>
    <div class="img-wrapper">
      <img v-if="pictures?.url" :src="pictures?.url" alt="avatar" />
    </div>
  </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import type { UploadChangeParam, UploadProps } from 'ant-design-vue'
import PictureVO = API.PictureVO
import { uploadPictureByUrlUsingPost, uploadPictureUsingPost } from '@/api/pictureController.ts'

interface Props {
  spaceId?: number
  pictures?: API.PictureVO
  onSuccess?: (newPicture: PictureVO) => void
}

const props = defineProps<Props>()
const fileUrl = ref<string>()
const loading = ref<boolean>(false)
const handleUpload = async () => {
  loading.value = true
  try {
    const params: API.PictureUploadRequest = { fileUrl: fileUrl.value }
    params.spaceId = props.spaceId
    if (props.pictures) {
      params.id = props.pictures.id
    }
    //调用图片上传接口
    const res = await uploadPictureByUrlUsingPost(params)
    if (res.data.code === 0 && res.data.data) {
      message.success('图片上传成功')
      //将图片信息上传给父组件
      props.onSuccess?.(res.data.data)
    } else {
      message.error('图片上传失败' + res.data.message)
    }
  } catch (error) {
    console.error('图片上传失败', error)
    message.error('图片上传失败' + error.message)
  }
  loading.value = false
}
</script>
<style scoped>
.url-picture-upload {
  margin-bottom: 16px;
}

.url-picture-upload img {
  max-width: 100%;
  max-height: 480px;
}

.url-picture-upload .img-wrapper {
  text-align: center;
  margin-top: 16px;
}

</style>
