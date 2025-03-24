import { requestUtil, getBaseUrl } from '../../utils/requestUtil.js';
// 定义公共的url
const baseUrl = getBaseUrl();

Page({
  data: {
    // 用于记录当前是登录还是注册状态，默认是登录态展示
    isRegistered: false,
    // 登录相关输入的账号和密码
    loginUserName: '',
    loginPassword: '',
    // 注册相关输入的昵称、账号和密码
    nickName: '',
    registerUserName: '',
    registerPassword: '',
    // 用于存储头像图片的临时路径
    imagePath: '',
    thisToken: '',
  },

  // 处理用户名输入（登录相关）
  handleUserNameInput(e) {
    this.setData({
      loginUserName: e.detail.value
    });
  },

  // 处理密码输入（登录相关）
  handlePasswordInput(e) {
    this.setData({
      loginPassword: e.detail.value
    });
  },

  // 处理昵称输入（注册相关）
  handleNickNameInput(e) {
    this.setData({
      nickName: e.detail.value
    });
  },

  // 处理注册用户名输入
  handleRegisterUserNameInput(e) {
    this.setData({
      registerUserName: e.detail.value
    });
  },

  // 处理注册密码输入
  handleRegisterPasswordInput(e) {
    this.setData({
      registerPassword: e.detail.value
    });
  },

  // 选择头像
  chooseAvatar() {
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        this.setData({
          imagePath: res.tempFilePaths[0]
        });
      }
    });
  },

  // 切换到注册状态
  toggleToRegister() {
    this.setData({
      isRegistered: true
    });
  },

  // 切换到登录状态
  toggleToLogin() {
    this.setData({
      isRegistered: false
    });
  },

  // 模拟登录请求（实际应用需对接后端接口验证用户信息）
  async handleLogin() {
    const data = {
      loginName: this.data.loginUserName,
      password: this.data.loginPassword
    };
    const result = await requestUtil({
      url: `/user/login`,
      method: 'POST',
      data
    });
    if (result.code === 200) {
      // 登录成功，显示弹窗提示
      wx.showModal({
        title: '登录成功',
        content: '您已成功登录，点击确认返回首页。',
        showCancel: false,
        success: (res) => {
          if (res.confirm) {
            // 用户点击确认后跳转到首页
            const app = getApp();
            app.globalData.isLoggedIn = true;
            app.globalData.token = result.token;
            console.log("token = " + app.globalData.token);
            wx.switchTab({
              url: '/pages/index/index'
            });
          }
        }
      });
    } else {
      // 登录失败，显示模糊提示
      wx.showModal({
        title: '登录失败',
        content: '账号或密码错误，请重试。',
        showCancel: false,
        success: (res) => {
          if (res.confirm) {
            console.log('用户点击确认');
          }
        }
      });
    }
  },

  // 模拟注册请求（实际应用需对接后端接口存储用户信息），完善后真正发送注册请求到后端
  async handleRegister() {
    if (!this.data.imagePath) {
      wx.showToast({
        title: '请选择头像',
        icon: 'none'
      });
      return;
    }
    const result = await wx.uploadFile({
      url: 'http://127.0.0.1:8080/user/register', // 后端接收图片上传的接口路径，根据实际情况调整
      filePath: this.data.imagePath, // 要上传的文件路径，即之前选择图片获取到的临时路径
      name: 'file', // 对应后端接收文件参数的名称，需和后端 @RequestPart("file") 对应
      header: {
          'Content-Type': 'multipart/form-data'
      },
      formData: {
        // 可以添加其他表单数据
        nickName: this.data.nickName,
        loginName: this.data.registerUserName,
        password: this.data.registerPassword
      },
      success: (res) => {
          const data = res.statusCode;
          if (data === 200) {
              wx.showToast({
                  title: '图片上传成功',
                  icon: 'success'
              });
          } else {
              wx.showToast({
                  title: data.message,
                  icon: 'none'
              });
          }
      },
      fail: (err) => {
          console.error('图片上传失败', err);
          wx.showToast({
              title: '图片上传失败，请稍后重试',
              icon: 'none'
          });
      }
  });
    if (result.code === 200) {
      // 注册成功逻辑，比如提示用户等，可以根据实际需求完善
      wx.showToast({
        title: '注册成功',
        icon: 'success'
      });
      this.toggleToLogin();
    } else {
      // 注册失败逻辑，提示用户等
      console.log("result的值");
      console.log(result);
      wx.showToast({
        title: result.message,
        icon: 'none'
      });
    }
  },

  // 模拟获取文件流及文件名的方法（需根据小程序实际文件系统API调整）
  async getFileStream(filePath) {
    return {
      stream: wx.getFileSystemManager().readFileSync(filePath),
      name: filePath.split('/').pop() // 简单获取文件名，可根据实际情况优化文件名处理逻辑
    };
  }
})