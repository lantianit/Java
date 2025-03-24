import { requestUtil, getBaseUrl } from '../../utils/requestUtil.js';

Page({
  data: {
    baseUrl: '',
    user: null,
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

  async onLoad() {
    this.checkLoginAndLoadData();
  },

  async onShow() {
    this.checkLoginAndLoadData();
  },

  // 格式化时间
  formatDate(dateString) {
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}`;
  },

  // 处理退出账号的函数
  handleLogout() {
    const app = getApp();
    // 将全局登录状态设置为false，表示已退出登录
    app.globalData.isLoggedIn = false;
    // 跳转到登录页面，可根据实际需求调整跳转方式和页面路径
    wx.redirectTo({
      url: '/pages/login/login'
    });
  },

  // 新增方法：处理跳转到历史记录页面
  navigateToHistory() {
    wx.navigateTo({
      url: '/pages/historyPage/index' // 假设新的页面路径为 /pages/historyPage/index
    });
  },

  // 新增方法：处理跳转到点赞记录页面
  navigateToLikeHistory() {
    wx.navigateTo({
      url: '/pages/likeHistoryPage/index' // 假设新的页面路径为 /pages/likeHistoryPage/index
    });
  },

  // 新增方法：处理跳转到收藏记录页面
  navigateToFavoriteHistory() {
    wx.navigateTo({
      url: '/pages/favoriteHistoryPage/index' // 假设新的页面路径为 /pages/favoriteHistoryPage/index
    });
  },
});