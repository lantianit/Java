import { requestUtil, getBaseUrl } from '../../utils/requestUtil.js';

Page({
  data: {
    baseUrl: '',
    user: null,
    favoriteHistory: [] // 存储视频收藏记录
  },

  onLoad() {
    this.checkLoginAndLoadData();
  },

  // 提取公共逻辑到该函数，用于判断登录状态并执行相应操作
  checkLoginAndLoadData() {
    const app = getApp();
    const baseUrl = getBaseUrl();
    const loginStatus = app.globalData.isLoggedIn;
    this.setData({ baseUrl });

    if (!loginStatus) {
      console.log("用户未登录，跳转到登录页面");
      wx.redirectTo({ url: '/pages/login/login' });
    } else {
      this.loadUserData();
    }
  },

  async loadUserData() {
    try {
      await this.getUserByToken();
      const userId = this.data.user ? this.data.user.userId : null;
      if (userId) {
        await this.getFavoriteHistory(userId);
      }
    } catch (error) {
      console.error("加载用户数据出错:", error);
    }
  },

  async getUserByToken() {
    try {
      const app = getApp();
      const token = app.globalData.token;
      const result = await requestUtil({
        url: '/user/getUserByToken',
        method: "GET",
        data: { token }
      });
      this.setData({ user: result.message });
    } catch (error) {
      console.error("获取用户信息出错:", error);
    }
  },

  // 获取视频收藏记录
  async getFavoriteHistory(userId) {
    try {
      const result = await requestUtil({
        url: '/video/interaction/getFavoritedVideos',
        method: "GET",
        data: { userId }
      });
      console.log("获取视频收藏记录原始结果:", result);

      if (result) {
        const formattedHistory = this.formatHistory(result);
        this.setData({ favoriteHistory: formattedHistory });
        console.log("格式化后的favoriteHistory:", this.data.favoriteHistory);
      } else {
        console.log("获取视频收藏记录失败");
      }
    } catch (error) {
      console.error("获取视频收藏记录出错:", error);
    }
  },

  // 格式化历史记录
  formatHistory(history) {
    return history.map(item => {
      const date = new Date(item.favoritedTime);
      const year = date.getFullYear();
      const month = ('0' + (date.getMonth() + 1)).slice(-2);
      const day = ('0' + date.getDate()).slice(-2);
      const hour = ('0' + date.getHours()).slice(-2);
      const minute = ('0' + date.getMinutes()).slice(-2);
      const second = ('0' + date.getSeconds()).slice(-2);

      return {
        ...item,
        favoritedTime: `${year}年${month}月${day}日${hour}时${minute}分${second}秒`
      };
    });
  }
}); 