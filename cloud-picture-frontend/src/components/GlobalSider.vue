<template>
  <div id="globalSider">
    <a-layout-sider v-if="loginUserStore.loginUser.id" width="200" breakpoint="lg" collapsed-width="0px">
      <a-menu
        v-model:selectedKeys="current"
        mode="inline"
        :items="menuItems"
        @click="doMenuClick"
      />
    </a-layout-sider>
  </div>
</template>
<script lang="ts" setup>
import { computed, customRef, h, ref } from 'vue'
import { PictureOutlined, UserOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { userLogoutUsingPost } from '@/api/userController.ts'

const loginUserStore = useLoginUserStore()

const menuItems = [
  {
    key: '/',
    icon: () => h(PictureOutlined),
    label: '公共图库',
    title: '公共图库',
  },
  {
    key: '/my_space',
    icon: () => h(UserOutlined),
    label: '我的空间',
    title: '我的空间',
  },
]

const router = useRouter()

//监听路由变化, 高亮当前菜单项
const current = ref<string[]>([])
router.afterEach((to, from) => {
  current.value = [to.path]
})

//点击事件，跳转到对应key的vue页面
const doMenuClick = ({ key }) => {
  router.push({
    path: key,
  })
}

</script>

<style scoped>
#globalSider .ant-layout-sider {
  background: none;
}
</style>
