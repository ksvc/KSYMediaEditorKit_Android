# 金山云短视频编辑SDK KSYMediaEditorKit

关键名词解释：
- 视频录制：采集摄像头及麦克风音视频数据，通过预处理、编码、复用等过程最终生成一个本地的mp4文件  
- 视频预览编辑：对指定视频播放的同时添加音视频滤镜、水印同时提供处理后的音频和视频的预览 
- 视频合成：对原始视频加入滤镜、水印等特效并输出mp4文件
- KS3:金山云存储服务  
- SDK鉴权：取得SDK的使用权

## 阅读对象
本文档面向所有使用[金山云短视频SDK][KSYMediaEditorKit]的开发、测试人员等, 要求读者具有一定的Android编程开发经验，并且要求读者具备阅读[wiki][wiki]的习惯。

|![svod_1.png](https://raw.githubusercontent.com/wiki/ksvc/KSYMediaEditorKit_iOS/images/svod_1.png)|![svod_2.png](https://raw.githubusercontent.com/wiki/ksvc/KSYMediaEditorKit_iOS/images/svod_2.png)|![svod_3.png](https://raw.githubusercontent.com/wiki/ksvc/KSYMediaEditorKit_iOS/images/svod_3.png)|

|![svod_4.png](https://raw.githubusercontent.com/wiki/ksvc/KSYMediaEditorKit_iOS/images/svod_4.png)|![svod_5.png](https://raw.githubusercontent.com/wiki/ksvc/KSYMediaEditorKit_iOS/images/svod_5.png)|


## 1 功能介绍
[KSYMediaEditorKit][KSYMediaEditorKit]是一款由金山云提供的的可以快速集成的短视频编辑SDK，当前支持以下功能：

* [x] [短视频录制](https://github.com/ksvc/KSYMediaEditorKit_Android/wiki/Recorder_Config_Params)（支持[美颜](https://github.com/ksvc/KSYStreamer_Android/wiki/Video_Filter_Inner)[滤镜](https://github.com/ksvc/KSYStreamer_Android/wiki/style_filter)、[麦克风&配乐控制](https://github.com/ksvc/KSYMediaEditorKit_Android/wiki/audio_config)，[麦克风&配乐音量控制](https://github.com/ksvc/KSYMediaEditorKit_Android/wiki/audio_config)、[前置镜像](https://github.com/ksvc/KSYStreamer_Android/wiki/front_camera_mirror)、[动态贴纸](https://docs.ksyun.com/read/latest/142/_book/index.html)、[水印](https://github.com/ksvc/KSYStreamer_Android/wiki/WaterMark)）
* [x] 短视频文件导入，支持mp4/3gp/mov
* [x] 录制或导入视频预览[编辑](https://github.com/ksvc/KSYMediaEditorKit_Android/wiki/Edit_Confi_Params)
* [x] 编辑合成添加[滤镜](https://github.com/ksvc/KSYStreamer_Android/wiki/style_filter)
* [x] 编辑合成添加[水印](https://github.com/ksvc/KSYMediaEditorKit_Android/wiki/edit_watermark)
* [x] 编辑合成添加[背景音乐](https://github.com/ksvc/KSYMediaEditorKit_Android/wiki/audio_config)
* [x] 编辑合成[视频时长裁剪](https://github.com/ksvc/KSYMediaEditorKit_Android/wiki/video_range)
* [x] 编辑文件合成，支持H.264、H.265、GIF编码
* [x] 合成文件上传KS3
* [x] 上传后文件预览播放 
* [x] 录制添加[动态贴纸](https://docs.ksyun.com/read/latest/142/_book/index.html) 
* [x] 编辑添加[静态贴纸](https://github.com/ksvc/KSYMediaEditorKit_Android/wiki/static_sticker) 
* [x] 录制[断点续拍](https://github.com/ksvc/KSYMediaEditorKit_Android/wiki/clip_record)
* [x] 美颜、特效
* [x] 编辑添加[字幕](https://github.com/ksvc/KSYMediaEditorKit_Android/wiki/TextSticker)
* [x] 录制编辑[变声变调](https://github.com/ksvc/KSYStreamer_Android/wiki/Audio_Filter)

## 1.1 运行环境  
- 最低支持版本为Android 4.4 (API level 19)
- 支持的CPU架构：armv7, arm64, x86


## 1.2 关于费用
[KSYMediaEditorKit][KSYMediaEditorKit]短视频SDK是一款免费SDK，录制、编辑和播放功能都免费提供，可以用于商业集成和使用。

License说明请见[wiki][license]

### 1.2.1 鉴权
短视频SDK涉及两个鉴权，区别如下：
* SDK鉴权免费，是必需的
* KS3(金山云存储服务)鉴权涉及费用，可以选择不用

#### 1.2.1.1 SDK鉴权
使用[KSYMediaEditorKit短视频编辑SDK][KSYMediaEditorKit]前需要注册金山云帐号，SDK需要使用开发者帐号鉴权。请[在此注册][ksyun]开发者帐号。

SDK鉴权本身不需要付费。


为了开始开发用于SDK鉴权所需要的鉴权串，提供了服务器端鉴权需要的代码：

* 服务器鉴权代码--JAVA版本

https://github.com/ksvc/KSYMediaEditorKit_iOS/tree/master/server/java/auth

* 服务器鉴权代码--GO版本

https://github.com/ksvc/KSYMediaEditorKit_iOS/tree/master/server/python/auth


#### 1.2.1.2 KS3鉴权
使用[KSYMediaEditorKit短视频编辑SDK][KSYMediaEditorKit]将合成的短视频上传至[KS3][KS3](金山云存储服务)存储时，需要满足KS3的鉴权要求。

如果您的APP不使用[金山云的对象存储服务][KS3]或者使用其他家云存储提供的存储或者CDN服务，上传阶段置null即可。

如果使用[金山云对象存储][KS3]需要开通商务帐号（涉及付费业务），请直接联系金山云商务。

### 1.2.2 付费

[KSYMediaEditorKit][KSYMediaEditorKit]可以免费使用。涉及付费的包括：
* 动态贴纸（可以不集成，如果需要集成需要向第三方供应商付费）
* 云存储（可以不集成）
* 点播CDN（可以不集成）

涉及的云存储和CDN，具体费用请参考[金山云官网][ksyun]


## 2. SDK集成

### 2.1 系统框图

<img src="https://raw.githubusercontent.com/wiki/ksvc/KSYMediaEditorKit_Android/images/shortVideo.png" width = "708" height = "499.5" alt="图片名称" align=center />

### 2.2 集成说明
具体集成步骤请阅读[wiki][wiki]

## 3. demo试用
请见[版本下载说明](https://github.com/ksvc/KSYMediaEditorKit_Android/releases)。

## 4. 商务合作
Demo中的鉴权串等只能供Demo使用,正式上线需要申请金山云账号，请联系金山云商务。

## 5. 反馈与建议
### 5.1 反馈模板  

| 类型    | 描述|
| :---: | :---:| 
|SDK名称|KSYMediaEditorKit_android|
| SDK版本 | v1.1.0|
| 设备型号  | oppo r9s  |
| OS版本  | Android 6.0.1 |
| 问题描述  | 描述问题出现的现象  |
| 操作描述  | 描述经过如何操作出现上述问题                     |
| 额外附件   | 文本形式控制台log、crash报告、其他辅助信息（界面截屏或录像等） |

### 5.2 联系方式
- 主页：[金山云](http://www.ksyun.com/)
- 邮箱：<zengfanping@kingsoft.com>
- QQ讨论群：574179720
- Issues: <https://github.com/ksvc/KSYMediaEditorKit_Android/issues>

<a href="http://www.ksyun.com/"><img src="https://raw.githubusercontent.com/wiki/ksvc/KSYLive_Android/images/logo.png" border="0" alt="金山云计算" /></a>

[ksyun]:https://v.ksyun.com
[license]:https://github.com/ksvc/KSYMediaEditorKit_Android/wiki/license
[wiki]:https://github.com/ksvc/KSYMediaEditorKit_Android/wiki
[KSYMediaEditorKit]:https://github.com/ksvc/KSYMediaEditorKit_Android
[ks3]:https://www.ksyun.com/proservice/storage_service
