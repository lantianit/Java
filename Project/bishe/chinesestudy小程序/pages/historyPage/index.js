import { requestUtil, getBaseUrl } from '../../utils/requestUtil.js';

Page({
  data: {
    baseUrl: '',
    user: null,
    videoHistory: [] // 存储视频观看历史记录
  },

  onLoad() {
    this.checkLoginAndLoadData();
  },

  // 提取公共逻辑到该函数，用于判断登录状态并执行相应操作
  checkLoginAndLoadData() {
    const app = getApp();
    const baseUrl = getBaseUrl();
    const loginStatus = app.globalData.isLoggedIn;
    this.setData({
      baseUrl,
    });
    if (!loginStatus) {
      console.log("准备跳转");
      wx.redirectTo({
        url: '/pages/login/login'
      });
    } else {
      this.loadUserData();
    }
  },

  async loadUserData() {
    // 先获取用户信息
    await this.getUserByToken();
    // 获取视频观看历史记录
    const userId = this.data.user ? this.data.user.userId : null;
    if (userId) {
      await this.getWatchHistory(userId);
    }
  },

  async getUserByToken() {
    const app = getApp();
    const token = app.globalData.token;
    const result = await requestUtil({
      url: '/user/getUserByToken',
      method: "GET",
      data: { token }
    });
    this.setData({
      user: result.message
    });
  },

  // 获取视频观看历史记录
  async getWatchHistory(userId) {
    try {
      const result = await requestUtil({
        url: '/video/getWatchHistory',
        method: "GET",
        data: { userId }
      });
      console.log("获取视频历史记录原始结果:", result);
      
      if (result) {
        // 对数据进行预处理，格式化时间
        const formattedHistory = result.map(item => {
          const date = new Date(item.startTime);
          const year = date.getFullYear();
          const month = ('0' + (date.getMonth() + 1)).slice(-2);
          const day = ('0' + date.getDate()).slice(-2);
          const hour = ('0' + date.getHours()).slice(-2);
          const minute = ('0' + date.getMinutes()).slice(-2);
          const second = ('0' + date.getSeconds()).slice(-2);
          
          return {
            ...item,
            startTime: `${year}年${month}月${day}日${hour}时${minute}分${second}秒`
          };
        });

        this.setData({
          videoHistory: formattedHistory
        });
        console.log("格式化后的videoHistory:", this.data.videoHistory);
      } else {
        console.log("获取视频观看历史记录失败");
      }
    } catch (error) {
      console.error("获取视频历史记录出错:", error);
    }
  }
}); 