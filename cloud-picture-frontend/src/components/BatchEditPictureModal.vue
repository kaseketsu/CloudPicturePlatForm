<template>
  <div class="batch-edit-picture-modal">
    <a-modal v-model:visible="visible" title="批量编辑图片" :footer="false" @cancel="closeModal">
      <a-typography-paragraph type="secondary">* 只对当前页面的图片生效</a-typography-paragraph>
      <!--图片信息表单-->
      <a-form
        layout="vertical"
        name="dataForm"
        :model="dataForm"
        @finish="handleSubmit"
        style="margin-bottom: 16px"
      >
        <a-form-item name="category" label="图片分类">
          <a-auto-complete
            v-model:value="dataForm.category"
            placeholder="请输入图片分类"
            :options="categoryOptions"
            allow-clear
          />
        </a-form-item>
        <a-form-item name="tags" label="图片标签">
          <a-select
            v-model:value="dataForm.tags"
            mode="tags"
            placeholder="请输入图片标签"
            :options="tagOptions"
            allow-clear
          />
        </a-form-item>
        <a-form-item name="nameRule" label="命名规则">
          <a-input
            v-model:value="dataForm.nameRule"
            placeholder="请输入命名规则, 输入{序号}动态生成图片名称"
            allow-clear
          />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit" style="width: 100%">提交</a-button>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import {
  editPictureByBatchUsingPost,
  editPictureUsingPost,
  listPictureTagCategoryUsingGet,
} from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'

const dataForm = reactive<API.PictureEditByBatchRequest>({
  category: '',
  tags: [],
  nameRule: '',
})
const open = ref<boolean>(false)

//是否可见
const visible = ref(false)

interface Props {
  pictureList: API.PictureVO[]
  spaceId: number
  onSuccess: () => void
}

const props = withDefaults(defineProps<Props>(), {})

//打开组件
const openModal = () => {
  visible.value = true
}

//关闭组件
const closeModal = () => {
  visible.value = false
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

const handleSubmit = async (values: any) => {
  if (!props.pictureList) {
    return
  }
  const res = await editPictureByBatchUsingPost({
    pictureIdList: props.pictureList.map((picture) => picture.id),
    spaceId: props.spaceId,
    ...values,
  })
  //操作结果
  if (res.data.code === 0 && res.data.data) {
    message.success('操作成功')
    closeModal()
    props.onSuccess?.()
  } else {
    message.error('操作失败' + res.data.message)
  }
}

onMounted(() => {
  getTagCategoryOptions()
})

//暴露函数给父组件
defineExpose({
  openModal,
})
</script>
