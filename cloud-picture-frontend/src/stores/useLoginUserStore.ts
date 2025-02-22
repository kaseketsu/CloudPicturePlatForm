import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { getLoginUserUsingGet } from '@/api/userController.ts'

export const useLoginUserStore = defineStore('loginUser', () => {
  const loginUser = ref<API.LoginUserVO>({
    userName: '未登录',
  })

  /**
   * 远程获取用户登录信息
   */
  async function fetchLoginUser() {
    const response = await getLoginUserUsingGet()
    if (response.data.code === 0 && response.data.data) {
      loginUser.value = response.data.data
    }
    //测试用户代码
    // setTimeout(() => {
    //   loginUser.value = { userName: '测试用户', id: 1 }
    // }, 3000)
  }

  /**
   * 生成一个新用户
   * @param newLoginUser
   */
  function setLoginUser(newLoginUser: any) {
    loginUser.value = newLoginUser
  }

  return { loginUser, fetchLoginUser, setLoginUser }
})
