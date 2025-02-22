<template>
  <a-modal
    class="image-cropper"
    v-model:visible="visible"
    title="编辑图片"
    :footer="false"
    @cancel="closeModal"
  >
    <!-- 图片裁切组件 -->
    <vue-cropper
      ref="cropperRef"
      :img="imageUrl"
      output-type="png"
      :info="true"
      :can-move="false"
      :can-move-box="true"
      :fixed-box="false"
      :auto-crop="true"
      :center-box="true"
    />

    <div style="margin-bottom: 15px" />

    <!-- 图片操作 -->
    <div class="image-cropper-action">
      <a-space>
        <a-button @click="rotateLeft">向左旋转</a-button>
        <a-button @click="rotateRight">向右旋转</a-button>
        <a-button @click="changeScale(1)">放大</a-button>
        <a-button @click="changeScale(-1)">缩小</a-button>
        <a-button @click="handleConfirm" type="primary" :loading="loading">确认</a-button>
      </a-space>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import PictureVO = API.PictureVO
import { uploadPictureUsingPost } from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'

interface Props {
  imageUrl?: string
  spaceId?: number
  pictures?: API.PictureVO
  onSuccess?: (newPicture: PictureVO) => void
}

const props = defineProps<Props>()

//获取图片裁切器引用
const cropperRef = ref()

//缩放比例
const changeScale = (num) => {
  cropperRef.value?.changeScale(num)
}
const rotateLeft = () => {
  cropperRef.value?.rotateLeft()
}
const rotateRight = () => {
  cropperRef.value?.rotateRight()
}

const handleConfirm = () => {
  cropperRef.value.getCropBlob((blob: Blob) => {
    const fileName = (props.pictures?.name ?? 'image') + '.png'
    const file = new File([blob], fileName, { type: blob.type })
    handleUpload({ file })
  })
}

const loading = ref(false)

const handleUpload = async ({ file }: any) => {
  loading.value = true
  try {
    const params = props.pictures ? { id: props.pictures.id } : {}
    params.spaceId = props.spaceId
    //调用图片上传接口
    const res = await uploadPictureUsingPost(params, {}, file)
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

//是否可见
const visible = ref(true)

//打开组件
const openModal = () => {
  visible.value = true
}

//关闭组件
const closeModal = () => {
  visible.value = false
}

//暴露函数给父组件
defineExpose({
  openModal,
})
</script>

<style>
.image-cropper {
  text-align: center;
}

.image-cropper .vue-cropper{
  height: 400px;
}
</style>
