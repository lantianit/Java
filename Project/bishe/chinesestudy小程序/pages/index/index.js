import { requestUtil,getBaseUrl } from '../../utils/requestUtil.js';
import regeneratorRuntime from '../../lib/runtime/runtime';
Page({
  data: {
    swiperList: [],  // 定义swiperList数组
    baseUrl:'',
    bigTypeList:[],
    bigTypeList_row1:[],
    bigTypeList_row2:[],
    recommendList:[]
  },
  onLoad: function (options) {
    this.setData({
      baseUrl: getBaseUrl()
    });
    this.getSwiperList();
    this.getBigTypeList();
    this.getRecommendList();
  },
  async getSwiperList(){
    const result =  await requestUtil({ url: '/carouseImage/getCarouseImage', method: "GET" })
    this.setData({
      swiperList: result.message,
      baseUrl: getBaseUrl()
    });
  },

  async getBigTypeList(){
    const result = await requestUtil({
      url: "/bigType/getAll",
      method: "GET"
    });
    console.log(result)
    const bigTypeList = result.message;
    const bigTypeList_row1 = bigTypeList.filter((item, index)=>{
      return index<5;
    })
    const bigTypeList_row2 = bigTypeList.filter((item,index)=>{
      return index>=5;
    })
    this.setData({
      bigTypeList,
      bigTypeList_row1,
      bigTypeList_row2,
    })
  },

  async getRecommendList(){
    const result = await requestUtil({
      url: "/video/getRecommendVideo",
      method: "GET"
    });
    const recommendList = result.message;
    this.setData({
      recommendList
    })
  },

  // 点击分类跳转到分类页面
  handleTypeJump(e){
    const {index} = e.currentTarget.dataset;
    console.log("index" + index)
    const app = getApp();
    app.globalData.index = index;
    wx.switchTab({
      url: '/pages/classification/index'
    })
  },
  // 点击视频标题跳转到视频页面
  handleVideoJump(e){
    const {indexVideo} = e.currentTarget.dataset;
    console.log("indexVideo" + indexVideo)
    const app = getApp();
    app.globalData.indexVideo = indexVideo;
    wx.navigateTo({
      url: '/pages/videoPage/index'
    })
  }

});