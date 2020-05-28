# floating_log_view

## 一、简介
该组件用于在开发过程中实时查看日志输出，方便调试。

App级的浮窗，无须申请任何权限，兼容所有系统版本。


## 二、使用方法

#### 1、在工程根目录build.gradle中添加maven仓库
```
implementation 'com.github.yangqiangH:floating_log_view:1.0.2'
```

##### 2.在module目录的build.gradle添加依赖
```
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

##### 3、初始化和显示FloatingLogView
```
LogViewManager.initLogView(context)
LogViewManager.attachNow(activity)
```

##### 4、打印日志
```
LogViewManager.printLog("report", "something")
```

##### 5、销毁FloatingLogView
```
LogViewManager.destroyView(context)
```
## 三、感谢

```
该组件基于如下开源库：https://github.com/leotyndale/EnFloatingView
感谢EnFloatingView的作者leotyndale
```
