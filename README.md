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
短视频SDK是一款付费SDK，SDK的录制和编辑功能免费提供，但是使用SDK合成功能前需要进行SDK鉴权，具体收费方式，请联系金山云商务  

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
### 1.6.1 关键功能集成流程： 
1. SDK鉴权,需要向AppServer获取鉴权信息，并通过SDK的AuthInfoManager的checkAuth接口来完成。
- 示例代码：参考ShortVideoActivity的onAuthClick函数
- 流程说明：参考1.6.2中模块`1:SDK鉴权` 
- wiki:(TODO) 
> 鉴权在合成前必须完成，否则影响合成

2. 准备待编辑文件，提供录制和本地文件导入两种方式来生成待编辑文件  
- 示例代码：
录制文件，提供KSYRecordKit来完成录制功能
* [x] 配置录制参数：使用ShortVideoConfigDialog示例了录制参数的配置，参考ShortVideoActivity的onRecordClick函数 
* [x] 录制预览显示：调用KSYRecordKit的startCameraPreview启动录制预览显示，参考RecordActiviyt的startCameraPreviewWithPermCheck()
* [x] 录制开始：调用KSYRecordKit的startRecord(string)开始了录制，参考RecordAcitvity的startRecord()函数
* [x] 录制结束：调用KSYRecordKit的stopRecord()停止录制，参考RecordAcitvity的stopRecord()函数  
>SDK内部当前没有控制录制时长，建议在3s～5分钟之间，Demo中控制的最短为5s 

文件导入，Demo中提供了文件导入的示例代码，参考FileImportActivity（此部分不涉及与SDK交互）
- 流程说明：参考1.6.2 中模块`2:短视频录制和导入` 

3. 输入编辑文件路径，启动EditActivity对第二步中生成的文件进行编辑，SDK提供KSYEditKit类来完成编辑功能    
- 示例代码：
* [x] 启动编辑：调用KSYEditKit的函数startEditPreview(string)来启动编辑预览，参考EditActivity中startEditPreview()函数
>只提供mp4、3gpp、mov三种文件的编辑，该函数会对文件格式进行check，不符合会抛出IllegalArgumentException异常
* [x] 添加滤镜：只提供GPU滤镜的添加，参考EditAcitvity中initBeautyUI()函数，与推流库的滤镜设置方式基本相同，参考[内置美颜](https://github.com/ksvc/KSYStreamer_Android/wiki/Video_Filter_Inner)和[自定义GPU滤镜](https://github.com/ksvc/KSYStreamer_Android/wiki/Video_Filter)
* [x] 添加水印：参考EditActivity中onWaterMarkLogoClick()函数，只提供图片水印的添加，与推流库的图片水印基本相同，参考[水印](https://github.com/ksvc/KSYStreamer_Android/wiki/WaterMark)
- 流程说明：参考1.6.2 中模块`3:短视频预览编辑合成`中的1～3步骤

4. 编辑结束，开始文件合成
- 示例代码：
* [x] 配置合成参数：使用ShortVideoConfigDialog示例了录制参数的配置，当前合成提供帧率、encodeType、videobitrate，后续会持续完善
* [x] 开始合成：调用KSYEditKit的函数startCompose(string)来开始合成，参考EditActivity的onNextClick(函数)，合成过程的信息通过OnInfoListener和OnErrorListener来提供，消息通知参考mOnErrorListener和mOnInfoListener，具体说明，请移步[状态和错误回调](https://github.com/ksvc/KSYMediaEditorKit_Android/wiki/Info&Error_Listener)
>startCompose中会触发SDK鉴权检查，鉴权失败返回SHORTVIDEO_ERROR_SDK_AUTHFAILED消息，合成无法继续  
>调用KSYEditKit的getProgress()可以获取到合成进度，App需要自己启动Time来更新进度显示，参考ComposeAlertDialog中的composeStarted()函数
- 流程说明：参考1.6.2 中模块`3:短视频预览编辑合成`中的4～8步骤 

5. 文件合成结束，开始上传文件 
- 示例代码：
* [x] 触发上传：接收到回调合成结束SHORTVIDEO_COMPOSE_FINISHED后，在回调接口中返回上传信息KS3ClientWrap.KS3UploadInfo，即可触发上传，参考mOnInfoListener中相关处理  
* [x] 上传鉴权：接收到回调SHORTVIDEO_GET_KS3AUTH后，需要向AppServer请求KS3鉴权Token，Token获取代码参考KS3TokenTask，在回调中返回Token信息，KS3Client会检查token是否正确，正确继续开始文件上传，错误，文件上传失败，文件上传回调参考EditAcitvity中，mPutObjectResponseHandler  
> 通过PutObjectResponseHandler的回调onTaskProgress(double progress)可以获取到上传进度
- 流程说明：参考1.6.2 中模块`4:短视频上传` 

6. 上传后文件预览播放 
- 示例代码
* [x] 获取文件地址：当接收到PutObjectResponseHandler的回调onTaskSuccess后，说明上传成功，可以开始获取上传后的文件播放地址，Demo中提供HttpRequestTask类来示例http请求，地址获取参考ComposeAlertDialog中的uploadFinished()函数  
* [x] 开始预览播放：文件获取成功后，可以使用播放器对其进行播放，Demo中示例参考ComposeAlertDialog中的startPreview() 
>此部分不涉及与SDK交互  
- 流程说明：参考1.6.2 中模块`5:上传后文件预览播放` 


### 1.6.2 模块及流程图
不要被以下流程吓到哦，仔细阅读，对您集成有事半功倍的作用 
<img src="https://raw.githubusercontent.com/wiki/ksvc/KSYMediaEditorKit_Android/images/shortvideo_moreinfo.png" width = "742" height = "1451" alt="图片名称" align=center />  

## 1.7 功能点详细说明
- SDK鉴权（TODO）
- [状态和错误回调](https://github.com/ksvc/KSYMediaEditorKit_Android/wiki/Info&Error_Listener)
- [KS3Client](https://docs.ksyun.com/read/latest/65/_book/sdk/android.html)

## 1.8 商务合作
demo中鉴权只能Demo使用
正式上线需要申请金山云账号，请联系金山云商务。

## 1.9 反馈与建议
- 主页：[金山云](http://www.ksyun.com/)
- 邮箱：<zengfanping@kingsoft.com>
- QQ讨论群：574179720
- Issues: <https://github.com/ksvc/KSYMediaEditorKit_Android/issues>













