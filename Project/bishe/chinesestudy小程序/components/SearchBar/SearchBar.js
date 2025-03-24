// components/SearchBar/SearchBar.js
import { requestUtil } from '../../utils/requestUtil.js';

Component({

  /**
   * 组件的属性列表
   */
  properties: {

  },

  /**
   * 组件的初始数据
   */
  data: {
    searchQuery: '',
    searchResults: [] // 存储搜索结果
  },

  /**
   * 组件的方法列表
   */
  methods: {
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
        const result = await requestUtil({
          url: '/video/searchByTitle',
          method: 'GET',
          data: { title }
        });
        console.log("搜索结果:", result);

        if (result) {
          this.setData({
            searchResults: result
          });
          // 触发自定义事件，将结果传递给父组件
          this.triggerEvent('searchComplete', { results: result });
        } else {
          wx.showToast({
            title: '未找到相关视频',
            icon: 'none'
          });
        }
      } catch (error) {
        console.error("搜索视频出错:", error);
        wx.showToast({
          title: '搜索出现异常，请稍后再试',
          icon: 'none'
        });
      }
    }
  }
})