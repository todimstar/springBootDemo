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

版本管理✅

12.22 10:25

时间类：沿用旧异常引用，兼容性修改时间类，了解了ObjectMapper，手写了一遍，并通过WebMvcConfigurer注入了一遍，最后验证了确实可以用@Compent等直接覆盖注入实现同样效果，除非必须配置优先级的再去WebMvcConfigurer里配置顺序注入

12.23 15:09

重写异常类：异常类中的时间诡异，貌似配置了JsonFormat

12.24 16:14

重构异常类：移动Result，删除旧GlobalExceptionHandler,测试完成旧可用，准备菜刀注解和旧异常引用删除并行

12.25 旧异常已删除，菜刀注解中，swager2升级为Openapi3

菜刀注解：再Controller和VO DTO POJO等地加上

12.27 20:46

springDoc内核版本需要于springboot的spring框架版本匹配

本项目由于使用较新的3.5.5springboot，导致spring框架为2.7.0+，需要更新SpringDoc为最新版不是稳定版

| Spring Boot 版本  | Spring Framework | 推荐 SpringDoc | 推荐 Knife4j                             | 备注                |
| ----------------- | ---------------- | -------------- | ---------------------------------------- | ------------------- |
| **2.x (老项目)**  | 5.x              | 1.7.x          | 2.0.9                                    | 还在用 javax 包名   |
| **3.0.x ~ 3.1.x** | 6.0.x            | 2.0.x ~ 2.1.x  | 4.1.0 ~ 4.3.0                            | 开始用 jakarta 包名 |
| **3.2.x ~ 3.3.x** | 6.1.x            | 2.2.x ~ 2.5.x  | 4.4.0 ~ 4.5.0（默认使用springDoc 2.3.0） | 目前的主流稳定版    |
| **3.4.x ~ 3.5.x** | **6.2.x**        | **2.7.0+**     | **4.5.0 + 手动升级SpringDoc**为2.7.0     | **你现在的状态**    |

结论：**因你用的 Spring Boot 版本太新（超前），导致 Knife4j 内置的 SpringDoc 跟不上的时候，所以需要像今天这样手动打补丁**、

12.28 23:58

对于创建帖子添加了分类id为必填项，并在Post MVC流程中加入了分区的校验和数据库录入

补充了获取当前用户下的帖子分页查询

12.29 23:58

补全了pagePostsByUserId，用user/{userId}，补齐用户getUserById，给Service层使用，增强用户登录校验，对封禁用户遣返

>
> 5.未来：准备使用pageable制作post遗漏的接口-pagePostsByUserId，之后可能还需要用同样的方法升级PagePosts这个公开接口。可能得在了解一下pagehelper的排序功能，或者直接按照黄金时代做着先，然后接着测试，测试完去到下个模块文件上传模块

接着接口测试然后下个模块

封存技术分支1.分页查询的多种条件查询，比如倒序正序，按点赞量正序等。

技术分支2.角色状态在各功能的鉴权，帖子状态更新和鉴权

> [!NOTE]
>
> 哎呀不对，所以有关用户的都严验证一下用户是否被封禁呀，哎呀又不对，应该只封禁用户的登录和创建帖子和评论，用户的帖子该存在存在，评论该存在存在？，管理员没有被封禁的业务情况，除非你改了数据库

> [!TIP]
>
> 布豪，用户权限和帖子展示控制先放一边，能正常展示和删除就行，那些加花的放在最后一周管理员开发再开发。到时候可以看  [封存的技术分支.md](C:\Ep\Code\Java\springBootDemo\background_details_doc\封存的技术分支.md)

12.30 23:05

复习了一下Mybatis语法，我感觉就4个@，对应增删改查

23:23

往post表里添加了summary，

对于帖子查询：

场景一：在首页，分页查询帖子（getPostsByPage - 早期型，后需要在Post添加一个简介resume和categoryName用于展示✅，~~并升级VO~~）和标题（GetAllTitle - 需要升级为分页）并点击查看详情（用ById）

 --- *TODO:先跑上线先，之后可以优化为标题和简介分页查询一个接口单个查询一个接口，还可能要条件查询，做首页优化用，在v2.0时开始*

加简介summary(300字)和categoryName要改数据库和createPost，~~改Mapper用Left塞进VO~~

> [!WARNING]
>
> 将categoryName放在查询时跟category表LEFT查询，会导致查询速度慢，建议放在post中，回归全post场景；即创造post和更新id时去category查一次更新名字，之后更新频率少于查询频率，可以用post中字段而不多次联表。故postVO没啥用，createPostDTO还可以



场景二：在浏览页，ById查询到帖子内容和标题和分区和点赞数等信息，排除简介和封面图(其实字也不多，需要VO时再说吧这个改不改VO来传都无所谓的╮(╯_╰)╭)

场景三：被举报的帖子被管理员查询，照样按照首页的展示形式(标题加简介，点击后展示内容;写到举报时再根据举报方式决定这里查询用一个列表记录所有id单个查询，还是post又加一个状态去分页查询，哦想起来了举报有表的，对照着表上去单个查询好了) - 故本场景也不需要修改和新增post功能

完成String字数校验和post表升级，测试post创建和修改即可，✅

评论功能crud等待，也许还要加一个评论数量给post去展示，那感觉还是要一个VO？毕竟点赞数这些好像会实时更新诶，要是每查询一次就多表联合查找不就得post表联合like、categories、收藏、评论表？不过claude在帮我设计数据库表时又将这么多的属性放在post表里，结果最后因为要实时更新其实也不能加速查询速度？那放在post表里有什么用？为什么要怎么设计？是不是设计得不好？还有我这样用left联合查询多表是这种场景的最优解吗？根据现代开发规范和我该学习到的知识，目前的数据库表设计与postMVC三层因为post表中都包含了必要字段所以几乎没有用到VO，这样做如何评价？

1.关于post表冗余字段，点赞数这些怎么不会实时更新，你在点赞时存到点赞表还一并存到post表就好了呀，并发太高就用redis拦着定时更新就好。

2.使用冗余字段加事务同步更新计数字段

3.更新VO,summaryVO(展示于首页，title,summary,coverurl,like_count,xxx_count,不带content)和DetailVO(除了summaryVO还要加作者信息，还要当前用户是否点赞，收藏、是否关注作者等信息)，

4.哎，想只专注于提升技术栈呀，那先找要学的技术栈项目，搞个demo然后融进来，

5.想把业务实现和技术点分开呀，业务参考ai问答和别人项目，技术点参考别人视频demo

6.**实现两个VO**和对应接口查询--数据库语句 - 需要leftJoin或者

7.数据库语法+无视风险直接文件上传接口开发

> [pom里的5种scope](learingKnowledge.md##26.1.3 22:22 pom里的5种scope)
>
> 原本的BeanUntils.copyProperties浅拷贝且用反射性能低，升级为mapstuct的转换器@mapper(componentModel = "spring")用spring代理实现可注入/@mapper() -> INSTANCE单例调用

8.TODO:哎呀，这个Post模块有点麻烦的，还有要区分作者和普通用户，作者要多返回不同状态的帖子，普通用户只返回发布的帖子，对于Page和getById都需要多个判断在Service

2026.1.4

更新了两个VO返回值，getById、createPost返回DetailVO，PageByUserId、pagePostSummary列表升级为SummaryVO

删除帖子有分用户软删除和管理员硬删除

获取帖子都要排除status为4的，对于Mapper的status都在Service当参数传进去好了，这样就好状态控制

正在测试以上四个接口，并加了一个管理员改状态接口，不小心又实现一个PageByUserId三状态过滤查询，但是建议单个帖子在Service层过滤，列表查询才在数据库过滤。

2026.1.5

四个接口和新功能：三状态过滤、软硬删除、单帖和列表过滤都已完成并测试通过，进入文件上传开发

2.新学到一个游标分页（首页无限瀑布流）和反向游标分页（聊天记录获取）

3.通过Aspect注解配置类加配置文件xml实现AOP切面慢查询日志

4.直接开始文件上传功能

2026.1.9

redis引入：1.依赖；2.配置数据源yml；3.Commen配置类以序列化输入输出字符编码；4.使用



> 那么会出现用户最后一秒提交验证码然后redis里验证在springboot里失效导致最终验证失败的情况吗？不过这种场景应该也不影响吧。又不是秒杀系统。不过引入redis时听说redis更新中已经更新了轻型消息队列？我们如果不引用MQ或者kafuka这些大型消息队列用得上redis的吗？还是springboot也自带轻型消息队列？毕竟之后可能会做通知系统消息系统啥的，虽然只是可能，时间快不够找实习了。不知道这个项目要做到什么程度才能开始找实习面试