<template>
  <div id="addPictureBatchPage">
    <h2 style="margin-bottom: 16px">
      {{ '批量创建图片' }}
    </h2>
    <!--图片信息表单-->
    <a-form
      layout="vertical"
      name="formData"
      :model="formData"
      @finish="handleSubmit"
      style="margin-bottom: 16px"
    >
      <a-form-item name="searchText" label="关键词">
        <a-input v-model:value="formData.searchText" placeholder="请输入关键词" allow-clear />
      </a-form-item>
      <a-form-item name="count" label="抓取数量">
        <a-input-number
          v-model:value="formData.count"
          placeholder="输入抓取数量"
          :min="1"
          :max="30"
          style="min-width: 180px"
          allow-clear
        />
      </a-form-item>
      <a-form-item name="namePrefix" label="名称前缀">
        <a-input
          v-model:value="formData.namePrefix"
          placeholder="请输入名称前缀, 会自动补充序号"
          allow-clear
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%" :loading="loading"
          >批量创建</a-button
        >
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import {
 uploadPictureByBatchUsingPost
} from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref<boolean>(false)
const formData = reactive<API.PictureUploadByBatchRequest>({
  count: 10,
})
const handleSubmit = async (values: any) => {
  loading.value = true
  const res = await uploadPictureByBatchUsingPost({
    ...formData,
  })
  //操作结果
  if (res.data.code === 0 && res.data.data) {
    message.success(`图片创建成功，共${res.data.data}条`)
    //跳转到主页
    router.push({
      path: `/`,
    })
  } else {
    message.error('图片创建失败' + res.data.message)
  }
  loading.value = false
}

</script>

<style scoped>
#addPictureBatchPage {
  max-width: 720px;
  margin: 0 auto;
}
</style>
