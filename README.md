# floating_log_view

## 使用方法：

### 1.在工程根目录build.gradle中添加maven仓库

allprojects {

    repositories {
    
      maven { 
      
        url 'https://jitpack.io' 
        
      }
      
     }
     
 }
 

### 2.在module目录的build.gradle添加依赖
implementation 'com.github.yangqiangH:floating_log_view:1.0.2'

### 3.初始化和显示FloatingLogView
LogViewManager.initLogView(context)

LogViewManager.attachNow(activity)

### 4.打印日志
LogViewManager.printLog("report", "something")
     
### 5.销毁FloatingLogView
LogViewManager.destroyView(context)



