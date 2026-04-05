package com.app.douyin.pro.lib.media.model

sealed class AppError(val message: String, val code: Int) {
    object NetworkError : AppError("网络连接错误", 1001)
    object ServerError : AppError("服务器响应异常", 1002)
    object MediaExportError : AppError("视频导出失败", 2001)
    object CameraAccessError : AppError("无法访问摄像头", 3001)
    data class CustomError(val msg: String, val errorCode: Int = -1) : AppError(msg, errorCode)
    object UnknownError : AppError("未知错误", -1)
}
