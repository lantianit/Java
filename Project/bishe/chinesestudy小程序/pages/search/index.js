// pages/search/index.js
import { requestUtil } from '../../utils/requestUtil.js';

Page({

  /**
   * 页面的初始数据
   */
  data: {
    searchQuery: '',
    searchResults: [], // 存储视频搜索结果
    characterResults: [] // 存储汉字搜索结果
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {

  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady() {

  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {

  },

  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide() {

  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload() {

  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh() {

  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom() {

  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage() {

  },

  // 处理搜索输入框内容变化
  onSearchInput(e) {
    this.setData({
      searchQuery: e.detail.value
    });
  },

  // 搜索视频
  async searchVideos() {
    const title = this.data.searchQuery.trim();
    if (!title) {
      wx.showToast({
        title: '请输入搜索关键词',
        icon: 'none'
      });
      return;
    }

    try {
      // 搜索视频
      const videoResult = await requestUtil({
        url: '/video/searchByTitle',
        method: 'GET',
        data: { title }
      });

      // 搜索汉字
      const characterResult = await requestUtil({
        url: '/video/searchCharacter',
        method: 'GET',
        data: { character: title }
      });

      console.log("视频搜索结果:", videoResult);
      console.log("汉字搜索结果:", characterResult);

      this.setData({
        searchResults: videoResult || [],
        characterResults: characterResult || []
      });

      if (!videoResult && !characterResult) {
        wx.showToast({
          title: '未找到相关内容',
          icon: 'none'
        });
      }
    } catch (error) {
      console.error("搜索出错:", error);
      wx.showToast({
        title: '搜索出现异常，请稍后再试',
        icon: 'none'
      });
    }
  }
})