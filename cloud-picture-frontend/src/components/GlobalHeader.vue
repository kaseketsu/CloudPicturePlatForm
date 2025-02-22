<template>
  <div id="globalHeader">
    <a-row :wrap="false">
      <a-col flex="200px">
        <router-link to="/">
          <div class="title-bar">
            <img class="image" src="../assets/logo.png" alt="logo" />
            <div class="title">小花云图库</div>
          </div>
        </router-link>
      </a-col>

      <a-col flex="auto">
        <a-menu
          v-model:selectedKeys="current"
          mode="horizontal"
          :items="items"
          @click="doMenuClick"
        />
      </a-col>
      <!--      用户信息展示-->
      <a-col flex="120px">
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser.id">
            <a-dropdown>
              <a-space>
                <a-avatar :src="loginUserStore.loginUser.userAvatar" />
                {{ loginUserStore.loginUser.userName ?? '无名' }}
              </a-space>
              <template #overlay>
                <a-menu>
                  <a-menu-item>
                    <router-link to="/my_space">
                      <UserOutlined />
                      我的空间
                    </router-link>
                  </a-menu-item>
                  <a-menu-item @click="doLogOut">
                    <LogoutOutlined />
                    退出登录
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
          <div v-else>
            <a-button type="primary" href="/user/login">登录</a-button>
          </div>
        </div>
      </a-col>
    </a-row>
  </div>
</template>
<script lang="ts" setup>
import { computed, h, ref } from 'vue'
import {
  MailOutlined,
  AppstoreOutlined,
  SettingOutlined,
  UserOutlined,
  SlackSquareOutlined,
  LogoutOutlined,
} from '@ant-design/icons-vue'
import { MenuProps, message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { userLogoutUsingPost } from '@/api/userController.ts'

const loginUserStore = useLoginUserStore()

const originItems = [
  {
    key: '/',
    icon: () => h(SlackSquareOutlined),
    label: '主页',
    title: '主页',
  },
  {
    key: '/add_picture',
    label: '创建图片',
    title: '创建图片',
  },
  {
    key: '/add_space',
    label: '创建空间',
    title: '创建空间',
  },
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: '/admin/pictureManage',
    label: '图片管理',
    title: '图片管理',
  },
  {
    key: '/admin/spaceManage',
    label: '空间管理',
    title: '空间管理',
  },
  {
    key: 'others',
    label: h('a', { href: 'https://bilibili.com', target: '_blank' }, 'bilibili'),
    title: 'bilibili',
  },
]
//过滤菜单项
const filterMenus = (menus = [] as MenuProps['items']) => {
  return menus?.filter((menu) => {
    //导航栏权限校验
    if (menu?.key?.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      if (!loginUser || loginUser.userRole !== 'admin') {
        return false
      }
    }
    return true
  })
}

//过滤后的路由数组
const items = computed(() => {
  return filterMenus(originItems)
})

const router = useRouter()

//点击事件，跳转到对应key的vue页面
const doMenuClick = ({ key }) => {
  router.push({
    path: key,
  })
}

//注销实践
const doLogOut = async () => {
  const response = await userLogoutUsingPost()
  if (response.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: '未登录',
    })
    message.success('退出登录成功')
    await router.push('/user/login')
  } else {
    message.error('退出登录失败' + response.data.message)
  }
}

//监听路由变化, 高亮当前菜单项
const current = ref<string[]>([])
router.afterEach((to, from) => {
  current.value = [to.path]
})
</script>

<style scoped>
#globalHeader .title-bar {
  display: flex;
  align-items: center;
}

.image {
  height: 48px;
}

.title {
  color: black;
  font-size: 18px;
  margin-left: 16px;
}
</style>
