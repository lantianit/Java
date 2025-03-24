// 导入request请求工具方法



import { getBaseUrl, requestUtil } from "../../utils/requestUtil.js";



import regeneratorRuntime from '../../lib/runtime/runtime';







Page({







    /**



     * 页面的初始数据



     */



    data: {



        currentBoldItem: 'detail', // 初始设置加粗的菜单项为"详情"



        baseUrl: '',



        scrollTop: 0,



        video: '',



        comments: [],



        commentInput: '',



        user: '',



        watchStartTime: null, // 新增：记录视频开始观看的时间



        isLiked: false,



        isFavorited: false,



    },







    onLoad: function (options) {



        const videoId = options.videoId;



        this.startTime = options.startTime ? parseInt(options.startTime, 10) : 0;



        // 获取用户信息后再初始化视频详情和记录开始时间



        this.getUserByToken().then(() => {



            this.getVideoDetail(videoId).then(() => {



                // Create video context and play video after data is loaded



                this.videoContext = wx.createVideoContext('myVideo');



                this.videoContext.seek(this.startTime);



                this.videoContext.play();



            });



            this.startVideo(videoId);



            this.checkUserInteractions(videoId);



            // 记录开始观看时间



            this.setData({



                watchStartTime: new Date()



            });



        });



    },







    onReady: function () {



        // 创建视频上下文



        this.videoContext = wx.createVideoContext('myVideo');







        // 自动跳转到指定时间



        this.videoContext.seek(this.startTime);



        this.videoContext.play(); // 确保视频开始播放



    },







    handleClick: function (e) {



        const clickedItem = e.currentTarget.dataset.item;



        this.setData({



            currentBoldItem: clickedItem, // 更新当前加粗的菜单项标识



            scrollTop: 0 // 每次点击菜单时可将滚动位置重置为顶部（可根据实际需求调整）



        });



    },







    // 获取视频信息



    async getVideoDetail(videoId) {



        const commentResult = await requestUtil({ url: "/video/getVideoAndCommentByVideoId", data: { videoId } });



        const formatComments = commentResult.message.map(comment => {



            const date = new Date(comment.createTime);



            const year = date.getFullYear();



            const month = ('0' + (date.getMonth() + 1)).slice(-2);  // 月份从0开始计数，需要加1并补0



            const day = ('0' + date.getDate()).slice(-2);



            const hour = ('0' + date.getHours()).slice(-2);



            const minute = ('0' + date.getMinutes()).slice(-2);



            comment.createTime = `${year}年${month}月${day}日${hour}时${minute}分`;



            return comment;



        });



        const videoResult = await requestUtil({ url: "/video/getVideoByVideoId", data: { videoId } });



        const baseUrl = getBaseUrl();



        this.setData({



            baseUrl,



            video: videoResult.message,



            comments: formatComments



        })



        console.log(this.data.video);



        console.log(this.data.comments);



    },







    // 假设 requestUtil 是封装的请求工具函数



    // 记录视频开始播放时间



    async startVideo(videoId) {



        const app = getApp();



        const userId = this.data.user.userId;



        console.log("开启视频页面，当前userId:" + userId);







        // 检查 userId 是否有效



        if (!userId) {



            console.error('userId 无效，请检查用户信息');



            return;



        }







        try {



            await requestUtil({



                url: '/video/startVideo',



                method: 'POST',



                // 设置请求头为 application/json



                header: {



                    'Content-Type': 'application/json'



                },



                // 将数据转换为 JSON 字符串



                data: JSON.stringify({



                    userId: parseInt(userId, 10), // 确保传递 userId



                    videoId: parseInt(videoId, 10) // 确保传递 videoId



                })



            });



        } catch (error) {



            console.error('请求出错:', error);



        }



    },







    // 记录视频结束播放时间并更新观看记录



    async endVideo(videoId) {



        const app = getApp();



        const userId = this.data.user.userId;



        console.log("关闭视频页面，当前userId:" + userId);







        // 检查 userId 是否有效



        if (!userId) {



            console.error('userId 无效，请检查用户信息');



            return;



        }







        // 计算开始观看时间



        const startTime = this.data.watchStartTime;







        // 构造用户观看记录对象



        const watchHistory = {



            userId: parseInt(userId, 10),



            videoId: parseInt(videoId, 10),



            startTime: this.data.watchStartTime, // 转换为后端需要的格式



            progress: 0,



            isFinished: true // 假设视频结束就是看完了



        };







        try {



            // 发送更新观看记录请求



            await requestUtil({



                url: '/video/updateWatchHistory',



                method: 'POST',



                header: {



                    'Content-Type': 'application/json'



                },



                data: JSON.stringify(watchHistory)



            });



        } catch (error) {



            console.error('更新观看记录请求出错:', error);



        }



    },







    // 发送评论的方法



    async sendComment() {



        const app = getApp();



        if (app.globalData.token === '') {



            wx.showToast({



                title: '请先登录',



                icon: 'none'



            });



            return;



        }



        await this.getUserByToken();



        const comment = this.data.commentInput;



        if (!comment) {



            wx.showToast({



                title: '请输入评论内容',



                icon: 'none'



            });



            return;



        }



        console.log("当前用户");



        console.log(this.data.user);



        const userId = this.data.user.userId;



        console.log("当前用户发送评论,userId：" + userId);



        const videoId = this.data.video.videoId;



        const commentData = {



            videoId,



            userId,



            comment



        };



        try {



            const result = await requestUtil({



                url: '/video/addComment',



                method: 'POST',



                data: commentData



            });



            if (result) {



                // 评论发送成功后，刷新评论列表（重新获取评论数据）



                await this.getVideoDetail(videoId);



                wx.showToast({



                    title: '评论发送成功',



                    icon: 'success'



                });



            } else {



                wx.showToast({



                    title: '评论发送失败',



                    icon: 'none'



                });



            }



        } catch (error) {



            console.error('发送评论出错:', error);



            wx.showToast({



                title: '评论发送出现异常，请稍后再试',



                icon: 'none'



            });



        }



    },







    // 处理评论输入框内容变化的方法示例



    onCommentInput(e) {



        this.setData({



            commentInput: e.detail.value



        });



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



        console.log("用户是否赋值");



        console.log(this.data.user);



    },







    // 监听视频播放结束事件



    onReady() {



        const videoContext = wx.createVideoContext('myVideo');



        videoContext.onEnded(() => {



            const videoId = this.data.video.videoId;



            this.endVideo(videoId);



        });



    },







    // 页面卸载时触发



    onUnload() {



        const videoId = this.data.video.videoId;



        this.endVideo(videoId);



    },







    // 检查用户是否已点赞或收藏视频



    async checkUserInteractions(videoId) {



        const userId = this.data.user.userId;



        const likeResult = await requestUtil({



            url: '/video/interaction/isLiked',



            method: 'GET',



            data: { userId, videoId }



        });



        const favoriteResult = await requestUtil({



            url: '/video/interaction/isFavorited',



            method: 'GET',



            data: { userId, videoId }



        });



        this.setData({



            isLiked: likeResult.isLiked,



            isFavorited: favoriteResult.isFavorited



        });



    },







    // 切换点赞状态



    async toggleLike() {



        const userId = this.data.user.userId;



        const videoId = this.data.video.videoId;



        if (this.data.isLiked) {



            // 取消点赞



            await requestUtil({



                url: '/video/interaction/unlike',



                method: 'GET',



                data: { userId, videoId }



            });



        } else {



            // 点赞



            await requestUtil({



                url: '/video/interaction/like',



                method: 'GET',



                data: { userId, videoId }



            });



        }



        this.setData({



            isLiked: !this.data.isLiked



        });



    },







    // 切换收藏状态



    async toggleFavorite() {



        const userId = this.data.user.userId;



        const videoId = this.data.video.videoId;



        if (this.data.isFavorited) {



            // 取消收藏



            await requestUtil({



                url: '/video/interaction/unfavorite',



                method: 'GET',



                data: { userId, videoId }



            });



        } else {



            // 收藏



            await requestUtil({



                url: '/video/interaction/favorite',



                method: 'GET',



                data: { userId, videoId }



            });



        }



        this.setData({



            isFavorited: !this.data.isFavorited



        });



    },



})


