12.13日，重启项目
1.现象：依照原本社区论坛项目修改黑马的jwt密钥和时长配置，后发现未知报错

原因：原来是黑马jwtTokenAdminInterceptor没有兜底异常检查，抛出非特定异常没有处理

查错过程:手动添加else后兜底查出问题是修改黑马jwt后根据原本jwt密钥生成的token无法解析

解决方案：修改为使用新的jwt密钥生成token后，成功通过验证

2.现象：controller、service、mapper层都配置了正确泛型和传参类型，类型中没有密码属性，但是最终查询员工数据返回时还是使用了entity型的原始带有密码属性的employee

原因：xml层手写了动态查询逻辑，查询时使用的指向还是旧版本的emplyee没有更新

解决方案：更新为具体对应VO即可



12.18，又是重启项目

1.复习了PUT、POST、PATCH，记录在语雀

黑马将启用禁用API设置为POST，但三者其实都能传递请求体

2.简单完成一个启用禁用小功能，成功

3.从黑马中学到了可以直接设置Mapper层一个动态update方法，这样多处的修改就可以只用一个xxxMapper.updata(xxx实体/DTO)

4.了解了黑马暴力隐藏密码方法，直接在service层serpassword("***")，但我们都觉得直接多个VO更优雅，而且之前已经凭直觉做了，虽然如果只有一个password的差异也不是不能这样暴力。

12.19,好不容易又挣扎着开始开发了

学到了在DTO里复写setter可以实现参数回正限制，设计了分区DTO用isAdmin区分查询的巧思正在实现中，ReView了一些古早代码，写了TODO期待修正

进度：写了Category分区的Contorller和Service，差Mapper没写xml动态PageQuery，

12.21，又拖一天

还是调试之前的Admin和user智能区分的那个接口，发现异常类之前才重构了一半，之后还要返工吧异常类升级完成

setPageSize复制时漏了一个判定变量没改，小bug

复习了Controller里@RequestBoby会使DTO走Boby传递，如果没有任何注解就走Params(url:8080?xx&xx)

分Admin和User接口，简单分了分页查询和根据ID获取分区详情，其他的不想升级先了，下个模块注意吧

版本管理

时间类

重写异常类：

