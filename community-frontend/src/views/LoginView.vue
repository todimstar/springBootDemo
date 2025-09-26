<template>
  <div class="login-view">
    <div>
      <h1>欢迎回来</h1>
      <p>登录账号，开始新的旅程</p>
    </div>
    <div>
      <label for="usernameOrEmailInput">账号名/邮箱</label>
      <input
        ref="usernameOrEmailInput"
        v-model="usernameOrEmail"
        id="usernameOrEmailInput"
        type="text"
        placeholder="请输入账号名/邮箱"
      />
    </div>
    <div>
      <div class="password-header">
        <label for="passwordInput">密码</label>
        <a href="#">
          <small>忘记密码？</small>
        </a>
      </div>
      <input
        ref="passwordInput"
        v-model="password"
        id="passwordInput"
        type="password"
        placeholder="请输入密码"
      />
    </div>
    <div>
      <button @click="handleLogin" id="loginButton" type="submit" data-loading-text="正在提交...">
        登录
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth.js'

const usernameOrEmail = ref('')
const password = ref('')

const usernameOrEmailInput = ref(null)
const passwordInput = ref(null)

//获取userAuthStore实例
const authStore = useAuthStore()

async function handleLogin() {
  console.log('handleLogin到的数据:', {
    usernameOrEmail: usernameOrEmail.value,
    password: password.value,
  })
  // 之后会写API调用

  //基础校验
  if (!usernameOrEmail.value) {
    alert('请填写账号名/邮箱')
    usernameOrEmailInput.value.focus()
    return
  }
  if (!password.value) {
    alert('请填写密码')
    passwordInput.value.focus()
    return
  }

  try {
    await authStore.login(usernameOrEmail.value, password.value)
  } catch (error) {
    console.error('请求出错', error)
    const errorMessage = error.response?.data?.message || '服务器开小差了，稍后重试' //开小差就是返回结构异常或者空白
    alert('登录出错：' + errorMessage)
  }
}
</script>

<style scoped>
/* 将样式应用到整个组件的根元素 */
.login-view {
  max-width: 420px; /* 稍微加宽一点 */
  margin: 50px auto;
  padding: 2.5rem; /* 增加内边距，让内容有更多呼吸空间 */
  background: #ffffff;
  border-radius: 16px; /* 更大的圆角 */
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1); /* 更柔和、更弥散的阴影 */
  display: flex; /* 使用 Flexbox 布局 */
  flex-direction: column; /* 设置为主轴为垂直方向 */
  gap: 1.5rem; /* 设置 flex 子项之间的间距，替代 margin-bottom */
}

/* 优化标题和描述文本 */
.login-view h1 {
  font-size: 2rem;
  font-weight: 700;
  color: #333;
  text-align: center;
  margin-bottom: 0.5rem;
}

.login-view p {
  text-align: center;
  color: #666;
  margin-bottom: 1rem;
}

/* 优化表单组的 div 容器 */
.login-view > div {
  margin-bottom: 0; /* 由于使用了 gap，不再需要 margin-bottom */
}

.login-view label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600; /* 字体稍粗 */
  color: #555;
}

/* 优化输入框样式 */
.login-view input {
  width: 100%;
  padding: 0.8rem 1rem;
  border: 1px solid #ddd;
  border-radius: 8px; /* 圆角与容器协调 */
  font-size: 1rem;
  background-color: #f9f9f9;
  /*
    transition: all 0.3s ease;
    解释: transition 是一个 CSS 属性，用于创建平滑的过渡效果。
    - all: 表示所有可动画的属性（如 border-color, box-shadow）都将应用过渡效果。
    - 0.3s: 表示过渡的持续时间为 0.3 秒。
    - ease: 是一种缓动函数（easing function），表示过渡效果开始慢，中间快，结束时又变慢，模仿真实世界的物理效果。
  */
  transition: all 0.3s ease;
}

/*
  :focus 伪类 (pseudo-class)
  解释: 当用户点击或通过 Tab 键导航到某个元素（如 <input>）时，该元素就处于 :focus 状态。
  我们可以为这个状态定义特定的样式，以提供视觉反馈。
*/
.login-view input:focus {
  outline: none; /* 移除浏览器默认的蓝色或橙色轮廓 */
  border-color: hsla(160, 100%, 37%, 1); /* 将边框颜色变为主题色 */
  box-shadow: 0 0 0 3px hsla(160, 100%, 37%, 0.2); /* 添加一个柔和的发光效果 */
  background-color: #fff;
}

/* 密码输入框头部的布局 */
.password-header {
  display: flex;
  justify-content: space-between; /* 两端对齐 */
  align-items: center; /* 垂直居中 */
}

.password-header a {
  color: hsla(160, 100%, 37%, 1);
  text-decoration: none;
  font-size: 0.9rem;
}

.password-header a:hover {
  text-decoration: underline;
}

/* 优化按钮样式 */
.login-view button {
  width: 100%;
  padding: 0.8rem 1rem;
  border: none;
  border-radius: 8px;
  background-color: hsla(160, 100%, 37%, 1);
  color: white;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 0.5rem; /* 给按钮顶部增加一点空间 */
}

/*
  :hover 伪类
  解释: 当鼠标指针悬停在元素上时，该元素处于 :hover 状态。
*/
.login-view button:hover {
  background-color: hsla(160, 100%, 30%, 1); /* 悬停时颜色变深 */
  transform: translateY(-2px); /* 向上移动2像素，产生轻微的浮动感 */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/*
  :active 伪类
  解释: 当用户点击元素（鼠标按下但还未松开）时，该元素处于 :active 状态。
*/
.login-view button:active {
  transform: translateY(0); /* 按下时恢复原位 */
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1); /* 阴影变小，产生被按下的感觉 */
}
</style>
