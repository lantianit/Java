import {getBaseUrl, requestUtil} from "../../utils/requestUtil.js";
Page({
  data: {
      userRankList: [],
      baseUrl: ''
  },
  onLoad() {
      this.getRankList();
      const baseUrl = getBaseUrl();
      this.setData({
        baseUrl
      });
  },
  async getRankList() {
    const result = await requestUtil({
      url: '/user/rank',
    });
    console.log(result);
    this.setData({
      userRankList: result
    })
  }
});