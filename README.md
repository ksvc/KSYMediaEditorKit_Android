# KSYMediaEditor_Android
关键名次解释：
- 视频录制：采集摄像头及麦克风音视频数据，通过编码合成最终生成一个本地的mp4文件  
- 视频预览编辑：对指定视频播放的同时添加滤镜、水印并时时显示添加后的效果  
- 视频合成：对指定视频加入滤镜、水印等特效并输出mp4文件
- KS3:金山云服务  
- SDK鉴权：取得SDK的使用权

## 1.0 功能介绍
短视频SDK基于金山Android[融合库SDK](https://github.com/ksvc/KSYLive_Android)    
短视频SDK支持功能：
* [x] 短视频SDK鉴权
* [x] 短视频录制
* [x] 短视频文件导入
* [x] 录制或导入视频预览编辑
* [x] 编辑合成添加滤镜
* [x] 编辑合成添加水印
* [x] 编辑文件合成
* [x] 合成文件上传KS3
* [x] 上传后文件预览播放 

## 1.1 运行环境  
- 最低支持版本为Android 4.0 (API level 15)

### 1.2 关于费用
短视频SDK是一款付费SDK，SDK的录制和编辑功能免费提供，但是使用SDK合成功能前需要进行SDK鉴权，具体收费方式，请联系金山云商务（商务电话）

### 1.3 集成说明 
App通过直接集成libksyssv.jar来使用短视频功能，但要完全使用SDK的功能，还需要依赖以下库：
- 金山云Android融合库SDK，推荐直接使用gradle方式，详细参考[融合库SDK](https://github.com/ksvc/KSYLive_Android)     
- KS3上传SDK，推荐使用gradle方式依赖
``` gradle
dependencies {
    compile 'com.ksyun.ks3:app:1.4.1'
}
```
- volley依赖，推荐使用gradle方式依赖
``` gradle
dependencies {
    compile 'com.android.volley:volley:1.0.0'
}
```
- 同步网络请求依赖，直接使用android-async-http-1.4.6.jar  

### 1.4 模块说明

### 1.5 关键类说明  








