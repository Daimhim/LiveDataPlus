# 版本更新说明
##### 1.0.1
> 1. 初始化项目
# LiveData +
##### 项目简介
> LiveDataPlus 分解LiveData，并定制一些特殊的应用场景
##### 接入指南
###### 1. 依赖

```
implementation 'org.daimhim.livedataplus:livedataplus:1.0.1'
```
###### 2. 调用

```
LiveDataPlus 提供无状态 监听
LifecycleLiveData 类似原生 Lifecycle 状态监听
LazyLifecycleLiveData 类似原生 Lifecycle 状态监听，但屏蔽了自动恢复
```

###### 3. 扩展

可以继承LiveDataPlus，并实现抽象类ObserverWrapper，来定制自己的状态监听