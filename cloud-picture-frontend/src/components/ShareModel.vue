<template>
  <div>
    <a-modal v-model:visible="visible" :title="title" :footer="false" @cancel="closeModal">
      <h4>复制分享链接</h4>
      <a-typography-link copyable>
        {{ link }}
      </a-typography-link>
      <div style="margin-bottom: 11px" />
      <h4>手机扫码查看</h4>
      <a-qrcode :value="link" />
    </a-modal>
  </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue'

const open = ref<boolean>(false)

//是否可见
const visible = ref(false)

interface Props {
  title: string
  link: string
}

const props = withDefaults(defineProps<Props>(), {
  title: '分享图片',
  link: 'https://www.bilibili.com',
})

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
