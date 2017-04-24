# KSYMediaEditor_Android
关键名词解释：
- 视频录制：采集摄像头及麦克风音视频数据，通过编码合成等过程最终生成一个本地的mp4文件  
- 视频预览编辑：对指定视频播放的同时添加滤镜、水印并预览显示添加后的效果  
- 视频合成：对指定视频加入滤镜、水印等特效并输出mp4文件
- KS3:金山云服务  
- SDK鉴权：取得SDK的使用权

## 1 功能介绍
短视频SDK支持功能：
* [x] 短视频录制
* [x] 短视频文件导入，支持mp4/3gp/mov
* [x] 录制或导入视频预览编辑
* [x] 编辑合成添加滤镜
* [x] 编辑合成添加水印
* [x] 编辑文件合成，支持Mediacodec、libx264、H.265编码
* [x] 合成文件上传KS3
* [x] 上传后文件预览播放 

## 1.1 运行环境  
- 最低支持版本为Android 4.4 (API level 19)
- 支持的cpu架构：armv7, arm64, x86


## 1.2 关于费用
短视频SDK是一款免费SDK，录制和编辑功能免费提供。

License说明请见[wiki][license]

### 1.2.1 鉴权
使用SDK合成功能前需要注册金山云帐号，SDK需要使用开发者帐号鉴权。请[在此注册][ksyun]开发者帐号。

### 1.2.2 付费
短视频SDK本身可以免费使用，但是涉及的云存储上传、在线播放等云服务需要收费，具体费用请参考[金山云官网][ksyun]

## 2. SDK集成

### 2.1 系统框图

<img src="https://raw.githubusercontent.com/wiki/ksvc/KSYMediaEditorKit_Android/images/shortVideo.png" width = "708" height = "499.5" alt="图片名称" align=center />

### 2.2 集成说明
具体集成步骤请阅读[wiki][wiki]

## 3. 商务合作
demo中鉴权只能Demo使用
正式上线需要申请金山云账号，请联系金山云商务。

## 4. 反馈与建议
- 主页：[金山云](http://www.ksyun.com/)
- 邮箱：<zengfanping@kingsoft.com>
- QQ讨论群：574179720
- Issues: <https://github.com/ksvc/KSYMediaEditorKit_Android/issues>

[ksyun]:https://v.ksyun.com
[license]:https://github.com/ksvc/KSYMediaEditorKit_Android/wiki/license
[wiki]:https://github.com/ksvc/KSYMediaEditorKit_Android/wiki











