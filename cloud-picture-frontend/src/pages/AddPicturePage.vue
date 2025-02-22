<template>
  <div id="addPicturePage">
    <h2 style="margin-bottom: 16px">
      {{ route.query?.id ? '修改图片' : '创建图片' }}
    </h2>
    <a-typography-paragraph v-if="spaceId" type="secondary">
      保存至空间: <a :href="`/space/${spaceId}`" target="_blank"> {{ spaceId }}</a>
    </a-typography-paragraph>
    <!-- 选择上传方式 -->
    <a-tabs v-model:activeKey="uploadTab">
      <a-tab-pane key="file" tab="文件上传">
        <!-- 图片上传组件-->
        <PictureUpload :pictures="picture" :spaceId="spaceId" :on-success="onSuccess" />
      </a-tab-pane>
      <a-tab-pane key="url" tab="url上传" force-render>
        <!-- url图片上传组件 -->
        <UrlPictureUpload :pictures="picture" :spaceId="spaceId" :on-success="onSuccess" />
      </a-tab-pane>
    </a-tabs>
    <!--图片信息表单-->
    <a-form
      v-if="picture"
      layout="vertical"
      name="pictureForm"
      :model="pictureForm"
      @finish="handleSubmit"
      style="margin-bottom: 16px"
    >
      <a-form-item name="name" label="图片名称">
        <a-input v-model:value="pictureForm.name" placeholder="请输入图片名称" allow-clear />
      </a-form-item>
      <a-form-item name="introduction" label="图片简介">
        <a-textarea
          v-model:value="pictureForm.introduction"
          placeholder="输入图片简介"
          :autoSize="{ minRows: 2, maxRows: 5 }"
          allow-clear
        />
      </a-form-item>
      <a-form-item name="category" label="图片分类">
        <a-auto-complete
          v-model:value="pictureForm.category"
          placeholder="请输入图片分类"
          :options="categoryOptions"
          allow-clear
        />
      </a-form-item>
      <a-form-item name="tags" label="图片标签">
        <a-select
          v-model:value="pictureForm.tags"
          mode="tags"
          placeholder="请输入图片标签"
          :options="tagOptions"
          allow-clear
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">提交</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import PictureUpload from '@/components/PictureUpload.vue'
import { computed, onMounted, reactive, ref } from 'vue'
import {
  editPictureUsingPost,
  getPictureVoByIdUsingGet,
  listPictureTagCategoryUsingGet,
} from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import _default from 'ant-design-vue/es/vc-slick/inner-slider'
import UrlPictureUpload from '@/components/UrlPictureUpload.vue'
import ImageCropper from '@/components/ImageCropper.vue'

const router = useRouter()
const route = useRoute()
const picture = ref<API.PictureVO>()
const pictureForm = reactive<API.PictureEditRequest>({})
const uploadTab = ref<'file' | 'url'>('file')

const spaceId = computed(() => {
  return route.query?.spaceId
})

const handleSubmit = async (values: any) => {
  const pictureId = picture.value?.id
  if (!pictureId) {
    return
  }
  const res = await editPictureUsingPost({
    id: pictureId,
    spaceId: spaceId.value,
    ...values,
  })
  //操作结果
  if (res.data.code === 0 && res.data.data) {
    message.success('图片创建成功')
    //跳转到图片详情页
    router.push({
      path: `/picture/${pictureId}`,
    })
  } else {
    message.error('图片创建失败' + res.data.message)
  }
}
/**
 * 图片上传成功
 * @param newPicture
 */
const onSuccess = (newPicture: API.PictureVO) => {
  picture.value = newPicture
  pictureForm.name = newPicture.name
}

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

//获取老数据
const getOldPicture = async () => {
  //获取id
  const id = route.query?.id
  if (id) {
    const res = await getPictureVoByIdUsingGet({
      id,
    })
    if (res.data.code === 0 && res.data.data) {
      const data = res.data.data
      picture.value = data
      pictureForm.name = data.name
      pictureForm.introduction = data.introduction
      pictureForm.category = data.category
      pictureForm.tags = data.tags
    }
  }
}

onMounted(() => {
  getOldPicture()
})
</script>

<style scoped>
#addPicturePage {
  max-width: 720px;
  margin: 0 auto;
}
</style>
