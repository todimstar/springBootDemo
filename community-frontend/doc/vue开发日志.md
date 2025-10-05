2025-09-21  15:00
完成了vue的创建，并打通了前端到后端的第一次连接，用vite代理解决了跨域问题，前端可以通过axios访问后端的接口了。
20:40 后端也实现了CORS的config

23:14
炸肛，把原本的带模版vue删掉之后，仅靠ai无法重现vue，请看课再来

2025.09.22  11:43
666，原来是vsc里的ai说错了，不是RounterLink是router-link，大小写敏感。啊感觉gemini在vsc里降智了最近，期待gemini cli的发挥好点，不然真完蛋了。要是没有实习，考研也没戏，直接失业了。

2025.09.23 23:34
1.知道了vsc中vue需要在vue项目文件夹中打开vue插件才能运行补全
2.明天先跟coplit写完登录api联通，平时看视频，课上看vue文档配合电脑爽爽的

2025.09.25 12:25
需要理清auth.js中pinia跟App.vue的联通

2025.10.03 22:10
用StoreToRefs来保持响应式,解构出isAuthenticated和user
理解了computed的用法
在App.vue用useRoute来获取当前路由，判断是否为登录页
在App.vue中用v-if来判断是否显示欢迎信息
用localStorage测试清空登录信息，刷新后需要重新登录

2025.10.04 00:20