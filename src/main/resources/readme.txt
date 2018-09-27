# 分布式任务使用

1.注解使用

>编写类集成IntellifEasyJob并在上面添加@IntellifSimpleJob(cron="")

>在启动类中添加IntellifSimpleJobUtils.startJob("") 这里填写需要扫描的包名


注意：在使用热部署插件会导致无法注入。主要是类加载器不同导致的