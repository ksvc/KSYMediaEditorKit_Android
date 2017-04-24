# KSYMediaEditor_Android
关键名词解释：
- 视频录制：采集摄像头及麦克风音视频数据，通过编码合成等过程最终生成一个本地的mp4文件  
- 视频预览编辑：对指定视频播放的同时添加滤镜、水印并预览显示添加后的效果  
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
- 最低支持版本为Android 4.4 (API level 19)
- 支持的cpu架构：armv7, arm64, x86

## 1.2 关于费用
短视频SDK是一款付费SDK，SDK的录制和编辑功能免费提供，但是使用SDK合成功能前需要进行SDK鉴权，具体收费方式，请联系金山云商务。
License说明请见[wiki](https://github.com/ksvc/KSYMediaEditorKit_Android/wiki/license)

## 1.3 集成说明 
App通过直接集成libksyssv.jar来使用短视频功能，但要完全使用SDK的功能，还需要依赖以下库：

- 金山云Android融合库SDK，Demo中app/libs目录下面包含了融合库的jar和so，详细介绍参考[融合库SDK](https://github.com/ksvc/KSYLive_Android)    
> 当前版本暂时不支持使用jcenter来依赖融合库

- KS3上传SDK，Demo中app/libs目录下面的ks3-android-sdk_1.4.1.jar即为ks3的jar，详细参考[KS3Client](https://docs.ksyun.com/read/latest/65/_book/sdk/android.html)
- volley及org.apache.http.Header依赖，推荐使用gradle方式依赖
``` gradle
dependencies {
    compile 'com.android.volley:volley:1.0.0'
    compile 'org.apache.httpcomponents:httpcore:4.4.2'
}
```
- 同步网络请求依赖，直接使用android-async-http-1.4.6.jar  

## 1.4 架构说明
<img src="https://raw.githubusercontent.com/wiki/ksvc/KSYMediaEditorKit_Android/images/shortVideo.png" width = "708" height = "499.5" alt="图片名称" align=center />

## 1.5 关键类说明  
<img src="https://raw.githubusercontent.com/wiki/ksvc/KSYMediaEditorKit_Android/images/shortvideo_class.png" width = "750" height = "403" alt="图片名称" align=center />  

## 1.6 模块及流程说明  
<img src="https://raw.githubusercontent.com/wiki/ksvc/KSYMediaEditorKit_Android/images/shortvideo_moreinfo.png" width = "742" height = "1451" alt="图片名称" align=center />  

## 1.7 功能点详细说明
- SDK鉴权（TODO）
- [状态和错误回调](https://github.com/ksvc/KSYMediaEditorKit_Android/wiki/Info&Error_Listener)
- KS3文件上传服务（TODO）


## 1.8 商务合作
demo中鉴权只能Demo使用
正式上线需要申请金山云账号，请联系金山云商务。

## 1.9 反馈与建议
- 主页：[金山云](http://www.ksyun.com/)
- 邮箱：<zengfanping@kingsoft.com>
- QQ讨论群：574179720
- Issues: <https://github.com/ksvc/KSYMediaEditorKit_Android/issues>













