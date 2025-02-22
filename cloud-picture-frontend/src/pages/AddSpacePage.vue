<template>
  <div id="addSpacePage">
    <h2 style="margin-bottom: 16px">
      {{ route.query?.id ? '修改空间' : '创建空间' }}
    </h2>
    <!--空间信息表单-->
    <a-form
      layout="vertical"
      name="spaceForm"
      :model="spaceForm"
      @finish="handleSubmit"
      style="margin-bottom: 16px"
    >
      <a-form-item name="spaceName" label="空间名称">
        <a-input v-model:value="spaceForm.spaceName" placeholder="请输入空间名称" allow-clear />
      </a-form-item>
      <a-form-item name="spaceLevel" label="空间级别">
        <a-select
          v-model:value="spaceForm.spaceLevel"
          style="min-width: 180px"
          placeholder="请输入空间级别"
          :options="SPACE_LEVEL_OPTIONS"
          allow-clear
        ></a-select>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" :loading="loading" style="width: 100%"
          >{{ route.query?.id ? '修改' : '创建' }}</a-button
        >
      </a-form-item>
    </a-form>
    <!-- 空间级别介绍 -->
    <a-card title="空间级别介绍">
      <a-typography-paragraph>
        * 目前只支持开通普通版，如需升级空间，请联系小花同学
      </a-typography-paragraph>
      <a-typography-paragraph v-for="spaceLevel in spaceLevelList">
        {{ spaceLevel.text }}: 容量大小 {{ formatSize(spaceLevel.maxSize) }}, 图片数量
        {{ spaceLevel.maxCount }}
      </a-typography-paragraph>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import {
  addSpaceUsingPost,
  editSpaceUsingPost,
  getSpaceVoByIdUsingGet,
  listSpaceLevelUsingGet,
  updateSpaceUsingPost,
} from '@/api/spaceController.ts'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import { SPACE_LEVEL_OPTIONS } from '@/constant/space.ts'
import { formatSize } from '../utils'

const spaceLevelList = ref<API.SpaceLevel[]>([])
const router = useRouter()
const space = ref<API.SpaceVO>()
const spaceForm = reactive<API.SpaceAddRequest | API.SpaceEditRequest>({})
const loading = ref(false)

//获取空间级别
const fetchSpaceLevelList = async () => {
  const res = await listSpaceLevelUsingGet()
  if (res.data.code === 0 && res.data.data) {
    spaceLevelList.value = res.data.data
  } else {
    message.error('获取空间级别失败, ' + res.data.message)
  }
}

onMounted(() => {
  fetchSpaceLevelList()
})

const handleSubmit = async (values: any) => {
  const spaceId = space.value?.id
  loading.value = true
  let res
  //更新空间
  if (spaceId) {
    res = await updateSpaceUsingPost({
      id: spaceId,
      ...values,
    })
  } else {
    res = await addSpaceUsingPost({
      ...values,
    })
  }
  //操作结果
  if (res.data.code === 0 && res.data.data) {
    message.success('操作成功')
    //跳转到空间详情页
    router.push({
      path: `/space/${res.data.data}`,
    })
  } else {
    message.error('操作失败, ' + res.data.message)
  }
  loading.value = false
}

type Option = { value: string; label: string }

const route = useRoute()
//获取老数据
const getOldSpace = async () => {
  //获取id
  const id = route.query?.id
  if (id) {
    const res = await getSpaceVoByIdUsingGet({
      id,
    })
    if (res.data.code === 0 && res.data.data) {
      const data = res.data.data
      space.value = data
      spaceForm.spaceName = data.spaceName
      spaceForm.spaceLevel = data.spaceLevel
    }
  }
}

onMounted(() => {
  getOldSpace()
})
</script>

<style scoped>
#addSpacePage {
  max-width: 720px;
  margin: 0 auto;
}
</style>
